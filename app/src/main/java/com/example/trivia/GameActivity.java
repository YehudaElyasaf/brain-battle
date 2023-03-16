package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        IQuestionFetcher questionFetcher = new HttpQuestionFetcher();

        ArrayList<Question> questions = questionFetcher.getQuestions(3, DifficultyLevel.EASY, Category.COMPUTER_SCIENCE);
        questions.size();
    }
}