package com.example.dodo.quick_quizz;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;

public class QuestionLibrary {

    Context mContext;
    JSONArray  questions;

    public QuestionLibrary(Context context)
    {
        mContext = context;

        String jsonQuestions = loadJSONFromAsset();

        try {
            questions = new JSONArray (jsonQuestions);
            System.out.println("GOT THE FOLLOWING QUESTIONS FROM JSON:");
            System.out.println(questions);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("quizkatalog.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String getQuestion(int a){

        try {
            return questions.getJSONObject(a).getString("question");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

    }

    public String getChoices(int a){
        try {
            return questions.getJSONObject(a).getString("answerA");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getChoices2(int a){
        try {
            return questions.getJSONObject(a).getString("answerB");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getChoices3(int a){
        try {
            return questions.getJSONObject(a).getString("answerC");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getChoices4(int a){
        try {
            return questions.getJSONObject(a).getString("answerD");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getCorrectAnswer(int a){
        try {
            return questions.getJSONObject(a).getString("correctAnswer");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public int getNumberOfQuestions(){
        return questions.length();
    }
}
