package com.example.rosesphora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.onesignal.OneSignal;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText name, email, password, password2;
    private Button signup;
    private ImageView back;

    private DatabaseReference dataUsers;
    private DatabaseReference tokenDatabase;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        dataUsers = Utils.getDatabase().getReference("users");
        tokenDatabase = Utils.getDatabase().getReference("device_token");
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        back = (ImageView) findViewById(R.id.signin_back) ;

        name = (EditText) findViewById(R.id.signup_username);
        email = (EditText) findViewById(R.id.signup_email);
        password = (EditText) findViewById(R.id.signup_password);
        password2 = (EditText) findViewById(R.id.signup_password2);
        signup = (Button) findViewById(R.id.signup_sum);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    public  void registerUser(){
        String pwd = password.getText().toString().trim();
        String pwd2 = password2.getText().toString().trim();
        String login = email.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(login).matches()) {
            email.setError("Veuillez entrer une email valide svp!!");
            email.requestFocus();
            return;
        }
        if(login.isEmpty()){
            email.setError("Veuillez renseigner votre adresse mail svp!!");
            email.requestFocus();
            return;
        }
        if (pwd.isEmpty()) {
            password.setError("Veuillez renseigner votre mot de passe svp!!");
            password.requestFocus();
            return;
        }

        if(pwd.length()< 8){
            password.setError("Votre mot de passe doit avoir 8 caractères minimum!!");
            password.requestFocus();
            return;
        }

        if (!pwd.equals(pwd2)) {
            password2.setError("Vos mots de passe ne sont pas identiques");
            password2.requestFocus();
            return;
        }


        progressDialog.setTitle("Création de compte");
        progressDialog.setMessage("Veuillez patienter svp!!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(login, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, "Compte Créer avec succès", Toast.LENGTH_SHORT).show();
                            OneSignal.sendTag("Pub_Id", "notif");
                            progressDialog.dismiss();
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            String device_token = FirebaseInstanceId.getInstance().getToken();
                            String id = currentUser.getUid();
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("username", name.getText().toString().trim());
                            userMap.put("status", "Salut j'utilise Rose Saphira App");
                            userMap.put("image", "default");
                            userMap.put("type", "visiteur");
                            userMap.put("phone", "Votre numéro svp");
                            userMap.put("name", "Votre nom svp");
                            userMap.put("device_token", device_token);
                            tokenDatabase.child(id).setValue(device_token);
                            dataUsers.child(id).setValue(userMap);
                            Intent mainIntent = new Intent(SignUpActivity.this, HomeActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();


                        }else {
                            Toast.makeText(SignUpActivity.this, "Nous n'avons pas pu créer votre compte, vérifier votre connexion internet et réessayez", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            password.setText("");
                        }
                    }
                });

    }

}