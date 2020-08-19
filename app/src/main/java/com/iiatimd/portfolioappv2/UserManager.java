package com.iiatimd.portfolioappv2;

import android.content.SharedPreferences;

import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.User;

public class UserManager {

    private SharedPreferences userPref;
    private SharedPreferences.Editor editor;

    private static UserManager INSTANCE = null;

    private UserManager(SharedPreferences userPref) {
        this.userPref = userPref;
        this.editor = userPref.edit();
    }

    static synchronized UserManager getInstance(SharedPreferences userPref) {
        if (INSTANCE == null) {
            INSTANCE = new UserManager(userPref);
        }
        return INSTANCE;
    }

    public void saveUser(User user) {
        editor.putInt("id", user.getId()).commit();
        editor.putString("name", user.getName()).commit();
        editor.putString("lastname", user.getLastname()).commit();
        editor.putString("photo", user.getPhoto()).commit();
        editor.putString("email", user.getEmail()).commit();
    }

    public void deleteUser() {
        editor.remove("id").commit();
        editor.remove("name").commit();
        editor.remove("lastname").commit();
        editor.remove("photo").commit();
        editor.remove("email").commit();
    }
}
