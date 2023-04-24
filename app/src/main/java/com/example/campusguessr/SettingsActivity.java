package com.example.campusguessr;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.campusguessr.POJOs.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;

public class SettingsActivity extends AppCompatActivity {
  
  private FirebaseAuth mAuth;
  private DatabaseReference mDatabase;
  private User currentUser;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);
    mAuth = FirebaseAuth.getInstance();
    mDatabase = FirebaseDatabase.getInstance().getReference();
    
    ImageButton RankingsButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
    ImageButton CreateButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
    ImageButton PlayButton = (ImageButton) findViewById(R.id.navigate_play_tab_button);
    ImageButton ProfileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
  
    EditText NewUsername = (EditText) findViewById(R.id.settings_edittext_username);
    Button SubmitUsernameButton = (Button) findViewById(R.id.settings_username_button);
    
    SeekBar DesiredDistance = (SeekBar) findViewById(R.id.settings_seekbar_desiredDistance);
    SeekBar DesiredDifficulty = (SeekBar) findViewById(R.id.settings_seekbar_desiredDifficulty);
    
    mDatabase.child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
  
      @Override
      public void onComplete(@NonNull Task<DataSnapshot> task) {
        if (!task.isSuccessful()) {
          String str = "Error getting data: " + task.getException().getMessage();
          Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        }
        else {
          DataSnapshot result = task.getResult();
          Iterator iterator = result.getChildren().iterator();
          for(int i = 0; i < result.getChildrenCount(); i++) {
            DataSnapshot next = (DataSnapshot) iterator.next();
            HashMap user = (HashMap) next.getValue();
            if(user.get("name").equals(mAuth.getCurrentUser().getDisplayName())) {
              // current user found
              currentUser = new User();
              currentUser.setUid(user.get("uid"));
              currentUser.setName(user.get("name"));
              if(user.containsKey("desiredDistance")) {
                currentUser.setDesiredDistance(user.get("desiredDistance"));
              }
              else {
                currentUser.setDesiredDistance(50);
              }
              if(user.containsKey("desiredDifficulty")) {
                currentUser.setDesiredDifficulty(user.get("desiredDifficulty"));
              }
              else {
                currentUser.setDesiredDifficulty(50);
              }
              currentUser.setScore(user.get("score"));
            }
          }
        }
      }
    });
    // TODO set initial progress of seekbars by reading database
    // DesiredDistance.setProgress(currentUser.)
    // DesiredDifficulty.setProgress(currentUser.)
    
  
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
    ProfileButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
      }
    });
    
    SubmitUsernameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // attempt to set new username
        setUsername(String.valueOf(NewUsername.getText()));
      }
    });
  }
  
  private void setUsername(String username) {
    // ensure text does not contain invalid characters
    
    // ensure this username does not exist elsewhere in the database
    
    // set username
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
    // TODO push new values to database
  }
}