package com.example.mytasks;

 import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytasks.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private String userRole = "UNASSIGNED"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load initial state
        refreshUserStatus();

        // Debug: Initialize a default user if none exists
        initializeDefaultUser();

        // Refresh button for unassigned users
        binding.btnRefreshStatus.setOnClickListener(v -> {
            refreshUserStatus();
            Toast.makeText(this, "Status Refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeDefaultUser() {
        AppDatabase db = AppDatabase.getInstance(this);
        if (db.userDao().getCurrentUser() == null) {
            // By default, create an unassigned user
            db.userDao().insertUser(new User("New User", "UNASSIGNED"));
        }
    }

    private void refreshUserStatus() {
        // In a real app, you'd fetch this from Room or SharedPreferences
        // For this implementation, we query our AppDatabase
        User user = AppDatabase.getInstance(this).userDao().getCurrentUser();
        
        if (user != null && user.role != null && !user.role.isEmpty()) {
            userRole = user.role;
        } else {
            userRole = "UNASSIGNED";
        }

        setupRoleBasedUI();
    }

    private void setupRoleBasedUI() {
        // --- Reset Visibilities ---
        binding.dashboardGrid.setVisibility(View.GONE);
        binding.unassignedStateContainer.setVisibility(View.GONE);
        binding.addTask.setVisibility(View.GONE);

        if (userRole.equalsIgnoreCase("MANAGER")) {
            // MANAGER STATE
            binding.dashboardGrid.setVisibility(View.VISIBLE);
            binding.addTask.setVisibility(View.VISIBLE);
            
            // FAB: Open Task Creation
            binding.addTask.setOnClickListener(v -> {
                // startActivity(new Intent(this, CreateTaskActivity.class));
                Toast.makeText(this, "Opening Create Task (Manager)", Toast.LENGTH_SHORT).show();
            });

            // Manager-specific intents
            binding.todoOption.setOnClickListener(v -> navigateTo("ManagerTodosActivity"));
            binding.noticesOption.setOnClickListener(v -> navigateTo("ManagerNoticesActivity"));
            binding.dashboardOption.setOnClickListener(v -> navigateTo("ManagerStatsActivity"));
            binding.requestOption.setOnClickListener(v -> navigateTo("ManagerRequestsActivity"));

        } else if (userRole.equalsIgnoreCase("EMPLOYEE")) {
            // EMPLOYEE STATE
            binding.dashboardGrid.setVisibility(View.VISIBLE);
            binding.addTask.setVisibility(View.GONE); // Employees cannot create tasks

            // Employee-specific intents
            binding.todoOption.setOnClickListener(v -> navigateTo("EmployeeTodosActivity"));
            binding.noticesOption.setOnClickListener(v -> navigateTo("EmployeeNoticesActivity"));
            binding.dashboardOption.setOnClickListener(v -> navigateTo("EmployeeStatsActivity"));
            binding.requestOption.setOnClickListener(v -> navigateTo("EmployeeRequestsActivity"));

        } else {
            // UNASSIGNED STATE (Default)
            binding.unassignedStateContainer.setVisibility(View.VISIBLE);
        }
    }

    private void navigateTo(String activityName) {
        // This is a helper to show where the explicit intents would go
        // In a real project, you would use: new Intent(this, TargetActivity.class)
        Toast.makeText(this, "Navigating to: " + activityName, Toast.LENGTH_SHORT).show();
    }
}
