package com.lacaprjc.expended.util

import com.lacaprjc.expended.R
import com.lacaprjc.expended.ui.model.Account
import org.json.JSONObject

fun Account.AccountType.getAssociatedColor() = when (this) {
    Account.AccountType.CASH -> R.color.colorCashCard
    Account.AccountType.CREDIT -> R.color.colorCreditCard
    Account.AccountType.CHECKING -> R.color.colorCheckingCard
    Account.AccountType.SAVINGS -> R.color.colorSavingsCard
    Account.AccountType.PERSONAL -> R.color.colorPersonalCard
    Account.AccountType.BUDGET -> R.color.colorBudgetCard
}

fun Account.AccountType.getAssociatedIcon() = when (this) {
    Account.AccountType.CASH -> R.drawable.ic_noun_cash_3506076
    Account.AccountType.CREDIT -> R.drawable.ic_noun_card_3510430
    Account.AccountType.CHECKING -> R.drawable.ic_noun_check_1923696
    Account.AccountType.SAVINGS -> R.drawable.ic_noun_saving_1959320
    Account.AccountType.PERSONAL -> R.drawable.ic_noun_personal_104667
    Account.AccountType.BUDGET -> R.drawable.ic_noun_budget_2406949
}

fun Account.toJson(): JSONObject = JSONObject()
    .put("name", this.name)
    .put("accountType", this.accountType.name)
    .put("notes", this.notes)
    .put("accountId", this.accountId)