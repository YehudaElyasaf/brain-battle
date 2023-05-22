package com.example.trivia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
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
    private Fragment loadingFragment;
    private Fragment gameIdFragment;
    private FirebaseFirestore firestore;

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
        //is creator = is this player the game creator
        gameVM.setCreator(getIntent().getBooleanExtra(IS_NEW_GAME_EXTRA, false));

        homeImgBtn.setOnClickListener(this);
        for (Button answer : answerButtons)
            answer.setOnClickListener(this);
        recordImgBtn.setOnClickListener(this);

        if (gameVM.getMyPlayer() == null) {
            //init screen
            //get user data
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            firestore.collection(USERS_COLLECTION_PATH).document(email).get().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GameActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();//not shown
                    backToMainMenu();
                }
            }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //add user to viewModel
                    User user = documentSnapshot.toObject(User.class);
                    gameVM.setMyPlayer(new Player(user));

                    //create/join game
                    //screen not initialized
                    if(gameVM.isCreator()){
                        //create new game
                        int[] extras = getIntent().getIntArrayExtra(NEW_GAME_EXTRAS);
                        int questionsCount = extras[QUESTIONS_COUNT_INDEX];
                        DifficultyLevel difficultyLevel = DifficultyLevel.values()[extras[DIFFICULTY_LEVEL_INDEX]];
                        Category category = Category.values()[extras[CATEGORY_INDEX]];

                        GetQuestionsAsync getQuestionsAsync = new GetQuestionsAsync();
                        getQuestionsAsync.execute(questionsCount, difficultyLevel.ordinal(), category.ordinal());
                    }
                    else{
                        //not creator
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

    //TODO: make non-static
    public static Fragment showLoadingFragment(FragmentManager fm) {
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        Fragment loadingFragment = new LoadingFragment();
        fragmentTransaction.replace(R.id.gameLayout, loadingFragment);
        fragmentTransaction.commit();

        return loadingFragment;
    }

    public static void hideLoadingFragment(FragmentManager fm, Fragment loadGameFragment) {
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.hide(loadGameFragment);
        fragmentTransaction.commit();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.gameLayout, fragment);
        fragmentTransaction.commit();
    }
    private void hideFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(fragment);
        fragmentTransaction.commit();
    }

    private void showEndGameFragment() {
        homeImgBtn.setEnabled(false);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.gameLayout, new EndGameFragment());
        fragmentTransaction.commit();
    }

    private class EndGameAsync extends AsyncTask<Void, Void, Void> {
        private Fragment waitForEnemyFragment;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingFragment = showLoadingFragment(getSupportFragmentManager());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //update user score
            gameVM.getMyPlayer().setTotalCorrect(gameVM.getMyPlayer().getTotalCorrect() + gameVM.getMyTotalCorrect());
            gameVM.getMyPlayer().setTotalWrong(gameVM.getMyPlayer().getTotalWrong() + gameVM.getMyTotalWrong());
            gameVM.getMyPlayer().setScore(gameVM.getMyPlayer().getScore() + gameVM.calculatePoints());
            User myPlayerAsUser = (User)gameVM.getMyPlayer();

            //send new user data to users list
            firestore.collection(USERS_COLLECTION_PATH).document(gameVM.getMyPlayer().getEmail())
                    .set(myPlayerAsUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(GameActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                                backToMainMenu();
                            }
                            else{
                                int questionCount = gameVM.getQuestions().size();
                                if(gameVM.getOtherPlayer().getCurrentQuestionIndex() == questionCount)
                                    //enemy finished too
                                    showEndGameFragment();
                                else{
                                    waitForEnemyFragment = new WaitToEnemyFragment();
                                    showFragment(waitForEnemyFragment);

                                    //wait until enemy ends the game too
                                    gameVM.getGameLiveData().observe(GameActivity.this, new Observer<Game>() {
                                        @Override
                                        public void onChanged(Game game) {
                                            if(gameVM.getOtherPlayer().getCurrentQuestionIndex() == questionCount){
                                                hideFragment(waitForEnemyFragment);
                                                showEndGameFragment();
                                            }

                                        }
                                    });
                                }
                            }
                        }
                    });

            return null;
        }
    }

    private class GetQuestionsAsync extends AsyncTask<Integer, Integer, ArrayList<Question>> {
        @Override
        protected void onPreExecute() {
            loadingFragment = showLoadingFragment(getSupportFragmentManager());
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

                sendGameToFirestore();
            }
        }
    }


    private class JoinGameAsync extends AsyncTask<Integer, Integer, Void>{
        //TODO: move asyncTasks to their fragment in Create/JoinGame ???
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingFragment = showLoadingFragment(getSupportFragmentManager());
        }

        @Override
        //params: id
        protected Void doInBackground(Integer... integers) {
            Player myPlayer = gameVM.getMyPlayer(); //getting the game from firestore overrides player2 to null
            int id = integers[0];
            String strId = Integer.toString(id);
            firestore.collection(GAMES_COLLECTION_PATH).document(strId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //game loaded successfully
                    if(documentSnapshot.exists()){
                        Game game = documentSnapshot.toObject(Game.class);

                        gameVM.setGame(game);
                        gameVM.enableGameSyncWithFirestore(GameActivity.this);
                        gameVM.setMyPlayer(myPlayer);

                        hideLoadingFragment(getSupportFragmentManager(), loadingFragment);
                        showCurrentQuestion();
                    }
                    else{
                        Toast.makeText(GameActivity.this, "Wrong game ID!", Toast.LENGTH_SHORT).show();
                        backToMainMenu();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GameActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                    backToMainMenu();
                }
            });

            return null;
        }
    }

    private void sendGameToFirestore() {
        //random integer between 100,000-999,999
        int minId = (int)Math.pow(10, JoinGameFragment.GAME_ID_LENGTH - 1);
        int maxId = (int)Math.pow(10, JoinGameFragment.GAME_ID_LENGTH);
        int gameId = minId + (new Random().nextInt(maxId - minId));

        gameVM.setGameId(gameId);

        //add game to DB, and wait for an enemy
        //TODO: validate ID not duplicated
        //TODO: change loading label

        firestore.collection(GAMES_COLLECTION_PATH).document(Integer.toString(gameId))
                .set(gameVM.getGame()).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Failed to create game!", Toast.LENGTH_SHORT).show();
                backToMainMenu();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        hideLoadingFragment(getSupportFragmentManager(), loadingFragment);
                        gameIdFragment = new GameIdFragment();
                        showFragment(gameIdFragment);
                        gameVM.enableGameSyncWithFirestore(GameActivity.this);

                        //wait until a an enemy joins
                        gameVM.getGameLiveData().observe(GameActivity.this, new Observer<Game>() {
                            @Override
                            public void onChanged(Game game) {
                                if(game.getPlayer2() != null){
                                    //enemy joined
                                    hideFragment(gameIdFragment);
                                    showCurrentQuestion();
                                }
                            }
                        });
                    }
                });
    }

    public void backToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void showCurrentQuestion() {
        int currentQuestionIndex = 0;
        Player myPlayer = gameVM.getMyPlayer();
        if(myPlayer != null)
            currentQuestionIndex = myPlayer.getCurrentQuestionIndex();
        int totalQuestions = gameVM.getQuestions().size();

        if(currentQuestionIndex >= totalQuestions)
            //no more questions
            return;

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

    private void sendAnswer(int answerIndex, Button answerButton) {
        int currentQuestionIndex = gameVM.getMyPlayer().getCurrentQuestionIndex();
        boolean isCorrect = gameVM.getQuestions().get(currentQuestionIndex).correctAnswer == answerIndex;

        ArrayList<Boolean> isCorrectList = gameVM.getMyPlayer().getIsCorrectList();
        isCorrectList.add(isCorrect);
        gameVM.setMyIsCorrectList(isCorrectList);

        if (isCorrect) {
            answerButton.setBackgroundColor(MyColor.CORRECT_GREEN);
        } else {
            answerButton.setBackgroundColor(MyColor.WRONG_RED);

            answerButtons[gameVM.getQuestions().get(currentQuestionIndex).correctAnswer].setBackgroundColor(MyColor.CORRECT_GREEN);
        }
        drawIsCorrectOnProgressBar(isCorrect, currentQuestionIndex);

        gameVM.setMyCurrentQuestionIndex(currentQuestionIndex + 1);

        for (Button answerBtn : answerButtons)
            answerBtn.setEnabled(false);
        recordImgBtn.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentQuestionIndex == gameVM.getQuestions().size() - 1) {
                    //game ended
                    EndGameAsync endGameAsync = new EndGameAsync();
                    endGameAsync.execute();

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

        Player myPlayer = gameVM.getMyPlayer();
        if(myPlayer != null){
            ArrayList<Boolean> isCorrectList = gameVM.getMyPlayer().getIsCorrectList();
            for(int i = 0; i < isCorrectList.size(); i++)
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
