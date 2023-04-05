package com.example.campusguessr.POJOs;

import android.location.Location;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class Challenge {
    private final UUID id;
    private Location location;
    private String imageURL;
    private Orientation orientation;
    private String name;
    private String description;
    private String createdBy;
    private Date createdAt;
}

