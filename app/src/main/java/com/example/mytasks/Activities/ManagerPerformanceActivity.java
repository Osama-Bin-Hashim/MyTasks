package com.example.mytasks.Activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytasks.Adapters.RosterAdapter;
import com.example.mytasks.AppDatabase;
import com.example.mytasks.Models.RosterStats;
import com.example.mytasks.Project;
import com.example.mytasks.R;
import com.example.mytasks.Task;
import com.example.mytasks.User;

import java.util.ArrayList;
import java.util.List;

public class ManagerPerformanceActivity extends AppCompatActivity {
    
    private int activeProjectId;
    private Project activeProject;
    private RosterAdapter rosterAdapter;
    
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

        activeProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
        
        setupRecyclerView();
        loadProjectAnalytics(activeProjectId);
        setupRosterEnrollment();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvRosterPerformance);
        rosterAdapter = new RosterAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(rosterAdapter);
    }

    private void loadProjectAnalytics(int projectId) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 0. Fetch Project for Roster
            activeProject = db.projectDao().getProjectById(projectId);
            
            // 1. Global Metrics
            int totalTasksCount = db.taskDao().getTaskCountByProject(projectId);
            int completedTasksCount = db.taskDao().getCompletedTaskCountByProject(projectId);
            int activeTasks = totalTasksCount - completedTasksCount;
            
            int globalRate = 0;
            if (totalTasksCount > 0) {
                globalRate = (completedTasksCount * 100) / totalTasksCount;
            }

            // 2. Roster Performance Aggregation
            List<RosterStats> performanceList = new ArrayList<>();
            if (activeProject != null && activeProject.projectRoster != null && !activeProject.projectRoster.isEmpty()) {
                String[] memberNames = activeProject.projectRoster.split(", ");
                List<Task> allProjectTasks = db.taskDao().getTasksByProject(projectId);
                
                for (String username : memberNames) {
                    int userTotal = 0;
                    int userCompleted = 0;
                    
                    for (Task task : allProjectTasks) {
                        if (task.assigneeId != null && task.assigneeId.contains(username)) {
                            userTotal++;
                            if ("DONE".equals(task.status)) {
                                userCompleted++;
                            }
                        }
                    }
                    performanceList.add(new RosterStats(username, userTotal, userCompleted));
                }
            }

            final int finalRate = globalRate;
            final int finalCompleted = completedTasksCount;
            final int finalActive = activeTasks;
            final String rosterText = (activeProject != null) ? activeProject.projectRoster : "";

            runOnUiThread(() -> {
                TextView tvRate = findViewById(R.id.tvGlobalProgress);
                ProgressBar pb = findViewById(R.id.pbGlobalProgress);
                TextView tvSummary = findViewById(R.id.tvTaskSummary);
                TextView tvRoster = findViewById(R.id.tvRosterList);

                tvRate.setText("Global Progress: " + finalRate + "%");
                pb.setProgress(finalRate);
                tvSummary.setText("Total Active: " + finalActive + " | Total Closed: " + finalCompleted);
                tvRoster.setText("Team: " + (rosterText == null || rosterText.isEmpty() ? "No members enrolled" : rosterText));
                
                rosterAdapter.setRosterStatsList(performanceList);
            });
        });
    }

    private void setupRosterEnrollment() {
        EditText inputUsername = findViewById(R.id.inputNewMemberUsername);
        findViewById(R.id.btnAddMemberToProject).setOnClickListener(v -> {
            String username = inputUsername.getText().toString().trim();
            if (username.isEmpty()) {
                inputUsername.setError("Username required");
                return;
            }

            verifyAndAddMember(username);
        });
    }

    private void verifyAndAddMember(String username) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = db.userDao().getUserByUsername(username);
            if (user == null) {
                runOnUiThread(() -> Toast.makeText(this, "User does not exist!", Toast.LENGTH_SHORT).show());
                return;
            }

            if (activeProject != null) {
                String currentRoster = activeProject.projectRoster;
                if (currentRoster == null) currentRoster = "";
                
                if (currentRoster.contains(username)) {
                    runOnUiThread(() -> Toast.makeText(this, "User already in roster", Toast.LENGTH_SHORT).show());
                    return;
                }

                if (!currentRoster.isEmpty()) {
                    currentRoster += ", ";
                }
                currentRoster += username;
                activeProject.projectRoster = currentRoster;

                db.projectDao().updateProject(activeProject);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, username + " enrolled successfully!", Toast.LENGTH_SHORT).show();
                    ((EditText)findViewById(R.id.inputNewMemberUsername)).setText("");
                    loadProjectAnalytics(activeProjectId);
                });
            }
        });
    }
}
