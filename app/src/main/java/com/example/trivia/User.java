package com.example.trivia;

public class User {
    private int id;
    private String username;
    private String password;
    private int totalCorrect;
    private int totalWrong;

    public User(int id, String username, String password, int totalCorrect, int totalWrong) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.totalCorrect = totalCorrect;
        this.totalWrong = totalWrong;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
