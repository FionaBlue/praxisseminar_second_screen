package com.wildLive.secondScreen;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

// This activity calls the Quiz when an advertisement starts on the First Screen
public class QuizActivity extends AppCompatActivity {
    private QuestionLibrary questionLibrary;                        // library that contains all questions
    private Handler handler = new Handler();                        // for setting answer-color dynamically and handling answer click
    public Button buttonA, buttonB, buttonC, buttonD;               // quiz answer buttons
    private ImageView closeButton;                                  // for closing quiz activity manually
    private TextView question;                                      // quiz question text
    private TextView scoretext;                                     // actual reached score

    private String answer;                                          // current answer
    private int score = 0, questionNumber = 0;                      // initializing score and number of questions

    private SignalRClient srClient = null;
    static QuizActivity quizActivity;

    // quiz controls (volume changes and play/pause of video)
    public ImageView quizPauseButton, quizPlayButton;
    private ImageView quizVolumeUpButton, quizVolumeDownButton;
    private SeekBar videoProgress;

    // cast buttons
    private MenuItem activeCast, inactiveCast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quizActivity = this;

        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        score = sp.getInt("score", 0);
        questionNumber = sp.getInt("questionNumber", 0);

        WildLive app = (WildLive) getApplication();
        srClient = app.getSRClient();

        questionLibrary = new QuestionLibrary(QuizActivity.this);
        initiateElements();
        createButtonListeners();
        updateQuestion();
        updateScore(score);

        Bundle extras = getIntent().getExtras();
        int videoProgressInt = extras.getInt("videoProgress");
        updateVideoProgress(videoProgressInt);

