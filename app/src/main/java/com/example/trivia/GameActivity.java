package com.example.trivia;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String GAMES_COLLECTION_PATH = "games";
    public static final String USERS_COLLECTION_PATH = "users";

    public static final String NEW_GAME_EXTRAS = "extras";
    public static final String IS_NEW_GAME_EXTRA = "isNewGame";
    public static final String GAME_ID_EXTRA = "gameId";

    public static final int QUESTIONS_COUNT_INDEX = 0;
    public static final int DIFFICULTY_LEVEL_INDEX = 1;
    public static final int CATEGORY_INDEX = 2;

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
    private FirebaseFirestore firestore;
    private boolean isPlayer1;

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
        firestore = FirebaseFirestore.getInstance();
        isPlayer1 = getIntent().getBooleanExtra(IS_NEW_GAME_EXTRA, false);

        homeImgBtn.setOnClickListener(this);
        for (Button answer : answerButtons)
            answer.setOnClickListener(this);
        recordImgBtn.setOnClickListener(this);

        if (gameVM.getUser() == null) {
            //init screen
            //get user data
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            firestore.collection(USERS_COLLECTION_PATH).document(email).get().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GameActivity.this, "Connection error!", Toast.LENGTH_SHORT);
                    backToMainMenu();
                }
            }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //add user to viewModel
                    User user = documentSnapshot.toObject(User.class);
                    gameVM.setUser(user);

                    //create/join game
                    //screen not initialized
                    if(isPlayer1){
                        //create new game
                        int[] extras = getIntent().getIntArrayExtra(NEW_GAME_EXTRAS);
                        int questionsCount = extras[QUESTIONS_COUNT_INDEX];
                        DifficultyLevel difficultyLevel = DifficultyLevel.values()[extras[DIFFICULTY_LEVEL_INDEX]];
                        Category category = Category.values()[extras[CATEGORY_INDEX]];

                        GetQuestionsAsync getQuestionsAsync = new GetQuestionsAsync();
                        getQuestionsAsync.execute(questionsCount, difficultyLevel.ordinal(), category.ordinal());
                    }
                    else{
                        //join existing game
                        int id = getIntent().getIntExtra(GAME_ID_EXTRA, -1);
                        JoinGameAsync joinGameAsync = new JoinGameAsync();
                        joinGameAsync.execute(id);
                    }
                }
        });

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
        //params: question count, difficulty level, category
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
                gameVM.setQuestions(questions);
                Player player1 = new Player(gameVM.getUser());
                gameVM.getGame().setPlayer1(player1);

                startGame();
            }
        }
    }


    private class JoinGameAsync extends AsyncTask<Integer, Integer, Void>{
        //TODO: move asyncTasks to their fragment in Create/JoinGame ???
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingFragment();
        }

        @Override
        //params: id
        protected Void doInBackground(Integer... integers) {
            int id = integers[0];
            String strId = Integer.toString(id);
            firestore.collection(GAMES_COLLECTION_PATH).document(strId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        Game game = documentSnapshot.toObject(Game.class);

                        Player player2 = new Player(gameVM.getUser());
                        game.setPlayer2(player2);

                        gameVM.setGame(game);
                        hideLoadingFragment();
                        showCurrentQuestion();
                    }
                    else{
                        Toast.makeText(GameActivity.this, "Wrong game ID!", Toast.LENGTH_SHORT);
                        backToMainMenu();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GameActivity.this, "Connection error!", Toast.LENGTH_SHORT);
                    backToMainMenu();
                }
            });

            return null;
        }
    }

    private void startGame() {
        //100,000-999,999
        int minId = (int)Math.pow(10, JoinGameFragment.GAME_ID_LENGTH - 1);
        int maxId = (int)Math.pow(10, JoinGameFragment.GAME_ID_LENGTH);
        int gameId = minId + (new Random().nextInt(maxId - minId));

        //add game to DB, and wait for an enemy
        //TODO: validate ID not duplicated
        //TODO: change loading label

        firestore.collection(GAMES_COLLECTION_PATH).document(Integer.toString(gameId)).set(
                gameVM.getGame()
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Failed to create game!", Toast.LENGTH_SHORT).show();
                backToMainMenu();
            }
        });

        hideLoadingFragment();
        showCurrentQuestion();
    }

    private void backToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void showCurrentQuestion() {
        int currentQuestionIndex = getMyCurrentQuestionIndex();

        Question question = gameVM.getQuestions().get(currentQuestionIndex);

        currentQuestionLbl.setText((currentQuestionIndex + 1) +
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

    private int getMyCurrentQuestionIndex() {
        if(isPlayer1)
            return gameVM.getPlayer1().getCurrentQuestionIndex();
        else
            return gameVM.getPlayer2().getCurrentQuestionIndex();
    }

    private void setMyCurrentQuestionIndex(int index) {
        if(isPlayer1)
            gameVM.getPlayer1().setCurrentQuestionIndex(index);
        else
            gameVM.getPlayer2().setCurrentQuestionIndex(index);
    }

    private void sendAnswer(int answerIndex, Button answerButton) {
        boolean isCorrect = gameVM.getQuestions().get(getMyCurrentQuestionIndex()).correctAnswer == answerIndex;

        ArrayList<Boolean> isCorrectList = gameVM.getIsCorrectList();
        isCorrectList.add(isCorrect);
        gameVM.setIsCorrectList(isCorrectList);

        if (isCorrect) {
            answerButton.setBackgroundColor(MyColor.CORRECT_GREEN);
        } else {
            answerButton.setBackgroundColor(MyColor.WRONG_RED);

            answerButtons[gameVM.getQuestions().get(getMyCurrentQuestionIndex()).correctAnswer].setBackgroundColor(MyColor.CORRECT_GREEN);
        }
        drawIsCorrectOnProgressBar(isCorrect, getMyCurrentQuestionIndex());

        setMyCurrentQuestionIndex(getMyCurrentQuestionIndex() + 1);

        for (Button answerBtn : answerButtons)
            answerBtn.setEnabled(false);
        recordImgBtn.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getMyCurrentQuestionIndex() == gameVM.getQuestions().size()) {
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
