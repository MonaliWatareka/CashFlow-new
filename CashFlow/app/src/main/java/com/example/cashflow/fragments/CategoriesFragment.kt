package com.example.cashflow.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cashflow.R
import com.example.cashflow.models.TransactionType
import com.example.cashflow.utils.PreferencesManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class CategoriesFragment : Fragment() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var pieChart: PieChart
    private lateinit var tvTotalExpenses: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())

        pieChart = view.findViewById(R.id.pie_chart)
        tvTotalExpenses = view.findViewById(R.id.tv_total_expenses)

        setupPieChart()
        loadCategoryData()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            legend.isEnabled = true
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            setHoleRadius(45f)
            setTransparentCircleRadius(50f)
        }
    }

    private fun loadCategoryData() {
        val transactions = preferencesManager.getTransactions()
        val expensesByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }

        val totalExpenses = expensesByCategory.values.sum()
        tvTotalExpenses.text = String.format("Total Expenses: %.2f", totalExpenses)

        val entries = expensesByCategory.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }
} 