        if(srClient != null){
            srClient.sendMsg("score"+String.valueOf(score));

            srClient.setMessageListener(new SignalRClient.SignalRCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    if(message.toString().contains("firstScreenConnected")){
                        srClient.isConnectedToFS = true;
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveQuizData();
    }

    public static QuizActivity getInstance(){
        return quizActivity;
    }

    private void updateVideoProgress(int progress){
        videoProgress.setProgress(progress);
    }

    //saves the score into shared preferences on the smartphone
    private void saveQuizData(){
        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("score",score);

        edit.putInt("questionNumber",questionNumber);
        edit.apply();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.quiz, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        activeCast = menu.findItem(R.id.action_cast_connected_quiz);
        inactiveCast = menu.findItem(R.id.action_cast_quiz);
        if(srClient.isConnectedToFS == true){
            activeCast.setVisible(true);
            inactiveCast.setVisible(false);
        } else {
            activeCast.setVisible(false);
            inactiveCast.setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    // initiates every element in the layout
    public void initiateElements(){
        buttonA = (Button) findViewById(R.id.buttonA);
        buttonB = (Button) findViewById(R.id.buttonB);
        buttonC = (Button) findViewById(R.id.buttonC);
        buttonD = (Button) findViewById(R.id.buttonD);

        closeButton = (ImageView) findViewById(R.id.closeButton);

        question = (TextView) findViewById(R.id.question);
        scoretext = (TextView) findViewById(R.id.score);

        quizPlayButton = findViewById(R.id.video_play_quiz);
        quizPauseButton = findViewById(R.id.video_pause_quiz);
        quizVolumeUpButton = findViewById(R.id.video_volumeUp_quiz);
        quizVolumeDownButton = findViewById(R.id.video_volumeDown_quiz);

        videoProgress = findViewById(R.id.videoProgress_quiz);
    }

    // disables the buttons to prevent the user clicking on them while not needed
    private void disableButtons(){
        buttonA.setClickable(false);
        buttonB.setClickable(false);
        buttonC.setClickable(false);
        buttonD.setClickable(false);
        closeButton.setClickable(false);
    }

    // enables the buttons
    private void enableButtons(){
        buttonA.setClickable(true);
        buttonB.setClickable(true);
        buttonC.setClickable(true);
        buttonD.setClickable(true);
        closeButton.setClickable(true);
    }

    //changes the button colour to green and updates sccore if the answer is correct
    public void changeButtonColorRight(final Button button){
        button.setBackgroundResource(R.drawable.quiz_button_right);
        disableButtons();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackgroundResource(R.drawable.quiz_button_default);
                score = score+1;
                updateScore(score);

                if(srClient != null){
                    srClient.sendMsg("score"+String.valueOf(score));
                }

                incrementQuestion();
                updateQuestion();
                enableButtons();
            }
        }, 1000);
    }

    //changes the button colour to red if the answer is wrong
    public void changeButtonColorWrong(final Button button){
        button.setBackgroundResource(R.drawable.quiz_button_wrong);
        disableButtons();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackgroundResource(R.drawable.quiz_button_default);
                incrementQuestion();
                updateQuestion();
                enableButtons();
            }
        }, 1000);
    }

    // sets the click listeners to all buttons
    public void createButtonListeners(){

        quizPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quizPlayButton.setVisibility(View.GONE);
                quizPauseButton.setVisibility(View.VISIBLE);
                if(srClient != null){
                    srClient.sendMsg("icon play quiz");
                }
            }
        });

        quizPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quizPlayButton.setVisibility(View.VISIBLE);
                quizPauseButton.setVisibility(View.GONE);
                if(srClient != null){
                    srClient.sendMsg("icon pause quiz");
                }
            }
        });

        quizVolumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(srClient != null){
                    srClient.sendMsg("icon volumeUp quiz");
                }
            }
        });

        quizVolumeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(srClient != null){
                    srClient.sendMsg("icon volumeDown quiz");
                }
            }
        });


        closeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //if ad did not finish yet, enable wildlivefox.png at InformationActivityLayout
                // remember last question so the user doesnt have to start again with the first question
                finish();
            }

        });

        buttonA.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (buttonA.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonA);
                }
                else{
                    changeButtonColorWrong(buttonA);
                    showRightAnswer();
                }
            }

        });


        buttonB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (buttonB.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonB);
                }
                else{
                    changeButtonColorWrong(buttonB);
                    showRightAnswer();
                }
            }

        });
        buttonC.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (buttonC.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonC);

                }
                else{
                    changeButtonColorWrong(buttonC);
                    showRightAnswer();
                }
            }

        });
        buttonD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (buttonD.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonD);
                }
                else{
                    changeButtonColorWrong(buttonD);
                    showRightAnswer();
                }
            }

        });
    }

    private void showRightAnswer(){
        if(buttonA.getText().toString().trim().equals(answer.trim())){
            buttonA.setBackgroundResource(R.drawable.quiz_button_right);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonA.setBackgroundResource(R.drawable.quiz_button_default);
                }
            }, 1000);
        } else if(buttonB.getText().toString().trim().equals(answer.trim())){
            buttonB.setBackgroundResource(R.drawable.quiz_button_right);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonB.setBackgroundResource(R.drawable.quiz_button_default);
                }
            }, 1000);
        } else if(buttonC.getText().toString().trim().equals(answer.trim())){
            buttonC.setBackgroundResource(R.drawable.quiz_button_right);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonC.setBackgroundResource(R.drawable.quiz_button_default);
                }
            }, 1000);
        } else if(buttonD.getText().toString().trim().equals(answer.trim())){
            buttonD.setBackgroundResource(R.drawable.quiz_button_right);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonD.setBackgroundResource(R.drawable.quiz_button_default);
                }
            }, 1000);
        }
    }

    private void incrementQuestion(){
        if(questionNumber < questionLibrary.getNumberOfQuestions()-1 ) {

            questionNumber++;
        }
        else{
            questionNumber = 0;
        }
    }

    private void updateQuestion(){
        question.setText(questionLibrary.getQuestion(questionNumber));

        buttonA.setText(questionLibrary.getChoices(questionNumber));
        buttonB.setText(questionLibrary.getChoices2(questionNumber));
        buttonC.setText(questionLibrary.getChoices3(questionNumber));
        buttonD.setText(questionLibrary.getChoices4(questionNumber));

        answer = questionLibrary.getCorrectAnswer(questionNumber);


    }

    private void updateScore(int point) {
        scoretext.setText(String.valueOf(point));
    }
}