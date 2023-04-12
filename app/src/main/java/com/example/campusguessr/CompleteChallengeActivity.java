package com.example.campusguessr;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.campusguessr.databinding.CompleteChallengeBinding;

public class CompleteChallengeActivity extends FragmentActivity implements OnMapReadyCallback {
  
  private GoogleMap mMap;
  private CompleteChallengeBinding binding;
  
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
    
    
    
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }
  
  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Madison, WI
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
  
    LatLng madison = new LatLng(43.0722, -89.4008);
    mMap.addMarker(new MarkerOptions().position(madison).title("Marker in Madison"));
    mMap.moveCamera(CameraUpdateFactory.newLatLng(madison));
    mMap.setMinZoomPreference(17.0f);
  }
}