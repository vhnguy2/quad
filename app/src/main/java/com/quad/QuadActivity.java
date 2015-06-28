package com.quad;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class QuadActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!QuadApplication.getInstance().isUserLoggedIn()) {
      LoginActivity.launchActivity(this, getIntent());
    }
  }
}
