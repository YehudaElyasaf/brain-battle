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
    private TextView currentQuestionLbl;
    private TextView questionLbl;
    private Button[] answerButtons;

    private ImageButton homeImgBtn;
    private ImageView progressImg;
    private Canvas pbCanvas;
    private Bitmap progressBitmap;
    private ImageButton recordImgBtn;

    private GameViewModel gameVM;
    private Fragment loadGameFragment;

    public static final int QUESTIONS_COUNT_INDEX = 0;
    public static final int DIFFICULTY_LEVEL_INDEX = 1;
    public static final int CATEGORY_INDEX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        currentQuestionLbl = findViewById(R.id.currentQuestionLbl);
        questionLbl = findViewById(R.id.questionLbl);
        answerButtons = new Button[4];
        answerButtons[0] = findViewById(R.id.answer0Btn);
        answerButtons[1] = findViewById(R.id.answer1Btn);
        answerButtons[2] = findViewById(R.id.answer2Btn);
        answerButtons[3] = findViewById(R.id.answer3Btn);

        homeImgBtn = findViewById(R.id.homeBtn);
        recordImgBtn = findViewById(R.id.recordImgBtn);
        progressImg = findViewById(R.id.progressImg);

        gameVM = new ViewModelProvider(this).get(GameViewModel.class);

        homeImgBtn.setOnClickListener(this);
        for (Button answer : answerButtons)
            answer.setOnClickListener(this);
        recordImgBtn.setOnClickListener(this);

        int[] extras = getIntent().getIntArrayExtra("extras");
        int questionsCount = extras[QUESTIONS_COUNT_INDEX];
        DifficultyLevel difficultyLevel = DifficultyLevel.values()[extras[DIFFICULTY_LEVEL_INDEX]];
        Category category = Category.values()[extras[CATEGORY_INDEX]];

        if (gameVM.getQuestions() == null) {
            //screen not initialized
            GetQuestionsAsync getQuestionsAsync = new GetQuestionsAsync();
            getQuestionsAsync.execute(questionsCount, difficultyLevel.ordinal(), category.ordinal());
        } else {
            //screen already initialized
            showCurrentQuestion();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        initProgressBarCanvas();
    }

    private void showLoadingFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        loadGameFragment = new LoadGameFragment();
        fragmentTransaction.replace(R.id.gameLayout, loadGameFragment);
        fragmentTransaction.commit();
    }

    public void hideLoadingFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(loadGameFragment);
        fragmentTransaction.commit();
    }

    private void showEndGameFragment() {
        homeImgBtn.setEnabled(false);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.gameLayout, new EndGameFragment());
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
                gameVM.setQuestions(questions);
                showCurrentQuestion();
            }
        }
    }

    private void backToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void showCurrentQuestion() {
        Question question = gameVM.getCurrentQuestion();

        currentQuestionLbl.setText((gameVM.getCurrentQuestionIndex() + 1) +
                "/" +
                gameVM.getQuestions().size());

        questionLbl.setText(question.getQuestion());
        for (int i = 0; i < answerButtons.length; i++)
            answerButtons[i].setText(question.getAnswers().get(i));

        int color = getResources().getColor(R.color.main_color_500);
        for (Button answer : answerButtons)
            answer.setBackgroundColor(getResources().getColor(R.color.main_color_500));

        for (Button answerBtn : answerButtons)
            answerBtn.setEnabled(true);
        recordImgBtn.setEnabled(true);
    }

    private void sendAnswer(int answerIndex, Button answerButton) {
        boolean isCorrect = gameVM.getCurrentQuestion().correctAnswer == answerIndex;

        ArrayList<Boolean> isCorrectList = gameVM.getIsCorrectList();
        isCorrectList.add(isCorrect);
        gameVM.setIsCorrectList(isCorrectList);

        if (isCorrect) {
            answerButton.setBackgroundColor(MyColor.CORRECT_GREEN);
        } else {
            answerButton.setBackgroundColor(MyColor.WRONG_RED);

            answerButtons[gameVM.getCurrentQuestion().correctAnswer].setBackgroundColor(MyColor.CORRECT_GREEN);
        }
        drawIsCorrectOnProgressBar(isCorrect, gameVM.getCurrentQuestionIndex());

        gameVM.setCurrentQuestionIndex(gameVM.getCurrentQuestionIndex() + 1);

        for (Button answerBtn : answerButtons)
            answerBtn.setEnabled(false);
        recordImgBtn.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gameVM.getCurrentQuestionIndex() == gameVM.getQuestions().size()) {
                    showEndGameFragment();

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

        ArrayList<Boolean> isCorrectList = gameVM.getIsCorrectList();
        if(isCorrectList == null){
            gameVM.setIsCorrectList(new ArrayList<>());
        }else
        {
            for(int i=0;i<isCorrectList.size();i++)
                drawIsCorrectOnProgressBar(isCorrectList.get(i), i);
        }
    }

    private void drawIsCorrectOnProgressBar(boolean isCorrect, int currentQuestion) {
        int totalQuestions = gameVM.getQuestions().size();

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
        for(int i = 0; i< answerButtons.length; i++)
            if(v.getId()== answerButtons[i].getId())
                sendAnswer(i, (Button) v);

        switch (v.getId()) {
            case R.id.recordImgBtn:
                break;
        }
    }
}
