package com.example.a3_5_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox plusCheck, minusCheck, multCheck, divCheck;
    private CheckBox plusDigits1Check, plusDigits2Check, plusDigits3Check, plusDigits4Check;
    private CheckBox minusDigits1Check, minusDigits2Check, minusDigits3Check, minusDigits4Check;
    private CheckBox multDigits1Check, multDigits2Check, multDigits3Check, multDigits4Check;
    private CheckBox divDigits1Check, divDigits2Check, divDigits3Check, divDigits4Check;

    private CheckBox reminderCheck;
    private TextView reminderTimeTextView;
    private EditText timeLimitEditText;

    private int reminderHour = 9;
    private int reminderMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.settingsToolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        plusCheck = findViewById(R.id.plusCheck);
        minusCheck = findViewById(R.id.minusCheck);
        multCheck = findViewById(R.id.multCheck);
        divCheck = findViewById(R.id.divCheck);

        plusDigits1Check = findViewById(R.id.plusDigits1Check);
        plusDigits2Check = findViewById(R.id.plusDigits2Check);
        plusDigits3Check = findViewById(R.id.plusDigits3Check);
        plusDigits4Check = findViewById(R.id.plusDigits4Check);

        minusDigits1Check = findViewById(R.id.minusDigits1Check);
        minusDigits2Check = findViewById(R.id.minusDigits2Check);
        minusDigits3Check = findViewById(R.id.minusDigits3Check);
        minusDigits4Check = findViewById(R.id.minusDigits4Check);

        multDigits1Check = findViewById(R.id.multDigits1Check);
        multDigits2Check = findViewById(R.id.multDigits2Check);
        multDigits3Check = findViewById(R.id.multDigits3Check);
        multDigits4Check = findViewById(R.id.multDigits4Check);

        divDigits1Check = findViewById(R.id.divDigits1Check);
        divDigits2Check = findViewById(R.id.divDigits2Check);
        divDigits3Check = findViewById(R.id.divDigits3Check);
        divDigits4Check = findViewById(R.id.divDigits4Check);

        reminderCheck = findViewById(R.id.reminderCheck);
        reminderTimeTextView = findViewById(R.id.reminderTimeTextView);
        timeLimitEditText = findViewById(R.id.timeLimitEditText);

        reminderTimeTextView.setOnClickListener(v -> showTimePickerDialog());

        loadSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    private void showTimePickerDialog() {
        int hour = reminderHour;
        int minute = reminderMinute;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    reminderHour = hourOfDay;
                    reminderMinute = minuteOfHour;
                    updateReminderTimeText(reminderHour, reminderMinute);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void updateReminderTimeText(int h, int m) {
        String timeStr = String.format("%02d:%02d", h, m);
        reminderTimeTextView.setText("Время напоминания: " + timeStr);
    }

    private void loadSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        plusCheck.setChecked(prefs.getBoolean("op_plus", true));
        minusCheck.setChecked(prefs.getBoolean("op_minus", true));
        multCheck.setChecked(prefs.getBoolean("op_mult", true));
        divCheck.setChecked(prefs.getBoolean("op_div", false));

        plusDigits1Check.setChecked(prefs.getBoolean("op_plus_digits_1", true));
        plusDigits2Check.setChecked(prefs.getBoolean("op_plus_digits_2", true));
        plusDigits3Check.setChecked(prefs.getBoolean("op_plus_digits_3", true));
        plusDigits4Check.setChecked(prefs.getBoolean("op_plus_digits_4", false));

        minusDigits1Check.setChecked(prefs.getBoolean("op_minus_digits_1", true));
        minusDigits2Check.setChecked(prefs.getBoolean("op_minus_digits_2", true));
        minusDigits3Check.setChecked(prefs.getBoolean("op_minus_digits_3", false));
        minusDigits4Check.setChecked(prefs.getBoolean("op_minus_digits_4", false));

        multDigits1Check.setChecked(prefs.getBoolean("op_mult_digits_1", true));
        multDigits2Check.setChecked(prefs.getBoolean("op_mult_digits_2", true));
        multDigits3Check.setChecked(prefs.getBoolean("op_mult_digits_3", false));
        multDigits4Check.setChecked(prefs.getBoolean("op_mult_digits_4", false));

        divDigits1Check.setChecked(prefs.getBoolean("op_div_digits_1", true));
        divDigits2Check.setChecked(prefs.getBoolean("op_div_digits_2", true));
        divDigits3Check.setChecked(prefs.getBoolean("op_div_digits_3", false));
        divDigits4Check.setChecked(prefs.getBoolean("op_div_digits_4", false));

        int time_limit = prefs.getInt("time_limit", 60);
        timeLimitEditText.setText(String.valueOf(time_limit));

        boolean reminderEnabled = prefs.getBoolean("reminder_enabled", false);
        reminderCheck.setChecked(reminderEnabled);
        reminderHour = prefs.getInt("reminder_hour", 9);
        reminderMinute = prefs.getInt("reminder_minute", 0);
        updateReminderTimeText(reminderHour, reminderMinute);

        reminderTimeTextView.setEnabled(reminderEnabled);
    }

    private void saveSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("op_plus", plusCheck.isChecked());
        editor.putBoolean("op_minus", minusCheck.isChecked());
        editor.putBoolean("op_mult", multCheck.isChecked());
        editor.putBoolean("op_div", divCheck.isChecked());

        editor.putBoolean("op_plus_digits_1", plusDigits1Check.isChecked());
        editor.putBoolean("op_plus_digits_2", plusDigits2Check.isChecked());
        editor.putBoolean("op_plus_digits_3", plusDigits3Check.isChecked());
        editor.putBoolean("op_plus_digits_4", plusDigits4Check.isChecked());

        editor.putBoolean("op_minus_digits_1", minusDigits1Check.isChecked());
        editor.putBoolean("op_minus_digits_2", minusDigits2Check.isChecked());
        editor.putBoolean("op_minus_digits_3", minusDigits3Check.isChecked());
        editor.putBoolean("op_minus_digits_4", minusDigits4Check.isChecked());

        editor.putBoolean("op_mult_digits_1", multDigits1Check.isChecked());
        editor.putBoolean("op_mult_digits_2", multDigits2Check.isChecked());
        editor.putBoolean("op_mult_digits_3", multDigits3Check.isChecked());
        editor.putBoolean("op_mult_digits_4", multDigits4Check.isChecked());

        editor.putBoolean("op_div_digits_1", divDigits1Check.isChecked());
        editor.putBoolean("op_div_digits_2", divDigits2Check.isChecked());
        editor.putBoolean("op_div_digits_3", divDigits3Check.isChecked());
        editor.putBoolean("op_div_digits_4", divDigits4Check.isChecked());

        int timeLimit = 60;
        try {
            timeLimit = Integer.parseInt(timeLimitEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            timeLimit = 60;
        }
        editor.putInt("time_limit", timeLimit);

        boolean reminderEnabled = reminderCheck.isChecked();
        editor.putBoolean("reminder_enabled", reminderEnabled);
        editor.putInt("reminder_hour", reminderHour);
        editor.putInt("reminder_minute", reminderMinute);

        editor.apply();
        setReminder(reminderEnabled);
    }

    private void setReminder(boolean enabled) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (enabled) {
            long triggerTime = getTriggerTime(reminderHour, reminderMinute);
            if (alarmManager != null) {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            }
        } else {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private long getTriggerTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.getTimeInMillis();
    }
}
