package com.example.trivia;

import java.util.ArrayList;
import java.util.Objects;

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
    public Player(Player player) {
        super(player);
        currentQuestionIndex = player.currentQuestionIndex;
        isCorrectList = player.isCorrectList;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return currentQuestionIndex == player.currentQuestionIndex && Objects.equals(isCorrectList, player.isCorrectList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentQuestionIndex, isCorrectList);
    }
}
