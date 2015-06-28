package com.quad;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

public class LoginActivity extends Activity {

  private EditText mPhoneNumberView;
  private TextView mLoginButton;

  public static void launchActivity(Activity activity, Intent intent) {
    // TODO(viet): clear top and new task
    activity.startActivity(new Intent(activity, LoginActivity.class));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    mPhoneNumberView = (EditText) findViewById(R.id.login_phone_number);
    mLoginButton = (TextView) findViewById(R.id.login_button);

    setupListeners();
  }

  private void setupListeners() {
    mLoginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // TODO(viet): send a text to this number and verify, but for now just trust that this
        //             is the ID.
        String phone = mPhoneNumberView.getText().toString();
        if (StringUtils.isEmpty(phone)) {
          Toast.makeText(LoginActivity.this, "Please fill out the form", Toast.LENGTH_SHORT).show();
        }
        QuadApplication.getInstance().setUserId(phone);
//        ConversationActivity.launchActivity();
        finish();
      }
    });
  }
}
