package com.example.trivia;

public class User {
    protected String username;
    protected String uid;
    protected int points;
    protected int totalCorrect;
    protected int totalWrong;

    public User(String username, String uid, int points, int totalCorrect, int totalWrong) {
        this.username = username;
        this.uid = uid;
        this.points = points;
        this.totalCorrect = totalCorrect;
        this.totalWrong = totalWrong;
    }

    public User() {
        this.username = "";
        this.uid = "";
        this.points = -1;
        this.totalCorrect = -1;
        this.totalWrong = -1;
    }

    public User(User user) {
        this.username = user.username;
        this.uid = user.uid;
        this.totalCorrect = user.totalCorrect;
        this.totalWrong = user.totalWrong;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(int totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public int getTotalWrong() {
        return totalWrong;
    }

    public void setTotalWrong(int totalWrong) {
        this.totalWrong = totalWrong;
    }

    public double calculateScore(){
        return Math.pow(totalCorrect, 1.6) / (totalWrong + 10);
    }
}
