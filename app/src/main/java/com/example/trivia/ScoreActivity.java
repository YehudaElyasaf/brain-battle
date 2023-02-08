package com.example.trivia;

import static com.example.trivia.StartGameActivity.MIN_SWIPE_LENGTH;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class ScoreActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener{
    private GestureDetectorCompat swipeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        swipeDetector = new GestureDetectorCompat(this, this);
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
        if(Math.abs(e2.getX() - e1.getX()) > MIN_SWIPE_LENGTH){
            //swipe
            if(e1.getX() > e2.getX()){
                //left swipe

                Intent intent = new Intent(this, StartGameActivity.class);
                startActivity(intent);
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        swipeDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}