package com.example.rosesphora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView avatar, editAvatar;
    private TextInputEditText name, email, phone, editUsername;
    private TextView username, status;
    private ImageView back;
    private ImageButton edit;
    private Button updateProfile, editProfile;

    private FirebaseUser mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        back = (ImageView) findViewById(R.id.profile_go_back);
        status = (TextView) findViewById(R.id.profile_satus);
        avatar = (CircleImageView) findViewById(R.id.profile_avatar);
        username = (TextView) findViewById(R.id.profile_username);
        name = (TextInputEditText) findViewById(R.id.profile_name);
        email = (TextInputEditText) findViewById(R.id.profile_email);
        phone = (TextInputEditText) findViewById(R.id.profile_phone);
        updateProfile = (Button) findViewById(R.id.profile_update);
        editAvatar = (CircleImageView) findViewById(R.id.profile_avatar);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = Utils.getDatabase().getReference().child("users").child(mAuth.getUid());
        mUserDatabase.keepSynced(true);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingActivity.this, HomeActivity.class));
                finish();
            }
        });
    }

    public  void showDialog(){
        AlertDialog.Builder alert;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            alert = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        }else{
            alert = new AlertDialog.Builder(this);
        }

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogue_box, null);

        editUsername = (TextInputEditText) findViewById(R.id.profile_name);

        editProfile = (Button) findViewById(R.id.profile_update);

        alert.setView(view);
        //alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                username.setText(dataSnapshot.child("username").getValue().toString().trim());
                status.setText(dataSnapshot.child("status").getValue().toString().trim());
                name.setText(dataSnapshot.child("name").getValue().toString().trim());
                email.setText(mAuth.getEmail().toString());
                phone.setText(dataSnapshot.child("phone").getValue().toString().trim());
                Picasso.get().load(dataSnapshot.child("image").getValue().toString().trim()).placeholder(R.drawable.avatar).into(avatar);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}