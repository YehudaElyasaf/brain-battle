package com.example.trivia;

import java.util.ArrayList;
public class Game {
        private ArrayList<Question> questions;
        //player1 is the player who created the game, player2 is the other one.
        private Player player1, player2;
        private int id;

        public Game() {
                questions = new ArrayList<>();
                player1 = null;
                player2 = null;
                id = -1;
        }

        public ArrayList<Question> getQuestions() {
                return questions;
        }

        public int getId() {
                return id;
        }

        public void setId(int id) {
                this.id = id;
        }

        public void setQuestions(ArrayList<Question> questions) {
                this.questions = questions;
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
