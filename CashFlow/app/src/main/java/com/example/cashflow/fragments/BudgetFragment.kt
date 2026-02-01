package com.example.cashflow.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.cashflow.R
import com.example.cashflow.models.TransactionType
import com.example.cashflow.utils.PreferencesManager
import java.util.*

class BudgetFragment : Fragment() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var etBudget: EditText
    private lateinit var btnSetBudget: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvRemaining: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())

        etBudget = view.findViewById(R.id.et_budget)
        btnSetBudget = view.findViewById(R.id.btn_set_budget)
        progressBar = view.findViewById(R.id.progress_bar)
        tvProgress = view.findViewById(R.id.tv_progress)
        tvRemaining = view.findViewById(R.id.tv_remaining)

        setupUI()
        updateBudgetProgress()
    }

    private fun setupUI() {
        val currentBudget = preferencesManager.getMonthlyBudget()
        if (currentBudget > 0) {
            etBudget.setText(currentBudget.toString())
        }

        btnSetBudget.setOnClickListener {
            val budgetText = etBudget.text.toString()
            val budget = budgetText.toDoubleOrNull()

            if (budget == null || budget <= 0) {
                Toast.makeText(context, "Please enter a valid budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            preferencesManager.setMonthlyBudget(budget)
            updateBudgetProgress()
            Toast.makeText(context, "Budget updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBudgetProgress() {
        val budget = preferencesManager.getMonthlyBudget()
        if (budget <= 0) {
            progressBar.progress = 0
            tvProgress.text = "Set your monthly budget"
            tvRemaining.text = ""
            return
        }

        val currentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val expenses = preferencesManager.getTransactions()
            .filter { transaction ->
                transaction.type == TransactionType.EXPENSE &&
                transaction.date.after(currentMonth.time) &&
                transaction.date.before(Calendar.getInstance().time)
            }
            .sumOf { it.amount }

        val progress = ((expenses / budget) * 100).toInt().coerceIn(0, 100)
        progressBar.progress = progress
        tvProgress.text = String.format("Spent: %.2f of %.2f", expenses, budget)
        tvRemaining.text = String.format("Remaining: %.2f", budget - expenses)

        // Check if budget is nearly exceeded or exceeded
        if (progress >= 90) {
            showBudgetAlert(progress)
        }
    }

    private fun showBudgetAlert(progress: Int) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Budget Alert")
            .setContentText(
                if (progress >= 100)
                    "You have exceeded your monthly budget!"
                else
                    "You have used $progress% of your monthly budget"
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        private const val CHANNEL_ID = "budget_alerts"
    }
} 