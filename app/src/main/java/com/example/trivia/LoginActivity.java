package com.example.trivia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

enum Mode {
    LOGIN,
    SIGNUP
}

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String LOGIN_PREFERENCES_FILE = "loginSp";
    private static final String LOGIN_PREFERENCES_USERNAME = "username";
    private static final String LOGIN_PREFERENCES_PASSWORD = "password";

    private Button loginButton;
    private TextView toggleLoginModeLbl;
    private TextView toggleLoginModeLink;
    private EditText usernameTxt;

    private EditText passwordTxt;
    private EditText passwordAgainTxt;
    private CheckBox rememberMeCb;
    private TextView loginStatusLbl;

    private Mode mode;

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
        if (!validateUsernameAnsPassword(username, password))
            return;
        FirebaseAuth.getInstance().signInWithEmailAndPassword(User.usernameToEmail(username), password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //if user isn't listed in "users" list, then create user and add it
                    //can happen if user was signed in and had an connection error before he was added to list
                    String username = User.emailToUsername(task.getResult().getUser().getEmail());
                    String uid = task.getResult().getUser().getUid();
                    FirebaseFirestore.getInstance()
                            .collection(GameActivity.USERS_COLLECTION_PATH).document(username).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(!documentSnapshot.exists()){
                                                //user isn't in users list
                                                FirebaseFirestore.getInstance()
                                                        .collection(GameActivity.USERS_COLLECTION_PATH).document(username)
                                                        .set(new User(username, uid, 0, 0, 0))
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                //user is already in users list
                                                startMainMenuActivity();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loginStatusLbl.setText(e.getMessage());
                                }
                            });

                } else
                    loginStatusLbl.setText(task.getException().getMessage());
            }
        });
    }

    private void signup(String username, String password, String passwordAgain) {
        //validate username
        if (usernameTxt.getText().toString().indexOf('@') != -1) {
            //username is added '@1.1' to be an email address, therefore it mustn't have @ in it
            loginStatusLbl.setText("Username mustn't include '@'");
            setWrongColors(usernameTxt);
            return;
        }

        if (!password.equals(passwordAgain)) {
            loginStatusLbl.setText("Passwords doesn't match");
            passwordTxt.setText("");
            passwordAgainTxt.setText("");
            setWrongColors(passwordTxt);
            setWrongColors(passwordAgainTxt);

            validateUsernameAnsPassword(username, password);
            return;
        }

        if (!validateUsernameAnsPassword(username, password))
            return;

        String email = User.usernameToEmail(username);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //add user to users list
                    User user = new User(username, FirebaseAuth.getInstance().getUid(), 0, 0, 0);
                    FirebaseFirestore.getInstance().
                            collection(GameActivity.USERS_COLLECTION_PATH).document(username).set(user).
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        startMainMenuActivity();
                                    else
                                        loginStatusLbl.setText(task.getException().getMessage());
                                }
                            });
                } else
                    loginStatusLbl.setText(task.getException().getMessage());
            }
        });
    }

    private boolean validateUsernameAnsPassword(String username, String password) {
        boolean retValue = true;

        if (username.length() == 0) {
            loginStatusLbl.setText(loginStatusLbl.getText() + "\nPlease enter username");
            setWrongColors(usernameTxt);
            retValue = false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
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
                //if required, save username and password to shared preferences
                saveLoginDataToSharedPreferences();

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

    private void saveLoginDataToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFERENCES_FILE, MODE_PRIVATE);

        if (rememberMeCb.isChecked()) {
            //save to shared preferences
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

            sharedPreferencesEditor.putString(LOGIN_PREFERENCES_USERNAME, usernameTxt.getText().toString());
            sharedPreferencesEditor.putString(LOGIN_PREFERENCES_PASSWORD, passwordTxt.getText().toString());

            //apply changes
            sharedPreferencesEditor.apply();
        } else if (sharedPreferences.getAll().size() != 0) { //there is already shared preferences file
            //delete shared preferences
            sharedPreferences.edit().clear().apply();
        }
    }

    private void loadLoginDataFromSharedPreferences() {
        //try load the data
        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFERENCES_FILE, MODE_PRIVATE);
        String username = sharedPreferences.getString(LOGIN_PREFERENCES_USERNAME, null);
        String password = sharedPreferences.getString(LOGIN_PREFERENCES_PASSWORD, null);

        //if shared preferences was found, show saved username and password in EditTexts
        if (username != null) {
            usernameTxt.setText(username);
            rememberMeCb.setChecked(true);
        }
        if (password != null) {
            passwordTxt.setText(password);
            rememberMeCb.setChecked(true);
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

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            //FirebaseAuth.getInstance().signOut();
            startMainMenuActivity();
        else
            loadLoginDataFromSharedPreferences();

        mode = Mode.LOGIN;
        switchToLoginMode();
    }


    private void startMainMenuActivity() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
