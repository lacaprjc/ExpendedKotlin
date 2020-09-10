package com.lacaprjc.expended.listAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lacaprjc.expended.databinding.TransactionItemBinding
import com.lacaprjc.expended.ui.model.Transaction
import java.time.format.DateTimeFormatter

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
                oldItem.transactionId == newItem.transactionId

            override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
                oldItem.amount == newItem.amount
                        && oldItem.name == newItem.name
                        && oldItem.date == newItem.date
                        && oldItem.forAccountId == newItem.forAccountId
                        && oldItem.media == newItem.media
                        && oldItem.notes == newItem.notes

        }

        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yy")
    }

    private val differ: AsyncListDiffer<Transaction> = AsyncListDiffer(this, DIFF_CALLBACK)

    class ViewHolder(val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        TransactionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = differ.currentList[position]

        holder.binding.transactionName.text = transaction.name
        holder.binding.transactionAmount.text = transaction.amount.toString()
        holder.binding.transactionDate.text = transaction.date.format(DATE_TIME_FORMATTER)
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun submitTransactions(transactions: List<Transaction>) = differ.submitList(transactions)
}