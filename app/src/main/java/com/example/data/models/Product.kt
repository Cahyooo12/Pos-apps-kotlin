package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sku: String,
    val name: String,
    val category: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stockQuantity: Int,
    val minStockAlert: Int = 5,
    val unit: String = "pcs",
    val description: String = ""
)
