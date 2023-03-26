package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.app.GameManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private TextView questionLbl;
    private Button answer1Btn;
    private Button answer2Btn;
    private Button answer3Btn;
    private Button answer4Btn;

    private TextView isCorrectLbl;
    private ImageButton recordImgBtn;
    private ProgressBar gameQuestionPb;

    private GameViewModel gameViewModel;
    private Fragment loadGameActivity;

    static final int QUESTIONS_COUNT_INDEX = 0;
    static final int DIFFICULTY_LEVEL_INDEX = 1;
    static final int CATEGORY_INDEX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        questionLbl = findViewById(R.id.questionLbl);
        answer1Btn = findViewById(R.id.answer1Btn);
        answer2Btn = findViewById(R.id.answer2Btn);
        answer3Btn = findViewById(R.id.answer3Btn);
        answer4Btn = findViewById(R.id.answer4Btn);

        isCorrectLbl = findViewById(R.id.isCorrectLbl);
        recordImgBtn = findViewById(R.id.recordImgBtn);
        gameQuestionPb = findViewById(R.id.gameQuestionPb);

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);

        int questionsCount = 3;
        DifficultyLevel difficultyLevel = DifficultyLevel.EASY;
        Category category = Category.COMPUTER_SCIENCE;

        GetQuestionsAsync getQuestionsAsync = new GetQuestionsAsync();
        getQuestionsAsync.execute(questionsCount, difficultyLevel.ordinal(), category.ordinal());
    }

    private void showLoadingFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        loadGameActivity = new LoadGameFragment();
        fragmentTransaction.replace(R.id.gameLayout, loadGameActivity);
        fragmentTransaction.commit();
    }

    public void hideLoadingFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(loadGameActivity);
        fragmentTransaction.commit();
    }

    private class GetQuestionsAsync extends AsyncTask<Integer, Integer, ArrayList<Question>>{
        @Override
        protected void onPreExecute() {
            showLoadingFragment();
        }

        @Override
        protected ArrayList<Question> doInBackground(Integer... integers) {
            IQuestionFetcher questionFetcher = new HttpQuestionFetcher();
            return questionFetcher.getQuestions(integers[QUESTIONS_COUNT_INDEX], integers[DIFFICULTY_LEVEL_INDEX], integers[CATEGORY_INDEX]);
        }

        @Override
        protected void onPostExecute(ArrayList<Question> questions) {
            if(questions == null || questions.size() == 0){
                Toast.makeText(getBaseContext(), "Couldn't fetch questions!", Toast.LENGTH_SHORT).show();
                backToMainMenu();
            }
            else{
                hideLoadingFragment();
                gameViewModel.setQuestions(questions);
                showCurrentQuestion();
            }
        }
    }

    private void backToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void showCurrentQuestion(){
        Question question = gameViewModel.getCurrentQuestion();

        questionLbl.setText(question.getQuestion());
        answer1Btn.setText(question.getAnswers()[0]);
        answer2Btn.setText(question.getAnswers()[1]);
        answer3Btn.setText(question.getAnswers()[2]);
        answer4Btn.setText(question.getAnswers()[3]);
    }
}
