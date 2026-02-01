package com.example.cashflow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflow.R
import com.example.cashflow.models.Transaction
import com.example.cashflow.models.TransactionType
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_title)
        val category: TextView = view.findViewById(R.id.tv_category)
        val date: TextView = view.findViewById(R.id.tv_date)
        val amount: TextView = view.findViewById(R.id.tv_amount)
        val deleteButton: ImageButton = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.title.text = transaction.title
        holder.category.text = transaction.category
        holder.date.text = dateFormat.format(transaction.date)
        holder.amount.text = String.format("%.2f", transaction.amount)
        holder.amount.setTextColor(
            holder.itemView.context.getColor(
                if (transaction.type == TransactionType.INCOME)
                    R.color.green
                else R.color.red
            )
        )

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
} 