package com.example.campusguessr;

import androidx.fragment.app.Fragment;

import com.example.campusguessr.POJOs.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    List<Location> locations;
    private GoogleMap mMap;

    public MapFragment(Location[] locs) {
        super(R.layout.map_fragment);
        locations = Arrays.asList(locs);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
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

        MarkerOptions startMarker = new MarkerOptions();
        // Add markers for each location
        for (Location loc : locations) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (locations.indexOf(loc) == 0) {
                mMap.addMarker(startMarker.position(latLng).title("Start"));
            }
            else if (locations.indexOf(loc) == locations.size() - 1) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("End"));
            }
            else {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Attempt " + (locations.indexOf(loc))));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startMarker.getPosition()));
        mMap.setMinZoomPreference(17.0f);
    }
}
