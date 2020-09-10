package com.lacaprjc.expended.listAdapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lacaprjc.expended.databinding.AccountItemBinding
import com.lacaprjc.expended.ui.model.Account
import com.lacaprjc.expended.util.getAssociatedColor
import com.lacaprjc.expended.util.getAssociatedIcon

class AccountAdapter(
    private val onClickListener: ((Account) -> Unit)? = null,
    private val onLongClickListener: ((Account) -> Unit)? = null
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Account>() {
            override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean =
                oldItem.accountId == newItem.accountId

            override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean =
                oldItem.name == newItem.name
                        && oldItem.balance == newItem.balance
                        && oldItem.accountType == newItem.accountType
                        && oldItem.notes == newItem.notes

        }
    }

    private val differ: AsyncListDiffer<Account> = AsyncListDiffer(this, DIFF_CALLBACK)

    class ViewHolder(val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        AccountItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account: Account = differ.currentList[position]
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
            text = "$ ${account.balance}"
            setTextColor(accountColor)
        }

        holder.binding.divider.background = ColorDrawable(accountColor)
        holder.binding.root.setOnClickListener {
            onClickListener?.let {
                it(account)
            }
        }
        holder.binding.root.setOnLongClickListener {
            onLongClickListener?.let {
                it(account)
            }
            true
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun submitAccounts(accounts: List<Account>) {
        differ.submitList(accounts)
    }
}