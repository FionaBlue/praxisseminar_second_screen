package com.wildLive.secondScreen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// AlertActivity shows user-feedback when no internet connection is available, see InternetConnectionHandler
public class AlertActivity extends Activity {

    private Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        //Button for closing Activity
        okButton = findViewById(R.id.alertButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
