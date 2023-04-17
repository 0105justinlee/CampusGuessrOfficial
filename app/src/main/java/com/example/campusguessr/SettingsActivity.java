package com.example.campusguessr;

import static android.content.ContentValues.TAG;

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

public class SettingsActivity extends AppCompatActivity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);
    ImageButton RankingsButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
    ImageButton CreateButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
    ImageButton PlayButton = (ImageButton) findViewById(R.id.navigate_play_tab_button);
    ImageButton ProfileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
  
    EditText NewUsername = (EditText) findViewById(R.id.settings_edittext_username);
    Button SubmitUsernameButton = (Button) findViewById(R.id.settings_username_button);
    
    SeekBar DesiredDistance = (SeekBar) findViewById(R.id.settings_seekbar_desiredDistance);
    SeekBar DesiredDifficulty = (SeekBar) findViewById(R.id.settings_seekbar_desiredDifficulty);
    // TODO set initial progress of seekbars by reading database
    // DesiredDistance.setProgress()
    // DesiredDifficulty.setProgress()
    
  
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
    DesiredDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    
      }
  
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
    
      }
  
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
    
      }
    });
    DesiredDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
  
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        Log.d(TAG, "onProgressChanged: " + i + " " + b);
      }
  
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStartTrackingTouch: ");
      }
  
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch: ");
        Log.d(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
        seekBar.setProgress(100);
      }
    });
  }
  
  private void setUsername(String username) {
    // ensure text does not contain invalid characters
    
    // ensure this username does not exist elsewhere in the database
    
    // set username
  }
}