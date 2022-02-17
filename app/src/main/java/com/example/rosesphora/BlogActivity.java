package com.example.rosesphora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BlogActivity extends AppCompatActivity {

    private FloatingActionButton post;
    private TextInputEditText f_author, f_title, f_link, f_content;
    private Button f_send;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mBlogDatabase;
    private RecyclerView mrecyclerView;
    private ProgressDialog mProgressDialog;
    private androidx.appcompat.widget.Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.blog_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Nouveaux postes du blog ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBlogDatabase = Utils.getDatabase().getReference().child("blogs");
        mProgressDialog = new ProgressDialog(this);

        mrecyclerView = (RecyclerView) findViewById(R.id.blog_list);
        mLayoutManager = new LinearLayoutManager(this);
        mrecyclerView.setHasFixedSize(true);
        mrecyclerView.setLayoutManager(mLayoutManager);

        post = (FloatingActionButton) findViewById(R.id.new_blog);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlogForm();
            }
        });
    }

    private void showBlogForm() {

        AlertDialog.Builder alert;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        }else {
            alert = new AlertDialog.Builder(this);
        }

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialogue_box, null);

        f_author = (TextInputEditText) view.findViewById(R.id.blog_author);
        f_title = (TextInputEditText) view.findViewById(R.id.blog_title);
        f_link = (TextInputEditText) view.findViewById(R.id.blog_link);
        f_content = (TextInputEditText) view.findViewById(R.id.blog_content);

        f_send = (Button) view.findViewById(R.id.blog_send);

        alert.setView(view);
        alert.setCancelable(true);

        f_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String blog_id = mBlogDatabase.push().getKey();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());

                Map update_hashMap = new HashMap();
                update_hashMap.put("post_date", strDate);
                update_hashMap.put("blog_title", f_title.getText().toString());
                update_hashMap.put("blog_content", f_content.getText().toString());
                update_hashMap.put("author", f_author.getText().toString());
                update_hashMap.put("blog_link", "Cliquer sur le lien pour lire la suite : "+f_link.getText().toString());


                mBlogDatabase.child(blog_id).setValue(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(BlogActivity.this, BlogActivity.class);
                            startActivity(intent);

                            mProgressDialog.dismiss();
                            Toast.makeText(BlogActivity.this, "Publication ajoutée avec succès", Toast.LENGTH_SHORT).show();
                            OneSignal.sendTag("Pub_Id", "non-notif");
                            sendNotification();
                        }
                        else
                        {
                            mProgressDialog.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(BlogActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        AlertDialog dialog = alert.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    public void sendNotification()
    {


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic OGU1YTFhMTgtMzIwZS00NTUzLWI4NmMtMTQ5NDVkMzBlNzNl");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"54daa68a-e69c-4113-89ec-d52033165f69\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"Pub_Id\", \"relation\": \"=\", \"value\": \"" + "notif" + "\"}],"

                                + "\"data\": {\"foo\": \"bar\"},"
                                + "\"contents\": {\"en\": \"Nouvel ajout dans le blog\"}"
                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });

        OneSignal.sendTag("Pub_Id", "notif");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogActivity.BlogsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogsViewHolder>(
                Blog.class,
                R.layout.blog_template,
                BlogsViewHolder.class,
                mBlogDatabase.orderByChild("post_date")
        ) {
            @Override
            protected void populateViewHolder(final BlogsViewHolder blogsViewHolder, final Blog blog, int i) {

                blogsViewHolder.setAuthor(blog.getAuthor());
                blogsViewHolder.setDate(blog.getPost_date());
                blogsViewHolder.setTitle(blog.getBlog_title());
                blogsViewHolder.setContent(blog.getBlog_content());
                blogsViewHolder.setLink(blog.getBlog_link());
            }
        };

        mrecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    public static class BlogsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BlogsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setAuthor(String author)
        {
            TextView authors = (TextView) mView.findViewById(R.id.author_blog);
            authors.setText(author);
        }

        public void setDate(String date)
        {
            TextView dateP = (TextView) mView.findViewById(R.id.post_date_blog);
            dateP.setText(date);
        }

        public void setTitle(String title)
        {
            TextView mTitle = (TextView) mView.findViewById(R.id.title_blog);
            mTitle.setText(title);
        }

        public void setContent(String content)
        {
            TextView mContent = (TextView) mView.findViewById(R.id.content_blog);
            mContent.setText(content);
        }

        public void setLink(String link)
        {
            TextView mLink = (TextView) mView.findViewById(R.id.link_blog);
            mLink.setMovementMethod(LinkMovementMethod.getInstance());
            mLink.setText(link);
        }

    }
}