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
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campusguessr.POJOs.Attempt;
import com.example.campusguessr.POJOs.Challenge;
import com.example.campusguessr.POJOs.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class StartChallengeActivity extends AppCompatActivity {
    private int maxDistance;
    private float desiredDifficulty;
    private int challengesFetched = 0;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Challenge oldChallenge;
    private Challenge currentChallenge;
    JSONObject challengeObj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_challenge);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        Button startChallengeButton = (Button) findViewById(R.id.start_challenge_button);
        Button rerollButton = findViewById(R.id.reroll_button);
        ImageButton rankingsButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
        ImageButton createButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
        ImageButton profileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
        getSettings();
        getChallenge();
        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChallengeActivity.class);
                intent.putExtra("challenge", String.valueOf(challengeObj));
                startActivity(intent);
            }
        });
        rerollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChallenge();
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

    private AtomicInteger toCheck;

    /**
     * Gets a random challenge from the Firebase real time database and stores to currentChallenge
     */
    private void getChallenge() {
        mDatabase.child("challenges").orderByKey().startAt(UUID.randomUUID().toString())
                .limitToFirst(10).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            String str = "Error getting data: " + task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            DataSnapshot dataSnapshot = task.getResult();
                            if (!dataSnapshot.hasChildren()) {
                                // If random UUID excluded all entries, try again
                                getChallenge();
                                return;
                            }
                            toCheck = new AtomicInteger((int) dataSnapshot.getChildrenCount());
                            ((ImageView) findViewById(R.id.start_challenge_image)).setImageBitmap(null);
                            currentChallenge = null;
                            for (DataSnapshot childSnap:dataSnapshot.getChildren()) {
                                JSONObject currentChallengeObj = new JSONObject((Map) childSnap.getValue());
                                Challenge newChallenge = new ObjectMapper().convertValue(childSnap.getValue(), Challenge.class);
                                if (oldChallenge != null && newChallenge.getId().toString().equals(oldChallenge.getId().toString())) {
                                    toCheck.getAndAdd(-1);
                                    if (toCheck.get() == 0) {
                                        getChallenge();
                                    }
                                    continue;
                                }
                                getLocation(newChallenge, currentChallengeObj);
                            }
                        }
                    }
                });
    }

    /**
     * Gets the current location and compares it with the challenge goal
     * Displays distance from challenge location
     */
    private void getLocation(Challenge challenge, JSONObject challengeObj) {
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
                            com.example.campusguessr.POJOs.Location challengeLocation = challenge.getLocation();
                            double distance_latitude = (currentCoords[0]- challengeLocation.getLatitude())*364000;
                            double distance_longitude = (currentCoords[1]-challengeLocation.getLongitude())*288200;
                            int distance = (int) Math.sqrt(distance_latitude*distance_latitude+distance_longitude*distance_longitude);
                            if (distance > maxDistance) {
                                toCheck.getAndAdd(-1);
                                if (toCheck.get() == 0) {
                                    getChallenge();
                                }
                                return;
                            }
                            getDifficulty(challenge, distance, challengeObj);
                        }
                    }
                });
    }

    /**
     * Gets the current challenge's difficulty and displays it for the user
     */
    private void getDifficulty(Challenge challenge, int distance, JSONObject challengeObj) {
        mDatabase.child("attempt-by-challenge").child(challenge.getId().toString())
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            String str = "Error getting data: " + task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Map<Object, String> attempts = (Map<Object, String>) task.getResult().getValue();
                            if (attempts == null) {
                                if (currentChallenge != null) {
                                    return;
                                }
                                TextView difficultyView = findViewById(R.id.start_challenge_difficulty);
                                difficultyView.setText(String.format("Difficulty: medium"));
                                currentChallenge = challenge;
                                StartChallengeActivity.this.challengeObj = challengeObj;
                                oldChallenge = challenge;
                                TextView distanceTextView = findViewById(R.id.start_challenge_distance);
                                String distanceText = "Distance: " + distance + " feet";
                                distanceTextView.setText(distanceText);
                                new RetrieveImageTask().execute();
                                return;
                            }
                            AtomicInteger attemptsCount = new AtomicInteger(attempts.values().size());
                            AtomicInteger attemptsChecked = new AtomicInteger(0);
                            AtomicInteger guessesCount = new AtomicInteger(0);
                            for (String attemptID:attempts.values()) {
                                mDatabase.child("attempts").child(attemptID).get()
                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        Attempt attempt = new ObjectMapper().convertValue(task.getResult().getValue(), Attempt.class);
                                        if (attempt == null) {
                                            attemptsCount.getAndAdd(-1);
                                        }
                                        else {
                                            attemptsChecked.getAndAdd(1);
                                            guessesCount.getAndAdd(attempt.getGuesses().length);
                                        }
                                        if (attemptsCount.get() == attemptsChecked.get()) {
                                            if (currentChallenge == null) {
                                                float avgGuesses = (float) guessesCount.get() / attemptsCount.get();
                                                if (desiredDifficulty == 0 && avgGuesses > 2.5
                                                        || desiredDifficulty == 1 && (avgGuesses > 4.5 || avgGuesses < 2.5)
                                                        || desiredDifficulty == 2 && avgGuesses < 4.5) {
                                                    toCheck.getAndAdd(-1);
                                                    if (toCheck.get() == 0) {
                                                        getChallenge();
                                                    }
                                                    return;
                                                }
                                                TextView difficultyView = findViewById(R.id.start_challenge_difficulty);
                                                if (attemptsCount.get() == 0) {
                                                    // Default difficulty medium if challenge is unattempted
                                                    difficultyView.setText(String.format("Difficulty: medium"));
                                                } else if (avgGuesses < 2.5) {
                                                    difficultyView.setText(String.format("Difficulty: easy"));
                                                } else if (avgGuesses < 4.5) {
                                                    difficultyView.setText(String.format("Difficulty: medium"));
                                                } else {
                                                    difficultyView.setText(String.format("Difficulty: hard"));
                                                }
                                                currentChallenge = challenge;
                                                StartChallengeActivity.this.challengeObj = challengeObj;
                                                oldChallenge = challenge;
                                                TextView distanceTextView = findViewById(R.id.start_challenge_distance);
                                                String distanceText = "Distance: " + distance + " feet";
                                                distanceTextView.setText(distanceText);
                                                new RetrieveImageTask().execute();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void getSettings() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    String str = "Error getting data: " + task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                }
                User currentUser = new ObjectMapper().convertValue(task.getResult().getValue(), User.class);
                maxDistance = currentUser.getDesiredDistance()*5280/100;
                int userDifficulty = currentUser.getDesiredDifficulty();
                if (userDifficulty < 34) {
                    desiredDifficulty = 0; // easy
                }
                else if (userDifficulty < 67) {
                    desiredDifficulty = 1; // medium
                }
                else {
                    desiredDifficulty = 2; // hard
                }
            }
        });
    }

    /**
     * Retrieves the challenge image asynchronously and displays it to the user
     */
    class RetrieveImageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            challengesFetched++;
            if (challengesFetched > 30) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "Excessive bandwidth used, please try again", Toast.LENGTH_SHORT).show();
                return null;
            }
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
