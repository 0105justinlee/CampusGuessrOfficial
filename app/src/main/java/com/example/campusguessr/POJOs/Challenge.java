package com.example.campusguessr.POJOs;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {
    UUID id;
    Location location;
    String imageURL;
    Orientation orientation;
    String name;
    String description;
    String createdBy;
    Date createdAt;
}

