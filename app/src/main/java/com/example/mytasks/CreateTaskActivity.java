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

        binding.btnSaveTask.setOnClickListener(v -> {
            // Logic to save task to Room would go here
            saveTaskToDatabase();
        });
    }

    private void saveTaskToDatabase() {
        String title = binding.etTaskTitle.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Task newTask = new Task();
        newTask.title = title;
        newTask.description = binding.etTaskDesc.getText().toString();
        newTask.status = "PENDING";
        newTask.priority = 2; // Default to High

        AppDatabase.getInstance(this).taskDao().updateTask(newTask); // Using update as a generic save for now
        
        Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
