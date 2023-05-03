package com.example.trivia;

import java.util.ArrayList;

public interface IQuestionFetcher {
    ArrayList<Question> getQuestions(int questionCount, int difficultyLevel, int category);
}
