package com.example.trivia;

public class User {
    protected String username;
    protected String uid;
    protected int score;
    protected int totalCorrect;
    protected int totalWrong;

    private static String EMAIL_SUFFIX = "@1.1";

    public User(String username, String uid, int score, int totalCorrect, int totalWrong) {
        this.username = username;
        this.uid = uid;
        this.score = score;
        this.totalCorrect = totalCorrect;
        this.totalWrong = totalWrong;
    }

    public User() {
        this.username = "";
        this.uid = "";
        this.score = -1;
        this.totalCorrect = -1;
        this.totalWrong = -1;
    }

    public User(User user) {
        this.username = user.username;
        this.uid = user.uid;
        this.score = user.score;
        this.totalCorrect = user.totalCorrect;
        this.totalWrong = user.totalWrong;
    }

    public static String usernameToEmail(String username) {
        return username + EMAIL_SUFFIX;
    }

    public static String emailToUsername(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    public String getUsername() {
        return username;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getEmail() {
        return usernameToEmail(username);
    }
}
