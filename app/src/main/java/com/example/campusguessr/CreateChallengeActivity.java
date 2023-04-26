package com.example.campusguessr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusguessr.POJOs.Challenge;
import com.example.campusguessr.POJOs.Location;
import com.example.campusguessr.POJOs.Orientation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class CreateChallengeActivity extends AppCompatActivity {
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

        // Keep title and description the same when activity recreated due to duplicate detection
        if(savedInstanceState != null) {
            String editTextString = savedInstanceState.getString("titleText");
            titleText.setText(editTextString);

            editTextString = savedInstanceState.getString("descriptionText");
            descriptionText.setText(editTextString);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // Save instances in editText fields in case of activity recreation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("titleText", titleText.getText().toString());
        outState.putString("descriptionText", descriptionText.getText().toString());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();
        if (requestCode == 0){
            if (resultCode == RESULT_OK) {
                location = extras.getDoubleArray("location");
                orientation = extras.getFloatArray("orientation");
                photoPath = extras.getString("photoPath");

                photoView.setImageURI(Uri.parse(photoPath));
            }
        }

         // Called after user presses the button from DuplicateDetectActivity
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // User pressed "yes": restart the current activity
                recreate();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User pressed "no": continue with the activity here
            }
        }
    }

    public void CapturePhoto(View view) {
        Intent intent = new Intent(this, CameraCapture.class);
        startActivityForResult(intent, 0);
    }

    public void submit(View view) {
        // Check for duplicates before submitting the challenge
        checkDuplicate();

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

    // Calculate orientation diff
    private double angleDiff(float[] orientationValues, double X, double Y, double Z) {
        double deltaAzimuth = Math.abs(Math.toDegrees(orientationValues[0]) - X);
        double deltaPitch = Math.abs(Math.toDegrees(orientationValues[1]) - Y);
        double deltaRoll = Math.abs(Math.toDegrees(orientationValues[2]) - Z);

        double angleDiff = Math.sqrt(Math.pow(deltaAzimuth, 2) + Math.pow(deltaPitch, 2) + Math.pow(deltaRoll, 2));
        return angleDiff;
    }

    // Calculate distance diff
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth's radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in kilometers
        return distance;
    }

    /*
     * Method to check duplicate and open duplicate detect activity
     */
    private void checkDuplicate() {
        // Get a reference to the Firebase Realtime Database
        String usernameString = mAuth.getCurrentUser().getDisplayName();
        String userId = mAuth.getCurrentUser().getUid();

        DatabaseReference dbRef = mDatabase.child("challenges");

        // Retrieve data from the database
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop through each child node
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve the child's key and data
                    String childKey = childSnapshot.getKey();

                    // Location variables
                    double curLatitude =
                            Double.parseDouble(childSnapshot.child("location").child("latitude").getValue().toString());
                    double curLongitude =
                            Double.parseDouble(childSnapshot.child("location").child("longitude").getValue().toString());
                    // Calculate the distance between the current coordinates and the coordinates in the database
                    double distance = distance(location[0], location[1], curLatitude, curLongitude);
                    double LOCATION_THRESHOLD = 0.2;  // 200 meters -> Can modify

                    // Orientation variables
                    float curX = Float.parseFloat(childSnapshot.child("orientation").child("x").getValue().toString());
                    float curY = Float.parseFloat(childSnapshot.child("orientation").child("y").getValue().toString());
                    float curZ = Float.parseFloat(childSnapshot.child("orientation").child("z").getValue().toString());
                    float ANGLE_THRESHOLD = 60.0f; // 60 degrees -> Can modify
                    // Calculate the difference in orientation between the current and database values
                    double angleDiff = angleDiff(orientation, curX, curY, curZ);

                    // if duplicate suspected
                    if (distance < LOCATION_THRESHOLD && angleDiff < ANGLE_THRESHOLD) {
                        // Move to duplicate detect activity
                        Intent intent = new Intent(getApplicationContext(), DuplicateDetectActivity.class);
                        intent.putExtra("Duplicate Picture", childKey);
                        intent.putExtra("photoPath", photoPath);
                        startActivityForResult(intent, 1);
                    }

//                    if (Math.abs(location[0] - curLatitude) < 5 && Math.abs(location[1] - curLongitude) < 5 && Math.abs(curX - orientation[0]) < 5 && Math.abs(curY - orientation[1]) < 5 && Math.abs(curZ - orientation[2]) < 5) {
//                        // Move to duplicate detect activity
//                        Intent intent = new Intent(getApplicationContext(), DuplicateDetectActivity.class);
//                        intent.putExtra("Duplicate Picture", childKey);
//                        intent.putExtra("photoPath", photoPath);
//                        startActivityForResult(intent, 1);
//                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }
}
