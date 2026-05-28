package com.example.mytasks.Activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ManagerRequestsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SECURITY SHIELD
        boolean isManager = getIntent().getBooleanExtra("IS_MANAGER", false);
        if (!isManager) {
            Toast.makeText(this, "Access Denied: Managers Only.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int activeProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
        
        TextView textView = new TextView(this);
        textView.setTextSize(20f);
        textView.setPadding(50, 50, 50, 50);
        textView.setText("MANAGER Requests: Workspace context verified for ID: " + activeProjectId);
        setContentView(textView);
    }
}
