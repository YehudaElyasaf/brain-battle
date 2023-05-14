package com.example.trivia;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private Game game;
    private User user;

    public GameViewModel() {
        game = new Game();
        user = null;
    }

    public Game getGame() {
        return game;
    }
    public void setGame(Game game) {
        this.game = game;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<Boolean> getIsCorrectList() {
        return game.getIsCorrectList();
    }

    public void setIsCorrectList(ArrayList<Boolean> isCorrectList) {
        game.setIsCorrectList(isCorrectList);
    }

    public void setQuestions(ArrayList<Question> questions) {
        game.setQuestions(questions);
    }

    public Player getPlayer1(){
        return game.getPlayer1();
    }
    public Player getPlayer2(){
        return game.getPlayer1();
    }

    public void setPlayer1(Player player1){
        game.setPlayer1(player1);
    }
    public void setPlayer2(Player player2){
        game.setPlayer2(player2);
    }

    public ArrayList<Question> getQuestions() {
        return game.getQuestions();
    }

    public int getTotalCorrect(){
        int count = 0;
        for(boolean isCorrect : game.getIsCorrectList())
            if(isCorrect)
                count++;

        return count;
    }
}
