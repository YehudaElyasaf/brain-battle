package com.example.trivia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
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

public class GameActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
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
    private Fragment waitForEnemyFragment;
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

        startNetworkStatusReceiver();

        gameVM = new ViewModelProvider(this).get(GameViewModel.class);
        firestore = FirebaseFirestore.getInstance();
        //is creator = is this player the game creator
        gameVM.setCreator(getIntent().getBooleanExtra(IS_NEW_GAME_EXTRA, false));

        //create alert builder to exit alert
        AlertDialog.Builder exitAlertDialogBuilder = new AlertDialog.Builder(this);
        exitAlertDialogBuilder.setMessage("Exit?");
        exitAlertDialogBuilder.setNegativeButton("No", null);
        exitAlertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //exit
                Intent intent = new Intent(getBaseContext(), MainMenuActivity.class);
                startActivity(intent);
                finish();
            }
        });
        homeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ask if user is sure
                exitAlertDialogBuilder.show();
            }
        });

        //show loading fragment
        loadingFragment = new LoadingFragment();
        showFragment(loadingFragment);

        //TODO: fix answerButtons colors
        for (Button answer : answerButtons)
            answer.setOnClickListener(this);
        recordImgBtn.setOnTouchListener(this);

        if (gameVM.getMyPlayer() == null) {
            //init screen
            //get user data
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String username = User.emailToUsername(email);
            firestore.collection(USERS_COLLECTION_PATH).document(username).get().addOnFailureListener(new OnFailureListener() {
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
                    if (gameVM.isCreator()) {
                        //create new game
                        int[] extras = getIntent().getIntArrayExtra(NEW_GAME_EXTRAS);
                        int questionsCount = extras[QUESTIONS_COUNT_INDEX];
                        DifficultyLevel difficultyLevel = DifficultyLevel.values()[extras[DIFFICULTY_LEVEL_INDEX]];
                        Category category = Category.values()[extras[CATEGORY_INDEX]];

                        GetQuestionsAsync getQuestionsAsync = new GetQuestionsAsync();
                        getQuestionsAsync.execute(questionsCount, difficultyLevel.ordinal(), category.ordinal());
                    } else {
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
            hideFragment(loadingFragment);
            gameVM.enableGameSyncWithFirestore(GameActivity.this);
            showCurrentQuestion();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        initProgressBarCanvas();
    }

    private void showFragment(Fragment fragment) {
        disableAllButtons();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.gameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void hideFragment(Fragment fragment) {
        enableAllButtons();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(fragment);
        fragmentTransaction.commit();
    }

    private void showEndGameFragment() {
        disableAllButtons();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.gameLayout, new EndGameFragment());
        fragmentTransaction.commit();
    }

    private void waitForEnemy() {
        loadingFragment = new LoadingFragment();
        showFragment(loadingFragment);

        int questionCount = gameVM.getQuestions().size();
        if (gameVM.getOtherPlayer().getCurrentQuestionIndex() == questionCount)
            //enemy already finished
            endGame();
        else {
            waitForEnemyFragment = new WaitToEnemyFragment();
            showFragment(waitForEnemyFragment);
            //wait until enemy ends the game too
            gameVM.getGameLiveData().observe(GameActivity.this, new Observer<Game>() {
                @Override
                public void onChanged(Game game) {
                    if (gameVM.getOtherPlayer().getCurrentQuestionIndex() == questionCount) {
                        //enemy finished

                        //try delete game
                        //if failed, do nothing
                        firestore.collection(GAMES_COLLECTION_PATH)
                                .document(Integer.toString(gameVM.getGame().getId())).delete();
                        hideFragment(waitForEnemyFragment);
                        endGame();
                    }
                }
            });
        }
    }

    private void endGame() {
        //update user score
        gameVM.getMyPlayer().setTotalCorrect(gameVM.getMyPlayer().getTotalCorrect() + gameVM.getMyPlayer().getTotalCorrectInGame());
        gameVM.getMyPlayer().setTotalWrong(gameVM.getMyPlayer().getTotalWrong() + gameVM.getMyPlayer().getTotalWrongInGame());

        int myPoints = gameVM.getMyPlayer().calculatePoints();
        int otherPoints = gameVM.getOtherPlayer().calculatePoints();
        if (myPoints > otherPoints)
            //you won, add points to total score
            gameVM.getMyPlayer().setScore(gameVM.getMyPlayer().getScore() + myPoints);

        User myPlayerAsUser = gameVM.getMyPlayer();
        //send new user data to users list
        firestore.collection(USERS_COLLECTION_PATH).document(gameVM.getMyPlayer().getUsername())
                .set(myPlayerAsUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(GameActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                            backToMainMenu();
                        } else {
                            //game updated successfully
                            showEndGameFragment();
                        }
                    }
                });
    }


    private class GetQuestionsAsync extends AsyncTask<Integer, Integer, ArrayList<Question>> {
        @Override
        //params: question count, difficulty level, category
        protected ArrayList<Question> doInBackground(Integer... integers) {
            HttpQuestionFetcher questionFetcher = new HttpQuestionFetcher();
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


    private class JoinGameAsync extends AsyncTask<Integer, Integer, Void> {
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

                    if (documentSnapshot.exists()) {
                        Game game = documentSnapshot.toObject(Game.class);

                        //check if game started
                        if (game.getPlayer2() != null) {
                            //game already started
                            Toast.makeText(getContext(), "Game has already started", Toast.LENGTH_SHORT).show();
                            backToJoinGameFragment();
                        }

                        gameVM.setGame(game);
                        gameVM.enableGameSyncWithFirestore(GameActivity.this);
                        gameVM.setMyPlayer(myPlayer);

                        hideFragment(loadingFragment);
                        showCurrentQuestion();
                    } else {
                        Toast.makeText(GameActivity.this, "Wrong game ID!", Toast.LENGTH_SHORT).show();
                        backToJoinGameFragment();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GameActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                    backToJoinGameFragment();
                }
            });

            return null;
        }
    }

    private void sendGameToFirestore() {
        //random integer between 100,000-999,999
        int minId = (int) Math.pow(10, JoinGameFragment.GAME_ID_LENGTH - 1);
        int maxId = (int) Math.pow(10, JoinGameFragment.GAME_ID_LENGTH);
        int gameId = minId + (new Random().nextInt(maxId - minId));

        //check if ID isn't already taken
        firestore.collection(GAMES_COLLECTION_PATH).document(Integer.toString(gameId)).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //game ID already taken
                            //generate new ID (recursively)
                            sendGameToFirestore();
                            return;
                        }

                        //valid game ID
                        gameVM.setGameId(gameId);

                        //add game to DB, and wait for an enemy
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
                                        hideFragment(loadingFragment);
                                        gameIdFragment = new GameIdFragment();
                                        showFragment(gameIdFragment);
                                        gameVM.enableGameSyncWithFirestore(GameActivity.this);

                                        //wait until a an enemy joins
                                        gameVM.getGameLiveData().observe(GameActivity.this, new Observer<Game>() {
                                            @Override
                                            public void onChanged(Game game) {
                                                if (game.getPlayer2() != null) {
                                                    //enemy joined
                                                    hideFragment(gameIdFragment);
                                                    showCurrentQuestion();
                                                }
                                            }
                                        });
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), "Failed to create game!", Toast.LENGTH_SHORT).show();
                        backToMainMenu();
                    }
                });
    }

    public void backToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void backToJoinGameFragment() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra(MainMenuActivity.EXTRA_FRAGMENT_NAME, MainMenuActivity.EXTRA_JOIN_GAME_FRAGMENT);
        startActivity(intent);
        finish();
    }

    private void showCurrentQuestion() {
        int currentQuestionIndex = 0;
        Player myPlayer = gameVM.getMyPlayer();
        if (myPlayer != null)
            currentQuestionIndex = myPlayer.getCurrentQuestionIndex();
        int totalQuestions = gameVM.getQuestions().size();

        if (currentQuestionIndex >= totalQuestions)
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

        enableAllButtons();
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

        disableAllButtons();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentQuestionIndex == gameVM.getQuestions().size() - 1) {
                    //game ended
                    waitForEnemy(); //function waitForEnemy also ends the game

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
        if (myPlayer != null) {
            ArrayList<Boolean> isCorrectList = gameVM.getMyPlayer().getIsCorrectList();
            for (int i = 0; i < isCorrectList.size(); i++)
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
        for (int i = 0; i < answerButtons.length; i++)
            if (v.getId() == answerButtons[i].getId())
                sendAnswer(i, (Button) v);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.recordImgBtn) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                AnswerRecorder.startRecording(this, answerButtons);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                AnswerRecorder.stopRecording();
            }
        }

        return true;
    }

    private Context getContext() {
        return this;
    }

    private void startNetworkStatusReceiver() {
        //create receiver
        ImageView img = findViewById(R.id.noInternetImg);
        NetworkStatusReceiver networkStatusReceiver = new NetworkStatusReceiver(img);

        //run receiver
        registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void enableAllButtons() {
        for (Button answerBtn : answerButtons)
            answerBtn.setEnabled(true);
        recordImgBtn.setEnabled(true);
        homeImgBtn.setEnabled(true);
    }

    private void disableAllButtons() {
        for (Button answerBtn : answerButtons)
            answerBtn.setEnabled(false);
        recordImgBtn.setEnabled(false);
        homeImgBtn.setEnabled(false);
    }
}
