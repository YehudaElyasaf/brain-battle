package com.example.trivia;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private MutableLiveData<Game> game;
    private boolean isCreator; //is this player the game creator?

    public GameViewModel() {
        game = new MutableLiveData<>();
        game.setValue(new Game());
        isCreator = true;
    }

    public Game getGame() {
        return game.getValue();
    }

    public void setGame(Game game) {
        this.game.setValue(game);
    }

    public boolean isCreator() {
        return isCreator;
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    public void setQuestions(ArrayList<Question> questions) {
        getGame().setQuestions(questions);
    }

    public Player getPlayer1() {
        return getGame().getPlayer1();
    }

    public Player getPlayer2() {
        return getGame().getPlayer2();
    }

    public void setPlayer1(Player player1) {
        getGame().setPlayer1(player1);
    }

    public void setPlayer2(Player player2) {
        getGame().setPlayer2(player2);
    }

    public ArrayList<Question> getQuestions() {
        return getGame().getQuestions();
    }

    public Player getMyPlayer() {
        if (isCreator)
            //creator is player1
            return getGame().getPlayer1();
        else
            return getGame().getPlayer2();
    }

    public void setMyPlayer(Player player) {
        //creator is player1
        Game newGame = getGame();
        if (isCreator)
            newGame.setPlayer1(player);
        else
            newGame.setPlayer2(player);

        game.setValue(newGame);
    }

    //save the state of current player
    //used to know whether it is changed in game's observer
    private Player previousMyPlayer;

    public void enableGameSyncWithFirestore(Context context) {
        previousMyPlayer = getMyPlayer();
        String gameId = Integer.toString(getGame().getId());

        //when other player is changed, update Game locally
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(GameActivity.GAMES_COLLECTION_PATH).document(gameId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error == null && snapshot != null && snapshot.exists()) {
                    //no exception
                    Game newGame = snapshot.toObject(Game.class);

                    if (newGame.getPlayer2() != null) {
                        //if player2 is null, game hasn't yet started
                        Game gameValue = getGame();
                        if (isCreator && !newGame.getPlayer2().equals(gameValue.getPlayer2())) {
                            //player 2 has changed
                            gameValue.setPlayer2(newGame.getPlayer2());
                            game.setValue(gameValue);
                        }
                        if (!isCreator && !newGame.getPlayer1().equals(gameValue.getPlayer1())) {
                            //player 1 has changed
                            gameValue.setPlayer1(newGame.getPlayer1());
                            game.setValue(gameValue);
                        }
                    }
                }
                else if(error != null){
                    //unknown error
                    Toast.makeText(context, "An error occurred!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, MainMenuActivity.class);
                    context.startActivity(intent);
                    ((GameActivity) context).finish();
                }
            }
        });

        //when my player changed, update in firestore
        game.observe((LifecycleOwner) context, new Observer<Game>() {
            @Override
            public void onChanged(Game newGame) {
                if ((previousMyPlayer == null && getMyPlayer() != null) ||
                        (previousMyPlayer != null && !previousMyPlayer.equals(getMyPlayer())))
                    //player has changed
                    firestore.collection(GameActivity.GAMES_COLLECTION_PATH).document(gameId).set(newGame).
                            addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Connection error!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, MainMenuActivity.class);
                                    context.startActivity(intent);
                                    ((GameActivity) context).finish();
                                }
                            });

                if (getMyPlayer() == null)
                    //copy constructor doesn't work with null
                    previousMyPlayer = null;
                else
                    previousMyPlayer = new Player(getMyPlayer());
            }
        });
    }


    public MutableLiveData<Game> getGameLiveData() {
        return game;
    }

    public void setMyCurrentQuestionIndex(int i) {
        //creator is player1
        Game newGame = getGame();
        if (isCreator)
            newGame.getPlayer1().setCurrentQuestionIndex(i);
        else
            newGame.getPlayer2().setCurrentQuestionIndex(i);

        game.setValue(newGame);
    }

    public void setMyIsCorrectList(ArrayList<Boolean> isCorrectList) {
        //creator is player1
        Game newGame = getGame();
        if (isCreator)
            newGame.getPlayer1().setIsCorrectList(isCorrectList);
        else
            newGame.getPlayer2().setIsCorrectList(isCorrectList);

        game.setValue(newGame);
    }

    public void setGameId(int gameId) {
        Game newGame = getGame();
        newGame.setId(gameId);
        game.setValue(newGame);
    }

    public Player getOtherPlayer() {
        if (isCreator)
            return getGame().getPlayer2();
        else
            return getGame().getPlayer1();
    }
}
