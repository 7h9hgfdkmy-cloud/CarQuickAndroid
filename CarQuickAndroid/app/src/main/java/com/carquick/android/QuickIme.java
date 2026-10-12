package com.carquick.android;
import android.inputmethodservice.InputMethodService;import android.view.*;import android.view.inputmethod.*;import android.widget.*;import java.util.*;
public class QuickIme extends InputMethodService {
 @Override public View onCreateInputView(){return keyboard();}
 @Override public void onStartInputView(EditorInfo info,boolean restarting){super.onStartInputView(info,restarting);setInputView(keyboard());}
 View keyboard(){LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(12,8,12,8);
  LinkedHashMap<String,List<String>> groups=new LinkedHashMap<>();for(String line:Config.lines(this)){int i=line.indexOf('|');if(i>0)groups.computeIfAbsent(line.substring(0,i),k->new ArrayList<>()).add(line.substring(i+1));}
  LinearLayout tabs=new LinearLayout(this);HorizontalScrollView ts=new HorizontalScrollView(this);ts.addView(tabs);root.addView(ts);
  ScrollView sc=new ScrollView(this);LinearLayout rows=new LinearLayout(this);rows.setOrientation(1);sc.addView(rows);root.addView(sc,new LinearLayout.LayoutParams(-1,420));
  for(String category:groups.keySet())add(tabs,category,()->show(rows,groups.get(category)));
  if(!groups.isEmpty())show(rows,groups.values().iterator().next());
  LinearLayout tools=new LinearLayout(this);add(tools,"其他鍵盤",()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());add(tools,"⌫",()->{if(getCurrentInputConnection()!=null)getCurrentInputConnection().deleteSurroundingText(1,0);});add(tools,"傳送",()->{InputConnection c=getCurrentInputConnection();EditorInfo e=getCurrentInputEditorInfo();if(c==null)return;int a=e.imeOptions&EditorInfo.IME_MASK_ACTION;if(a!=EditorInfo.IME_ACTION_NONE&&a!=EditorInfo.IME_ACTION_UNSPECIFIED)c.performEditorAction(a);else c.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));});root.addView(tools);return root;
 }
 void show(LinearLayout rows,List<String> messages){rows.removeAllViews();for(String s:messages)add(rows,s,()->{if(getCurrentInputConnection()!=null)getCurrentInputConnection().commitText(s,1);});}
 void add(LinearLayout box,String text,Runnable action){Button b=new Button(this);b.setText(text);b.setOnClickListener(v->action.run());box.addView(b);}
}
