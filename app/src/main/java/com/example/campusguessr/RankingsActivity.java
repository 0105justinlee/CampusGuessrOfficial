package com.example.campusguessr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class RankingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rankings);
        ImageButton PlayButton = (ImageButton) findViewById(R.id.navigate_play_tab_button);
        ImageButton CreateButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
        ImageButton ProfileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StartChallengeActivity.class));
            }
        });
        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateChallengeActivity.class));
            }
        });
        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });
    }
}
