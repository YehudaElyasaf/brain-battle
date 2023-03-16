package com.example.trivia;


import android.text.PrecomputedText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;


public class HttpQuestionFetcher implements IQuestionFetcher {
    private static final String DATABASE_URL_ADDRESS = "https://opentdb.com/api.php";

    private static final int OK_RESPONSE_CODE = 0;

    private String response;

    @Override
    public ArrayList<Question> getQuestions(int questionCount, DifficultyLevel difficultyLevel, Category category) {
        response = "";
        //run in other thread, because networking isn't allowed in main thread
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(DATABASE_URL_ADDRESS + getParams(questionCount, difficultyLevel, category));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                        throw new Exception("HTTP returned response code " + connection.getResponseCode());

                    BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                    String line = reader.readLine();
                    while (line != null) {
                        response += line;
                        line = reader.readLine();
                    }
                    reader.close();
                } catch (Exception e) {
                    response = "";
                }

            }
        });
        try {
            sendThread.start();
            sendThread.join();
        } catch (Exception e) {
            return null;
        }

        if (response.equals(""))
            //fetching failed
            return null;

        return deserializeQuestions(difficultyLevel, category);
    }

    private String getParams(int questionCount, DifficultyLevel difficultyLevel, Category category) {
        String params = "";

        params += "?amount=";
        params += Integer.toString(questionCount);

        params += "&type=multiple";

        params += "&difficulty=";
        if (difficultyLevel == DifficultyLevel.EASY)
            params += "easy";
        else if (difficultyLevel == DifficultyLevel.MEDIUM)
            params += "medium";
        else if (difficultyLevel == DifficultyLevel.HARD)
            params += "hard";

        if (category != Category.ALL) {
            params += "&category=";
            if (category == Category.GENERAL_KNOWLEDGE)
                params += Integer.toString(9);
            else if (category == Category.SCIENCE)
                params += Integer.toString(17);
            else if (category == Category.COMPUTER_SCIENCE)
                params += Integer.toString(18);
        }

        return params;
    }

    private ArrayList<Question> deserializeQuestions(DifficultyLevel difficultyLevel, Category category) {
        ArrayList<Question> questions = new ArrayList<Question>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            int responseCode = Integer.parseInt(jsonObject.getString("response_code"));
            if(responseCode != OK_RESPONSE_CODE)
                return null;

            JSONArray questionsJsonObject = jsonObject.getJSONArray("results");

            for(int i=0; i<jsonObject.length(); i++){
                JSONObject questionJsonObject = questionsJsonObject.getJSONObject(i);
                Question question = new Question();

                question.setCategory(category);
                question.setDifficultyLevel(difficultyLevel);

                String questionString = questionJsonObject.getString("question");
                question.setQuestion(questionString);

                String correctAnswer = questionJsonObject.getString("correct_answer");
                question.setCorrectAnswer(correctAnswer);

                JSONArray incorrectAnswerJsonArray = questionJsonObject.getJSONArray("incorrect_answers");
                String[] incorrectAnswers = new String[incorrectAnswerJsonArray.length()];
                for(int j=0; j<incorrectAnswerJsonArray.length(); j++)
                    incorrectAnswers[j] = incorrectAnswerJsonArray.getString(j);
                question.setIncorrectAnswers(incorrectAnswers);

                questions.add(question);
            }

        } catch (Exception e) {
            return null;
        }

        return questions;
    }
}
