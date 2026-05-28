package com.example.mytasks.Activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytasks.Adapters.TaskAdapter;
import com.example.mytasks.AppDatabase;
import com.example.mytasks.Project;
import com.example.mytasks.Task;
import com.example.mytasks.databinding.ActivityTodoWorkspaceBinding;

import java.util.List;

public class TodoWorkspaceActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private ActivityTodoWorkspaceBinding binding;
    private TaskAdapter adapter;
    private int projectId;
    private boolean isManager;
    private String currentUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTodoWorkspaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        isManager = getIntent().getBooleanExtra("IS_MANAGER", false);
        currentUsername = getIntent().getStringExtra("LOGGED_IN_USERNAME");
        
        loadInitialData();
    }

    private void loadInitialData() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Get Project Context
            Project project = db.projectDao().getProjectById(projectId);
            if (project == null) return;

            // 2. Load Tasks
            List<Task> tasks = db.taskDao().getTasksByProjectSorted(projectId);

            runOnUiThread(() -> {
                setupRecyclerView(tasks);
            });
        });
    }

    private void setupRecyclerView(List<Task> tasks) {
        adapter = new TaskAdapter(isManager, currentUsername, this);
        adapter.setTasks(tasks);
        binding.todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.todoRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onMarkDone(Task task) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            task.status = "DONE";
            // Mocking execution metrics: 80% of limit
            task.timeTakenMillis = (long) (task.timeLimitMillis * 0.8);
            
            db.taskDao().updateTask(task);

            // Refresh list
            List<Task> updatedTasks = db.taskDao().getTasksByProjectSorted(projectId);
            runOnUiThread(() -> {
                adapter.setTasks(updatedTasks);
                Toast.makeText(this, "Task marked as DONE", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
