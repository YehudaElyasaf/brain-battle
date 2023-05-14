package com.example.trivia;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private Game game;

    public GameViewModel() {
        game = new Game();
    }

    public Game getGame() {
        return game;
    }
    public void setGame(Game game) {
        this.game = game;
    }

    public ArrayList<Boolean> getIsCorrectList() {
        return game.getIsCorrectList();
    }

    public void setIsCorrectList(ArrayList<Boolean> isCorrectList) {
        game.setIsCorrectList(isCorrectList);
    }

    public int getCurrentQuestionIndex() {
        return game.getCurrentQuestionIndex();
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        game.setCurrentQuestionIndex(currentQuestionIndex);
    }


    public void setQuestions(ArrayList<Question> questions) {
        game.setQuestions(questions);
        game.setCurrentQuestionIndex(0);
    }

    public ArrayList<Question> getQuestions() {
        return game.getQuestions();
    }

    public Question getCurrentQuestion() {
        return game.getQuestions().get(game.getCurrentQuestionIndex());
    }

    public int getTotalCorrect(){
        int count = 0;
        for(boolean isCorrect : game.getIsCorrectList())
            if(isCorrect)
                count++;

        return count;
    }
}
