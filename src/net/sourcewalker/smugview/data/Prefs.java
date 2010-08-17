package net.sourcewalker.smugview.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Prefs {

    SharedPreferences store;

    public Prefs(Context ctx) {
        store = ctx.getSharedPreferences(Prefs.class.getName(),
                Context.MODE_PRIVATE);
    }

    public String getUsername() {
        return store.getString("login.user", null);
    }

    public void setUsername(String value) {
        setString("login.user", value);
    }

    public String getPassword() {
        return store.getString("login.password", null);
    }

    public void setPassword(String value) {
        setString("login.password", value);
    }

    private void setString(String key, String value) {
        Editor editor = store.edit();
        editor.putString(key, value);
        editor.commit();
    }

}
