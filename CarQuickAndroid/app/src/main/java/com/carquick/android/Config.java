package com.carquick.android;
import android.content.*;
import org.json.*;
import java.util.*;
final class Config {
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("config",0);}
 static String car(Context c){return prefs(c).getString("car","");}
 static String[] lines(Context c){return prefs(c).getString("shortcuts","跑車|9205／深藍／5分\n跑車|9205／深藍／10分\n長單|9205／深藍／20分\n未見|已到，未見乘客").split("\\n");}
}
