package com.example.trivia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AdapterView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainMenuActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    StartGameFragment startGameFragment;
    ScoreFragment scoreFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        navigationView = findViewById(R.id.mainNavigationView);
        startGameFragment = new StartGameFragment();
        scoreFragment = new ScoreFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, startGameFragment).commit();
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                switch (item.getItemId()){
                    case R.id.startGameFragment:
                        fragmentTransaction.replace(R.id.mainFragmentContainer, startGameFragment).commit();
                        return true;

                    case R.id.scoreFragment:
                        fragmentTransaction.replace(R.id.mainFragmentContainer, scoreFragment).commit();
                        return true;
                }

                return false;
            }
        });
    }
}