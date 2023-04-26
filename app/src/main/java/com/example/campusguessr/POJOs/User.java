package com.example.campusguessr.POJOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid;
    private String name;
    private int desiredDistance;
    private int desiredDifficulty;
    private long score = 0;
}
