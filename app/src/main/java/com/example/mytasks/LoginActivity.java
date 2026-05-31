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

import com.example.mytasks.Activities.MainActivity;
import com.example.mytasks.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SESSION PERSISTENCE: Check for existing login
        android.content.SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        if (pref.getInt("LOGGED_IN_USER_ID", -1) != -1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.loginBtn.setOnClickListener(v -> handleLogin());

        binding.registerOption.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String username = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loginBtn.setVisibility(View.GONE);
        binding.loginAcProgressBar.setVisibility(View.VISIBLE);

        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = db.userDao().getUserByUsername(username);

            runOnUiThread(() -> {
                binding.loginBtn.setVisibility(View.VISIBLE);
                binding.loginAcProgressBar.setVisibility(View.GONE);

                if (user != null && Objects.equals(user.password, password)) {
                    // SAVE SESSION
                    getSharedPreferences("UserSession", MODE_PRIVATE)
                            .edit()
                            .putInt("LOGGED_IN_USER_ID", user.id)
                            .putString("LOGGED_IN_USERNAME", user.username)
                            .apply();

                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials or user not found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}