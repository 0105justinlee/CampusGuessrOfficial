package com.example.campusguessr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.Executor;

public class ChallengeActivity extends AppCompatActivity {
    int guessesMade = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private double[] currentCoords;
    double[] currentChallenge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        /*Thread getLocationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getLocation();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        getLocationThread.start();*/
        getLocation();
        getChallenge();

        // Initialize buttons
        ImageButton RankingsButton = findViewById(R.id.navigate_ranking_tab_button);
        ImageButton ProfileButton = findViewById(R.id.navigate_profile_tab_button);
        Button GuessButton = findViewById(R.id.guess_button);

        // Set up navigation menu
        RankingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RankingsActivity.class));
            }
        });
        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        // Main challenge guessing logic
        GuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text;
                guessesMade++;
                double distance_latitude = (currentCoords[0]-currentChallenge[0])*364000;
                double distance_longitude = (currentCoords[1]-currentChallenge[1])*288200;
                double distance = Math.sqrt(distance_latitude*distance_latitude+distance_longitude*distance_longitude);
                switch (guessesMade) {
                    case 1:
                       text = findViewById(R.id.guess_1_distance);
                       break;
                    case 2:
                        text = findViewById(R.id.guess_2_distance);
                        break;
                    case 3:
                        text = findViewById(R.id.guess_3_distance);
                        break;
                    case 4:
                        text = findViewById(R.id.guess_4_distance);
                        break;
                    default:
                        text = findViewById(R.id.guess_4_distance);
                        // TODO: fail user
                }
                if (distance < 50) {
                    text.setText("You are there!");
                    return; // TODO switch to challenge complete activity
                }
                text.setText("You are " + distance + " feet away!");
            }
        });
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locationRequest = new LocationRequest.Builder(100).build();
        fusedLocationClient.requestLocationUpdates(locationRequest, new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        }, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (location != null) {
                    currentCoords = new double[]{location.getLatitude(), location.getLongitude()};
                }
            }
        });
    }
    private void getChallenge() {
        currentChallenge = new double[]{43.0715302, -89.40853};
    }
}