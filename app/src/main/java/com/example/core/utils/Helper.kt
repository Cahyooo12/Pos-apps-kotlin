package com.example.core.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helper {
    fun formatRupiah(amount: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            // Remove fractional digits if any (.00)
            var formatted = format.format(amount)
            if (formatted.endsWith(",00")) {
                formatted = formatted.substring(0, formatted.length - 3)
            }
            formatted.replace("Rp", "Rp ")
        } catch (e: Exception) {
            "Rp ${String.format("%,d", amount.toLong()).replace(',', '.')}"
        }
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return format.format(date)
    }
}
