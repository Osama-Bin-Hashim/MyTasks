package com.example.mytasks;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mytasks.databinding.ActivityCreateTaskBinding;

public class CreateTaskActivity extends AppCompatActivity {
    private ActivityCreateTaskBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int projectId = getIntent().getIntExtra("PROJECT_ID", -1);

        binding.btnSaveTask.setOnClickListener(v -> {
            saveTaskToDatabase(projectId);
        });
    }

    private void saveTaskToDatabase(int projectId) {
        String title = binding.etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (projectId == -1) {
            Toast.makeText(this, "Invalid Project Context", Toast.LENGTH_SHORT).show();
            return;
        }

        Task newTask = new Task();
        newTask.projectId = projectId;
        newTask.title = title;
        newTask.description = binding.etTaskDesc.getText().toString();
        newTask.status = "PENDING";
        newTask.priority = 2; // Default to High

        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // FIX: Using insertTask instead of updateTask for new records
            db.taskDao().insertTask(newTask);
            
            runOnUiThread(() -> {
                Toast.makeText(CreateTaskActivity.this, "Task Created Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
