package com.example.cashflow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashflow.R
import com.example.cashflow.adapters.TransactionAdapter
import com.example.cashflow.databinding.FragmentTransactionsBinding
import com.example.cashflow.databinding.DialogAddTransactionBinding
import com.example.cashflow.models.Transaction
import com.example.cashflow.models.TransactionType
import com.example.cashflow.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionAdapter: TransactionAdapter
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())

        setupRecyclerView()
        setupUI()
        loadTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            emptyList(),
            onItemClick = { transaction ->
                showAddTransactionDialog(transaction)
            },
            onDeleteClick = { transaction ->
                showDeleteConfirmationDialog(transaction)
            }
        )
        binding.recyclerTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun setupUI() {
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog(transaction: Transaction? = null) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Setup category spinner
        val categories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Other")
        dialogBinding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            categories
        )

        // Setup date picker
        dialogBinding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                dialogBinding.etDate.setText(dateFormat.format(Date(selection)))
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }

        // Pre-fill fields if editing
        transaction?.let {
            dialogBinding.etTitle.setText(it.title)
            dialogBinding.etAmount.setText(it.amount.toString())
            dialogBinding.spinnerCategory.setSelection(categories.indexOf(it.category))
            dialogBinding.etDate.setText(dateFormat.format(it.date))
            dialogBinding.radioGroupType.check(
                if (it.type == TransactionType.INCOME) R.id.radio_income
                else R.id.radio_expense
            )
        }

        // Handle save button
        dialogBinding.btnAddTransaction.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString()
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
            val category = dialogBinding.spinnerCategory.selectedItem.toString()
            val dateStr = dialogBinding.etDate.text.toString()
            val type = if (dialogBinding.radioGroupType.checkedRadioButtonId == R.id.radio_income)
                TransactionType.INCOME else TransactionType.EXPENSE

            if (title.isBlank() || amount == null || dateStr.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = dateFormat.parse(dateStr) ?: Date()
            val newTransaction = Transaction(
                id = transaction?.id ?: System.currentTimeMillis(),
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = type
            )

            if (transaction == null) {
                preferencesManager.saveTransaction(newTransaction)
            } else {
                preferencesManager.updateTransaction(newTransaction)
            }

            loadTransactions()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                preferencesManager.deleteTransaction(transaction.id)
                loadTransactions()
                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadTransactions() {
        val transactions = preferencesManager.getTransactions()
            .sortedByDescending { it.date }
        transactionAdapter.updateTransactions(transactions)
    }
} 