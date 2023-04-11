package com.example.campusguessr;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.campusguessr.POJOs.Attempt;
import com.example.campusguessr.POJOs.Challenge;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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

    public void submitChallengeMock(View view) {
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
        submitChallenge("mock", mockGuesses, 600);
    }

    public void submitChallenge(String challengeId, Location[] guesses, int playtime) {
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
            int myScore = guesses.length;
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

            DatabaseReference scoreRef = mDatabase.child("user")
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
                        } else {
                            Log.d(TAG, "submitChallenge: failed to get score");
                        }
                    });
        });
    }
}
