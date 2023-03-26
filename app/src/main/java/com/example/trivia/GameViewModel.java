package com.example.trivia;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private ArrayList<Question> questions;
    private int currentQuestionIndex;

    public int getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(int totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    private int totalCorrect;

    public GameViewModel() {
        questions = null;
        currentQuestionIndex = 0;
        totalCorrect = 0;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }


    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
        currentQuestionIndex = 0;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public Question getCurrentQuestion(){
        return questions.get(currentQuestionIndex);
    }
}
