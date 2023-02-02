package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

enum DifficultyLevel{
    ALL,
    EASY,
    MEDIUM,
    HARD
}
enum Category{
    ALL,
    GENERAL_KNOWLEDGE,
    HISTORY,
    SCIENCE,
    PROGRAMMING
}

public class StartGameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
    }
}