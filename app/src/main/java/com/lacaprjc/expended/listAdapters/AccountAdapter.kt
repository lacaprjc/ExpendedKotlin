package com.lacaprjc.expended.listAdapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lacaprjc.expended.databinding.AccountItemBinding
import com.lacaprjc.expended.model.Account
import com.lacaprjc.expended.model.AccountWithBalance
import com.lacaprjc.expended.util.getAssociatedColor
import com.lacaprjc.expended.util.getAssociatedIcon
import com.lacaprjc.expended.util.toStringWithDecimalPlaces
import java.util.*

class AccountAdapter(
    private val onClickListener: ((Account) -> Unit)? = null
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AccountWithBalance>() {
            override fun areItemsTheSame(
                oldItem: AccountWithBalance,
                newItem: AccountWithBalance
            ): Boolean =
                oldItem.account.accountId == newItem.account.accountId

            override fun areContentsTheSame(
                oldItem: AccountWithBalance,
                newItem: AccountWithBalance
            ) =
                oldItem.account.name == newItem.account.name
                        && oldItem.account.orderPosition == newItem.account.orderPosition
                        && oldItem.account.accountType == newItem.account.accountType
                        && oldItem.account.notes == newItem.account.notes
                        && oldItem.balance == newItem.balance
        }
    }

    private val differ: AsyncListDiffer<AccountWithBalance> = AsyncListDiffer(this, DIFF_CALLBACK)

    class ViewHolder(val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        AccountItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentAccountWithBalance = differ.currentList[position]
        val account: Account = currentAccountWithBalance.account
        val accountIcon = account.accountType.getAssociatedIcon()
        val accountColor = ContextCompat.getColor(
            holder.binding.root.context,
            account.accountType.getAssociatedColor()
        )

        with(holder.binding.accountName) {
            text = account.name
            setTextColor(accountColor)
        }

        with(holder.binding.accountType) {
            text = account.accountType.name
            setTextColor(accountColor)
            setCompoundDrawablesRelativeWithIntrinsicBounds(accountIcon, 0, 0, 0)
            this.refreshDrawableState()
        }

        with(holder.binding.accountBalance) {
            text = "$ ${currentAccountWithBalance.balance.toStringWithDecimalPlaces(2)}"
            setTextColor(accountColor)
        }

        holder.binding.divider.background = ColorDrawable(accountColor)
        holder.binding.root.setOnClickListener {
            onClickListener?.let {
                it(account)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun getItemAtPosition(position: Int): AccountWithBalance = differ.currentList[position]

    fun moveItem(from: Int, to: Int) {
        val currentList: MutableList<AccountWithBalance> = differ.currentList.toMutableList()
        Collections.swap(currentList, from, to)
        submitAccounts(currentList)
    }

    fun submitAccounts(accounts: List<AccountWithBalance>) {
        differ.submitList(accounts)
    }
}