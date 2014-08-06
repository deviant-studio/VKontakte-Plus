package ds.vkplus;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

	public static SharedPreferences get() {
		return PreferenceManager.getDefaultSharedPreferences(App.instance());
	}

}
