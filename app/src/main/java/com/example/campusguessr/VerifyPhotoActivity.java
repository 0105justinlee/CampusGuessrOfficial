package com.example.campusguessr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class VerifyPhotoActivity extends AppCompatActivity {

    ImageButton backButton;
    ImageView userPhoto;
    ImageView savedPhoto;
    Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_photo);

        // Initialize views and buttons
        backButton = (ImageButton) findViewById(R.id.verify_photo_back_button);
        userPhoto = (ImageView) findViewById(R.id.userPhoto);
        savedPhoto = (ImageView) findViewById(R.id.actualPhoto);
        confirmButton = (Button) findViewById(R.id.verify_photo_post_button);
    }




}