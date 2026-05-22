package com.example.mytasks;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int projectId;
    public int assigneeId; // Employee ID
    public String title;
    public String description;
    
    // 1 = Critical, 2 = High, 3 = Medium, 4 = Low
    public int priority;
    
    public long timeLimitMillis;
    public long timeTakenMillis;
    public String status; // e.g., "PENDING", "IN_PROGRESS", "DONE"

    public Task() {}
}
