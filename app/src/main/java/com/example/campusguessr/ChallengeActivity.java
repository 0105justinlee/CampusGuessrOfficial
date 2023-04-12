package com.example.campusguessr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.campusguessr.POJOs.Attempt;
import com.example.campusguessr.POJOs.Challenge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ChallengeActivity extends AppCompatActivity {
    String TAG = "ChallengeActivity";
    int guessesMade = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private double[] currentCoords;
    double[] currentChallenge;
    ArrayList<String> guesses;
    ArrayList<Location> guessLocations; // Locations for mapping player path
    GuessAdapter adapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getChallenge();

        // Initialize guess tracking lists
        guesses = new ArrayList<String>();
        guessLocations = new ArrayList<Location>();

        // Set up recycler view for guesses
        RecyclerView recyclerView = findViewById(R.id.guesses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuessAdapter(guesses);
        recyclerView.setAdapter(adapter);

        // Initialize buttons
        ImageButton RankingsButton = findViewById(R.id.navigate_ranking_tab_button);
        ImageButton ProfileButton = findViewById(R.id.navigate_profile_tab_button);
        Button GuessButton = findViewById(R.id.guess_button);

        // Set up navigation menu
        RankingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RankingsActivity.class));
            }
        });
        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        // Main challenge guessing logic
        GuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locationRequest = new LocationRequest.Builder(100).build();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(ChallengeActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentCoords = new double[]{location.getLatitude(), location.getLongitude()};
                            guessesMade++;
                            double distance_latitude = (currentCoords[0]-currentChallenge[0])*364000;
                            double distance_longitude = (currentCoords[1]-currentChallenge[1])*288200;
                            double distance = Math.sqrt(distance_latitude*distance_latitude+distance_longitude*distance_longitude);
                            if (distance < 50) {
                                Toast.makeText(ChallengeActivity.this, "You won!", Toast.LENGTH_SHORT).show();
                                // covert guessLocations to com.example.campusguessr.POJOs.Location[]
                                com.example.campusguessr.POJOs.Location[] guessesMade = new com.example.campusguessr.POJOs.Location[guessLocations.size()];
                                submitChallenge("test", guessesMade, 0);
                                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                            }
                            guesses.add("You are " + distance + " feet away!");
                            guessLocations.add(location);
                            adapter.notifyItemInserted(guesses.size()-1);
                        }
                    }
                });
    }
    private void getChallenge() {
        currentChallenge = new double[]{43.07176, -89.4014};
    }

    public Future<Challenge> submitChallenge(String challengeId, com.example.campusguessr.POJOs.Location[] guesses, int playtime) {
        CompletableFuture<Challenge> f2 = new CompletableFuture<>();
        CompletableFuture<Challenge> f = new CompletableFuture<>();
        mDatabase.child("challenges").child(challengeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot ds = task.getResult();
                if (ds.exists()) {
                    Map m = (Map<String, Object>) ds.getValue();
                    ObjectMapper mapper = new ObjectMapper();
                    Challenge challenge = mapper.convertValue(m, Challenge.class);
                    f.complete(challenge);
                } else {
                    Toast.makeText(this, "challenge does not exist", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "submitChallenge: challenge does not exist");
                    f.cancel(false);
                }
            } else {
                Log.d(TAG, "submitChallenge: failed to get challenge");
                f.cancel(true);
            }
        });

        f.thenAccept(challenge -> {
            Integer distance = challenge.getLocation().distanceTo(guesses[guesses.length - 1]);
            int myScore = distance / guesses.length;
            String uId = mAuth.getCurrentUser().getUid();
            Date currentTime = Calendar.getInstance().getTime();

            // Create new Attempt object and upload
            Attempt attempt = new Attempt(UUID.randomUUID().toString(), challengeId, uId, guesses, currentTime);
            Map attMap = new ObjectMapper().convertValue(attempt, Map.class);
            mDatabase.child("attempt")
                    .child(attempt.getId())
                    .setValue(attMap);

            mDatabase.child("attempt-by-user")
                    .child(uId)
                    .push()
                    .setValue(attempt.getId());

            mDatabase.child("attempt-by-challenge")
                    .child(challengeId)
                    .push()
                    .setValue(attempt.getId());

            DatabaseReference scoreRef = mDatabase.child("users")
                    .child(uId)
                    .child("score");
            scoreRef
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot ds = task.getResult();
                            if (ds != null && ds.getValue() != null) {
                                int score = ds.getValue(Integer.class);
                                scoreRef.setValue(score + myScore);
                            } else {
                                scoreRef.setValue(myScore);
                            }
                            Toast.makeText(this, "Submitted Challenge", Toast.LENGTH_SHORT).show();
                            f2.complete(challenge);
                        } else {
                            Log.d(TAG, "submitChallenge: failed to get score");
                            f2.cancel(true);
                        }
                    });
        });
        return f2;
    }
}