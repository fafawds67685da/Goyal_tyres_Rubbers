package com.hellodev.app.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object NumberFormatter {
    
    /**
     * Formats number to Indian numbering system with commas
     * Example: 1234567 -> 12,34,567
     */
    fun formatIndianCurrency(amount: Double): String {
        if (amount == 0.0) return "₹0"
        
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)
        
        val formatter = DecimalFormat("#,##,###.##")
        val parts = absAmount.toString().split(".")
        val integerPart = parts[0].toLongOrNull() ?: 0
        val decimalPart = if (parts.size > 1) parts[1] else ""
        
        // Format integer part with Indian style
        val formattedInteger = when {
            integerPart < 1000 -> integerPart.toString()
            else -> {
                val str = integerPart.toString()
                val lastThree = str.takeLast(3)
                val remaining = str.dropLast(3)
                
                val builder = StringBuilder()
                var count = 0
                for (i in remaining.length - 1 downTo 0) {
                    if (count == 2) {
                        builder.insert(0, ',')
                        count = 0
                    }
                    builder.insert(0, remaining[i])
                    count++
                }
                builder.append(',').append(lastThree)
                builder.toString()
            }
        }
        
        val result = if (decimalPart.isNotEmpty() && decimalPart.take(2) != "00") {
            "$formattedInteger.${decimalPart.take(2)}"
        } else {
            formattedInteger
        }
        
        return if (isNegative) "-₹$result" else "₹$result"
    }
    
    /**
     * Formats number with commas (no currency symbol)
     */
    fun formatNumber(number: Int): String {
        return formatNumber(number.toDouble())
    }
    
    fun formatNumber(number: Double): String {
        val formatter = DecimalFormat("#,##,###.##")
        return formatter.format(number)
    }
}
