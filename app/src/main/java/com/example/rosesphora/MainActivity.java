  package com.example.rosesphora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.onesignal.OneSignal;

  public class MainActivity extends AppCompatActivity {

    private Button login;
    private Button sigup;

    private FirebaseAuth mAuth;

      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

          // Logging set to help debug issues, remove before releasing your app.
          OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

          // OneSignal Initialization
          OneSignal.startInit(this)
                  .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                  .unsubscribeWhenNotificationsAreDisabled(true)
                  .init();

        mAuth = FirebaseAuth.getInstance();

        login = (Button) findViewById(R.id.login_btn);
        sigup = (Button) findViewById(R.id.signin_btn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        sigup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });

    }

      @Override
      protected void onStart() {
          super.onStart();
          OneSignal.sendTag("Pub_Id", "notif");
          FirebaseUser currentUser = mAuth.getCurrentUser();
          if(currentUser != null){
              startActivity(new Intent(MainActivity.this, HomeActivity.class));
              finish();
          }
      }

}