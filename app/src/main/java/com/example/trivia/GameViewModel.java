package com.example.trivia;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private ArrayList<Question> questions;
    private ArrayList<Boolean> isCorrectList; //each cell represents the question in the same index
    private int currentQuestionIndex;

    public GameViewModel() {
        questions = null;
        isCorrectList = null;
        currentQuestionIndex = 0;
    }

    public ArrayList<Boolean> getIsCorrectList() {
        return isCorrectList;
    }

    public void setIsCorrectList(ArrayList<Boolean> isCorrectList) {
        this.isCorrectList = isCorrectList;
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

    public Question getCurrentQuestion() {
        return questions.get(currentQuestionIndex);
    }

    public int getTotalCorrect(){
        int count = 0;
        for(boolean isCorrect : isCorrectList)
            if(isCorrect)
                count++;

        return count;
    }
}
