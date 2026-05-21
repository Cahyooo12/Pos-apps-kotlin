package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val timestamp: Long,
    val totalAmount: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val paymentMethod: String, // Tunai, QRIS, Transfer, Debit
    val cashPaid: Double = 0.0,
    val cashReturn: Double = 0.0,
    val customerName: String = "Umum"
)
