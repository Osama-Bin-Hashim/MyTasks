package com.example.mytasks.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytasks.AppDatabase;
import com.example.mytasks.LoginActivity;
import com.example.mytasks.Project;
import com.example.mytasks.CreateTaskActivity;
import com.example.mytasks.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<Project> projectsList = new ArrayList<>();
    private int savedProjectSelectionId = -1;
    private int currentSessionUserId = -1;
    private String currentSessionUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // IDENTITY PERSISTENCE: Read session ID
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentSessionUserId = pref.getInt("LOGGED_IN_USER_ID", -1);
        currentSessionUsername = pref.getString("LOGGED_IN_USERNAME", "");

        // SECURITY GUARD: Redirect to login if no valid session
        if (currentSessionUserId == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkspace();
    }

    private void loadWorkspace() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Fetch All Projects
            List<Project> allProjects = db.projectDao().getAllProjects();
            List<Project> filteredProjects = new ArrayList<>();

            // PROJECT FILTERING: Managers or Assigned Employees only
            for (Project project : allProjects) {
                if (currentSessionUserId == project.managerId) {
                    filteredProjects.add(project);
                } else {
                    // Check if assigned to any task in this project
                    List<com.example.mytasks.Task> projectTasks = db.taskDao().getTasksByProject(project.id);
                    for (com.example.mytasks.Task task : projectTasks) {
                        if (task.assigneeId != null && task.assigneeId.contains(currentSessionUsername)) {
                            filteredProjects.add(project);
                            break;
                        }
                    }
                }
            }
            
            projectsList = filteredProjects;

            runOnUiThread(() -> {
                if (projectsList.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    populateProjectSpinner();
                }
            });
        });
    }

    private void showEmptyState() {
        binding.projectCard.setVisibility(View.GONE);
        binding.menuGrid.setVisibility(View.GONE);
        binding.emptyStateContainer.setVisibility(View.VISIBLE);
        binding.addTask.setVisibility(View.VISIBLE); // Keep FAB for project creation
    }

    private void hideEmptyState() {
        binding.projectCard.setVisibility(View.VISIBLE);
        binding.menuGrid.setVisibility(View.VISIBLE);
        binding.emptyStateContainer.setVisibility(View.GONE);
    }

    private void populateProjectSpinner() {
        List<String> displayNames = new ArrayList<>();
        for (Project p : projectsList) {
            displayNames.add(p.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, displayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.projectSpinner.setAdapter(adapter);

        // RESTORE STATE: Find matched index and set selection
        if (savedProjectSelectionId != -1) {
            for (int i = 0; i < projectsList.size(); i++) {
                if (projectsList.get(i).id == savedProjectSelectionId) {
                    binding.projectSpinner.setSelection(i, false);
                    break;
                }
            }
        }

        binding.projectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Actual project selected
                Project selected = projectsList.get(position);
                savedProjectSelectionId = selected.id;

                // INJECT SYSTEM LOGS: Security Audit
                Log.d("SECURITY_AUDIT", "Current Logged-in User ID: " + currentSessionUserId);
                Log.d("SECURITY_AUDIT", "Selected Project Manager ID: " + selected.managerId);

                // ENFORCE STRICT VISIBILITY RE-EVALUATION
                boolean isCurrentUserManager = (currentSessionUserId == selected.managerId);
                Log.d("SECURITY_AUDIT", "Is User Manager? Answer: " + isCurrentUserManager);

                if (isCurrentUserManager) {
                    // Active Manager Mode
                    binding.addTask.setVisibility(View.VISIBLE);
                } else {
                    // Strict Restricted Employee Mode
                    binding.addTask.setVisibility(View.GONE);
                }

                evaluateUserRole(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void evaluateUserRole(Project selectedProject) {
        boolean isManager = (currentSessionUserId == selectedProject.managerId);

        runOnUiThread(() -> {
            if (isManager) {
                binding.addTask.setVisibility(View.VISIBLE);
            } else {
                binding.addTask.setVisibility(View.GONE);
            }
            
            // Update Toast for tracking
            Toast.makeText(MainActivity.this, 
                "Active Project: " + selectedProject.name + " (ID: " + selectedProject.id + ")", 
                Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            pref.edit().clear().apply(); // Wipe out user session identity data

            // Route securely back to Auth Flow
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnCreateProjectInline.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateProjectActivity.class);
            intent.putExtra("USER_ID", currentSessionUserId);
            startActivity(intent);
        });

        binding.todoOption.setOnClickListener(v -> {
            int selectedPosition = binding.projectSpinner.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION && !projectsList.isEmpty()) {
                Project selected = projectsList.get(selectedPosition);
                boolean isCurrentUserManager = (currentSessionUserId == selected.managerId);
                Intent intent = new Intent(this, TodoWorkspaceActivity.class);
                intent.putExtra("PROJECT_ID", selected.id);
                intent.putExtra("IS_MANAGER", isCurrentUserManager);
                intent.putExtra("LOGGED_IN_USERNAME", currentSessionUsername);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a project first", Toast.LENGTH_SHORT).show();
            }
        });

        binding.noticesOption.setOnClickListener(v -> {
            Toast.makeText(this, "Notices Module Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.dashboardOption.setOnClickListener(v -> {
            int selectedPosition = binding.projectSpinner.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION && !projectsList.isEmpty()) {
                Project selected = projectsList.get(selectedPosition);
                boolean isCurrentUserManager = (currentSessionUserId == selected.managerId);
                
                Intent intent;
                if (isCurrentUserManager) {
                    intent = new Intent(this, ManagerPerformanceActivity.class);
                } else {
                    intent = new Intent(this, EmployeePerformanceActivity.class);
                }
                intent.putExtra("PROJECT_ID", selected.id);
                intent.putExtra("IS_MANAGER", isCurrentUserManager);
                startActivity(intent);
            }
        });

        binding.cardRequests.setOnClickListener(v -> {
            int selectedPosition = binding.projectSpinner.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION && !projectsList.isEmpty()) {
                Project selected = projectsList.get(selectedPosition);
                boolean isCurrentUserManager = (currentSessionUserId == selected.managerId);
                
                Intent intent;
                if (isCurrentUserManager) {
                    intent = new Intent(this, ManagerRequestsActivity.class);
                } else {
                    intent = new Intent(this, EmployeeRequestsActivity.class);
                }
                intent.putExtra("PROJECT_ID", selected.id);
                intent.putExtra("IS_MANAGER", isCurrentUserManager);
                startActivity(intent);
            }
        });

        binding.addTask.setOnClickListener(v -> {
            int selectedPosition = binding.projectSpinner.getSelectedItemPosition();
            
            if (projectsList.isEmpty()) {
                // EMPTY STATE: Route to Project Creation
                Intent intent = new Intent(MainActivity.this, CreateProjectActivity.class);
                intent.putExtra("USER_ID", currentSessionUserId);
                startActivity(intent);
            } else {
                // WORKING STATE: Fall back to current Manager-only Task Creation
                if (selectedPosition != AdapterView.INVALID_POSITION) {
                    Project selected = projectsList.get(selectedPosition);
                    boolean isCurrentUserManager = (currentSessionUserId == selected.managerId);
                    if (isCurrentUserManager) {
                        Intent intent = new Intent(this, CreateTaskActivity.class);
                        intent.putExtra("PROJECT_ID", selected.id);
                        intent.putExtra("IS_MANAGER", true);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
