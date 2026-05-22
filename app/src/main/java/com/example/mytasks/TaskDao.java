package com.example.mytasks;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks WHERE assigneeId = :employeeId AND status != 'DONE' ORDER BY priority ASC")
    List<Task> getAllPendingTasksForEmployee(int employeeId);

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    List<Task> getAllTasksForProject(int projectId);

    @Update
    void updateTask(Task task);
}
