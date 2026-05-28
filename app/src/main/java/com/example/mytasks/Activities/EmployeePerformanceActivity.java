package com.example.mytasks.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytasks.AppDatabase;
import com.example.mytasks.R;

public class EmployeePerformanceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_performance);

        int activeProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUsername = pref.getString("LOGGED_IN_USERNAME", "");

        loadPerformanceData(activeProjectId, currentUsername);
    }

    private void loadPerformanceData(int projectId, String username) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int total = db.taskDao().getEmployeeTaskCount(projectId, username);
            int completed = db.taskDao().getEmployeeCompletedTaskCount(projectId, username);
            
            int rate = 0;
            if (total > 0) {
                rate = (completed * 100) / total;
            }

            final int finalRate = rate;
            runOnUiThread(() -> {
                TextView tvTotal = findViewById(R.id.tvTotalTasks);
                TextView tvCompleted = findViewById(R.id.tvCompletedTasks);
                TextView tvRate = findViewById(R.id.tvCompletionRate);
                ProgressBar pb = findViewById(R.id.pbCompletion);

                tvTotal.setText("Your Total Assigned Tasks: " + total);
                tvCompleted.setText("Completed Tasks: " + completed);
                tvRate.setText("Your Task Completion Rate: " + finalRate + "%");
                pb.setProgress(finalRate);
            });
        });
    }
}
