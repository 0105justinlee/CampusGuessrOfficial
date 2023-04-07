package com.example.campusguessr.POJOs;

import android.location.Location;

import java.util.Date;

import lombok.Data;

@Data
public class Attempt {
    private final String id;
    private final String challengeId;
    private final String userId;
    private final Location[] guesses;
    private final Date createdAt;
}
