package com.example.a3_5_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.TextView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<SolvedTask> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.historyToolbar);
        setSupportActionBar(toolbar);

        // Включаем кнопку "Назад" и задаем заголовок
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("История"); // Явно задаем заголовок
        }

        recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
        adapter = new HistoryAdapter(tasks);
        recyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String historyJson = prefs.getString("history", "[]");
        try {
            JSONArray arr = new JSONArray(historyJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                SolvedTask t = new SolvedTask();
                t.number1 = obj.getInt("number1");
                t.number2 = obj.getInt("number2");
                t.operation = obj.getString("operation");
                t.userAnswer = obj.getInt("user_answer");
                t.correctAnswer = obj.getInt("correct_answer");
                t.correct = obj.getBoolean("correct");
                t.digits = obj.getInt("digits");
                t.solveTime = obj.getLong("solve_time");
                t.timestamp = obj.getLong("timestamp");

                // Добавляем в начало списка
                tasks.add(0, t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Класс для хранения задачи
    class SolvedTask {
        int number1;
        int number2;
        String operation;
        int userAnswer;
        int correctAnswer;
        boolean correct;
        int digits;
        long solveTime;
        long timestamp;
    }

    // Адаптер для отображения списка
    class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {
        private List<SolvedTask> items;

        HistoryAdapter(List<SolvedTask> tasks) {
            this.items = tasks;
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = getLayoutInflater().inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            SolvedTask t = items.get(position);
            String correctness = t.correct ? "Верно" : "Неверно";
            String text = String.format(
                    "%d %s %d = %d (%s)\nРазрядность: %d\nВремя: %d мс",
                    t.number1, t.operation, t.number2, t.correctAnswer, correctness, t.digits, t.solveTime);
            if (!t.correct) {
                text += "\nВаш ответ: " + t.userAnswer;
            }
            holder.textView.setText(text);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public HistoryViewHolder(android.view.View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.historyItemText);
        }
    }
}
