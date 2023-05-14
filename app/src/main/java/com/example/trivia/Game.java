package com.example.trivia;

import java.util.ArrayList;
public class Game {
        private ArrayList<Question> questions;
        private ArrayList<Boolean> isCorrectList; //each cell represents the question in the same index
        //player1 is the player who created the game, player2 is the other one.
        private Player player1, player2;

        public Game() {
                questions = null;
                player1 = null;
                player2 = null;
        }

        public ArrayList<Question> getQuestions() {
                return questions;
        }

        public void setQuestions(ArrayList<Question> questions) {
                this.questions = questions;
        }

        public ArrayList<Boolean> getIsCorrectList() {
                return isCorrectList;
        }

        public void setIsCorrectList(ArrayList<Boolean> isCorrectList) {
                this.isCorrectList = isCorrectList;
        }

        public Player getPlayer1() {
                return player1;
        }

        public void setPlayer1(Player player1) {
                this.player1 = player1;
        }

        public Player getPlayer2() {
                return player2;
        }

        public void setPlayer2(Player player2) {
                this.player2 = player2;
        }
}
