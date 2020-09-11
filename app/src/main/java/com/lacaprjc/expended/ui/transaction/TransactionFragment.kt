package com.lacaprjc.expended.ui.transaction

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navGraphViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.lacaprjc.expended.MainApplication
import com.lacaprjc.expended.R
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.databinding.FragmentTransactionBinding
import com.lacaprjc.expended.listAdapters.TransactionAdapter.Companion.DATE_FORMATTER
import com.lacaprjc.expended.listAdapters.TransactionAdapter.Companion.TIME_FORMATTER
import com.lacaprjc.expended.ui.model.Transaction
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TransactionFragment : Fragment(R.layout.fragment_transaction) {
    private lateinit var binding: FragmentTransactionBinding

    private val transactionViewModel by navGraphViewModels<TransactionViewModel>(R.id.mobile_navigation) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dao =
                    (requireActivity().application as MainApplication).getDatabase().accountDao()
                return TransactionViewModel(AccountsWithTransactionsRepository(dao)) as T
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentTransactionBinding.bind(view)

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
            setOnClickListener { view ->
                val datePicker = MaterialDatePicker.Builder.datePicker().build()
                datePicker.addOnPositiveButtonClickListener { date ->
                    val formattedDate: LocalDateTime =
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(date),
                            ZoneId.systemDefault()
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
                // TODO: 9/10/20 theme the TimePickerDialog
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { timePicker, hourOfDay, minute ->
                        text = LocalTime.of(hourOfDay, minute).format(TIME_FORMATTER)
                    },
                    0,
                    0,
                    false
                ).show()
            }
        }

        binding.positiveButton.setOnClickListener {
            context?.let { context ->
                binding.root.findFocus()?.let { focus ->
                    context.getSystemService(InputMethodManager::class.java)
                        .hideSoftInputFromWindow(focus.windowToken, 0)
                }
            }
            when (transactionViewModel.getState().value) {
                TransactionViewModel.State.ADD -> addTransaction()
                TransactionViewModel.State.EDIT -> updateTransaction()
                else -> {
                }
            }
        }

        binding.negativeButton.setOnClickListener {
            deleteTransaction(transactionViewModel.getWorkingTransaction().value!!)
        }

        binding.neutralButton.setOnClickListener {
            resetTransaction()
        }

        transactionViewModel.getWorkingTransaction().observe(viewLifecycleOwner) {
            setTransaction(it)
        }

        transactionViewModel.getState().observe(viewLifecycleOwner) { state ->
            when (state) {
                TransactionViewModel.State.ADD -> {
                    binding.positiveButton.text = "ADD"
                    binding.positiveButton.setIconResource(R.drawable.baseline_add_black_24dp)
                    binding.neutralButton.visibility = View.INVISIBLE
                    binding.negativeButton.visibility = View.INVISIBLE
                }
                TransactionViewModel.State.EDIT -> {
                    binding.positiveButton.text = "Update"
                    binding.positiveButton.setIconResource(R.drawable.baseline_system_update_alt_black_24dp)
                    binding.negativeButton.visibility = View.VISIBLE
                    binding.neutralButton.visibility = View.VISIBLE
                }
                TransactionViewModel.State.UPDATED, TransactionViewModel.State.ADDED, TransactionViewModel.State.DELETED -> resetTransaction()
                else -> {
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

        return Transaction(forAccountId, amount, localDateTime, name, notes, "", id)
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

    private fun resetTransaction() {
        val newTransaction = Transaction(
            forAccountId = transactionViewModel.getForAccountId().value!!,
            0.0,
            LocalDateTime.now(),
            "",
            ""
        )

        transactionViewModel.setWorkingTransaction(newTransaction)
        transactionViewModel.setState(TransactionViewModel.State.ADD)
    }

    private fun setTransaction(transaction: Transaction) {
        binding.transactionNameInput.editText!!.setText(transaction.name)
        binding.transactionAmountInput.editText!!.setText(transaction.amount.toString())
        binding.transactionDateInput.text = transaction.date.format(DATE_FORMATTER)
        binding.transactionTimeInput.text = transaction.date.format(TIME_FORMATTER)
    }
}