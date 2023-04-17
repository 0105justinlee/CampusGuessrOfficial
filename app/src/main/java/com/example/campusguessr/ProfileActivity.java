package com.example.campusguessr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Map;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    private DatabaseReference mDatabase;

    // hashmap structure: assume that all of the challenges here are the user's challenges they submitted
    // challengeid
    //      -> ArrayList
    //          -> title
    //          -> description
    //          -> photo url
    Map<String, ArrayList<String>> myChallenges = new HashMap<String, ArrayList<String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        ImageButton RankingsButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
        ImageButton CreateButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
        ImageButton PlayButton = (ImageButton) findViewById(R.id.navigate_play_tab_button);
        ImageButton SettingsButton = (ImageButton) findViewById(R.id.profile_settings_button);

        TextView username = findViewById(R.id.profile_username);
        mAuth = FirebaseAuth.getInstance();

        String usernameString = mAuth.getCurrentUser().getDisplayName();
        String userId = mAuth.getCurrentUser().getUid();
        username.setText(userId);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query myChallengesQuery = mDatabase.child("challenges").orderByChild("createdBy").equalTo(userId);
        myChallengesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(getBaseContext(), "Successfully retrieved challenges", Toast.LENGTH_SHORT).show();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    //Toast.makeText(getBaseContext(), "processing postsnapshot", Toast.LENGTH_SHORT).show();
                    ArrayList<String> challengeInfo = new ArrayList<String>();
                    // arraylist -> title, description, photo_link
                    challengeInfo.add((String)postSnapshot.child("name").getValue());
                    challengeInfo.add((String)postSnapshot.child("description").getValue());
                    challengeInfo.add((String)postSnapshot.child("imageURL").getValue());

                    String challengeId = (String)postSnapshot.getKey();
                    myChallenges.put(challengeId, challengeInfo);
                }
                System.out.println(myChallenges.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
                Toast.makeText(getBaseContext(), "Failed to retrieve challenges", Toast.LENGTH_SHORT).show();
            }
        });

        RecyclerView myChallengesRecycler = findViewById(R.id.recyclerview_recently_played);
        myChallengesRecycler.setLayoutManager(new LinearLayoutManager(this));


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
        SettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
    }
}
