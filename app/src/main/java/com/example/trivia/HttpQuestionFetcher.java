package com.example.trivia;


import android.text.Html;
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
import java.util.Locale;



public class HttpQuestionFetcher implements IQuestionFetcher {
    private static final String DATABASE_URL_ADDRESS = "https://opentdb.com/api.php";

    private static final int OK_RESPONSE_CODE = 0;
    private static final int GENERAL_KNOWLEDGE_CATEGORY_ID = 9;
    private static final int SCIENCE_CATEGORY_ID = 17;
    private static final int COMPUTER_SCIENCE_CATEGORY_ID = 18;

    private String response;

    @Override
    public ArrayList<Question> getQuestions(int questionCount, int difficultyLevel, int category) {
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

        return deserializeQuestions(DifficultyLevel.values()[difficultyLevel], Category.values()[category]);
    }

    private String getParams(int questionCount, int difficultyLevel, int category) {
        String params = "";

        params += "?amount=";
        params += Integer.toString(questionCount);

        params += "&type=multiple";

        params += "&difficulty=";
        if (difficultyLevel == DifficultyLevel.EASY.ordinal())
            params += "easy";
        else if (difficultyLevel == DifficultyLevel.MEDIUM.ordinal())
            params += "medium";
        else if (difficultyLevel == DifficultyLevel.HARD.ordinal())
            params += "hard";

        if (category != Category.ALL.ordinal()) {
            params += "&category=";
            if (category == Category.GENERAL_KNOWLEDGE.ordinal())
                params += Integer.toString(GENERAL_KNOWLEDGE_CATEGORY_ID);
            else if (category == Category.SCIENCE.ordinal())
                params += Integer.toString(SCIENCE_CATEGORY_ID);
            else if (category == Category.COMPUTER_SCIENCE.ordinal())
                params += Integer.toString(COMPUTER_SCIENCE_CATEGORY_ID);
        }

        return params;
    }

    private ArrayList<Question> deserializeQuestions(DifficultyLevel difficultyLevel, Category category) {
        ArrayList<Question> questions = new ArrayList<Question>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            int responseCode = Integer.parseInt(jsonObject.getString("response_code"));
            if (responseCode != OK_RESPONSE_CODE)
                return null;

            JSONArray questionsJsonObject = jsonObject.getJSONArray("results");

            for (int i = 0; i < questionsJsonObject.length(); i++) {
                JSONObject questionJsonObject = questionsJsonObject.getJSONObject(i);
                Question question = new Question();

                question.setCategory(category);
                question.setDifficultyLevel(difficultyLevel);

                String questionString = questionJsonObject.getString("question");
                question.setQuestion(questionString);

                String correctAnswer = questionJsonObject.getString("correct_answer");

                JSONArray incorrectAnswersJsonArray = questionJsonObject.getJSONArray("incorrect_answers");
                String[] answers = new String[incorrectAnswersJsonArray.length() + 1]; //+1 for correct answer

                int j;
                for (j = 0; j < incorrectAnswersJsonArray.length(); j++)
                    answers[j] = incorrectAnswersJsonArray.getString(j);
                answers[j] = correctAnswer;

                for(j = 0; j < answers.length; j++){
                    answers[j] = Html.fromHtml(answers[j], Html.FROM_HTML_MODE_LEGACY).toString();
                }
                question.setQuestion(Html.fromHtml(question.getQuestion(), Html.FROM_HTML_MODE_LEGACY).toString());

                question.setAnswers(answers);
                //TODO: shuffle answers
                question.setCorrectAnswer(1);

                questions.add(question);
            }

        } catch (Exception e) {
            return null;
        }

        return questions;
    }
}
