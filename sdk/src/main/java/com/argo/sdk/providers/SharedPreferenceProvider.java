package com.argo.sdk.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Provider;

/**
 * Created by user on 6/26/15.
 */
public class SharedPreferenceProvider implements Provider<SharedPreferences> {

    public interface PreferenceWriter{

        void write(SharedPreferences.Editor editor);

    }

    private Context context;
    private SharedPreferences sharedPreferences;

    public SharedPreferenceProvider(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public SharedPreferences get() {
        return sharedPreferences;
    }

    public void save(String name, String value){
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(name, value);
        editor.commit();
    }

    public void save(String name, int value){
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public void save(String name, boolean value){
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    public void commit(PreferenceWriter writer){
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        writer.write(editor);
        editor.commit();
    }
}
