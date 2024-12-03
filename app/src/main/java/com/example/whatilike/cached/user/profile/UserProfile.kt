package com.example.whatilike.cached.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken


@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val uid: String,
    val nickname: String,
    val photoUrl: String
)

