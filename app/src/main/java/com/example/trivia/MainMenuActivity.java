package com.example.trivia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainMenuActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener {
    public static final String EXTRA_FRAGMENT_NAME = "fragment";
    public static final int EXTRA_SCORE_FRAGMENT = 0;
    public static final int EXTRA_NEW_GAME_FRAGMENT = 1;
    public static final int EXTRA_JOIN_GAME_FRAGMENT = 2;
    public static final int EXTRA_SETTINGS_FRAGMENT = 3;
    private static final int MIN_SWIPE_LENGTH = 150;

    private GestureDetectorCompat swipeDetector;
    private BottomNavigationView navigationView;
    private NewGameFragment newGameFragment;
    private JoinGameFragment joinGameFragment;
    private ScoreFragment scoreFragment;
    private SettingsFragment settingsFragment;
    private Intent bgMusicIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //start background music service
        bgMusicIntent = new Intent(this, MusicService.class);
        ContextCompat.startForegroundService(this, bgMusicIntent);

        navigationView = findViewById(R.id.mainNavigationView);
        newGameFragment = new NewGameFragment();
        joinGameFragment = new JoinGameFragment();
        scoreFragment = new ScoreFragment();
        settingsFragment = new SettingsFragment();
        swipeDetector = new GestureDetectorCompat(this, this);

        //the opening fragment is passed through intent
        //default is new game fragment
        int startFragment = getIntent().getIntExtra(EXTRA_FRAGMENT_NAME, -1);
        switch (startFragment){
            case EXTRA_SCORE_FRAGMENT:
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, scoreFragment).commit();
                navigationView.setSelectedItemId(R.id.scoreFragment);
                break;
            case EXTRA_JOIN_GAME_FRAGMENT:
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, joinGameFragment).commit();
                navigationView.setSelectedItemId(R.id.joinGameFragment);
                break;
            case EXTRA_SETTINGS_FRAGMENT:
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, settingsFragment).commit();
                navigationView.setSelectedItemId(R.id.settingsFragment);
                break;
            default:
                //new game fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, newGameFragment).commit();
                navigationView.setSelectedItemId(R.id.newGameFragment);
                break;
        }

        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.newGameFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, newGameFragment).commit();
                        return true;

                    case R.id.joinGameFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, joinGameFragment).commit();
                        return true;

                    case R.id.scoreFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, scoreFragment).commit();
                        return true;

                    case R.id.settingsFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, settingsFragment).commit();
                        return true;
                }

                return false;
            }
        });
    }
    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if(Math.abs(e2.getX() - e1.getX()) > MIN_SWIPE_LENGTH){
            //swipe
            if(e2.getX() > e1.getX()){
                //right swipe
                if(currentFragment instanceof JoinGameFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, newGameFragment).commit();
                    navigationView.setSelectedItemId(R.id.newGameFragment);
                }
                else if(currentFragment instanceof NewGameFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, scoreFragment).commit();
                    navigationView.setSelectedItemId(R.id.scoreFragment);
                }
                else if(currentFragment instanceof SettingsFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, joinGameFragment).commit();
                    navigationView.setSelectedItemId(R.id.joinGameFragment);
                }
            }
            else{
                //left swipe
                if(currentFragment instanceof ScoreFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, newGameFragment).commit();
                    navigationView.setSelectedItemId(R.id.newGameFragment);
                }
                else if(currentFragment instanceof NewGameFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, joinGameFragment).commit();
                    navigationView.setSelectedItemId(R.id.joinGameFragment);
                }
                else if(currentFragment instanceof JoinGameFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, settingsFragment).commit();
                    navigationView.setSelectedItemId(R.id.settingsFragment);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        swipeDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}