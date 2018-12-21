package com.example.videostream;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Objects;

public class CamerRecord extends AppCompatActivity {

    private static final String TAG = "CamerRecord";
    static final int REQUEST_VIDEO_CAPTURE = 1;
//    private VideoView mVideoView;
//    private MediaController mediaController;
    private StorageReference mStorageRef;
    private ProgressBar progressBar;
    private ImageButton takePhoto;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camer_record);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        takePhoto = (ImageButton) findViewById(R.id.takevideo);
        status = (TextView) findViewById(R.id.status);
//        mVideoView = (VideoView) findViewById(R.id.cameravideo);
//        mediaController= new MediaController(this);
//        mediaController.setAnchorView(mVideoView);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
            Uri videoUri = intent.getData();
            Log.d(TAG, "onActivityResult: URI = "+videoUri);
//            mVideoView.setMediaController(mediaController);
//            mVideoView.setVideoURI(videoUri);
//            mVideoView.requestFocus();
//            mVideoView.start();

//            Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
            final StorageReference riversRef = mStorageRef.child(""+"sample");

            if (videoUri != null) {
                riversRef.putFile(videoUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return riversRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            status.setText("Video Uploaded Successfully");
                            Uri downloadUri = task.getResult();
                            Log.d(TAG, "onComplete: link = "+downloadUri);
                            Intent intent = new Intent(CamerRecord.this,MainActivity.class);
                            intent.putExtra("uri",downloadUri);
                            startActivity(intent);
                            finish();
                        } else {
                            status.setText("Video Uploaded Not Successfull");
                            Toast.makeText(CamerRecord.this, "upload failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }


    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }
}
