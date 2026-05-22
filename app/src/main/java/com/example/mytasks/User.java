package com.example.mytasks;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String username;
    public String role; // "UNASSIGNED", "MANAGER", "EMPLOYEE"
    
    public User() {}
    
    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }
}
