package com.lacaprjc.expended.ui.account

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentAccountBinding
import com.lacaprjc.expended.model.Account
import com.lacaprjc.expended.model.Account.AccountType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AccountDetailsFragment : Fragment(R.layout.fragment_account) {
    private lateinit var accountNameInput: TextInputLayout
    private lateinit var cashAccountCard: MaterialCardView
    private lateinit var creditAccountCard: MaterialCardView
    private lateinit var checkingAccountCard: MaterialCardView
    private lateinit var savingsAccountCard: MaterialCardView
    private lateinit var personalAccountCard: MaterialCardView
    private lateinit var budgetAccountCard: MaterialCardView

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val accountViewModel: AccountViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAccountBinding.bind(view)

        setupAccountCard()
        setupAccountTypeCards()
        subscribeObservers()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupAccountCard() {
        accountNameInput = binding.accountNameInput

        binding.positiveButton.setOnClickListener {
            when (accountViewModel.getState().value) {
                AccountViewModel.State.NEW_ACCOUNT -> addAccount()
                AccountViewModel.State.EDIT_ACCOUNT -> updateAccount()
                else -> {
                }
            }
        }

        binding.negativeButton.setOnClickListener {
            deleteAccount(accountViewModel.getWorkingAccount().value!!.first)
        }

        with(binding.accountBalanceInputEditText) {
            setOnClickListener {
                if (text.toString() == "0.0") {
                    setText("")
                }
            }

            doOnTextChanged { text, start, _, _ ->
                text?.let { chars ->
                    val input = chars.toString()
                    val decimals = input.substringAfter(".", "")

                    if (decimals.isNotBlank() && decimals.length > 2) {
                        binding.accountBalanceInputEditText.setText(chars.take(start))
                        binding.accountBalanceInputEditText.setSelection(start)
                    }
                }
            }
        }
    }

    private fun setupAccountTypeCards() {
        cashAccountCard = binding.cashAccount.apply {
            setOnClickListener { accountViewModel.setAccountType(AccountType.CASH) }
        }
        creditAccountCard = binding.creditAccount.apply {
            setOnClickListener { accountViewModel.setAccountType(AccountType.CREDIT) }
        }
        checkingAccountCard = binding.checkingAccount.apply {
            setOnClickListener { accountViewModel.setAccountType(AccountType.CHECKING) }
        }
        savingsAccountCard = binding.savingsAccount.apply {
            setOnClickListener { accountViewModel.setAccountType(AccountType.SAVINGS) }
        }
        personalAccountCard = binding.personalAccount.apply {
            setOnClickListener { accountViewModel.setAccountType(AccountType.PERSONAL) }
        }
        budgetAccountCard = binding.budgetAccount.apply {
            setOnClickListener { accountViewModel.setAccountType(AccountType.BUDGET) }
        }
    }

    private fun subscribeObservers() {
        lifecycleScope.launchWhenStarted {
            accountViewModel.getState().collect { state ->
                when (state) {
                    AccountViewModel.State.NEW_ACCOUNT -> {
                        resetAllFields()
                        binding.accountBalanceLabel.text = "Starting Balance"
                        binding.accountBalanceInput.isEnabled = true

                        binding.positiveButton.text = "ADD"
                        binding.positiveButton.setIconResource(R.drawable.baseline_add_black_24dp)
                        binding.negativeButton.visibility = View.INVISIBLE
                    }
                    AccountViewModel.State.EDIT_ACCOUNT -> {
                        binding.accountBalanceLabel.text = "Account Balance"
                        binding.accountBalanceInput.isEnabled = false

                        binding.positiveButton.text = "Update"
                        binding.positiveButton.setIconResource(R.drawable.baseline_system_update_alt_black_24dp)
                        binding.negativeButton.visibility = View.VISIBLE
                    }
                    AccountViewModel.State.UPDATED_ACCOUNT, AccountViewModel.State.DELETE_ACCOUNT, AccountViewModel.State.ADDED_ACCOUNT -> resetAllFields()
                    else -> {
                    }
                }
            }
        }

        accountViewModel.getAccountType().observe(viewLifecycleOwner) { selectedAccountType(it) }
        accountViewModel.getWorkingAccount().observe(viewLifecycleOwner) {
            setAccount(it.first, it.second)
        }
    }

    private fun selectedAccountType(type: AccountType) {
        var accountCardColor = 0
        var accountCardStartIconDrawable = 0

        cashAccountCard.strokeWidth = 0
        creditAccountCard.strokeWidth = 0
        checkingAccountCard.strokeWidth = 0
        savingsAccountCard.strokeWidth = 0
        personalAccountCard.strokeWidth = 0
        budgetAccountCard.strokeWidth = 0

        when (type) {
            AccountType.CASH -> {
                cashAccountCard.strokeWidth = 4
                accountCardColor = R.color.colorCashCard
                accountCardStartIconDrawable = R.drawable.ic_noun_cash_3506076
            }
            AccountType.CREDIT -> {
                creditAccountCard.strokeWidth = 4
                accountCardColor = R.color.colorCreditCard
                accountCardStartIconDrawable = R.drawable.ic_noun_card_3510430
            }
            AccountType.CHECKING -> {
                checkingAccountCard.strokeWidth = 4
                accountCardColor = R.color.colorCheckingCard
                accountCardStartIconDrawable = R.drawable.ic_noun_check_1923696
            }
            AccountType.SAVINGS -> {
                savingsAccountCard.strokeWidth = 4
                accountCardColor = R.color.colorSavingsCard
                accountCardStartIconDrawable = R.drawable.ic_noun_saving_1959320
            }
            AccountType.PERSONAL -> {
                personalAccountCard.strokeWidth = 4
                accountCardColor = R.color.colorPersonalCard
                accountCardStartIconDrawable = R.drawable.ic_noun_personal_104667
            }
            AccountType.BUDGET -> {
                budgetAccountCard.strokeWidth = 4
                accountCardColor = R.color.colorBudgetCard
                accountCardStartIconDrawable = R.drawable.ic_noun_budget_2406949
            }
        }

        cashAccountCard.invalidate()
        creditAccountCard.invalidate()
        checkingAccountCard.invalidate()
        savingsAccountCard.invalidate()
        personalAccountCard.invalidate()
        budgetAccountCard.invalidate()

        binding.accountCardBackground.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    accountCardColor
                )
            )
        accountNameInput.startIconDrawable =
            ContextCompat.getDrawable(requireContext(), accountCardStartIconDrawable)

    }

    private fun buildAccount(id: Long = 0, orderPosition: Int = 0): Account? {
        val name = accountNameInput.editText!!.text.toString()

        if (name.isBlank()) {
            binding.accountNameInput.error = "Name must not be blank"
            return null
        } else {
            binding.accountNameInput.error = ""
        }

        val balance =
            try {
                binding.accountBalanceInput.editText!!.text.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.00
            }

        val type = accountViewModel.getAccountType().value!!
        val notes = binding.accountNotesInput.editText!!.text.toString()

        return Account(
            name = name,
            accountType = type,
            notes = notes,
            accountId = id,
            orderPosition = orderPosition
        )
    }

    private fun addAccount() {
        buildAccount(orderPosition = accountViewModel.getWorkingAccountOrderPosition())?.let {
            accountViewModel.addAccount(
                it,
                binding.accountBalanceInputEditText.text!!.toString().toDouble()
            )
        }
    }

    private fun updateAccount() {
        val accountWithBalance: Pair<Account, Double> = accountViewModel.getWorkingAccount().value!!
        buildAccount(accountWithBalance.first.accountId, orderPosition = accountWithBalance.first.orderPosition)?.let {
            accountViewModel.updateAccount(it)
        }
    }

    private fun deleteAccount(account: Account) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Account?")
            .setMessage("Are you sure you want to remove this account?")
            .setPositiveButton("Delete") { dialog, _ ->
                accountViewModel.deleteAccount(account)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun setAccount(account: Account, balance: Double) {
        binding.accountNameInput.editText!!.setText(account.name)
        binding.accountBalanceInput.editText!!.setText(if (balance != 0.0) balance.toString() else "")
        binding.accountNotesInput.editText!!.setText(account.notes)
    }

    private fun resetAllFields() {
        binding.accountBalanceInputEditText.setText("")
        binding.accountNameInputEditText.setText("")
        binding.accountBalanceInputEditText.setText("")
    }
}