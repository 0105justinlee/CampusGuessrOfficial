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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.var;

public class ChallengeActivity extends AppCompatActivity {
    String TAG = "ChallengeActivity";
    private FusedLocationProviderClient fusedLocationClient;
    private double[] currentCoords;
    private Challenge currentChallenge;
    private ArrayList<String> guesses;
    private GuessAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    ArrayList<Location> guessLocations; // Locations for mapping player path
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up recycler view for tracking user guesses
        guesses = new ArrayList<String>();
        guessLocations = new ArrayList<Location>();

        // Set up recycler view for guesses
        RecyclerView recyclerView = findViewById(R.id.guesses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuessAdapter(guesses);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase resources
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize buttons
        ImageButton RankingsButton = findViewById(R.id.navigate_ranking_tab_button);
        ImageButton ProfileButton = findViewById(R.id.navigate_profile_tab_button);
        Button GuessButton = findViewById(R.id.guess_button);

        // Initialize challenge for this session
        getChallenge();

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

    /**
     * Gets the current location and compares it with the challenge goal
     * Switches to challenge complete screen if the location is found, otherwise displays distance
     */
    private void getLocation() {
        // Check if permissions have been granted
        int finePermissionsGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarsePermissionsGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (finePermissionsGranted != PackageManager.PERMISSION_GRANTED && coarsePermissionsGranted != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Get location from location client
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(ChallengeActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentCoords = new double[]{location.getLatitude(), location.getLongitude()};
                            com.example.campusguessr.POJOs.Location challengeLocation = currentChallenge.getLocation();
                            double distance_latitude = (currentCoords[0]- challengeLocation.getLatitude())*364000;
                            double distance_longitude = (currentCoords[1]-challengeLocation.getLongitude())*288200;
                            double distance = Math.sqrt(distance_latitude*distance_latitude+distance_longitude*distance_longitude);

                            guesses.add("You are " + distance + " feet away!");
                            guessLocations.add(location);
                            adapter.notifyItemInserted(guesses.size()-1);

//                            if (distance < 50) {
                            if (distance < 50 || guesses.size() == 2) { // TODO: remove this line when testing is done
                                Toast.makeText(ChallengeActivity.this, "You won!", Toast.LENGTH_SHORT).show();
                                // covert guessLocations to com.example.campusguessr.POJOs.Location[]
                                com.example.campusguessr.POJOs.Location[] guessesMade = new com.example.campusguessr.POJOs.Location[guessLocations.size()];
                                for (int i = 0; i < guessLocations.size(); i++) {
                                    guessesMade[i] = new com.example.campusguessr.POJOs.Location(guessLocations.get(i).getLatitude(), guessLocations.get(i).getLongitude());
                                }
                                submitChallenge(currentChallenge.getId().toString(), guessesMade, 0).handle((result, error) -> {
                                    if (error != null) {
                                        Toast.makeText(ChallengeActivity.this, "Challenge submission failed!", Toast.LENGTH_SHORT).show();
                                        return null;
                                    }
                                    if (result != null) {
                                        Toast.makeText(ChallengeActivity.this, "Challenge submitted!", Toast.LENGTH_SHORT).show();
                                        var intent = new Intent(getApplicationContext(), CompleteChallengeActivity.class);
                                        intent.putExtra("attemptId", result.getId());
                                        startActivity(intent);
                                    }
                                    return null;
                                });
                            }
                        }
                    }
                });
    }

    /**
     * Gets a random challenge from the Firebase real time database and stores to currentChallenge
     */
    private void getChallenge() {
        try {
            currentChallenge = new ObjectMapper().readValue(getIntent().getStringExtra("challenge"), Challenge.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Attempt> submitChallenge(String challengeId, com.example.campusguessr.POJOs.Location[] guesses, int playtime) {
        var f = new CompletableFuture<Attempt>();
        mDatabase.child("challenges").child(challengeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot ds = task.getResult();
                if (ds.exists()) {
                    Map m = (Map<String, Object>) ds.getValue();
                    ObjectMapper mapper = new ObjectMapper();
                    Challenge challenge = mapper.convertValue(m, Challenge.class);
                    Integer distance = challenge.getLocation().distanceTo(guesses[0]);
                    int myScore = distance / guesses.length;
                    String uId = mAuth.getCurrentUser().getUid();
                    Date currentTime = Calendar.getInstance().getTime();
                    Log.d(TAG, "submitChallenge: " + myScore);

                    // Create new Attempt object and upload
                    Attempt attempt = new Attempt(UUID.randomUUID().toString(), uId, challengeId, guesses, myScore, playtime, currentTime);
                    Map attMap = new ObjectMapper().convertValue(attempt, Map.class);
                    Task t1 = mDatabase.child("attempts")
                            .child(attempt.getId())
                            .setValue(attMap);

                    Task t2 = mDatabase.child("attempt-by-user")
                            .child(uId)
                            .push()
                            .setValue(attempt.getId());

                    Task t3 = mDatabase.child("attempt-by-challenge")
                            .child(challengeId)
                            .push()
                            .setValue(attempt.getId());

                    DatabaseReference scoreRef = mDatabase.child("users")
                            .child(uId)
                            .child("score");

                    Task<DataSnapshot> t4 = scoreRef.get();

                    // make sure all tasks are complete
                    Tasks.whenAllComplete(t1, t2, t3, t4).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            if (task1.getResult().get(0).isSuccessful() && task1.getResult().get(1).isSuccessful() && task1.getResult().get(2).isSuccessful()) {
                                // update score
                                if (task1.getResult().get(3).isSuccessful()) {
                                    DataSnapshot ds1 = (DataSnapshot) task1.getResult().get(3).getResult();
                                    if (ds1.exists()) {
                                        Integer score = ds1.getValue(Integer.class);
                                        scoreRef.setValue(score + myScore);
                                    } else {
                                        scoreRef.setValue(myScore);
                                    }
                                    Toast.makeText(this, "Submitted Attempt", Toast.LENGTH_SHORT).show();
                                    f.complete(attempt);
                                }
                            } else {
                                Log.d(TAG, "submitChallenge: failed to upload attempt");
                                throw new CompletionException(new Exception("failed to upload attempt"));
                            }
                        } else {
                            Log.d(TAG, "submitChallenge: failed to upload attempt");
                            throw new CompletionException(task1.getException());
                        }
                    });

                } else {
                    Toast.makeText(this, "challenge does not exist", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "submitChallenge: challenge does not exist");
                    throw new CompletionException(new Exception("challenge does not exist"));
                }
            } else {
                Log.d(TAG, "submitChallenge: failed to get challenge");
                throw new CompletionException(task.getException());
            }
        });

        return f;
    }
}