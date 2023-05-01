package com.example.campusguessr;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusguessr.POJOs.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapFragment";
    List<Location> locations;
    private GoogleMap mMap;
    MapView mMapView;

    public MapFragment(Location[] locs) {
        super(R.layout.map_fragment);
        locations = Arrays.asList(locs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
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
        // Add markers for each location and add poly line connecting all locations
        for (int idx = 0; idx < locations.size(); idx++) {
            Location loc = locations.get(idx);
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (locations.indexOf(loc) == 0) {
                mMap.addMarker(startMarker.position(latLng).title("Start"));
            } else if (locations.indexOf(loc) == locations.size() - 1) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("End"));
            } else {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Attempt " + (locations.indexOf(loc))));
            }

            // add thick red polyline with arrows
            if (idx > 0) {
                Location prevLoc = locations.get(idx - 1);
                LatLng prevLatLng = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
                mMap.addPolyline(new com.google.android.gms.maps.model.PolylineOptions()
                        .add(prevLatLng, latLng)
                        .width(20)
                        .color(R.color.red)
                        .geodesic(true)
                        .startCap(new com.google.android.gms.maps.model.RoundCap())
                        .endCap(new com.google.android.gms.maps.model.RoundCap())
                );
                Log.d(TAG, "onMapReady: added polyline from " + prevLatLng.toString() + " to " + latLng.toString());
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(startMarker.getPosition()));
        mMap.setMinZoomPreference(17.0f);
    }
}
