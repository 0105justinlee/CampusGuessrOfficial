package com.example.campusguessr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class StartChallengeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_challenge);
        Button startChallengeButton = (Button) findViewById(R.id.start_challenge_button);
        ImageButton rankingsButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
        ImageButton profileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChallengeActivity.class));
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
    }
}
