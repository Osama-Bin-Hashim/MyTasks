package com.example.mytasks;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytasks.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    // ---------------------------------------------------------------------------------------------
    // VIEW BINDING SECTION
    // ---------------------------------------------------------------------------------------------
    private ActivityDashboardBinding binding;
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -----------------------------------------------------------------------------------------
        // VIEW BINDING INITIALIZATION
        // -----------------------------------------------------------------------------------------
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // -----------------------------------------------------------------------------------------

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
