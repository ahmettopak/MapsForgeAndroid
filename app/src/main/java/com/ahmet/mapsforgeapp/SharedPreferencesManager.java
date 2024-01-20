package com.ahmet.mapsforgeapp;


import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Author: Ahmet TOPAK$
 * Date: 1/20/2024$
 */
public class SharedPreferencesManager {
    private static final String PREF_NAME = "MapPrefs"; // SharedPreferences adı
    private static final String KEY_STRING_LIST = "mapList"; // Anahtar for string listesi

    // String listesini SharedPreferences'e kaydetme
    public static void saveStringList(Context context, List<String> stringList) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // String listesini Set'e çevirip kaydetme
        Set<String> stringSet = new HashSet<>(stringList);
        editor.putStringSet(KEY_STRING_LIST, stringSet);
        editor.apply();
    }

    // SharedPreferences'ten string listesini almak
    public static List<String> getStringList(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Set'ten String listesine çevirme
        Set<String> stringSet = preferences.getStringSet(KEY_STRING_LIST, new HashSet<>());
        return new ArrayList<>(stringSet);
    }
}
