package com.example.mytasks;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {

    /**
     * Requirement: Use ABORT to ensure data integrity during task creation.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    List<Task> getTasksByProject(int projectId);

    @Query("SELECT * FROM tasks WHERE assigneeId = :employeeId")
    List<Task> getTasksByAssignee(int employeeId);

    @Query("SELECT * FROM tasks WHERE status = :status")
    List<Task> getTasksByStatus(String status);
}
