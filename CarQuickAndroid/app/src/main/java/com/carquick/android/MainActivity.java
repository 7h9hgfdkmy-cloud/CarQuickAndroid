package com.carquick.android;
import android.app.*; import android.os.*; import android.provider.Settings; import android.content.*; import android.view.*; import android.widget.*;
public class MainActivity extends Activity {
 EditText car, shortcuts; Switch auto, ownRight;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  ScrollView scroll=new ScrollView(this); LinearLayout box=new LinearLayout(this); box.setOrientation(1);box.setPadding(24,30,24,24);scroll.addView(box);setContentView(scroll);
  text(box,"車訊快捷助手",24);text(box,"按懸浮鈕：擷取聊天室最後一則訊息（包含自己發出的），換行加上以下車訊，再傳送。先將聊天室滑到最底部。",16);
  car=new EditText(this);car.setHint("你的車訊，例如 9205／深藍／5分");car.setText(Config.car(this));box.addView(car);
  text(box,"快捷鍵盤內容：每行『分類|車訊』",16);shortcuts=new EditText(this);shortcuts.setMinLines(5);shortcuts.setText(String.join("\n",Config.lines(this)));box.addView(shortcuts);
  auto=new Switch(this);auto.setText("懸浮按鈕一鍵直接傳送");auto.setChecked(Config.prefs(this).getBoolean("auto",false));box.addView(auto);
  ownRight=new Switch(this);ownRight.setText("已確認：自己的 LINE 訊息在右側");ownRight.setChecked(Config.prefs(this).getBoolean("ownRight",false));box.addView(ownRight);
  text(box,"初次請關閉直接傳送，確認抓到正確的最後一則訊息後再開啟。程式不使用網路；無障礙服務只處理 LINE。",14);
  button(box,"儲存",()->{Config.prefs(this).edit().putString("car",car.getText().toString().trim()).putString("shortcuts",shortcuts.getText().toString()).putBoolean("auto",auto.isChecked()).putBoolean("ownRight",ownRight.isChecked()).apply();Toast.makeText(this,"已儲存",0).show();});
  button(box,"啟用 LINE 懸浮助手",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  button(box,"啟用快捷鍵盤",()->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
  button(box,"選擇鍵盤",()->((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
  text(box,"首次使用：到自己的 LINE 測試聊天室，滑到最底部，長按懸浮鈕校正訊息欄位。校正只選文字訊息，不選時間或人名。LINE 更新後可能需要重新校正。",15);
  button(box,"清除訊息辨識校正",()->{Config.prefs(this).edit().remove("messageId").apply();Toast.makeText(this,"已清除",0).show();});
 }
 void text(LinearLayout b,String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setPadding(0,14,0,14);b.addView(t);}
 void button(LinearLayout b,String s,Runnable r){Button x=new Button(this);x.setText(s);x.setOnClickListener(v->r.run());b.addView(x);}
}
