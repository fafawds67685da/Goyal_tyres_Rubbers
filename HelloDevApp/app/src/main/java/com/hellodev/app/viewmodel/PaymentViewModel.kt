package com.hellodev.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellodev.app.data.AppDatabase
import com.hellodev.app.data.Payment
import com.hellodev.app.data.PaymentRepository
import com.hellodev.app.data.PaymentTransaction
import com.hellodev.app.data.PaymentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val paymentRepository: PaymentRepository
    
    // Payment States
    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()
    
    private val _incomingPayments = MutableStateFlow<List<Payment>>(emptyList())
    val incomingPayments: StateFlow<List<Payment>> = _incomingPayments.asStateFlow()
    
    private val _outgoingPayments = MutableStateFlow<List<Payment>>(emptyList())
    val outgoingPayments: StateFlow<List<Payment>> = _outgoingPayments.asStateFlow()
    
    private val _pendingIncoming = MutableStateFlow(0.0)
    val pendingIncoming: StateFlow<Double> = _pendingIncoming.asStateFlow()
    
    private val _pendingOutgoing = MutableStateFlow(0.0)
    val pendingOutgoing: StateFlow<Double> = _pendingOutgoing.asStateFlow()
    
    private val _selectedPayment = MutableStateFlow<Payment?>(null)
    val selectedPayment: StateFlow<Payment?> = _selectedPayment.asStateFlow()
    
    private val _paymentTransactions = MutableStateFlow<List<PaymentTransaction>>(emptyList())
    val paymentTransactions: StateFlow<List<PaymentTransaction>> = _paymentTransactions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        val database = AppDatabase.getDatabase(application)
        paymentRepository = PaymentRepository(
            database.paymentDao(),
            database.paymentTransactionDao()
        )
        
        // Collect all payments
        viewModelScope.launch {
            paymentRepository.allPayments.collect { paymentList ->
                _payments.value = paymentList
            }
        }
        
        // Collect incoming payments
        viewModelScope.launch {
            paymentRepository.incomingPayments.collect { paymentList ->
                _incomingPayments.value = paymentList
            }
        }
        
        // Collect outgoing payments
        viewModelScope.launch {
            paymentRepository.outgoingPayments.collect { paymentList ->
                _outgoingPayments.value = paymentList
            }
        }
        
        // Collect pending incoming total
        viewModelScope.launch {
            paymentRepository.totalPendingIncoming.collect { total ->
                _pendingIncoming.value = total
            }
        }
        
        // Collect pending outgoing total
        viewModelScope.launch {
            paymentRepository.totalPendingOutgoing.collect { total ->
                _pendingOutgoing.value = total
            }
        }
    }
    
    fun getPendingPayments(): List<Payment> {
        return _payments.value.filter { !it.isFullyPaid }
    }
    
    fun getPendingPaymentsByType(type: PaymentType): List<Payment> {
        return _payments.value.filter { it.type == type && !it.isFullyPaid }
    }
    
    fun selectPayment(paymentId: Int) {
        viewModelScope.launch {
            val payment = _payments.value.find { it.id == paymentId }
            _selectedPayment.value = payment
            
            if (payment != null) {
                paymentRepository.getTransactionsForPayment(paymentId).collect { transactions ->
                    _paymentTransactions.value = transactions
                }
            }
        }
    }
    
    fun addPayment(payment: Payment) {
        viewModelScope.launch {
            _isLoading.value = true
            paymentRepository.insertPayment(payment)
            _isLoading.value = false
        }
    }
    
    fun updatePayment(payment: Payment) {
        viewModelScope.launch {
            _isLoading.value = true
            paymentRepository.updatePayment(payment)
            _isLoading.value = false
        }
    }
    
    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            _isLoading.value = true
            paymentRepository.deletePayment(payment)
            _isLoading.value = false
        }
    }
    
    fun addPaymentTransaction(paymentId: Int, amount: Double, remark: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                paymentRepository.addPaymentTransaction(paymentId, amount, remark)
                // Refresh the selected payment
                selectPayment(paymentId)
            } catch (e: Exception) {
                // Handle error (could add error state flow)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getTransactionsForPayment(paymentId: Int) = paymentRepository.getTransactionsForPayment(paymentId)
}
