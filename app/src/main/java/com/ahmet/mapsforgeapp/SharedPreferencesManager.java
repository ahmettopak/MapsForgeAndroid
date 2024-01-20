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

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context, String prefName, String keyStringList) {
        preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static SharedPreferencesManager.Builder with(Context context) {
        return new Builder(context);
    }

    // String listesini SharedPreferences'e kaydetme
    public void saveStringList(String key, List<String> stringList) {
        Set<String> stringSet = new HashSet<>(stringList);
        editor.putStringSet(key, stringSet);
        editor.apply();
    }

    // SharedPreferences'ten string listesini almak
    public List<String> getStringList(String key) {
        Set<String> stringSet = preferences.getStringSet(key, new HashSet<>());
        return new ArrayList<>(stringSet);
    }

    public static class Builder {
        private final Context context;
        private String prefName = "MapPrefs";
        private String keyStringList = "mapList";

        private Builder(Context context) {
            this.context = context;
        }

        public Builder setPrefName(String prefName) {
            this.prefName = prefName;
            return this;
        }

        public Builder setKeyStringList(String keyStringList) {
            this.keyStringList = keyStringList;
            return this;
        }

        public SharedPreferencesManager.Builder saveStringList(String key, List<String> stringList) {
            SharedPreferencesManager manager = new SharedPreferencesManager(context, prefName, keyStringList);
            manager.saveStringList(key, stringList);
            return this;
        }

        public List<String> getStringList(String key) {
            SharedPreferencesManager manager = new SharedPreferencesManager(context, prefName, keyStringList);
            return manager.getStringList(key);
        }

        public SharedPreferencesManager build() {
            return new SharedPreferencesManager(context, prefName, keyStringList);
        }
    }
}
