package com.nightshadow.mini.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stepsTaken: Int
)
