package com.example.campusguessr.POJOs;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attempt {
    private String id;
    private String challengeId;
    private String userId;
    private Location[] guesses;
    private Long score;
    private Integer playTime;
    private Date createdAt;
}
