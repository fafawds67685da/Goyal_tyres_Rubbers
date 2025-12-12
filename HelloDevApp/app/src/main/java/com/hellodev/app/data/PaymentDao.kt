package com.hellodev.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface PaymentDao {
    @Insert
    suspend fun insertPayment(payment: Payment): Long
    
    @Update
    suspend fun updatePayment(payment: Payment)
    
    @Delete
    suspend fun deletePayment(payment: Payment)
    
    @Query("SELECT * FROM payments ORDER BY dueDate ASC")
    fun getAllPayments(): Flow<List<Payment>>
    
    @Query("SELECT * FROM payments WHERE type = :type ORDER BY dueDate ASC")
    fun getPaymentsByType(type: PaymentType): Flow<List<Payment>>
    
    @Query("SELECT * FROM payments WHERE isFullyPaid = 0 ORDER BY dueDate ASC")
    fun getPendingPayments(): Flow<List<Payment>>
    
    @Query("SELECT * FROM payments WHERE type = :type AND isFullyPaid = 0 ORDER BY dueDate ASC")
    fun getPendingPaymentsByType(type: PaymentType): Flow<List<Payment>>
    
    @Query("SELECT * FROM payments WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: Int): Payment?
    
    @Query("SELECT SUM(totalAmount - paidAmount) FROM payments WHERE type = :type AND isFullyPaid = 0")
    fun getTotalPendingByType(type: PaymentType): Flow<Double?>
}

@Dao
interface PaymentTransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: PaymentTransaction): Long
    
    @Query("SELECT * FROM payment_transactions WHERE paymentId = :paymentId ORDER BY transactionDate DESC")
    fun getTransactionsForPayment(paymentId: Int): Flow<List<PaymentTransaction>>
    
    @Delete
    suspend fun deleteTransaction(transaction: PaymentTransaction)
}

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val transactionDao: PaymentTransactionDao
) {
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val pendingPayments: Flow<List<Payment>> = paymentDao.getPendingPayments()
    val incomingPayments: Flow<List<Payment>> = paymentDao.getPaymentsByType(PaymentType.INCOMING)
    val outgoingPayments: Flow<List<Payment>> = paymentDao.getPaymentsByType(PaymentType.OUTGOING)
    val totalPendingIncoming: Flow<Double> = paymentDao.getTotalPendingByType(PaymentType.INCOMING).map { it ?: 0.0 }
    val totalPendingOutgoing: Flow<Double> = paymentDao.getTotalPendingByType(PaymentType.OUTGOING).map { it ?: 0.0 }
    
    suspend fun insertPayment(payment: Payment): Long {
        return paymentDao.insertPayment(payment)
    }
    
    suspend fun updatePayment(payment: Payment) {
        paymentDao.updatePayment(payment)
    }
    
    suspend fun deletePayment(payment: Payment) {
        paymentDao.deletePayment(payment)
    }
    
    suspend fun getPaymentById(paymentId: Int): Payment? {
        return paymentDao.getPaymentById(paymentId)
    }
    
    suspend fun addPaymentTransaction(paymentId: Int, amount: Double, notes: String = ""): Boolean {
        val payment = getPaymentById(paymentId) ?: return false
        
        val newPaidAmount = payment.paidAmount + amount
        if (newPaidAmount > payment.totalAmount) return false
        
        // Insert transaction
        transactionDao.insertTransaction(
            PaymentTransaction(
                paymentId = paymentId,
                amount = amount,
                notes = notes
            )
        )
        
        // Update payment
        val isFullyPaid = newPaidAmount >= payment.totalAmount
        paymentDao.updatePayment(
            payment.copy(
                paidAmount = newPaidAmount,
                isFullyPaid = isFullyPaid
            )
        )
        
        return true
    }
    
    fun getTransactionsForPayment(paymentId: Int): Flow<List<PaymentTransaction>> {
        return transactionDao.getTransactionsForPayment(paymentId)
    }
}
