package com.supdevinci.quizz.data.local

import androidx.room.*
import com.supdevinci.quizz.model.UserEntity
import java.util.*

@Dao
interface UserDao {

    @Insert
    fun insertUser(user: UserEntity): Long

    @Update
    fun updateUser(user: UserEntity): Int

    @Query("""
    UPDATE users 
    SET score = :newScore,
        maxScore = CASE WHEN :newScore > maxScore THEN :newScore ELSE maxScore END,
        updatedAt = :updatedAt 
    WHERE id = :userId """)
    fun updateScore(userId: Int, newScore: Int, updatedAt: Date = Date()): Int


    @Query("SELECT * FROM users WHERE pseudo = :pseudo LIMIT 1")
    fun findUserByPseudo(pseudo: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun findById(id: Int): UserEntity?


    @Query("SELECT * FROM users ORDER BY maxScore DESC")
    fun getBestScores(): List<UserEntity>

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<UserEntity>
}