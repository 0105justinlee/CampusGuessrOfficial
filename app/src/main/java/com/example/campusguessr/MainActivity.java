package com.example.campusguessr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, GoogleSignInActivity.class);
        startActivity(intent);
    }

    public void goToCreateChallenge(View view) {
        Intent intent = new Intent(this, CreateChallengeActivity.class);
        startActivity(intent);
    }
}