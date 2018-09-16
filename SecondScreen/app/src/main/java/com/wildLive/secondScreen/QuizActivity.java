package com.wildLive.secondScreen;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {
    private QuestionLibrary questionLibrary;
    private Handler handler = new Handler();

    private Button buttonA;
    private Button buttonB;
    private Button buttonC;
    private Button buttonD;

    private Button closeButton;

    private TextView question;
    private TextView scoretext;

    private String answer;
    private int score = 0;
    private int questionNumber = 0;

    private int standardButtonColor;

    private SignalRClient srClient = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        score = sp.getInt("score", 0);
        questionNumber = sp.getInt("questionNumber", 0);

        Toast.makeText(QuizActivity.this, "LOADQUIZDATA - Score: "+score+" QuestionNumber: "+questionNumber, Toast.LENGTH_SHORT).show();

        WildLive app = (WildLive) getApplication();
        srClient = app.getSRClient();

        questionLibrary = new QuestionLibrary(QuizActivity.this);
        initiateElements();
        createButtonListeners();
        updateQuestion();
        updateScore(score);

        if(srClient != null){
            srClient.sendMsg("score"+String.valueOf(score));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        saveQuizData();
    }

    private void saveQuizData(){
        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("score",score);

        Toast.makeText(QuizActivity.this, "SAVEQUIZDATA - Score: "+score+" QuestionNumber: "+questionNumber, Toast.LENGTH_SHORT).show();

        edit.putInt("questionNumber",questionNumber);
        edit.apply();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.quiz, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void initiateElements(){
        buttonA = (Button) findViewById(R.id.buttonA);
        buttonB = (Button) findViewById(R.id.buttonB);
        buttonC = (Button) findViewById(R.id.buttonC);
        buttonD = (Button) findViewById(R.id.buttonD);

        standardButtonColor = ((ColorDrawable)buttonA.getBackground()).getColor();

        closeButton = (Button) findViewById(R.id.closeButton);

        question = (TextView) findViewById(R.id.question);
        scoretext = (TextView) findViewById(R.id.score);
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
        button.setBackgroundColor(Color.GREEN);
        disableButtons();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackgroundColor(standardButtonColor);
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
        button.setBackgroundColor(Color.RED);
        disableButtons();
        handler.postDelayed(new Runnable() {
            public void run() {
                button.setBackgroundColor(standardButtonColor);
                incrementQuestion();
                updateQuestion();
                enableButtons();
            }
        }, 1000);
    }

    public void createButtonListeners(){
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
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    changeButtonColorWrong(buttonA);
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
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    changeButtonColorWrong(buttonB);
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
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    changeButtonColorWrong(buttonC);
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    changeButtonColorWrong(buttonD);
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                }
            }

        });

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

        Toast.makeText(QuizActivity.this, "CurrentQuestionNumber: "+questionNumber, Toast.LENGTH_SHORT).show();

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