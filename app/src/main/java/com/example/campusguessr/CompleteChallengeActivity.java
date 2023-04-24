package com.example.campusguessr;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.campusguessr.POJOs.Attempt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.campusguessr.databinding.CompleteChallengeBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class CompleteChallengeActivity extends FragmentActivity {

  private String TAG = "CompleteChallengeActivity";
  
  private GoogleMap mMap;
  private CompleteChallengeBinding binding;
  Attempt attempt;

  FirebaseDatabase database = FirebaseDatabase.getInstance();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = CompleteChallengeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    ImageButton RankingButton = (ImageButton) findViewById(R.id.navigate_ranking_tab_button);
    ImageButton PlayButton = (ImageButton) findViewById(R.id.navigate_play_tab_button);
    ImageButton CreateButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
    ImageButton ProfileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
    RankingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(getApplicationContext(), RankingsActivity.class));
      }
    });
    
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

    String attemptId = getIntent().getStringExtra("attemptId");
    database.getReference().child("attempts").child(attemptId).get().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        Map m = (Map<String, Object>) task.getResult().getValue();
        ObjectMapper mapper = new ObjectMapper();
        attempt = mapper.convertValue(m, Attempt.class);
        getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, new MapFragment(attempt.getGuesses())).commit();
      }
    });

  }
}