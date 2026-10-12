package com.carquick.android;
import android.accessibilityservice.*;import android.view.accessibility.*;import android.view.*;import android.graphics.Rect;import android.os.*;import android.app.*;import android.widget.*;import java.util.*;

public class LineService extends AccessibilityService {
 static final String LINE="jp.naver.line.android";
 WindowManager wm;LinearLayout panel;WindowManager.LayoutParams lp;Handler handler=new Handler();boolean busy=false;
 @Override protected void onServiceConnected(){wm=(WindowManager)getSystemService(WINDOW_SERVICE);panel=new LinearLayout(this);panel.setOrientation(1);
  Button send=new Button(this);send.setText("車訊");send.setOnClickListener(v->append());send.setOnLongClickListener(v->{calibrate();return true;});panel.addView(send);
  Button undo=new Button(this);undo.setText("收回");undo.setOnClickListener(v->unsend());panel.addView(undo);
  TextView drag=new TextView(this);drag.setText("↕ 拖曳");drag.setGravity(17);panel.addView(drag);
  lp=new WindowManager.LayoutParams(-2,-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,android.graphics.PixelFormat.TRANSLUCENT);lp.gravity=Gravity.TOP|Gravity.LEFT;lp.x=12;lp.y=300;
  drag.setOnTouchListener(new View.OnTouchListener(){float x,y;int ox,oy;public boolean onTouch(View v,android.view.MotionEvent e){if(e.getAction()==0){x=e.getRawX();y=e.getRawY();ox=lp.x;oy=lp.y;}else if(e.getAction()==2){lp.x=Math.max(0,ox+(int)(e.getRawX()-x));lp.y=Math.max(0,oy+(int)(e.getRawY()-y));wm.updateViewLayout(panel,lp);}return true;}});wm.addView(panel,lp);panel.setVisibility(View.GONE);
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent e){if(panel!=null)panel.setVisibility(root()!=null?View.VISIBLE:View.GONE);}
 @Override public void onInterrupt(){busy=false;}
 @Override public void onDestroy(){if(panel!=null)wm.removeView(panel);handler.removeCallbacksAndMessages(null);super.onDestroy();}
 AccessibilityNodeInfo root(){AccessibilityNodeInfo r=getRootInActiveWindow();return r!=null&&LINE.contentEquals(r.getPackageName())?r:null;}
 List<AccessibilityNodeInfo> nodes(AccessibilityNodeInfo r){List<AccessibilityNodeInfo> out=new ArrayList<>();walk(r,out);return out;}
 void walk(AccessibilityNodeInfo n,List<AccessibilityNodeInfo> out){if(n==null)return;out.add(n);for(int i=0;i<n.getChildCount();i++)walk(n.getChild(i),out);}
 void note(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 void dialog(AlertDialog d){d.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);d.show();}
 void calibrate(){AccessibilityNodeInfo r=root();if(r==null){note("請開啟 LINE 聊天室");return;}List<AccessibilityNodeInfo> candidates=new ArrayList<>();for(AccessibilityNodeInfo n:nodes(r))if(n.isVisibleToUser()&&!n.isEditable()&&n.getText()!=null&&n.getViewIdResourceName()!=null)candidates.add(n);
  String[] labels=new String[candidates.size()];for(int i=0;i<labels.length;i++)labels[i]=candidates.get(i).getText()+"\n"+candidates.get(i).getViewIdResourceName();
  AlertDialog d=new AlertDialog.Builder(this).setTitle("選擇一則訊息文字，勿選人名／時間").setItems(labels,(a,i)->{Config.prefs(this).edit().putString("messageId",candidates.get(i).getViewIdResourceName()).apply();note("訊息欄位已校正；自己的訊息需在右側。先測試預覽。");}).setNegativeButton("取消",null).create();dialog(d);
 }
 AccessibilityNodeInfo latest(boolean own){AccessibilityNodeInfo r=root();if(r==null)return null;String id=Config.prefs(this).getString("messageId","");if(id.isEmpty()){note("請先長按車訊鈕校正");return null;}
  List<AccessibilityNodeInfo> matches=r.findAccessibilityNodeInfosByViewId(id);AccessibilityNodeInfo best=null;Rect screen=new Rect();r.getBoundsInScreen(screen);int bottom=-1;
  for(AccessibilityNodeInfo n:matches){Rect b=new Rect();n.getBoundsInScreen(b);if(!n.isVisibleToUser()||n.getText()==null||n.getText().toString().trim().isEmpty())continue;
   if(own && b.centerX()<=screen.centerX())continue;
   if(b.bottom>bottom){bottom=b.bottom;best=n;}
  }
  if(best==null)return null;
  // A selected text node must belong to the last visible chat row.
  // If a newer non-text row exists, stop instead of reusing an older text.
  AccessibilityNodeInfo row=best, container=null;
  while(row.getParent()!=null){AccessibilityNodeInfo parent=row.getParent();String cls=String.valueOf(parent.getClassName());if(cls.contains("RecyclerView")||cls.contains("ListView")){container=parent;break;}row=parent;}
  if(container==null){note("無法確認訊息清單結構，停止");return null;}
  AccessibilityNodeInfo last=null;int maxBottom=-1;
  for(int i=0;i<container.getChildCount();i++){AccessibilityNodeInfo child=container.getChild(i);Rect b=new Rect();child.getBoundsInScreen(b);if(child.isVisibleToUser()&&b.bottom>maxBottom){maxBottom=b.bottom;last=child;}}
  if(last==null)return null;
  if(!own&&!last.equals(row)){note("最新一則可能是非文字訊息，停止");return null;}
  if(own){boolean passed=false;for(int i=0;i<container.getChildCount();i++){AccessibilityNodeInfo child=container.getChild(i);if(child.equals(row)){passed=true;continue;}if(!passed||!child.isVisibleToUser())continue;boolean incomingText=false;for(AccessibilityNodeInfo n:nodes(child)){if(id.equals(n.getViewIdResourceName())&&n.getText()!=null){Rect b=new Rect();n.getBoundsInScreen(b);if(b.centerX()>screen.centerX()){note("後方另有自己的訊息，停止");return null;}incomingText=true;}}if(!incomingText){note("後方有無法辨識的訊息，停止收回");return null;}}}
  if(own&&!Config.prefs(this).getBoolean("ownRight",false)){note("請先在 App 確認自己的訊息位於右側");return null;}
  // Do not claim a newest message while a list can still scroll toward newer rows.
  for(AccessibilityNodeInfo n:nodes(r))if(n.isScrollable())for(AccessibilityNodeInfo.AccessibilityAction a:n.getActionList())if(a.getId()==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD){note("請將聊天室滑到最底部；目前不能確認最新訊息");return null;}
  return best;
 }
 AccessibilityNodeInfo editor(AccessibilityNodeInfo r){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo n:nodes(r))if(n.isVisibleToUser()&&n.isEditable()){if(found!=null)return null;found=n;}return found;}
 String val(AccessibilityNodeInfo n){return n.getText()==null?"":n.getText().toString();}
 void append(){if(busy)return;String car=Config.car(this);if(car.isEmpty()){note("先在 App 設定車訊");return;}AccessibilityNodeInfo msg=latest(false);if(msg==null){note("無法確認最新文字訊息，未傳送");return;}AccessibilityNodeInfo r=root(),edit=editor(r);if(edit==null||!val(edit).isEmpty()){note("找不到空白輸入框；保留現有草稿");return;}
  String source=val(msg),combined=source+"\n"+car;String messageId=msg.getViewIdResourceName();
  Runnable act=()->fillAndSend(source,messageId,combined);
  if(Config.prefs(this).getBoolean("auto",false))act.run();else dialog(new AlertDialog.Builder(this).setTitle("預覽合併訊息").setMessage(combined).setPositiveButton("傳送",(d,w)->act.run()).setNegativeButton("取消",null).create());
 }
 void fillAndSend(String source,String id,String combined){AccessibilityNodeInfo msg=latest(false),r=root();if(msg==null||!source.equals(val(msg))||!id.equals(msg.getViewIdResourceName())){note("畫面已改變，請重新操作");return;}AccessibilityNodeInfo e=editor(r);if(e==null||!val(e).isEmpty()){note("輸入框已有內容，停止");return;}
  busy=true;Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,combined);if(!e.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)){busy=false;note("無法填入訊息");return;}
  handler.postDelayed(()->{AccessibilityNodeInfo rr=root();AccessibilityNodeInfo ee=rr==null?null:editor(rr);if(ee==null||!combined.equals(val(ee))){busy=false;note("內容未確認，停止傳送");return;}
   AccessibilityNodeInfo send=exact(rr,new String[]{"傳送","发送","發送","Send"});if(send==null||!send.performAction(AccessibilityNodeInfo.ACTION_CLICK)){note("未找到傳送按鈕，請手動傳送");}else note("已按傳送，請以 LINE 訊息氣泡確認");busy=false;
  },350);
 }
 AccessibilityNodeInfo exact(AccessibilityNodeInfo r,String[] labels){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo n:nodes(r)){String t=val(n).trim(),desc=n.getContentDescription()==null?"":n.getContentDescription().toString().trim();for(String s:labels)if(n.isVisibleToUser()&&(s.equals(t)||s.equals(desc))){AccessibilityNodeInfo click=n;for(int i=0;i<3&&!click.isClickable()&&click.getParent()!=null;i++)click=click.getParent();if(click.isClickable()){if(found!=null&&!found.equals(click))return null;found=click;}}}return found;}
 void unsend(){if(busy)return;AccessibilityNodeInfo msg=latest(true);if(msg==null){note("無法確認自己最新的文字訊息，未收回");return;}
  AccessibilityNodeInfo target=msg;while(!target.isLongClickable()&&target.getParent()!=null)target=target.getParent();if(!target.isLongClickable()){note("訊息不支援長按，未收回");return;}
  busy=true;if(!target.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)){busy=false;note("無法開啟訊息選單");return;}
  handler.postDelayed(()->{AccessibilityNodeInfo r=root();AccessibilityNodeInfo undo=r==null?null:exact(r,new String[]{"收回","收回訊息","Unsend"});if(undo==null||!undo.performAction(AccessibilityNodeInfo.ACTION_CLICK)){busy=false;note("LINE 未提供收回選項；未刪除訊息");return;}
   handler.postDelayed(()->{AccessibilityNodeInfo rr=root();AccessibilityNodeInfo confirm=null;if(rr!=null){boolean hasDialog=false;for(AccessibilityNodeInfo n:nodes(rr))if(n.getClassName()!=null&&n.getClassName().toString().contains("Dialog"))hasDialog=true;if(hasDialog)confirm=exact(rr,new String[]{"收回","收回訊息","Unsend"});}if(confirm!=null)confirm.performAction(AccessibilityNodeInfo.ACTION_CLICK);busy=false;note("已操作收回；若 LINE 顯示確認視窗，請確認完成狀態");},350);
  },450);
 }
}
