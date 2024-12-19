package com.example.a3_5_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.google.android.material.appbar.MaterialToolbar;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarData;

import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.components.Description;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity {

    private TextView statsTextView;
    private BarChart complexityBarChart;
    private RadarChart operationsRadarChart;
    private ScrollView scrollView;
    private Button historyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_stats);

        MaterialToolbar toolbar = findViewById(R.id.statsToolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        scrollView = findViewById(R.id.scrollViewStats);
        statsTextView = findViewById(R.id.statsTextView);
        complexityBarChart = findViewById(R.id.complexityBarChart);
        operationsRadarChart = findViewById(R.id.operationsRadarChart);
        historyButton = findViewById(R.id.historyButton);

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(StatsActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        loadStats();
    }

    private void loadStats() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int totalSolved = prefs.getInt("total_solved", 0);
        int totalCorrect = prefs.getInt("total_correct", 0);
        int totalIncorrect = prefs.getInt("total_incorrect", 0);
        int totalSessions = prefs.getInt("total_sessions", 0);

        double accuracy = totalSolved > 0 ? (totalCorrect / (double) totalSolved) * 100.0 : 0.0;

        String[] ops = {"+", "-", "*", "/"};
        int[] solvedOps = new int[ops.length];
        int[] correctOps = new int[ops.length];

        for (int i = 0; i < ops.length; i++) {
            solvedOps[i] = prefs.getInt("op_" + ops[i] + "_solved", 0);
            correctOps[i] = prefs.getInt("op_" + ops[i] + "_correct", 0);
        }

        int[] digitsLevels = {1,2,3,4};
        int[] solvedDigits = new int[digitsLevels.length];
        int[] correctDigits = new int[digitsLevels.length];

        for (int i = 0; i < digitsLevels.length; i++) {
            int d = digitsLevels[i];
            solvedDigits[i] = prefs.getInt("digits_" + d + "_solved", 0);
            correctDigits[i] = prefs.getInt("digits_" + d + "_correct", 0);
        }

        long totalCorrectTime = prefs.getLong("total_correct_time", 0);
        int totalCorrectForTime = prefs.getInt("total_correct_for_time", 0);
        double avgTimeCorrect = (totalCorrectForTime > 0) ? (totalCorrectTime / (double) totalCorrectForTime) : 0;

        // Рассчет сложностного фактора
        double weightedSum = 0;
        double totalWeighted = 0;
        for (int i = 0; i < digitsLevels.length; i++) {
            if (solvedDigits[i] > 0) {
                double localAccuracy = correctDigits[i] / (double) solvedDigits[i];
                weightedSum += localAccuracy * digitsLevels[i];
                totalWeighted += digitsLevels[i];
            }
        }
        double complexityFactor = totalWeighted > 0 ? (weightedSum / totalWeighted) : 0;

        // Временной фактор:
        double timeFactor;
        if (avgTimeCorrect <= 5000) {
            timeFactor = 1.0;
        } else if (avgTimeCorrect >= 30000) {
            timeFactor = 0.0;
        } else {
            double ratio = (avgTimeCorrect - 5000) / 25000.0;
            timeFactor = 1.0 - ratio;
        }

        // Итоговый уровень
        double levelScore = 0.0;

        // Убедимся, что сложностной фактор и точность адекватны
        if (totalSolved > 0 && complexityFactor > 0) {
            levelScore = (accuracy / 100.0) * complexityFactor * timeFactor;
        }

        // Убедимся, что уровень не завышается для минимальных данных
        if (totalSolved <= 5) {
            levelScore *= 0.5; // Уменьшаем итоговый уровень для малой выборки
        }

        String levelInterpretation;
        if (levelScore < 0.3) {
            levelInterpretation = "Уровень: Новичок\nНужно улучшать и точность, и скорость, и решать более сложные задачи!";
        } else if (levelScore < 0.6) {
            levelInterpretation = "Уровень: Средний\nСредняя точность и сложность, стоит поднажать на скорость или сложность.";
        } else if (levelScore < 0.8) {
            levelInterpretation = "Уровень: Продвинутый\nХороший баланс скорости, точности и сложности!";
        } else {
            levelInterpretation = "Уровень: Эксперт\nОтличная точность, хорошая скорость и высокая сложность задач!";
        }


        String stats = "Всего сессий: " + totalSessions + "\n"
                + "Всего задач решено: " + totalSolved + "\n"
                + "Верно решено: " + totalCorrect + "\n"
                + "Неверно решено: " + totalIncorrect + "\n"
                + String.format("Точность: %.2f%%\n", accuracy)
                + String.format("Среднее время для верных (мс): %.2f\n", avgTimeCorrect)
                + String.format("Сложностной фактор: %.2f\n", complexityFactor)
                + String.format("Временной фактор: %.2f\n", timeFactor)
                + String.format("Итоговый уровень: %.2f\n", levelScore)
                + "\n" + levelInterpretation;

        statsTextView.setText(stats);

        setupComplexityBarChart(digitsLevels, solvedDigits, correctDigits);
        setupOperationsRadarChart(ops, solvedOps, correctOps);
    }

    private void setupComplexityBarChart(int[] digitsLevels, int[] solved, int[] correct) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < digitsLevels.length; i++) {
            float acc = 0f;
            if (solved[i] > 0) {
                acc = (float) correct[i] / (float) solved[i] * 100f;
            }
            entries.add(new BarEntry(i, acc));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Точность по сложности (%)");
        dataSet.setColor(Color.CYAN);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        complexityBarChart.setData(data);
        complexityBarChart.setDrawGridBackground(false);
        complexityBarChart.getAxisLeft().setTextColor(Color.WHITE);
        complexityBarChart.getAxisRight().setEnabled(false);
        complexityBarChart.getXAxis().setTextColor(Color.WHITE);
        complexityBarChart.getLegend().setTextColor(Color.WHITE);
        complexityBarChart.getDescription().setEnabled(false);

        String[] labels = new String[]{"1 разряд", "2 разряда", "3 разряда", "4 разряда"};
        complexityBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        complexityBarChart.getXAxis().setGranularity(1f);
        complexityBarChart.getXAxis().setGranularityEnabled(true);
        complexityBarChart.getXAxis().setLabelCount(labels.length);

        complexityBarChart.animateY(1000);
        complexityBarChart.invalidate();
    }

    private void setupOperationsRadarChart(String[] ops, int[] solvedOps, int[] correctOps) {
        ArrayList<RadarEntry> entries = new ArrayList<>();
        for (int i = 0; i < ops.length; i++) {
            float acc = 0f;
            if (solvedOps[i] > 0) {
                acc = (float) correctOps[i] / (float) solvedOps[i] * 100f;
            }
            entries.add(new RadarEntry(acc));
        }

        RadarDataSet dataSet = new RadarDataSet(entries, "Точность по операциям (%)");
        dataSet.setColor(Color.MAGENTA);
        dataSet.setFillColor(Color.MAGENTA);
        dataSet.setDrawFilled(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        RadarData data = new RadarData(dataSet);
        data.setDrawValues(true);

        operationsRadarChart.setData(data);
        operationsRadarChart.setBackgroundColor(Color.TRANSPARENT);
        operationsRadarChart.setWebColor(Color.WHITE);
        operationsRadarChart.setWebColorInner(Color.WHITE);
        operationsRadarChart.setWebLineWidth(1f);
        operationsRadarChart.setWebLineWidthInner(1f);
        operationsRadarChart.setSkipWebLineCount(0);

        Description desc = new Description();
        desc.setText("Операции");
        desc.setTextColor(Color.WHITE);
        operationsRadarChart.setDescription(desc);

        operationsRadarChart.getLegend().setTextColor(Color.WHITE);

        XAxis xAxis = operationsRadarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(ops));
        xAxis.setTextColor(Color.WHITE);

        YAxis yAxis = operationsRadarChart.getYAxis();
        yAxis.setTextColor(Color.WHITE);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);

        operationsRadarChart.animateXY(1000, 1000);
        operationsRadarChart.invalidate();
    }
}
