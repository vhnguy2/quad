package com.quad;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.firebase.client.Firebase;

import org.apache.commons.lang3.StringUtils;

public class QuadApplication extends Application {

  private static final String USER_ID_KEY = "userIdKey";

  private static QuadApplication mApp;

  private SharedPreferences mSharedPrefs;

  @Override
  public void onCreate() {
    super.onCreate();
    mApp = this;
    Firebase.setAndroidContext(this);
    Fresco.initialize(this);
    mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

  }

  public static QuadApplication getInstance() {
    return mApp;
  }

  public boolean isUserLoggedIn() {
    return !StringUtils.isEmpty(getUserId());
  }

  public void setUserId(String userId) {
    mSharedPrefs.edit().putString(USER_ID_KEY, userId).apply();
  }

  public String getUserId() {
    return mSharedPrefs.getString(USER_ID_KEY, null);
  }
}
