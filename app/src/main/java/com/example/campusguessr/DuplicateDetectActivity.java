package com.example.campusguessr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DuplicateDetectActivity extends AppCompatActivity {

    ImageView userPhoto;
    ImageView actualPhoto;
    Button yesButton;
    Button noButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duplicate_detect);

        userPhoto = findViewById(R.id.userPhoto);
        actualPhoto = findViewById(R.id.actualPhoto);
        yesButton = findViewById(R.id.duplicate_detect_button_yes);
        noButton = findViewById(R.id.duplicate_detect_button_no);

        Intent intent = getIntent();

        // Retrieve the file path of the user's photo from the intent
        String userPhotoPath = intent.getStringExtra("photoPath");

        // Load the user's photo into the userPhoto ImageView
        Bitmap userBitmap = BitmapFactory.decodeFile(userPhotoPath);
        userPhoto.setImageBitmap(userBitmap);

        // Retrieve the child key of the actual photo from the intent
        String childKey = intent.getStringExtra("Duplicate Picture");

        // Retrieve the actual photo from the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference().child("challenges").child(childKey).child("imageURL");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageURL = snapshot.getValue(String.class);
                    // Use a library like Picasso or Glide to load the image into the actualPhoto ImageView
                    Picasso.get().load(imageURL).into(actualPhoto);
                } else {
                    // Handle the case where the imageURL does not exist
                    Toast.makeText(DuplicateDetectActivity.this, "Image not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
                System.out.println("Database error: " + error.getMessage());
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }
}