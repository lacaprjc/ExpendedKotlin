package com.lacaprjc.expended.ui.transaction

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentTransactionBinding
import com.lacaprjc.expended.listAdapters.TransactionAdapter.Companion.DATE_FORMATTER
import com.lacaprjc.expended.listAdapters.TransactionAdapter.Companion.TIME_FORMATTER
import com.lacaprjc.expended.model.Transaction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@AndroidEntryPoint
class TransactionDetailsFragment : Fragment(R.layout.fragment_transaction) {
    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private val transactionViewModel: TransactionViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentTransactionBinding.bind(view)

        setupTransactionCard()
        observeData()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupTransactionCard() {
        val currentDateTime = LocalDateTime.now()

        binding.transactionAmountInputEditText.doOnTextChanged { text, start, _, _ ->
            text?.let { chars ->
                val input = chars.toString()
                val decimals = input.substringAfter(".", "")

                if (decimals.isNotBlank() && decimals.length > 2) {
                    binding.transactionAmountInputEditText.setText(chars.take(start))
                    binding.transactionAmountInputEditText.setSelection(start)
                }
            }
        }

        with(binding.transactionDateInput) {
            text = currentDateTime.format(DATE_FORMATTER)
            setOnClickListener {
                val datePicker: MaterialDatePicker<Long> =
                    MaterialDatePicker.Builder.datePicker().build()
                datePicker.addOnPositiveButtonClickListener { date ->
                    val formattedDate: LocalDateTime =
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(date),
                            ZoneOffset.UTC.normalized()
                        )
                    text =
                        formattedDate.format(DATE_FORMATTER)
                }
                datePicker.show(childFragmentManager, datePicker.toString())
            }

        }

        with(binding.transactionTimeInput) {
            text = currentDateTime.format(TIME_FORMATTER)
            setOnClickListener {
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        text = LocalTime.of(hourOfDay, minute).format(TIME_FORMATTER)
                    },
                    0,
                    0,
                    false
                ).show()
            }
        }

        binding.positiveButton.setOnClickListener {
            when (transactionViewModel.getState().value) {
                TransactionViewModel.State.NEW_TRANSACTION -> addTransaction()
                TransactionViewModel.State.EDIT_TRANSACTION -> updateTransaction()
                else -> {
                }
            }
        }


        binding.negativeButton.setOnClickListener {
            deleteTransaction(transactionViewModel.getWorkingTransaction().value!!)
        }
    }

    private fun observeData() {
        transactionViewModel.getWorkingTransaction().observe(viewLifecycleOwner) {
            setTransaction(it)
        }

        lifecycleScope.launchWhenStarted {
            transactionViewModel.getState().collect { state ->
                when (state) {
                    TransactionViewModel.State.NEW_TRANSACTION -> {
                        resetAllFields()
                        binding.positiveButton.text = "ADD"
                        binding.positiveButton.setIconResource(R.drawable.baseline_add_black_24dp)
                        binding.negativeButton.visibility = View.INVISIBLE
                    }
                    TransactionViewModel.State.EDIT_TRANSACTION -> {
                        binding.positiveButton.text = "Update"
                        binding.positiveButton.setIconResource(R.drawable.baseline_system_update_alt_black_24dp)
                        binding.negativeButton.visibility = View.VISIBLE
                    }
                    TransactionViewModel.State.UPDATED_TRANSACTION, TransactionViewModel.State.ADDED_TRANSACTION, TransactionViewModel.State.DELETED_TRANSACTION -> resetAllFields()
                    else -> {
                    }
                }

            }
        }
    }

    private fun buildTransaction(id: Long = 0): Transaction? {
        val name = binding.transactionNameInput.editText!!.text.toString()

        if (name.isBlank()) {
            binding.transactionNameInput.error = "Name must not be blank"
            return null
        } else {
            binding.transactionNameInput.error = ""
        }

        val amount = try {
            binding.transactionAmountInput.editText!!.text.toString().toDouble()
        } catch (e: NumberFormatException) {
            0.00
        }

        val localDate: LocalDate =
            LocalDate.parse(binding.transactionDateInput.text, DATE_FORMATTER)
        val localTime: LocalTime =
            LocalTime.parse(binding.transactionTimeInput.text, TIME_FORMATTER)
        val localDateTime: LocalDateTime = LocalDateTime.of(localDate, localTime)
        val notes = binding.transactionNotesInput.editText!!.text.toString()
        val forAccountId = transactionViewModel.getForAccountId().value!!

        return Transaction(name, forAccountId, amount, localDateTime, notes, "", id)
    }

    private fun addTransaction() {
        buildTransaction()?.let {
            transactionViewModel.addTransaction(it)
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        transactionViewModel.deleteTransaction(transaction)
    }

    private fun updateTransaction() {
        buildTransaction(transactionViewModel.getWorkingTransaction().value!!.transactionId)?.let {
            transactionViewModel.updateTransaction(it)
        }
    }

    private fun resetAllFields() {
        val currentDateTime = LocalDateTime.now()
        binding.transactionAmountInput.editText!!.setText("")
        binding.transactionAmountInputEditText!!.setText("")
        binding.transactionDateInput.text = currentDateTime.format(DATE_FORMATTER)
        binding.transactionTimeInput.text = currentDateTime.format(TIME_FORMATTER)
        binding.transactionNotesInputEditText.setText("")
    }

    private fun setTransaction(transaction: Transaction) {
        binding.transactionNameInputEditText.setText(transaction.name)
        binding.transactionAmountInputEditText.setText(if (transaction.amount != 0.0) transaction.amount.toString() else "")
        binding.transactionDateInput.text = transaction.date.format(DATE_FORMATTER)
        binding.transactionTimeInput.text = transaction.date.format(TIME_FORMATTER)
        binding.transactionNotesInputEditText.setText(transaction.notes)
    }
}