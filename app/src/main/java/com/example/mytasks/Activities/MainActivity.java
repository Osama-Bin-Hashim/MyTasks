package com.example.mytasks.Activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.mytasks.Project;
import com.example.mytasks.User;
import com.example.mytasks.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<Project> projectsList = new ArrayList<>();
    private User currentSessionUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            // 1. Fetch/Initialize Session User
            List<User> users = db.userDao().getAllUsers();
            if (users.isEmpty()) {
                db.userDao().insertUser(new User("test_user"));
                currentSessionUser = db.userDao().getUserByUsername("test_user");
            } else {
                currentSessionUser = users.get(0);
            }

            // 2. Fetch Projects
            projectsList = db.projectDao().getAllProjects();

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
        List<String> projectNames = new ArrayList<>();
        for (Project p : projectsList) {
            projectNames.add(p.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, projectNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.projectSpinner.setAdapter(adapter);

        binding.projectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Project selected = projectsList.get(position);
                evaluateUserRole(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void evaluateUserRole(Project selectedProject) {
        if (currentSessionUser != null) {
            boolean isManager = (currentSessionUser.id == selectedProject.managerId);
            
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
    }

    private void setupClickListeners() {
        binding.todoOption.setOnClickListener(v -> {
            Toast.makeText(this, "TODOs Module Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.noticesOption.setOnClickListener(v -> {
            Toast.makeText(this, "Notices Module Coming Soon", Toast.LENGTH_SHORT).show();
        });

        binding.dashboardOption.setOnClickListener(v -> {
            int selectedPosition = binding.projectSpinner.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION && !projectsList.isEmpty()) {
                Project selected = projectsList.get(selectedPosition);
                boolean isManager = (currentSessionUser != null && currentSessionUser.id == selected.managerId);
                
                Intent intent;
                if (isManager) {
                    intent = new Intent(this, ManagerPerformanceActivity.class);
                } else {
                    intent = new Intent(this, EmployeePerformanceActivity.class);
                }
                intent.putExtra("PROJECT_ID", selected.id);
                startActivity(intent);
            }
        });

        binding.cardRequests.setOnClickListener(v -> {
            int selectedPosition = binding.projectSpinner.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION && !projectsList.isEmpty()) {
                Project selected = projectsList.get(selectedPosition);
                boolean isManager = (currentSessionUser != null && currentSessionUser.id == selected.managerId);
                
                Intent intent;
                if (isManager) {
                    intent = new Intent(this, ManagerRequestsActivity.class);
                } else {
                    intent = new Intent(this, EmployeeRequestsActivity.class);
                }
                intent.putExtra("PROJECT_ID", selected.id);
                startActivity(intent);
            }
        });

        binding.addTask.setOnClickListener(v -> {
            if (projectsList == null || projectsList.isEmpty()) {
                // EMPTY STATE: Route to Project Creation
                Intent intent = new Intent(MainActivity.this, CreateProjectActivity.class);
                if (currentSessionUser != null) {
                    intent.putExtra("USER_ID", currentSessionUser.id);
                }
                startActivity(intent);
            } else {
                // WORKING STATE: Fall back to current Manager-only Task Creation
                int selectedPosition = binding.projectSpinner.getSelectedItemPosition();
                if (selectedPosition != AdapterView.INVALID_POSITION) {
                    Project selected = projectsList.get(selectedPosition);
                    if (currentSessionUser != null && currentSessionUser.id == selected.managerId) {
                        Intent intent = new Intent(this, com.example.mytasks.CreateTaskActivity.class);
                        intent.putExtra("PROJECT_ID", selected.id);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
