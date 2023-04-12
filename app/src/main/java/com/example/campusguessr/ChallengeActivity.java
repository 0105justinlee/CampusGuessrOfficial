package com.example.campusguessr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campusguessr.POJOs.Challenge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executor;

public class ChallengeActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private double[] currentCoords;
    private double[] currentChallenge;
    private ArrayList<String> guesses;
    private GuessAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up recycler view for tracking user guesses
        guesses = new ArrayList<String>();
        RecyclerView recyclerView = findViewById(R.id.guesses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuessAdapter(guesses);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase resources
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize buttons
        ImageButton RankingsButton = findViewById(R.id.navigate_ranking_tab_button);
        ImageButton ProfileButton = findViewById(R.id.navigate_profile_tab_button);
        Button GuessButton = findViewById(R.id.guess_button);

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
                            double distance_latitude = (currentCoords[0]-currentChallenge[0])*364000;
                            double distance_longitude = (currentCoords[1]-currentChallenge[1])*288200;
                            double distance = Math.sqrt(distance_latitude*distance_latitude+distance_longitude*distance_longitude);
                            guesses.add("You are " + distance + " feet away!");
                            adapter.notifyItemInserted(guesses.size()-1);
                        }
                    }
                });
    }
    private void getChallenge() {
        mDatabase.child("challenges").child("02672d4d-75b4-4c86-bb38-65c247f72f5f").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), ("Error getting data: " + task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
                else {
                    Challenge currentChallenge = new ObjectMapper().convertValue(task.getResult().getValue(), Challenge.class);
                    Toast.makeText(getApplicationContext(), currentChallenge.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        currentChallenge = new double[]{43.0715302, -89.40853};
    }
}