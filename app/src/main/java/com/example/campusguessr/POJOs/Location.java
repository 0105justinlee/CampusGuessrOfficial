package com.example.campusguessr.POJOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private double latitude;
    private double longitude;

    public Integer distanceTo(Location other) {
        // return distance in meters
        double lat1 = this.latitude;
        double lon1 = this.longitude;
        double lat2 = other.getLatitude();
        double lon2 = other.getLongitude();
        return (int) (Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2)) * 100000);
    }
}
