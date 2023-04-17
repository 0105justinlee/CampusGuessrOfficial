package com.example.campusguessr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;

import com.example.campusguessr.POJOs.Challenge;
import com.example.campusguessr.POJOs.Location;
import com.example.campusguessr.POJOs.Orientation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class CreateChallengeActivity extends Activity {
    private final String TAG = "Create Challenge";

    private double[] location = new double[2];
    private float[] orientation = new float[3];
    private String photoPath;

    private TextView titleText;
    private TextView descriptionText;
    private TextView locOriText;
    private TextView photoPathText;
    private ImageView photoView;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_challenge);

        titleText = findViewById(R.id.titleText);
        descriptionText = findViewById(R.id.descriptionText);
        locOriText = findViewById(R.id.locOriText);
        photoPathText = findViewById(R.id.photoPathText);
        photoView = findViewById(R.id.photoView);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    
        // TODO figure out why icons do not show up
        ImageButton RankingsButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
        ImageButton CreateButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
        ImageButton PlayButton = (ImageButton) findViewById(R.id.navigate_play_tab_button);
        ImageButton ProfileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
    
        RankingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RankingsActivity.class));
            }
        });
        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateChallengeActivity.class));
            }
        });
        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StartChallengeActivity.class));
            }
        });
        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Bundle extras = intent.getExtras();
        if (resultCode == RESULT_OK) {
            location = extras.getDoubleArray("location");
            orientation = extras.getFloatArray("orientation");
            photoPath = extras.getString("photoPath");

            locOriText.setText("Location: " + location[0] + ", " + location[1] + ", Orientation: " + orientation[0] + ", " + orientation[1] + ", " + orientation[2]);
            photoPathText.setText("Photo Path: " + photoPath);
            photoView.setImageURI(Uri.parse(photoPath));
        }
    }

    public void CapturePhoto(View view) {
        Intent intent = new Intent(this, CameraCapture.class);
        startActivityForResult(intent, 0);
    }

    public void submit(View view) {
        Challenge c1 = new Challenge();
        c1.setId(UUID.randomUUID());
        c1.setCreatedAt(new Date());
        c1.setCreatedBy(mAuth.getCurrentUser().getUid());
        c1.setName(titleText.getText().toString());
        c1.setDescription(descriptionText.getText().toString());
        Location location = new Location();
        location.setLatitude(this.location[0]);
        location.setLongitude(this.location[1]);
        c1.setLocation(location);
        c1.setOrientation(new Orientation(orientation[0], orientation[1], orientation[2]));

        StorageReference storageRef = storage.getReference("challenges/" + c1.getId().toString() + ".jpg");
        UploadTask task = storageRef.putFile(Uri.fromFile(new File(photoPath)));
        task.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
            try {
                storageRef.getDownloadUrl().onSuccessTask(uri -> {
                    c1.setImageURL(uri.toString());
                    Map c1Map = new ObjectMapper().convertValue(c1, Map.class);
                    mDatabase.child("challenges").child(c1.getId().toString()).setValue(c1Map);
                    return null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload challenge", Toast.LENGTH_SHORT).show();
                    return;
                });
            } catch (Exception e) {
                Toast.makeText(this, "Failed to upload challenge", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, MainActivity.class);
            startActivityForResult(intent, 0);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }
}
