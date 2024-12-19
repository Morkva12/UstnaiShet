package com.example.a3_5_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button startButton, settingsButton, statsButton;
    private TextView appTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_start);

        appTitleTextView = findViewById(R.id.appTitleTextView);
        startButton = findViewById(R.id.startButton);
        settingsButton = findViewById(R.id.settingsButton);
        statsButton = findViewById(R.id.statsButton);

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, StatsActivity.class);
            startActivity(intent);
        });
    }
}
