package ds.vkplus

import android.content.SharedPreferences
import android.preference.PreferenceManager


public fun prefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.instance)
