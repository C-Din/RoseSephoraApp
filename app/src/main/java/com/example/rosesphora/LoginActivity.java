package com.example.rosesphora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.onesignal.OneSignal;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private ImageView back;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthLitener;
    private DatabaseReference tokenDatabase;
    private DatabaseReference dataUsers;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.login_email_field);
        password = (EditText) findViewById(R.id.login_password_field);
        login = (Button) findViewById(R.id.login_sum);
        dataUsers = Utils.getDatabase().getReference("users");
        tokenDatabase = Utils.getDatabase().getReference("device_token");
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        back = (ImageView) findViewById(R.id.login_back);

        mAuthLitener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                }
            }
        };

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignIn();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    public void startSignIn(){

        String emaill = email.getText().toString().trim();
        String pwd = password.getText().toString().trim();
        if(emaill.isEmpty()){
            email.setError("Veuillez entrer une email valide svp!!");
            email.requestFocus();
            return;
        }
        if(pwd.isEmpty()){
            password.setError("Veuillez renseigner votre mot de passe svp!!");
            password.requestFocus();
            return;
        }
        progressDialog.setMessage("Connection de l'utilisateur");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(emaill, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            dataUsers.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    tokenDatabase.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);

                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Vous êtes connecté(e).", Toast.LENGTH_SHORT).show();
                                    OneSignal.sendTag("Pub_Id", "notif");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent mainIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Echec de la connexion, vérifiez vos informations et votre connexion internet puis réessayez.", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}