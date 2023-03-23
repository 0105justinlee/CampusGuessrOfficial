package com.example.campusguessr.DataTypes;

import android.location.Location;

import java.util.Date;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Challenge {
    class Orientation {
        public Float x;
        public Float y;
        public Float z;
    }

    @NonNull private Location location;
    @NonNull private String imageURL;
    @NonNull private Orientation orientation;
    @NonNull private String name;
    @NonNull private String description;
    @NonNull private User createdBy;
    @NonNull private Date createdAt;
}
