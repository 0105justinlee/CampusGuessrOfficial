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
  private boolean usernameUpdated;
  private boolean desiredDistanceUpdated;
  private boolean desiredDifficultyUpdated;
  
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
  
    usernameUpdated = false;
    desiredDistanceUpdated = false;
    desiredDifficultyUpdated = false;
    mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DataSnapshot> task) {
        if (!task.isSuccessful()) {
          String str = "Error getting data: " + task.getException().getMessage();
          Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        }
        else {
          HashMap user = (HashMap) task.getResult().getValue();
          currentUser = new User();
          Log.d(TAG, "onComplete: " + user.get("uid"));
  
          currentUser.setUid(user.get("uid").toString());
          currentUser.setName(user.get("name").toString());
          if(user.containsKey("desiredDistance")) {
            Long distance = (Long) user.get("desiredDistance");
            currentUser.setDesiredDistance(distance.intValue());
          }
          else {
            currentUser.setDesiredDistance(50);
          }
          if(user.containsKey("desiredDifficulty")) {
            Long difficulty = (Long) user.get("desiredDifficulty");
            currentUser.setDesiredDifficulty(difficulty.intValue());
          }
          else {
            currentUser.setDesiredDifficulty(50);
          }
          currentUser.setScore((Long)user.get("score"));
          Log.d(TAG, "onComplete: " + currentUser);
          NewUsername.setText(currentUser.getName());
          DesiredDistance.setProgress(currentUser.getDesiredDistance());
          DesiredDifficulty.setProgress(currentUser.getDesiredDifficulty());
        }
      }
    });
    
  
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
    
    DesiredDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    
      }
  
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
    
      }
  
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        desiredDifficultyUpdated = true;
        currentUser.setDesiredDifficulty(seekBar.getProgress());
      }
    });
    
    DesiredDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        // do nothing
      }
  
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        // do nothing
      }
  
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        desiredDistanceUpdated = true;
        currentUser.setDesiredDistance(seekBar.getProgress());
      }
    });
  }
  
  private void setUsername(String username) {
    if(username.length() > 50) {
      Toast.makeText(getApplicationContext(), "Username must have length < 50 characters", Toast.LENGTH_SHORT).show();
      return;
    }
    // ensure this username does not exist elsewhere in the database
    mDatabase.child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
  
      @Override
      public void onComplete(@NonNull Task<DataSnapshot> task) {
        if (!task.isSuccessful()) {
          String str = "Error getting data: " + task.getException().getMessage();
          Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        } else {
          DataSnapshot result = task.getResult();
          Iterator iterator = result.getChildren().iterator();
          for (int i = 0; i < result.getChildrenCount(); i++) {
            DataSnapshot next = (DataSnapshot) iterator.next();
            HashMap user = (HashMap) next.getValue();
            if (user.get("name").equals(username)) {
              Toast.makeText(getApplicationContext(), "This username has been taken", Toast.LENGTH_SHORT).show();
              return;
            }
          }
          // set username
          usernameUpdated = true;
          currentUser.setName(username);
          Toast.makeText(getApplicationContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
        }
      }
    });
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    // push new username
    if (usernameUpdated || desiredDistanceUpdated || desiredDifficultyUpdated) {
      HashMap map = new HashMap();
      map.put(mAuth.getCurrentUser().getUid(), currentUser);
      mDatabase.child("users").updateChildren(map);
    }
  }
}