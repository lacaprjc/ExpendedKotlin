package com.lacaprjc.expended.ui.account

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentAccountBinding
import com.lacaprjc.expended.ui.model.Account
import com.lacaprjc.expended.ui.model.Account.AccountType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFragment : Fragment(R.layout.fragment_account) {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var accountCard: MaterialCardView
    private lateinit var accountNameInput: TextInputLayout
    private lateinit var cashAccountCard: MaterialCardView
    private lateinit var creditAccountCard: MaterialCardView
    private lateinit var checkingAccountCard: MaterialCardView
    private lateinit var savingsAccountCard: MaterialCardView
    private lateinit var personalAccountCard: MaterialCardView
    private lateinit var budgetAccountCard: MaterialCardView

    private val accountViewModel
            by navGraphViewModels<AccountViewModel>(R.id.mobile_navigation) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAccountBinding.bind(view)

        accountCard = binding.accountBalanceCard

        accountNameInput = binding.accountNameInput

        binding.positiveButton.setOnClickListener {
            context?.let { context ->
                binding.root.findFocus()?.let { focus ->
                    context.getSystemService(InputMethodManager::class.java)
                        .hideSoftInputFromWindow(focus.windowToken, 0)
                }
            }
            when (accountViewModel.getState().value) {
                AccountViewModel.State.ADD -> addAccount()
                AccountViewModel.State.EDIT -> updateAccount()
                else -> {
                }
            }
        }

        binding.accountBalanceInputEditText.doOnTextChanged { text, start, _, _ ->
            text?.let { chars ->
                val input = chars.toString()
                val decimals = input.substringAfter(".", "")

                if (decimals.isNotBlank() && decimals.length > 2) {
                    binding.accountBalanceInputEditText.setText(chars.take(start))
                    binding.accountBalanceInputEditText.setSelection(start)
                }
            }
        }

        binding.negativeButton.setOnClickListener {
            deleteAccount(accountViewModel.getWorkingAccount().value!!.first)
        }

        binding.neutralButton.setOnClickListener {
            resetAccount()
        }

        with(binding.accountBalanceInputEditText) {
            setOnClickListener {
                if (text.toString() == "0.0") {
                    setText("")
                }
            }
        }

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

        accountViewModel.getAccountType().observe(viewLifecycleOwner) { selectedAccountType(it) }

        accountViewModel.getWorkingAccount().observe(viewLifecycleOwner) {
            setAccount(it.first, it.second)
        }

        accountViewModel.getState().observe(viewLifecycleOwner) {
            when (it) {
                AccountViewModel.State.ADD -> {
                    binding.accountBalanceLabel.text = "Starting Balance"
                    binding.accountBalanceInput.isEnabled = true

                    binding.positiveButton.text = "ADD"
                    binding.positiveButton.setIconResource(R.drawable.baseline_add_black_24dp)
                    binding.negativeButton.visibility = View.INVISIBLE
                    binding.neutralButton.visibility = View.INVISIBLE
                }
                AccountViewModel.State.EDIT -> {
                    binding.accountBalanceLabel.text = "Account Balance"
                    binding.accountBalanceInput.isEnabled = false

                    binding.positiveButton.text = "Update"
                    binding.positiveButton.setIconResource(R.drawable.baseline_system_update_alt_black_24dp)
                    binding.negativeButton.visibility = View.VISIBLE
                    binding.neutralButton.visibility = View.VISIBLE
                }
                AccountViewModel.State.UPDATED, AccountViewModel.State.DELETED, AccountViewModel.State.ADDED -> resetAccount()
                else -> {
                }
            }
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

        accountCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                accountCardColor
            )
        )
        accountNameInput.startIconDrawable =
            ContextCompat.getDrawable(requireContext(), accountCardStartIconDrawable)

    }

    private fun buildAccount(id: Long = 0): Account? {
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
            id
        )
    }

    private fun addAccount() {
        buildAccount()?.let {
            accountViewModel.addAccount(
                it,
                binding.accountBalanceInputEditText.text!!.toString().toDouble()
            )
        }
    }

    private fun updateAccount() {
        buildAccount(accountViewModel.getWorkingAccount().value!!.first.accountId)?.let {
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

    private fun resetAccount() {
        val newAccount = Account("", AccountType.CASH)
        accountViewModel.setWorkingAccount(newAccount, 0.0)
        accountViewModel.setState(AccountViewModel.State.ADD)
        binding.accountBalanceInput.editText!!.setText("")
    }

    private fun setAccount(account: Account, balance: Double) {
        binding.accountNameInput.editText!!.setText(account.name)
        binding.accountBalanceInput.editText!!.setText(balance.toString())
        binding.accountNotesInput.editText!!.setText(account.notes)
    }
}