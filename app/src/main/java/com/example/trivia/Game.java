package com.example.trivia;

import java.util.ArrayList;
public class Game {
        private ArrayList<Question> questions;
        private ArrayList<Boolean> isCorrectList; //each cell represents the question in the same index
        private int currentQuestionIndex;

        public Game() {
                questions = null;
                isCorrectList = null;
                currentQuestionIndex = 0;
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

        public int getCurrentQuestionIndex() {
                return currentQuestionIndex;
        }

        public void setCurrentQuestionIndex(int currentQuestionIndex) {
                this.currentQuestionIndex = currentQuestionIndex;
        }
}
