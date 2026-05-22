package com.example.mytasks;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users LIMIT 1") // Simplified for single user demo
    User getCurrentUser();

    @Insert
    void insertUser(User user);
    
    @Query("UPDATE users SET role = :newRole WHERE id = :userId")
    void updateRole(int userId, String newRole);
}
