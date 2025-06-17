package com.supdevinci.quizz.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.supdevinci.quizz.model.UserEntity
import java.util.*

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): LiveData<List<UserEntity>>

    @Query("UPDATE users SET score = :newScore, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateScore(userId: Int, newScore: Int, updatedAt: Date = Date())
}