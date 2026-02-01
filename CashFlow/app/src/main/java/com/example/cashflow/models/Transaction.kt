package com.example.cashflow.models

import java.util.Date

data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType
)

enum class TransactionType {
    INCOME,
    EXPENSE
} 