package com.example.campusguessr;

import android.app.Activity;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.campusguessr.POJOs.Challenge;
import com.example.campusguessr.POJOs.Orientation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DatabaseTestActivity extends Activity {
    private final String TAG = "DATABASE TEST";
    private DatabaseReference mDatabse;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_test);
        mDatabse = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
//      Run in another thread

        StorageReference storageRef = storage.getReference("challenges/img0.jpeg");
        UploadTask task = storageRef.putStream(getResources().openRawResource(R.raw.img0));

        task.addOnFailureListener(exception -> Log.d(TAG, "onFailure: "))
            .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "onSuccess: " + taskSnapshot.getMetadata().getName()));
        // wait for task to finish

        Challenge c1 = new Challenge(UUID.randomUUID());
        c1.setCreatedAt(new Date());
        c1.setCreatedBy(mAuth.getCurrentUser().getUid());
        c1.setDescription("Example description");
        c1.setLocation(new Location(""));
        c1.setOrientation(new Orientation(1.0, 2.0, 3.0));
        c1.setName("Example Name");
        c1.setImageURL("");

        try {
            Map c1Map = new ObjectMapper().convertValue(c1, Map.class);
            mDatabse.child("challenges").child(c1.getId().toString()).setValue(c1Map);
        } catch (Exception e) {
            Log.d(TAG, "bruh");
        }
    }
}
