package com.example.trivia;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AnswerRecorder {
    private static final String HEBREW = "iw-IL";
    private static SpeechRecognizer speechRecognizer = null;

    public static void startRecording(Context context, Button[] answerButtons) {
        //create speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //set language to Hebrew
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, HEBREW);

        //ask permission to record if required
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            //auto generated methods
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                try {
                    //get result from bundle
                    String word = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);

                    int intResult = -1;

                    //check if result is a number between 1-4
                    if (word.equals("one") || word.equals("אחת") || word.equals("1"))
                        intResult = 1;
                    else if (word.equals("two") || word.equals("שתיים") || word.equals("2"))
                        intResult = 2;
                    else if (word.equals("three") || word.equals("שלוש") || word.equals("3"))
                        intResult = 3;
                    else if (word.equals("four") || word.equals("ארבע") || word.equals("4"))
                        intResult = 4;

                    intResult--;

                    //get the button of this answer
                    Button answerButton = answerButtons[intResult];
                    if (answerButton.isEnabled())
                    //click this button
                        answerButton.callOnClick();
                } catch (Exception e) {
                    //value isn't a number
                    //or the number is out of range
                    //do nothing
                }

                //after recording, the speech recognizer is not needed
                speechRecognizer.destroy();
            }

            @Override
            public void onPartialResults(Bundle results) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        //try recording
        if (SpeechRecognizer.isRecognitionAvailable(context))
            speechRecognizer.startListening(recognizerIntent);
        else
            Toast.makeText(context, "Recording not available!", Toast.LENGTH_SHORT).show();
    }

    public static void stopRecording() {
        //stop speech recognizer
        speechRecognizer.stopListening();
    }
}
