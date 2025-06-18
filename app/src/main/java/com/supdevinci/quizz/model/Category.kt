package com.supdevinci.quizz.model

data class Category(
    val id: Int,
    val name: String
) {
    override fun toString(): String = name
}
