package com.example.mytasks.Models;

public class RosterStats {
    public String username;
    public int totalTasks;
    public int completedTasks;
    public int percentage;

    public RosterStats(String username, int totalTasks, int completedTasks) {
        this.username = username;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.percentage = (totalTasks > 0) ? (completedTasks * 100) / totalTasks : 0;
    }
}
