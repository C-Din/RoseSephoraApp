package com.example.rosesphora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar mToolbar;
    private DatabaseReference mPubDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAtuh;

    private CircleImageView drawerAvatar;
    private TextView drawerUsername, drawerStatus;

    private FloatingActionButton new_post;
    private RecyclerView mrecyclerView;

    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(HomeActivity.this));
        mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.universal_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Rose Saphira");
        new_post = (FloatingActionButton) findViewById(R.id.new_post);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        drawerAvatar = (CircleImageView) view.findViewById(R.id.drawer_avatar);
        drawerUsername = (TextView) view.findViewById(R.id.drawer_username);
        drawerStatus = (TextView) view.findViewById(R.id.drawer_satus);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPubDatabase = Utils.getDatabase().getReference().child("publications");
        mPubDatabase.keepSynced(true);
        mUserDatabase = Utils.getDatabase().getReference().child("users");
        mUserDatabase.keepSynced(true);
        mAtuh = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mrecyclerView = (RecyclerView) findViewById(R.id.post_list);
        mLayoutManager = new LinearLayoutManager(this);
        mrecyclerView.setHasFixedSize(true);
        mrecyclerView.setLayoutManager(mLayoutManager);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }

        });


        new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, PostActivity.class));
            }
        });

    }

    public void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()){

            case R.id.drawer_profil: startActivity(new Intent(HomeActivity.this, SettingActivity.class));
            break;

            case R.id.drawer_share:
                Toast.makeText(this, "Désolé le lien n'est pas encore disponible", Toast.LENGTH_SHORT).show();
                break;

            case R.id.drawer_blog:
                startActivity(new Intent(HomeActivity.this, BlogActivity.class));
                break;

            case R.id.drawer_logout_btn: mAtuh.signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        if(item.getItemId() == R.id.main_logout_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId() == R.id.main_blog)
        {
            startActivity(new Intent(HomeActivity.this, BlogActivity.class));
        }
        if(item.getItemId() == R.id.main_settings_btn)
        {
            startActivity(new Intent(HomeActivity.this, SettingActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser == null){
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        }else
        {
            mUserDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Picasso.get().load(dataSnapshot.child("image").getValue().toString().trim()).placeholder(R.drawable.avatar).into(drawerAvatar);
                    drawerStatus.setText(dataSnapshot.child("status").getValue().toString().trim());
                    drawerUsername.setText(dataSnapshot.child("username").getValue().toString().trim());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            mUserDatabase.child(mCurrentUser.getUid()).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue().toString().equals("admin"))
                    {
                        new_post.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        FirebaseRecyclerAdapter<Post, PubsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PubsViewHolder>(
                Post.class,
                R.layout.pub_template,
                PubsViewHolder.class,
                mPubDatabase.orderByChild("post_date")
        ) {
            @Override
            protected void populateViewHolder(final PubsViewHolder postViewHolder, final Post post, int i) {

                mUserDatabase.child(post.getUser_id()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        postViewHolder.textContent(post.getText_content());
                        postViewHolder.datePost("Date du post : "+ post.getPost_date());
                        postViewHolder.pubImage(post.getPub_image());
                        postViewHolder.setImageUser(dataSnapshot.child("image").getValue().toString());
                        postViewHolder.setLogin(dataSnapshot.child("username").getValue().toString());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        };

        mrecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PubsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public PubsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setImageUser(String image)
        {
            CircleImageView imageUser = (CircleImageView) mView.findViewById(R.id.avatar_user);
            Picasso.get().load(image).placeholder(R.drawable.avatar).into(imageUser);
        }
        public void setLogin(String login)
        {
            TextView userLogin = (TextView) mView.findViewById(R.id.user_name);
            userLogin.setText(login);
        }

        public void datePost(String date)
        {
            TextView dateP = (TextView) mView.findViewById(R.id.post_date);
            dateP.setText(date);
        }

        public void textContent(String content)
        {
            TextView txt_content = (TextView) mView.findViewById(R.id.text_content);
            txt_content.setText(content);
        }

        public void pubImage(String image)
        {
            ImageView pub = (ImageView) mView.findViewById(R.id.pub_image);

            if(image.equals("default"))
            {
                pub.setVisibility(View.GONE);
            }else {
                /*ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader . displayImage (image, pub);*/
                Picasso.get().load(image).into(pub);
            }
        }

    }
}