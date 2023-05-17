package com.example.trivia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

enum Mode {
    LOGIN,
    SIGNUP
}

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private Button loginButton;
    private TextView toggleLoginModeLbl;
    private TextView toggleLoginModeLink;
    private EditText usernameTxt;

    private EditText passwordTxt;
    private EditText passwordAgainTxt;
    private CheckBox rememberMeCb;
    private TextView loginStatusLbl;

    private Mode mode;
    private FirebaseAuth firebaseAuth;

    private void switchToLoginMode() {
        loginButton.setText("Log In");
        toggleLoginModeLbl.setText("Don't have a user?");
        toggleLoginModeLink.setText("SIGN UP");
        passwordAgainTxt.setVisibility(View.GONE);
        mode = Mode.LOGIN;
    }

    private void switchToSignupMode() {
        loginButton.setText("Sign Up");
        toggleLoginModeLbl.setText("Already have a user?");
        toggleLoginModeLink.setText("LOG IN");
        passwordAgainTxt.setVisibility(View.VISIBLE);
        mode = Mode.SIGNUP;
    }

    private void login(String username, String password) {
        if(!validateUsernameAnsPassword(username, password))
            return;
        firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    startMainMenuActivity();
                    //TODO: if user isn't listed in "users" list, then create user and add it
                    //can happen if user was signed in and had an connection error before he was added to list
                }
                else
                    loginStatusLbl.setText(task.getException().getMessage());
            }
        });
    }

    private void signup(String username, String password, String passwordAgain) {
        //TODO: sign in with user instead of mail
        if(!password.equals(passwordAgain)){
            loginStatusLbl.setText("Passwords doesn't match");
            passwordTxt.setText("");
            passwordAgainTxt.setText("");
            setWrongColors(passwordTxt);
            setWrongColors(passwordAgainTxt);

            validateUsernameAnsPassword(username, password);
            return;
        }

        if(!validateUsernameAnsPassword(username, password))
            return;

        String email = username;

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //add user to users list
                    User user = new User(username, firebaseAuth.getUid(), 0, 0, 0);
                    FirebaseFirestore.getInstance().
                            collection(GameActivity.USERS_COLLECTION_PATH).document(email).set(user).
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                        startMainMenuActivity();
                                    else
                                        loginStatusLbl.setText(task.getException().getMessage());
                                }
                            });
                }
                else
                    loginStatusLbl.setText(task.getException().getMessage());
            }
        });
    }

    private boolean validateUsernameAnsPassword(String username, String password) {
        boolean retValue = true;

        if(username.length() == 0){
            loginStatusLbl.setText(loginStatusLbl.getText() + "\nPlease enter username");
            setWrongColors(usernameTxt);
            retValue = false;
        }
        if(password.length() < MIN_PASSWORD_LENGTH){
            loginStatusLbl.setText(loginStatusLbl.getText() + "\nPassword must be at least 6 characters");
            setWrongColors(passwordTxt);
            setWrongColors(passwordAgainTxt);
            retValue = false;
        }

        return retValue;
    }

    @Override
    public void onClick(View v) {
        //reset "wrong" colors
        resetColors(usernameTxt);
        resetColors(passwordTxt);
        resetColors(passwordAgainTxt);
        loginStatusLbl.setText("");

        switch (v.getId()) {
            case R.id.loginBtn:
                if (mode == Mode.LOGIN)
                    login(usernameTxt.getText().toString(), passwordTxt.getText().toString());
                else if (mode == Mode.SIGNUP)
                    signup(usernameTxt.getText().toString(), passwordTxt.getText().toString(), passwordAgainTxt.getText().toString());
                break;

            case R.id.toggleLoginModeLink:
                if (mode == Mode.LOGIN)
                    switchToSignupMode();
                else if (mode == Mode.SIGNUP)
                    switchToLoginMode();
                break;
        }
    }

    private void setWrongColors(EditText editText) {
        editText.setHintTextColor(MyColor.WRONG_HINT_COLOR);
        editText.setBackgroundColor(MyColor.RED_100);
    }
    private void resetColors(EditText editText) {
        editText.setHintTextColor(MyColor.DEFAULT_HINT_COLOR);
        editText.setBackgroundColor(0); //transparent
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null)
            //TODO: connect automatically
            56//FirebaseAuth.getInstance().signOut();
            startMainMenuActivity();

        loginButton = findViewById(R.id.loginBtn);
        toggleLoginModeLbl = findViewById(R.id.toggleLoginModeLbl);
        toggleLoginModeLink = findViewById(R.id.toggleLoginModeLink);
        usernameTxt = findViewById(R.id.loginUsernameTxt);
        passwordTxt = findViewById(R.id.loginPasswordTxt);
        passwordAgainTxt = findViewById(R.id.loginPasswordAgainTxt);
        rememberMeCb = findViewById(R.id.loginRememberMeCb);
        loginStatusLbl = findViewById(R.id.loginStatusLbl);

        toggleLoginModeLink.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        mode = Mode.LOGIN;
        switchToLoginMode();
    }

    private void startMainMenuActivity() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
