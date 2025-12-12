package com.hellodev.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: PaymentType, // INCOMING or OUTGOING
    val partyName: String, // Customer name or Factory name
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: Long,
    val remark: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isFullyPaid: Boolean = false
) {
    val pendingAmount: Double
        get() = totalAmount - paidAmount
    
    val paymentProgress: Float
        get() = if (totalAmount > 0) (paidAmount / totalAmount).toFloat() else 0f
}

enum class PaymentType {
    INCOMING, // To receive from customers
    OUTGOING  // To pay to factories/suppliers
}

class PaymentTypeConverter {
    @androidx.room.TypeConverter
    fun fromPaymentType(type: PaymentType): String {
        return type.name
    }

    @androidx.room.TypeConverter
    fun toPaymentType(value: String): PaymentType {
        return PaymentType.valueOf(value)
    }
}

@Entity(tableName = "payment_transactions")
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val paymentId: Int, // Foreign key to Payment
    val amount: Double,
    val transactionDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)
