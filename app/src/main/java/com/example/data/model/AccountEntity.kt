package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val accountId: String,
    val name: String,
    val currency: String = "USD",
    val initialBalance: Double = 10000.0,
    val isDefault: Boolean = false
)
