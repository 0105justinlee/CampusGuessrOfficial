package com.example.campusguessr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duplicate_detect);
//        startActivity(new Intent(getApplicationContext(), StartChallengeActivity.class));
    }
}