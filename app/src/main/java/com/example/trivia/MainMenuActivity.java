package com.example.trivia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.AdapterView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainMenuActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener {
    public static final int MIN_SWIPE_LENGTH = 100;

    private GestureDetectorCompat swipeDetector;
    private BottomNavigationView navigationView;
    private StartGameFragment startGameFragment;
    private ScoreFragment scoreFragment;
    private SettingsFragment settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        navigationView = findViewById(R.id.mainNavigationView);
        startGameFragment = new StartGameFragment();
        scoreFragment = new ScoreFragment();
        settingFragment = new SettingsFragment();
        swipeDetector = new GestureDetectorCompat(this, this);

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, startGameFragment).commit();
        navigationView.setSelectedItemId(R.id.startGameFragment);

        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.startGameFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, startGameFragment).commit();
                        return true;

                    case R.id.scoreFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, scoreFragment).commit();
                        return true;

                    case R.id.settingsFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, settingFragment).commit();
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
                if(currentFragment instanceof StartGameFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, scoreFragment).commit();
                    navigationView.setSelectedItemId(R.id.scoreFragment);
                }
                else if(currentFragment instanceof SettingsFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, startGameFragment).commit();
                    navigationView.setSelectedItemId(R.id.startGameFragment);
                }
            }
            else{
                //left swipe
                if(currentFragment instanceof ScoreFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, startGameFragment).commit();
                    navigationView.setSelectedItemId(R.id.startGameFragment);
                }
                else if(currentFragment instanceof StartGameFragment){
                    fragmentTransaction.replace(R.id.mainFragmentContainer, settingFragment).commit();
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