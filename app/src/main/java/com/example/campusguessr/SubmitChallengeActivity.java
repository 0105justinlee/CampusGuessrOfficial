package com.example.campusguessr;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.example.campusguessr.POJOs.Attempt;
import com.example.campusguessr.POJOs.Challenge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class SubmitChallengeActivity extends Activity {
    private final String TAG = "Submit Challenge";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_challenge);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void submitChallengeMock() {
        Location[] mockGuesses = new Location[3];
        mockGuesses[0] = new Location("mock");
        mockGuesses[0].setLatitude(0.0);
        mockGuesses[0].setLongitude(0.0);
        mockGuesses[1] = new Location("mock");
        mockGuesses[1].setLatitude(0.0);
        mockGuesses[1].setLongitude(0.0);
        mockGuesses[2] = new Location("mock");
        mockGuesses[2].setLatitude(0.0);
        mockGuesses[2].setLongitude(0.0);
        submitChallenge("mock", mockGuesses);
    }

    public void submitChallenge(String challengeId, Location[] guesses) {
        int num_guess = guesses.length;
        String uId = mAuth.getCurrentUser().getUid();
        Date currentTime = Calendar.getInstance().getTime();

        mDatabase.child("challenge")
                .child(challengeId)
                .child(uId)
                .setValue(num_guess);

        // Create new Attempt object and upload
        Attempt attempt = new Attempt(UUID.randomUUID().toString(), challengeId, uId, guesses, currentTime);
        Map attMap = new ObjectMapper().convertValue(attempt, Map.class);
        mDatabase.child("attempt-by-user")
                .child(uId)
                .push()
                .setValue(attMap);
    }
}
