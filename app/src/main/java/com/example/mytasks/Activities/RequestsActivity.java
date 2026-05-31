package com.example.mytasks.Activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mytasks.Adapters.RequestsAdapter;
import com.example.mytasks.AppDatabase;
import com.example.mytasks.NotificationHelper;
import com.example.mytasks.Project;
import com.example.mytasks.Request;
import com.example.mytasks.User;
import com.example.mytasks.databinding.ActivityRequestsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestsActivity extends AppCompatActivity implements RequestsAdapter.OnRequestActionListener {

    private ActivityRequestsBinding binding;
    private RequestsAdapter adapter;
    private int currentUserId;
    private String currentUsername;
    private boolean isManager;
    private int projectId;
    private Project currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // IDENTITY PERSISTENCE
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = pref.getInt("LOGGED_IN_USER_ID", -1);
        currentUsername = pref.getString("LOGGED_IN_USERNAME", "Unknown");
        
        isManager = getIntent().getBooleanExtra("IS_MANAGER", false);
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);

        loadProjectAndRequests();

        if (isManager) {
            binding.fabSendRequest.setVisibility(android.view.View.GONE);
        } else {
            binding.fabSendRequest.setOnClickListener(v -> showSendMessageDialog());
        }
    }

    private void setupRecyclerView() {
        if (currentProject == null) {
            Log.e("RequestsActivity", "setupRecyclerView: currentProject is null, skipping setup.");
            return;
        }
        adapter = new RequestsAdapter(isManager, currentUserId, currentProject.managerId, this);
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRequests.setAdapter(adapter);
    }

    private void loadProjectAndRequests() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentProject = db.projectDao().getProjectById(projectId);
            runOnUiThread(() -> {
                setupRecyclerView();
                loadRequests();
            });
        });
    }

    private void loadRequests() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Request> allProjectRequests = db.requestDao().getRequestsByProject(projectId);
            List<Request> filteredRequests = new ArrayList<>();

            for (Request req : allProjectRequests) {
                // Rule A: Direct to Manager
                if ("DIRECT_TO_MANAGER".equals(req.type)) {
                    if (currentUserId == req.senderId || isManager) {
                        filteredRequests.add(req);
                        // Mark as read if receiving
                        if (!req.isRead && ((isManager && req.senderId != currentUserId) || req.receiverId == currentUserId)) {
                            req.isRead = true;
                            db.requestDao().updateRequest(req);
                        }
                    }
                } 
                // Rule B: Peer-to-Peer
                else if ("PEER_TO_PEER".equals(req.type)) {
                    if (currentUserId == req.senderId || currentUserId == req.receiverId || isManager) {
                        filteredRequests.add(req);
                        // Mark as read if receiving
                        if (!req.isRead && (req.receiverId == currentUserId || (isManager && req.senderId != currentUserId))) {
                            req.isRead = true;
                            db.requestDao().updateRequest(req);
                        }
                    }
                }
                // Legacy or Join Project
                else {
                    if (isManager || currentUserId == req.senderId) {
                        filteredRequests.add(req);
                        if (!req.isRead && isManager && req.senderId != currentUserId) {
                            req.isRead = true;
                            db.requestDao().updateRequest(req);
                        }
                    }
                }
            }

            runOnUiThread(() -> {
                if (filteredRequests.isEmpty()) {
                    binding.tvEmptyRequests.setVisibility(View.VISIBLE);
                    binding.rvRequests.setVisibility(View.GONE);
                } else {
                    binding.tvEmptyRequests.setVisibility(View.GONE);
                    binding.rvRequests.setVisibility(View.VISIBLE);
                    adapter.setRequestList(filteredRequests);
                }
            });
        });
    }

    private void showSendMessageDialog() {
        if (currentProject == null) {
            Toast.makeText(this, "Project data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Request / Message");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Recipient Spinner
        Spinner spinner = new Spinner(this);
        List<String> roster = new ArrayList<>();
        
        if (!isManager) {
            roster.add("Project Manager");
        }

        if (currentProject.projectRoster != null && !currentProject.projectRoster.isEmpty()) {
            String[] members = currentProject.projectRoster.split(", ");
            for (String member : members) {
                if (!member.equals(currentUsername)) {
                    roster.add(member);
                }
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roster);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        layout.addView(spinner);

        // Message EditText
        EditText input = new EditText(this);
        input.setHint("Type your message...");
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            String recipient = spinner.getSelectedItem().toString();
            if (message.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            sendRequest(recipient, message);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendRequest(String recipient, String message) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int receiverId;
            String type;
            
            if ("Project Manager".equals(recipient)) {
                receiverId = currentProject.managerId;
                type = "DIRECT_TO_MANAGER";
            } else {
                User user = db.userDao().getUserByUsername(recipient);
                receiverId = (user != null) ? user.id : -1;
                type = "PEER_TO_PEER";
            }

            Request newRequest = new Request(
                currentUserId,
                currentUsername,
                receiverId,
                projectId,
                currentProject.name,
                message,
                type,
                "PENDING",
                System.currentTimeMillis()
            );

            db.requestDao().insertRequest(newRequest);
            loadRequests();
            runOnUiThread(() -> {
                Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();
                NotificationHelper.showNotification(this, "Message Sent!", 
                    "To: " + recipient);
            });
        });
    }

    @Override
    public void onApprove(Request request) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // SECURITY_AUDIT
            Log.d("SECURITY_AUDIT", "Request APPROVED. ID: " + request.requestId + " by User ID: " + currentUserId);

            request.status = "APPROVED";
            db.requestDao().updateRequest(request);

            // Trigger Backend Action
            if ("JOIN_PROJECT".equals(request.type)) {
                Project project = db.projectDao().getProjectById(request.projectId);
                if (project != null) {
                    String roster = project.projectRoster;
                    if (roster == null) roster = "";
                    if (!roster.contains(request.senderName)) {
                        if (!roster.isEmpty()) roster += ", ";
                        roster += request.senderName;
                        project.projectRoster = roster;
                        db.projectDao().updateProject(project);
                    }
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Request Approved", Toast.LENGTH_SHORT).show();
                loadRequests();
            });
        });
    }

    @Override
    public void onReject(Request request) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // SECURITY_AUDIT
            Log.d("SECURITY_AUDIT", "Request REJECTED. ID: " + request.requestId + " by User ID: " + currentUserId);

            request.status = "REJECTED";
            db.requestDao().updateRequest(request);

            runOnUiThread(() -> {
                Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show();
                loadRequests();
            });
        });
    }

    @Override
    public void onDelete(Request request) {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.requestDao().deleteRequest(request);
            runOnUiThread(() -> {
                Toast.makeText(this, "Message Deleted", Toast.LENGTH_SHORT).show();
                loadRequests();
            });
        });
    }
}
