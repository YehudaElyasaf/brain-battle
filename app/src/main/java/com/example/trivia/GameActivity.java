package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.BitSet;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView questionLbl;
    private Button answer1Btn;
    private Button answer2Btn;
    private Button answer3Btn;
    private Button answer4Btn;

    private ImageView progressImg;
    Canvas pbCanvas;
    Bitmap progressBitmap;
    private ImageButton recordImgBtn;

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

        recordImgBtn = findViewById(R.id.recordImgBtn);
        progressImg = findViewById(R.id.progressImg);

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);

        answer1Btn.setOnClickListener(this);
        answer2Btn.setOnClickListener(this);
        answer3Btn.setOnClickListener(this);
        answer4Btn.setOnClickListener(this);
        recordImgBtn.setOnClickListener(this);

        int questionsCount = 4;
        DifficultyLevel difficultyLevel = DifficultyLevel.EASY;
        Category category = Category.GENERAL_KNOWLEDGE;

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

    private class GetQuestionsAsync extends AsyncTask<Integer, Integer, ArrayList<Question>> {
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
            if (questions == null || questions.size() == 0) {
                Toast.makeText(getBaseContext(), "Couldn't fetch questions!", Toast.LENGTH_SHORT).show();
                backToMainMenu();
            } else {
                hideLoadingFragment();
                gameViewModel.setQuestions(questions);
                showCurrentQuestion();

                initProgressBarCanvas();
            }
        }
    }

    private void backToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void showCurrentQuestion() {
        Question question = gameViewModel.getCurrentQuestion();

        questionLbl.setText(question.getQuestion());
        answer1Btn.setText(question.getAnswers()[0]);
        answer2Btn.setText(question.getAnswers()[1]);
        answer3Btn.setText(question.getAnswers()[2]);
        answer4Btn.setText(question.getAnswers()[3]);
    }

    private void chooseAnswer(int answerIndex) {
        boolean isCorrect = gameViewModel.getCurrentQuestion().correctAnswer == answerIndex;
        if (isCorrect) {
            //TODO: change color to green
            gameViewModel.setTotalCorrect(gameViewModel.getTotalCorrect() + 1);
        } else {
            //TODO: change color to red
        }
        drawOnQuestionProgressBar(isCorrect, gameViewModel.getCurrentQuestionIndex(), gameViewModel.getQuestions().size());
        gameViewModel.setCurrentQuestionIndex(gameViewModel.getCurrentQuestionIndex() + 1);

        if (gameViewModel.getCurrentQuestionIndex() == gameViewModel.getQuestions().size()) {
            //TODO: pass results to next screen
            Intent intent = new Intent(this, EndGameActivity.class);
            startActivity(intent);
            finish();
        } else {
            showCurrentQuestion();
        }
    }

    private void initProgressBarCanvas(){
        //draw question progress bar
        Paint paint = new Paint();
        paint.setColor(0xAACCCCCC); //light gray

        progressBitmap = Bitmap.createBitmap(progressImg.getWidth(), progressImg.getHeight(), Bitmap.Config.ARGB_8888);
        pbCanvas = new Canvas(progressBitmap);

        pbCanvas.drawRect(0, 0, pbCanvas.getWidth(), pbCanvas.getHeight(), paint);
        progressImg.setImageBitmap(progressBitmap);
    }

    private void drawOnQuestionProgressBar(boolean isCorrect, int currentQuestion, int totalQuestions) {
        Paint paint = new Paint();
        if(isCorrect)
            paint.setColor(Color.GREEN);
        else
            paint.setColor(Color.RED);

        int start = currentQuestion * (pbCanvas.getWidth() / totalQuestions);
        int end = (currentQuestion + 1) * (pbCanvas.getWidth() / totalQuestions);
        pbCanvas.drawRect(start, 0, end, pbCanvas.getHeight(), paint);

        progressImg.setImageBitmap(progressBitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.answer1Btn:
                chooseAnswer(0);
                break;
            case R.id.answer2Btn:
                chooseAnswer(1);
                break;
            case R.id.answer3Btn:
                chooseAnswer(2);
                break;
            case R.id.answer4Btn:
                chooseAnswer(3);
                break;

            case R.id.recordImgBtn:

        }
    }
}
