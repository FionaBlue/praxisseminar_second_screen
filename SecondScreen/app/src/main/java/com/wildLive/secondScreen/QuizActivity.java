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
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {
    private QuestionLibrary questionLibrary;
    private Handler handler = new Handler();

    public Button buttonA;
    public Button buttonB;
    public Button buttonC;
    public Button buttonD;
    private ImageView closeButton;

    private TextView question;
    private TextView scoretext;

    private String answer;
    private int score = 0;
    private int questionNumber = 0;

    private SignalRClient srClient = null;

    static QuizActivity quizActivity;

    public ImageView quizPauseButton;
    public ImageView quizPlayButton;
    private ImageView quizVolumeUpButton;
    private ImageView quizVolumeDownButton;
    private SeekBar videoProgress;

    private MenuItem activeCast;
    private MenuItem inactiveCast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quizActivity = this;

        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        score = sp.getInt("score", 0);
        questionNumber = sp.getInt("questionNumber", 0);

        //Toast.makeText(QuizActivity.this, "LOADQUIZDATA - Score: "+score+" QuestionNumber: "+questionNumber, Toast.LENGTH_SHORT).show();

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

    private void saveQuizData(){
        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("score",score);

        //Toast.makeText(QuizActivity.this, "SAVEQUIZDATA - Score: "+score+" QuestionNumber: "+questionNumber, Toast.LENGTH_SHORT).show();

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

    private void disableButtons(){
        buttonA.setClickable(false);
        buttonB.setClickable(false);
        buttonC.setClickable(false);
        buttonD.setClickable(false);
        closeButton.setClickable(false);
    }

    private void enableButtons(){
        buttonA.setClickable(true);
        buttonB.setClickable(true);
        buttonC.setClickable(true);
        buttonD.setClickable(true);
        closeButton.setClickable(true);
    }

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
                // your handler code here
                if (buttonA.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonA);
                    //optional
                    //Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    //Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    changeButtonColorWrong(buttonA);
                    showRightAnswer();
                }
            }

        });


        buttonB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // your handler code here
                if (buttonB.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonB);
                    //optional
                    //Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    //Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    changeButtonColorWrong(buttonB);
                    showRightAnswer();
                }
            }

        });
        buttonC.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // your handler code here
                if (buttonC.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonC);
                    //optional
                    //Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    changeButtonColorWrong(buttonC);
                    showRightAnswer();
                    //Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                }
            }

        });
        buttonD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // your handler code here
                if (buttonD.getText().toString().trim().equals(answer.trim())){
                    changeButtonColorRight(buttonD);
                    //optional
                    //Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    changeButtonColorWrong(buttonD);
                    showRightAnswer();
                    //Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void showRightAnswer(){
        if(buttonA.getText().toString().trim().equals(answer.trim())){
            changeButtonColorRight(buttonA);
        } else if(buttonB.getText().toString().trim().equals(answer.trim())){
            changeButtonColorRight(buttonB);
        } else if(buttonC.getText().toString().trim().equals(answer.trim())){
            changeButtonColorRight(buttonC);
        } else if(buttonD.getText().toString().trim().equals(answer.trim())){
            changeButtonColorRight(buttonD);
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

        //Toast.makeText(QuizActivity.this, "CurrentQuestionNumber: "+questionNumber, Toast.LENGTH_SHORT).show();

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