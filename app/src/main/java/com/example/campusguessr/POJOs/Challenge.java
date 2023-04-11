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
    private UUID id;
    private Location location;
    private String imageURL;
    private Orientation orientation;
    private String name;
    private String description;
    private String createdBy;
    private Date createdAt;
}

