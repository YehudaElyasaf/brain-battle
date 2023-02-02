package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

enum Mode{
    LOGIN,
    SIGNUP
}

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Button loginButton;
    TextView toggleLoginModeLbl;
    TextView toggleLoginModeLink;
    EditText usernameTxt;
    EditText passwordTxt;
    EditText passwordAgainTxt;
    CheckBox rememberMeCb;

    private Mode mode;

    private void switchToLoginMode(){
        loginButton.setText("Log In");
        toggleLoginModeLbl.setText("Don't have a user?");
        toggleLoginModeLink.setText("SIGN UP");
        passwordAgainTxt.setVisibility(View.GONE);
        mode = Mode.LOGIN;
    }
    private void switchToSignupMode(){
        loginButton.setText("Sign Up");
        toggleLoginModeLbl.setText("Already have a user?");
        toggleLoginModeLink.setText("LOG IN");
        passwordAgainTxt.setVisibility(View.VISIBLE);
        mode = Mode.SIGNUP;
    }

    private void login(){

    }
    private void signup(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginBtn:
                if(mode == Mode.LOGIN)
                    login();
                else if(mode == Mode.SIGNUP)
                    signup();
                break;

            case R.id.toggleLoginModeLink:
                if(mode == Mode.LOGIN)
                    switchToSignupMode();
                else if(mode == Mode.SIGNUP)
                    switchToLoginMode();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginBtn);
        toggleLoginModeLbl = findViewById(R.id.toggleLoginModeLbl);
        toggleLoginModeLink = findViewById(R.id.toggleLoginModeLink);
        usernameTxt = findViewById(R.id.loginUsernameTxt);
        passwordTxt = findViewById(R.id.loginPasswordTxt);
        passwordAgainTxt = findViewById(R.id.loginPasswordAgainTxt);
        rememberMeCb = findViewById(R.id.loginRememberMeCb);

        toggleLoginModeLink.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        mode = Mode.LOGIN;
        switchToLoginMode();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
