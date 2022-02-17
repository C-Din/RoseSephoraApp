package com.example.rosesphora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PostActivity extends AppCompatActivity {

    private EditText pubTitle;
    private LinearLayout addImage;
    private Button sendPub;
    private ImageView media;

    private androidx.appcompat.widget.Toolbar mToolbar;
    private int reqCode;
    private Intent mData;
    private String pub_id, urlImage;

    private DatabaseReference mPubDatabase;
    private FirebaseUser mCurrentUser;

    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;

    private Bitmap bitmap;

    private Uri ImageUri;

    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.pub_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Nouvelle Publication");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pubTitle = (EditText) findViewById(R.id.contentPub);
        addImage = (LinearLayout) findViewById(R.id.add_newPub);
        media = (ImageView) findViewById(R.id.contentmedia);
        sendPub = (Button) findViewById(R.id.pub_sum);
        mProgressDialog = new ProgressDialog(this);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String current_uid = mCurrentUser.getUid();

        mPubDatabase = FirebaseDatabase.getInstance().getReference().child("publications");
        mPubDatabase.keepSynced(true);
        pub_id = mPubDatabase.push().getKey();

        sendPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatePubtData();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                OpenGallery();

            }
        });
    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        reqCode = requestCode;
        mData = data;

        if (reqCode == GalleryPick  &&  resultCode == RESULT_OK  &&  data!=null)
        {
            ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(5000, 5000)
                    .start(this);
            media.setImageURI(ImageUri);
        }
    }

    private void StorePubInformation()
    {

        mProgressDialog.setTitle("Ajout de la publication en cours");
        mProgressDialog.setMessage("Svp patientez pendant que nous postons votre publication");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        if(ImageUri == null)
        {
            SaveProductInfoToDatabase();
        }
        else
        {
            if (reqCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(mData);

                ImageUri = result.getUri();

                final StorageReference filePath = mImageStorage.child("publications").child(mCurrentUser.getUid()).child(pub_id + ".jpg");

                final UploadTask uploadTask = filePath.putFile(ImageUri);


                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String message = e.toString();
                        Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(PostActivity.this, "Publication téléchargée avec succès", Toast.LENGTH_SHORT).show();

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                            {
                                if (!task.isSuccessful())
                                {
                                    throw task.getException();
                                }

                                urlImage = filePath.getDownloadUrl().toString();
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task)
                            {
                                if (task.isSuccessful())
                                {
                                    urlImage = task.getResult().toString();


                                    SaveProductInfoToDatabase();
                                }
                            }
                        });
                    }
                });
            }



        }

    }


    private void ValidatePubtData()
    {

        if (ImageUri == null && TextUtils.isEmpty(pubTitle.getText().toString()))
        {
            Toast.makeText(this, "Veuillez renseigner au moins une information svp!!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            StorePubInformation();
        }
    }


    private void SaveProductInfoToDatabase()
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c.getTime());

        Map update_hashMap = new HashMap();

        if(ImageUri == null)
        {
            update_hashMap.put("pub_image", "default");
        }
        else
        {
            update_hashMap.put("pub_image", urlImage);
        }


        update_hashMap.put("user_id", mCurrentUser.getUid());
        update_hashMap.put("post_date", strDate);
        update_hashMap.put("text_content", pubTitle.getText().toString().trim());

        mPubDatabase.child(pub_id).updateChildren(update_hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(PostActivity.this, MainActivity.class);
                            startActivity(intent);

                            mProgressDialog.dismiss();
                            Toast.makeText(PostActivity.this, "Publication ajoutée avec succès", Toast.LENGTH_SHORT).show();
                            OneSignal.sendTag("Pub_Id", "non-notif");
                            sendNotification();
                        }
                        else
                        {
                            mProgressDialog.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                                + "\"contents\": {\"en\": \"Nouveau post\"}"
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
}