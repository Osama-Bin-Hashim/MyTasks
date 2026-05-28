package com.example.mytasks.Activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytasks.AppDatabase;
import com.example.mytasks.R;
import com.example.mytasks.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerPerformanceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_performance);

        // SECURITY SHIELD
        boolean isManager = getIntent().getBooleanExtra("IS_MANAGER", false);
        if (!isManager) {
            Toast.makeText(this, "Access Denied: Managers Only.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int activeProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
        loadProjectAnalytics(activeProjectId);
    }

    private void loadProjectAnalytics(int projectId) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Global Metrics
            int totalTasks = db.taskDao().getTaskCountByProject(projectId);
            int completedTasks = db.taskDao().getCompletedTaskCountByProject(projectId);
            int activeTasks = totalTasks - completedTasks;
            
            int globalRate = 0;
            if (totalTasks > 0) {
                globalRate = (completedTasks * 100) / totalTasks;
            }

            // 2. Leaderboard Logic
            List<Task> allTasks = db.taskDao().getTasksByProject(projectId);
            Map<String, Integer> completionMap = new HashMap<>();
            
            for (Task task : allTasks) {
                if ("DONE".equals(task.status) && task.assigneeId != null) {
                    // Extract individual names from comma-separated list
                    String[] names = task.assigneeId.split(", ");
                    for (String name : names) {
                        Integer current = completionMap.get(name);
                        completionMap.put(name, (current == null ? 0 : current) + 1);
                    }
                }
            }

            String topUser = "None";
            int maxCompletions = 0;
            for (Map.Entry<String, Integer> entry : completionMap.entrySet()) {
                if (entry.getValue() > maxCompletions) {
                    maxCompletions = entry.getValue();
                    topUser = entry.getKey();
                }
            }

            final int finalRate = globalRate;
            final String finalTopUser = topUser;
            final int finalMax = maxCompletions;
            final int finalCompleted = completedTasks;
            final int finalActive = activeTasks;

            runOnUiThread(() -> {
                TextView tvRate = findViewById(R.id.tvGlobalProgress);
                ProgressBar pb = findViewById(R.id.pbGlobalProgress);
                TextView tvSummary = findViewById(R.id.tvTaskSummary);
                TextView tvContributor = findViewById(R.id.tvTopContributor);
                TextView tvContributorStats = findViewById(R.id.tvTopContributorStats);

                tvRate.setText("Global Progress: " + finalRate + "%");
                pb.setProgress(finalRate);
                tvSummary.setText("Total Active: " + finalActive + " | Total Closed: " + finalCompleted);
                tvContributor.setText("Top Contributor: " + finalTopUser);
                tvContributorStats.setText("Tasks Completed: " + finalMax);
            });
        });
    }
}
