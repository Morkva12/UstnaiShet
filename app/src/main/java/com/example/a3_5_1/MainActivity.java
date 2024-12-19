package com.example.a3_5_1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.TextView;
import android.widget.EditText;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Random;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView problemTextView, timerTextView, improvementTextView;
    private EditText answerEditText;
    private MaterialButton checkButton, exitButton;

    private int number1;
    private int number2;
    private String currentOperation;
    private int correctAnswer;

    private int tasksSolved = 0;
    private int tasksCorrect = 0;
    private int tasksIncorrect = 0;

    private long startTime;
    private long totalTime;
    private long problemStartTime;

    private Timer timer;
    private boolean sessionActive = false;
    private boolean endedEarly = false;

    // Настройки
    private boolean plusEnabled, minusEnabled, multEnabled, divEnabled;
    private boolean plusDigits1, plusDigits2, plusDigits3, plusDigits4;
    private boolean minusDigits1, minusDigits2, minusDigits3, minusDigits4;
    private boolean multDigits1, multDigits2, multDigits3, multDigits4;
    private boolean divDigits1, divDigits2, divDigits3, divDigits4;
    private int totalSeconds;

    // Звуки
    private MediaPlayer correctSoundPlayer;
    private MediaPlayer wrongSoundPlayer;

    // Для оценки улучшения
    // Сохраним уровень в начале сессии и будем сравнивать после каждого решения
    private double initialLevelScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        problemTextView = findViewById(R.id.problemTextView);
        timerTextView = findViewById(R.id.timerTextView);
        answerEditText = findViewById(R.id.answerEditText);
        checkButton = findViewById(R.id.checkButton);
        exitButton = findViewById(R.id.exitButton);
        improvementTextView = findViewById(R.id.improvementTextView);

        answerEditText.requestFocus();
        answerEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(answerEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        loadSettings();
        totalTime = totalSeconds * 1000;

        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound);
        wrongSoundPlayer = MediaPlayer.create(this, R.raw.wrong_sound);

        timerTextView.setText("Осталось: " + totalSeconds + " сек");

        // Рассчитаем изначальный уровень перед началом (до решения)
        initialLevelScore = calculateCurrentLevelScore();

        startSession();

        checkButton.setOnClickListener(v -> checkAnswer());
        exitButton.setOnClickListener(v -> {
            endedEarly = true;
            endSession();
        });
    }

    private void loadSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        plusEnabled = prefs.getBoolean("op_plus", true);
        minusEnabled = prefs.getBoolean("op_minus", true);
        multEnabled = prefs.getBoolean("op_mult", true);
        divEnabled = prefs.getBoolean("op_div", false);

        plusDigits1 = prefs.getBoolean("op_plus_digits_1", true);
        plusDigits2 = prefs.getBoolean("op_plus_digits_2", true);
        plusDigits3 = prefs.getBoolean("op_plus_digits_3", true);
        plusDigits4 = prefs.getBoolean("op_plus_digits_4", false);

        minusDigits1 = prefs.getBoolean("op_minus_digits_1", true);
        minusDigits2 = prefs.getBoolean("op_minus_digits_2", true);
        minusDigits3 = prefs.getBoolean("op_minus_digits_3", false);
        minusDigits4 = prefs.getBoolean("op_minus_digits_4", false);

        multDigits1 = prefs.getBoolean("op_mult_digits_1", true);
        multDigits2 = prefs.getBoolean("op_mult_digits_2", true);
        multDigits3 = prefs.getBoolean("op_mult_digits_3", false);
        multDigits4 = prefs.getBoolean("op_mult_digits_4", false);

        divDigits1 = prefs.getBoolean("op_div_digits_1", true);
        divDigits2 = prefs.getBoolean("op_div_digits_2", true);
        divDigits3 = prefs.getBoolean("op_div_digits_3", false);
        divDigits4 = prefs.getBoolean("op_div_digits_4", false);

        totalSeconds = prefs.getInt("time_limit", 60);
    }

    private void startSession() {
        sessionActive = true;
        generateProblem();
        startTime = System.currentTimeMillis();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                long remain = totalTime - elapsed;
                runOnUiThread(() -> {
                    if (remain >= 0) {
                        timerTextView.setText("Осталось: " + remain / 1000 + " сек");
                    }
                });
                if (remain <= 0) {
                    runOnUiThread(() -> {
                        endSession();
                    });
                    this.cancel();
                }
            }
        }, 1000, 1000);
    }

    private void endSession() {
        if (!sessionActive) return;
        sessionActive = false;
        if (timer != null) {
            timer.cancel();
        }

        saveStats();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (endedEarly) {
            builder.setTitle("Сессия завершена досрочно!");
        } else {
            builder.setTitle("Время вышло!");
        }
        builder.setMessage("Верных ответов: " + tasksCorrect + "\nНеверных ответов: " + tasksIncorrect);
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void generateProblem() {
        String[] opsList = getEnabledOperations();
        if (opsList.length == 0) {
            opsList = new String[]{ "+" };
        }

        currentOperation = opsList[new Random().nextInt(opsList.length)];

        int digits = getRandomDigitsForOperation(currentOperation);
        int maxNumber = (int) Math.pow(10, digits) - 1;
        Random random = new Random();
        number1 = random.nextInt(maxNumber) + 1;
        number2 = random.nextInt(maxNumber) + 1;

        if (currentOperation.equals("/")) {
            while (number2 == 0 || number1 % number2 != 0) {
                number1 = random.nextInt(maxNumber) + 1;
                number2 = random.nextInt(maxNumber) + 1;
            }
        }

        correctAnswer = calculateAnswer(number1, number2, currentOperation);

        problemTextView.setText(number1 + " " + currentOperation + " " + number2 + " = ?");
        answerEditText.setText("");
        answerEditText.requestFocus();
        problemStartTime = System.currentTimeMillis();
    }

    private String[] getEnabledOperations() {
        int count = 0;
        if (plusEnabled) count++;
        if (minusEnabled) count++;
        if (multEnabled) count++;
        if (divEnabled) count++;

        String[] arr = new String[count];
        int idx = 0;
        if (plusEnabled) arr[idx++] = "+";
        if (minusEnabled) arr[idx++] = "-";
        if (multEnabled) arr[idx++] = "*";
        if (divEnabled) arr[idx++] = "/";

        return arr;
    }

    private int getRandomDigitsForOperation(String op) {
        // Соберем список доступных разрядностей для данной операции
        boolean d1 = false, d2 = false, d3 = false, d4 = false;
        switch (op) {
            case "+":
                d1 = plusDigits1; d2 = plusDigits2; d3 = plusDigits3; d4 = plusDigits4;
                break;
            case "-":
                d1 = minusDigits1; d2 = minusDigits2; d3 = minusDigits3; d4 = minusDigits4;
                break;
            case "*":
                d1 = multDigits1; d2 = multDigits2; d3 = multDigits3; d4 = multDigits4;
                break;
            case "/":
                d1 = divDigits1; d2 = divDigits2; d3 = divDigits3; d4 = divDigits4;
                break;
        }

        // Собираем доступные разрядности
        ArrayList<Integer> available = new ArrayList<>();
        if (d1) available.add(1);
        if (d2) available.add(2);
        if (d3) available.add(3);
        if (d4) available.add(4);

        if (available.size() == 0) {
            // Если нет ни одной разрядности включено, по умолчанию 2
            available.add(2);
        }

        return available.get(new Random().nextInt(available.size()));
    }

    private int calculateAnswer(int a, int b, String op) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                return a / b;
        }
        return 0;
    }

    private String compareWithHistory(int digitsLevel) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String historyJson = prefs.getString("history", "[]");

        int previousSolved = 0;
        int previousCorrect = 0;
        long previousTotalTime = 0;

        try {
            JSONArray historyArray = new JSONArray(historyJson);
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject task = historyArray.getJSONObject(i);
                int digits = task.getInt("digits");
                if (digits == digitsLevel) {
                    previousSolved++;
                    if (task.getBoolean("correct")) {
                        previousCorrect++;
                    }
                    previousTotalTime += task.getLong("solve_time");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Рассчитываем прошлые показатели
        double previousAccuracy = previousSolved > 0 ? (previousCorrect / (double) previousSolved) * 100 : 0;
        long previousAvgTime = previousSolved > 0 ? (previousTotalTime / previousSolved) : 0;

        // Текущие показатели
        double currentAccuracy = tasksSolved > 0 ? (tasksCorrect / (double) tasksSolved) * 100 : 0;
        long currentAvgTime = tasksSolved > 0 ? (System.currentTimeMillis() - startTime) / tasksSolved : 0;

        // Формируем вывод
        StringBuilder comparison = new StringBuilder();
;

        return comparison.toString();
    }

    private void checkAnswer() {
        if (!sessionActive) return;
        String userAnswerStr = answerEditText.getText().toString().trim();
        if (userAnswerStr.isEmpty()) return;
        try {
            int userAnswer = Integer.parseInt(userAnswerStr);
            tasksSolved++;
            long problemEndTime = System.currentTimeMillis();
            long solveTime = problemEndTime - problemStartTime;

            boolean correct = (userAnswer == correctAnswer);
            if (correct) {
                tasksCorrect++;
                if (correctSoundPlayer != null) correctSoundPlayer.start();
                updateDetailedStats(true, currentOperation, getDigitsForCurrentProblem(), solveTime);
                addTaskToHistory(number1, number2, currentOperation, userAnswer, correctAnswer, true, getDigitsForCurrentProblem(), solveTime);
                generateProblem();
            } else {
                tasksIncorrect++;
                if (wrongSoundPlayer != null) wrongSoundPlayer.start();
                updateDetailedStats(false, currentOperation, getDigitsForCurrentProblem(), solveTime);
                addTaskToHistory(number1, number2, currentOperation, userAnswer, correctAnswer, false, getDigitsForCurrentProblem(), solveTime);
                answerEditText.setText("");
                answerEditText.requestFocus();
            }

            // Покажем улучшение или ухудшение уровня
            double currentLevel = calculateCurrentLevelScore();
            double diff = currentLevel - initialLevelScore;
            String levelMessage = "";
            if (diff > 0) {
                levelMessage = "Ваш уровень улучшился на " + String.format("%.2f", diff * 100) + "% с начала сессии!";
            } else if (diff < 0) {
                levelMessage = "Ваш уровень снизился на " + String.format("%.2f", Math.abs(diff) * 100) + "% с начала сессии.";
            } else {
                levelMessage = "Уровень не изменился с начала сессии.";
            }

            // Сравнение с историей
            String historyComparison = compareWithHistory(getDigitsForCurrentProblem());
            improvementTextView.setText(levelMessage + "\n" + historyComparison);

        } catch (NumberFormatException e) {
            // Игнорируем
        }
    }



    private int getDigitsForCurrentProblem() {
        // Определим разрядность по числам
        int max = Math.max(number1, number2);
        if (max < 10) return 1;
        else if (max < 100) return 2;
        else if (max < 1000) return 3;
        else return 4;
    }

    private void addTaskToHistory(int number1, int number2, String operation, int userAnswer, int correctAnswer, boolean correct, int digits, long solveTime) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String historyJson = prefs.getString("history", "[]");

        try {
            JSONArray historyArray = new JSONArray(historyJson);
            JSONObject taskObj = new JSONObject();
            taskObj.put("number1", number1);
            taskObj.put("number2", number2);
            taskObj.put("operation", operation);
            taskObj.put("user_answer", userAnswer);
            taskObj.put("correct_answer", correctAnswer);
            taskObj.put("correct", correct);
            taskObj.put("digits", digits);
            taskObj.put("solve_time", solveTime);
            taskObj.put("timestamp", System.currentTimeMillis());
            historyArray.put(taskObj);

            prefs.edit().putString("history", historyArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDetailedStats(boolean correct, String operation, int digits, long solveTime) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        // Статистика по операциям
        int solvedOp = prefs.getInt("op_" + operation + "_solved", 0) + 1;
        editor.putInt("op_" + operation + "_solved", solvedOp);
        if (correct) {
            int correctOp = prefs.getInt("op_" + operation + "_correct", 0) + 1;
            editor.putInt("op_" + operation + "_correct", correctOp);
        }

        String digitsKey = "digits_" + digits;
        int solvedDigits = prefs.getInt(digitsKey + "_solved", 0) + 1;
        editor.putInt(digitsKey + "_solved", solvedDigits);
        if (correct) {
            int correctDigits = prefs.getInt(digitsKey + "_correct", 0) + 1;
            editor.putInt(digitsKey + "_correct", correctDigits);

            long totalCorrectTime = prefs.getLong("total_correct_time", 0) + solveTime;
            int totalCorrectForTime = prefs.getInt("total_correct_for_time", 0) + 1;
            editor.putLong("total_correct_time", totalCorrectTime);
            editor.putInt("total_correct_for_time", totalCorrectForTime);
        }

        editor.apply();
    }

    private void saveStats() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int totalSolved = prefs.getInt("total_solved", 0) + tasksSolved;
        int totalCorrect = prefs.getInt("total_correct", 0) + tasksCorrect;
        int totalIncorrect = prefs.getInt("total_incorrect", 0) + tasksIncorrect;

        prefs.edit().putInt("total_solved", totalSolved).apply();
        prefs.edit().putInt("total_correct", totalCorrect).apply();
        prefs.edit().putInt("total_incorrect", totalIncorrect).apply();

        int totalSessions = prefs.getInt("total_sessions", 0) + 1;
        prefs.edit().putInt("total_sessions", totalSessions).apply();
    }

    private double calculateCurrentLevelScore() {
        // Расчитаем уровень пользователя по текущей статистике
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int totalSolved = prefs.getInt("total_solved", 0) + tasksSolved;
        int totalCorrect = prefs.getInt("total_correct", 0) + tasksCorrect;
        int totalIncorrect = prefs.getInt("total_incorrect", 0) + tasksIncorrect;

        double accuracy = totalSolved > 0 ? (totalCorrect / (double) totalSolved) : 0.0;

        int[] digitsLevels = {1, 2, 3, 4};
        int[] solvedDigits = new int[digitsLevels.length];
        int[] correctDigits = new int[digitsLevels.length];

        for (int i = 0; i < digitsLevels.length; i++) {
            int d = digitsLevels[i];
            solvedDigits[i] = prefs.getInt("digits_" + d + "_solved", 0);
            correctDigits[i] = prefs.getInt("digits_" + d + "_correct", 0);
        }

        // Время:
        long totalCorrectTime = prefs.getLong("total_correct_time", 0);
        int totalCorrectForTime = prefs.getInt("total_correct_for_time", 0);
        double avgTimeCorrect = (totalCorrectForTime > 0) ? (totalCorrectTime / (double) totalCorrectForTime) : 0;

        // 1. Расчет сложности (complexityFactor)
        double weightedSum = 0;
        double totalWeighted = 0;
        for (int i = 0; i < digitsLevels.length; i++) {
            if (solvedDigits[i] > 0) {
                double localAcc = correctDigits[i] / (double) solvedDigits[i];
                weightedSum += localAcc * digitsLevels[i];
                totalWeighted += digitsLevels[i];
            }
        }
        double complexityFactor = totalWeighted > 0 ? (weightedSum / totalWeighted) : 0;

        // 2. Переработка временного фактора (timeFactor)
        double timeFactor = calculateTimeFactor(avgTimeCorrect, complexityFactor, totalSolved);

        // 3. Итоговый уровень
        double levelScore = accuracy * complexityFactor * Math.sqrt(timeFactor);

        return levelScore;
    }

    private double calculateTimeFactor(double avgTime, double complexityFactor, int totalSolved) {
        if (totalSolved < 5) {
            return 1.0; // Игнорируем временной фактор, если решено меньше 5 задач
        }

        // Увеличиваем допустимое время для сложных задач
        double maxTimeForLevel = 5000 + (complexityFactor * 10000); // Базовое время + 10 секунд за сложность
        double minTimeForLevel = maxTimeForLevel * 0.2; // Минимальное время - 20% от максимального

        if (avgTime <= minTimeForLevel) {
            return 1.0; // Максимальный временной фактор
        } else if (avgTime >= maxTimeForLevel) {
            return 0.5; // Минимальный временной фактор (вместо 0.0)
        } else {
            // Линейное снижение временного фактора
            return 1.0 - ((avgTime - minTimeForLevel) / (maxTimeForLevel - minTimeForLevel) * 0.5);
        }

}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (correctSoundPlayer != null) {
            correctSoundPlayer.release();
        }
        if (wrongSoundPlayer != null) {
            wrongSoundPlayer.release();
        }
    }
}
