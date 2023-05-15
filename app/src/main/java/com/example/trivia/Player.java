package com.example.trivia;

public class Player extends User{
    //player is a user while a game
    private int currentQuestionIndex;

    public Player(String username, String uid, int points, int totalCorrect, int totalWrong) {
        super(username, uid, points, totalCorrect, totalWrong);
        currentQuestionIndex = 0;
    }
    public Player(User user) {
        super(user);
        currentQuestionIndex = 0;
    }
    public Player() {
        super();
        currentQuestionIndex = 0;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }
}
