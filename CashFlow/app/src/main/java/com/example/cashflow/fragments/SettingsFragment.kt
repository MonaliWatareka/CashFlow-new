package com.example.cashflow.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.cashflow.R
import com.example.cashflow.databinding.FragmentSettingsBinding
import com.example.cashflow.models.Transaction
import com.example.cashflow.utils.PreferencesManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private lateinit var preferencesManager: PreferencesManager
    private val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR")

    private val createBackup = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportBackup(it) }
    }

    private val openBackup = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importBackup(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())

        setupCurrencySpinner()
        loadCurrentSettings()
        setupSaveButton()

        binding.btnBackup.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            createBackup.launch("cashflow_backup_$timestamp.json")
        }

        binding.btnRestore.setOnClickListener {
            openBackup.launch(arrayOf("application/json"))
        }
    }

    private fun setupCurrencySpinner() {
        try {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                currencies
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerCurrency.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up currency options", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCurrentSettings() {
        try {
            // Load current currency
            val currentCurrency = preferencesManager.getCurrency()
            val currencyIndex = currencies.indexOf(currentCurrency)
            if (currencyIndex != -1) {
                binding.spinnerCurrency.setSelection(currencyIndex)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveSettings.setOnClickListener {
            try {
                // Save currency
                val selectedCurrency = binding.spinnerCurrency.selectedItem?.toString()
                if (selectedCurrency != null) {
                    preferencesManager.setCurrency(selectedCurrency)
                    Toast.makeText(context, "Currency saved successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportBackup(uri: Uri) {
        try {
            val transactions = preferencesManager.getTransactions()
            val backupData = BackupData(
                transactions = transactions,
                timestamp = System.currentTimeMillis()
            )
            
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(gson.toJson(backupData))
                }
            }
            
            Toast.makeText(requireContext(), "Backup created successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to create backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importBackup(uri: Uri) {
        try {
            val jsonString = requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }

            jsonString?.let {
                val type = object : TypeToken<BackupData>() {}.type
                val backupData = gson.fromJson<BackupData>(it, type)
                
                // Clear existing data and insert backup data
                backupData.transactions.forEach { transaction ->
                    preferencesManager.saveTransaction(transaction)
                }
                
                Toast.makeText(requireContext(), "Backup restored successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to restore backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class BackupData(
    val transactions: List<Transaction>,
    val timestamp: Long
) 