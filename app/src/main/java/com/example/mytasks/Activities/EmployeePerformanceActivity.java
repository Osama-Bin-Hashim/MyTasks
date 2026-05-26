package com.example.mytasks.Activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EmployeePerformanceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int activeProjectId = getIntent().getIntExtra("PROJECT_ID", -1);
        
        TextView textView = new TextView(this);
        textView.setTextSize(20f);
        textView.setPadding(50, 50, 50, 50);
        textView.setText("EMPLOYEE Performance: Workspace context verified for ID: " + activeProjectId);
        setContentView(textView);
    }
}
