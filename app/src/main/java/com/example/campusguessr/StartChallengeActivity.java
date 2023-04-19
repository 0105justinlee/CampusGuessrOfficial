package com.example.campusguessr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campusguessr.POJOs.Challenge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class StartChallengeActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private Challenge currentChallenge;
    JSONObject challengeObj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_challenge);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getChallenge();
        Button startChallengeButton = (Button) findViewById(R.id.start_challenge_button);
        ImageButton rankingsButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
        ImageButton createButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
        ImageButton profileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChallengeActivity.class);
                intent.putExtra("challenge", String.valueOf(challengeObj));
                startActivity(intent);
            }
        });
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateChallengeActivity.class));
            }
        });
        rankingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RankingsActivity.class));
            }
        });
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CreateChallengeActivity.class));
            }
        });
    }

    /**
     * Gets a random challenge from the Firebase real time database and stores to currentChallenge
     */
    private void getChallenge() {
        mDatabase.child("challenges").orderByKey().startAt(UUID.randomUUID().toString())
                .limitToFirst(1).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            String str = "Error getting data: " + task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            DataSnapshot dataSnapshot = task.getResult();
                            DataSnapshot childSnap = dataSnapshot.getChildren().iterator().next();
                            challengeObj = new JSONObject((Map) childSnap.getValue());
                            currentChallenge = new ObjectMapper().convertValue(childSnap.getValue(), Challenge.class);
                            getLocation();
                            new RetrieveImageTask().execute();
                        }
                    }
                });
    }

    /**
     * Gets the current location and compares it with the challenge goal
     * Displays distance from challenge location
     */
    private void getLocation() {
        // Check if permissions have been granted
        int finePermissionsGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarsePermissionsGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (finePermissionsGranted != PackageManager.PERMISSION_GRANTED && coarsePermissionsGranted != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Get location from location client
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(StartChallengeActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double[] currentCoords = new double[]{location.getLatitude(), location.getLongitude()};
                            com.example.campusguessr.POJOs.Location challengeLocation = currentChallenge.getLocation();
                            double distance_latitude = (currentCoords[0]- challengeLocation.getLatitude())*364000;
                            double distance_longitude = (currentCoords[1]-challengeLocation.getLongitude())*288200;
                            int distance = (int) Math.sqrt(distance_latitude*distance_latitude+distance_longitude*distance_longitude);
                            TextView distanceTextView = findViewById(R.id.start_challenge_distance);
                            String distanceText = "Distance: " + distance + " feet";
                            distanceTextView.setText(distanceText);
                        }
                    }
                });
    }

    class RetrieveImageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            URL newurl = null;
            final Bitmap mIcon_val;
            while (newurl == null) {
                try {
                    newurl = new URL(currentChallenge.getImageURL());
                } catch (MalformedURLException e) {
                    continue;
                }
            }
            try {
                mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ImageView imageView = findViewById(R.id.start_challenge_image);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(mIcon_val);
                }
            });
            return null;
        }
    }
}
