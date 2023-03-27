package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView questionLbl;
    private Button[] answerBtns;

    private ImageView progressImg;
    Canvas pbCanvas;
    Bitmap progressBitmap;
    private ImageButton recordImgBtn;

    private GameViewModel gameViewModel;
    private Fragment loadGameActivity;

    public static final int QUESTIONS_COUNT_INDEX = 0;
    public static final int DIFFICULTY_LEVEL_INDEX = 1;
    public static final int CATEGORY_INDEX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        questionLbl = findViewById(R.id.questionLbl);
        answerBtns = new Button[4];
        answerBtns[0] = findViewById(R.id.answer0Btn);
        answerBtns[1] = findViewById(R.id.answer1Btn);
        answerBtns[2] = findViewById(R.id.answer2Btn);
        answerBtns[3] = findViewById(R.id.answer3Btn);

        recordImgBtn = findViewById(R.id.recordImgBtn);
        progressImg = findViewById(R.id.progressImg);

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);

        for (Button answer : answerBtns)
            answer.setOnClickListener(this);
        recordImgBtn.setOnClickListener(this);

        int[] extras = getIntent().getIntArrayExtra("extras");
        int questionsCount = extras[QUESTIONS_COUNT_INDEX];
        DifficultyLevel difficultyLevel = DifficultyLevel.values()[extras[DIFFICULTY_LEVEL_INDEX]];
        Category category = Category.values()[extras[CATEGORY_INDEX]];

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
        for (int i = 0; i < answerBtns.length; i++)
            answerBtns[i].setText(question.getAnswers().get(i));

        int color = getResources().getColor(R.color.main_color_500);
        for (Button answer : answerBtns)
            answer.setBackgroundColor(getResources().getColor(R.color.main_color_500));

        for(Button answerBtn : answerBtns)
            answerBtn.setEnabled(true);
        recordImgBtn.setEnabled(true);
    }

    private void chooseAnswer(int answerIndex, Button answerButton) {
        boolean isCorrect = gameViewModel.getCurrentQuestion().correctAnswer == answerIndex;
        if (isCorrect) {
            answerButton.setBackgroundColor(MyColor.CORRECT_GREEN);

            gameViewModel.setTotalCorrect(gameViewModel.getTotalCorrect() + 1);
        } else {
            answerButton.setBackgroundColor(MyColor.WRONG_RED);

            answerBtns[gameViewModel.getCurrentQuestion().correctAnswer].setBackgroundColor(MyColor.CORRECT_GREEN);
        }
        drawIsCorrectOnProgressBar(isCorrect, gameViewModel.getCurrentQuestionIndex(), gameViewModel.getQuestions().size());

        gameViewModel.setCurrentQuestionIndex(gameViewModel.getCurrentQuestionIndex() + 1);

        for(Button answerBtn : answerBtns)
            answerBtn.setEnabled(false);
        recordImgBtn.setEnabled(false);

        Intent intent = new Intent(this, EndGameActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gameViewModel.getCurrentQuestionIndex() == gameViewModel.getQuestions().size()) {
                    //TODO: pass results to next screen
                    startActivity(intent);
                    finish();
                } else {
                    showCurrentQuestion();
                }
            }
        }, 1000);
    }

    private void initProgressBarCanvas() {
        //draw question progress bar
        Paint paint = new Paint();
        paint.setColor(0xAACCCCCC); //light gray

        progressBitmap = Bitmap.createBitmap(progressImg.getWidth(), progressImg.getHeight(), Bitmap.Config.ARGB_8888);
        pbCanvas = new Canvas(progressBitmap);

        pbCanvas.drawRect(0, 0, pbCanvas.getWidth(), pbCanvas.getHeight(), paint);
        progressImg.setImageBitmap(progressBitmap);
    }

    private void drawQuestionSeparatorOnProgressBar(int currentQuestion, int totalQuestions) {
        Paint paint = new Paint();
        paint.setColor(MyColor.GOLD);

        int start = (currentQuestion * (pbCanvas.getWidth() / totalQuestions)) - 10;
        int end = (currentQuestion * (pbCanvas.getWidth() / totalQuestions)) + 10;
        pbCanvas.drawRect(start, 0, end, pbCanvas.getHeight(), paint);

        progressImg.setImageBitmap(progressBitmap);
    }

    private void drawIsCorrectOnProgressBar(boolean isCorrect, int currentQuestion, int totalQuestions) {
        Paint paint = new Paint();
        if (isCorrect)
            paint.setColor(MyColor.CORRECT_GREEN);
        else
            paint.setColor(MyColor.WRONG_RED);

        int start = currentQuestion * (pbCanvas.getWidth() / totalQuestions);
        int end = (currentQuestion + 1) * (pbCanvas.getWidth() / totalQuestions);
        pbCanvas.drawRect(start, 0, end, pbCanvas.getHeight(), paint);

        progressImg.setImageBitmap(progressBitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.answer0Btn:
                chooseAnswer(0, (Button) v);
                break;
            case R.id.answer1Btn:
                chooseAnswer(1, (Button) v);
                break;
            case R.id.answer2Btn:
                chooseAnswer(2, (Button) v);
                break;
            case R.id.answer3Btn:
                chooseAnswer(3, (Button) v);
                break;

            case R.id.recordImgBtn:

        }
    }
}
