package com.example.trivia;

import java.util.ArrayList;

public interface IDatabase {
    void createUser(User user);
    User getUser(int id);
    ArrayList<User> getAllUsers();
    void deleteUser(int id);
}
