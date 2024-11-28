package com.example.whatilike.cached

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val uid: String,
    val nickname: String,
    val photoUrl: String
)
