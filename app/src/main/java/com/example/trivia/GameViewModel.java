package com.example.trivia;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private Game game;
    private boolean isCreator; //is this player the game creator?

    public GameViewModel() {
        game = new Game();
        isCreator = true;
    }

    public Game getGame() {
        return game;
    }
    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    public void setQuestions(ArrayList<Question> questions) {
        game.setQuestions(questions);
    }

    public Player getPlayer1(){
        return game.getPlayer1();
    }
    public Player getPlayer2(){
        return game.getPlayer2();
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

    public int getMyTotalCorrect(){
        int count = 0;
        for(boolean isCorrect : getMyPlayer().getIsCorrectList())
            if(isCorrect)
                count++;

        return count;
    }
    public int getMyTotalWrong(){
        return game.getQuestions().size() - getMyTotalCorrect();
    }

    public int calculatePoints() {
        //points = (totalCorrect * 20 / (totalWrong + 1)) * 3, with round

        return 10 * (int)(5 * (Math.pow(getMyTotalCorrect(), 1.2)) / (getMyTotalWrong() + 1));
    }

    public Player getMyPlayer() {
        if(isCreator)
            //creator is player1
            return game.getPlayer1();
        else
            return game.getPlayer2();
    }

    public void setMyPlayer(Player player) {
        //creator is player1
        if(isCreator)
            game.setPlayer1(player);
        else
            game.setPlayer2(player);
    }
}
