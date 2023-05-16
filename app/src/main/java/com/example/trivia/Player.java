package com.example.trivia;

import java.util.ArrayList;

public class Player extends User{
    //player is a user while a game
    private int currentQuestionIndex;
    private ArrayList<Boolean> isCorrectList; //each cell represents the question in the same index

    public Player(String username, String uid, int score, int totalCorrect, int totalWrong) {
        super(username, uid, score, totalCorrect, totalWrong);
        currentQuestionIndex = 0;
        isCorrectList = new ArrayList<>();
    }
    public Player(User user) {
        super(user);
        currentQuestionIndex = 0;
        isCorrectList = new ArrayList<>();
    }
    public Player() {
        super();
        currentQuestionIndex = 0;
        isCorrectList = new ArrayList<>();
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public ArrayList<Boolean> getIsCorrectList() {
        return isCorrectList;
    }

    public void setIsCorrectList(ArrayList<Boolean> isCorrectList) {
        this.isCorrectList = isCorrectList;
    }
}
