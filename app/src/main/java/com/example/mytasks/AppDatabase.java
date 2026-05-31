package com.example.mytasks;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Project.class, Task.class, Notice.class, Request.class}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;

    // Executor for asynchronous background operations
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract UserDao userDao();
    public abstract TaskDao taskDao();
    public abstract ProjectDao projectDao();
    public abstract NoticeDao noticeDao();
    public abstract RequestDao requestDao();

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "mytasks_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
