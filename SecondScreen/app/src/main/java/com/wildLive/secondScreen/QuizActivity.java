package com.wildLive.secondScreen;

import android.content.SharedPreferences;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        score = sp.getInt("score", 0);
        questionNumber = sp.getInt("questionNumber", 0);

        Toast.makeText(QuizActivity.this, "LOADQUIZDATA - Score: "+score+" QuestionNumber: "+questionNumber, Toast.LENGTH_SHORT).show();


        //DisplayMetrics dm = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(dm);

        //int width = dm.widthPixels;
        //int height = dm.heightPixels;

        //getWindow().setLayout((int)(width*.8),(int)(height*.6));

        questionLibrary = new QuestionLibrary(QuizActivity.this);
        initiateElements();
        createButtonListeners();
        updateQuestion();
        updateScore(score);
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

        closeButton = (Button) findViewById(R.id.closeButton);

        question = (TextView) findViewById(R.id.question);
        scoretext = (TextView) findViewById(R.id.score);
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
                    score = score+1;
                    updateScore(score);
                    incrementQuestion();
                    updateQuestion();


                    //optional
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    incrementQuestion();
                    updateQuestion();

                }
            }

        });


        buttonB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // your handler code here
                if (buttonB.getText().toString().trim().equals(answer.trim())){
                    score = score+1;
                    updateScore(score);
                    incrementQuestion();
                    updateQuestion();


                    //optional
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    incrementQuestion();
                    updateQuestion();

                }
            }

        });
        buttonC.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // your handler code here
                if (buttonC.getText().toString().trim().equals(answer.trim())){
                    score = score+1;
                    updateScore(score);
                    incrementQuestion();
                    updateQuestion();


                    //optional
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    incrementQuestion();
                    updateQuestion();

                }
            }

        });
        buttonD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // your handler code here
                if (buttonD.getText().toString().trim().equals(answer.trim())){
                    score = score+1;
                    updateScore(score);

                    incrementQuestion();
                    updateQuestion();


                    //optional
                    Toast.makeText(QuizActivity.this, "correct", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(QuizActivity.this, "wrong", Toast.LENGTH_SHORT).show();
                    incrementQuestion();
                    updateQuestion();

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
        System.out.println("" + point);
    }
}