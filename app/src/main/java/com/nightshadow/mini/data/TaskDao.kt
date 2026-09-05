package com.nightshadow.mini.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentTasks(): List<TaskEntity>
}
