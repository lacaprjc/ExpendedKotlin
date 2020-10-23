package com.lacaprjc.expended.ui.settings

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.di.IoDispatcher
import com.lacaprjc.expended.model.Account
import com.lacaprjc.expended.model.AccountWithTransactions
import com.lacaprjc.expended.model.Transaction
import com.lacaprjc.expended.util.DataFormat
import com.lacaprjc.expended.util.DataState
import com.lacaprjc.expended.util.toCsvRow
import com.lacaprjc.expended.util.toJson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class SettingsViewModel @ViewModelInject constructor(
    private val repository: AccountsWithTransactionsRepository,
    @IoDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val accountHeaderName = "Account Name"
        private const val accountHeaderId = "Account ID"
        private const val accountHeaderType = "Type"
        private const val accountHeaderNotes = "Notes"
        private const val accountHeaderBalance = "Balance"
        private const val accountHeaderOrderPosition = "OrderPosition"
        val accountHeaderRow =
            listOf(
                accountHeaderName,
                accountHeaderId,
                accountHeaderType,
                accountHeaderNotes,
                accountHeaderBalance,
                accountHeaderOrderPosition
            )

        private const val transactionHeaderName = "Transaction Name"
        private const val transactionHeaderId = "Transaction ID"
        private const val transactionHeaderForAccountId = "For Account With ID"
        private const val transactionHeaderAmount = "Amount"
        private const val transactionHeaderDate = "Date"
        private const val transactionHeaderNotes = "Notes"

        val transactionHeaderRow = listOf(
            transactionHeaderName,
            transactionHeaderId,
            transactionHeaderForAccountId,
            transactionHeaderAmount,
            transactionHeaderDate,
            transactionHeaderNotes
        )
    }

    private val dataState: MutableLiveData<DataState<Any>> =
        MutableLiveData<DataState<Any>>(DataState.Idle)

    private var currentDataFormat = DataFormat.JSON

    fun setCurrentDataFormat(format: DataFormat) {
        currentDataFormat = format
    }

    fun getDataState(): LiveData<DataState<Any>> = dataState

//    private suspend fun importAccountWithTransactions(accountWithTransactions: AccountWithTransactions) =
//        coroutineScope {
//            val accountId = async {
//                repository.addAccount(accountWithTransactions.account)
//            }
//
//            accountWithTransactions.transactions.forEach {
//                repository.addTransaction(it.copy(forAccountId = accountId.await()))
//            }
//        }

    fun setDataState(dataState: DataState<Any>) {
        this.dataState.postValue(dataState)
    }

    fun import(inputFileDescriptor: FileDescriptor) =
        viewModelScope.launch(defaultDispatcher) {
            dataState.postValue(DataState.Loading)

            val job = Job()
            launch(job + CoroutineExceptionHandler { _, throwable ->
                dataState.postValue(
                    DataState.Failed(
                        RuntimeException(
                            "Failed to import file",
                            throwable
                        )
                    )
                )
                throwable.printStackTrace()
            }) {
                when (currentDataFormat) {
                    DataFormat.JSON -> importJson(inputFileDescriptor)
                    DataFormat.CSV -> importCsv(inputFileDescriptor)
                    DataFormat.SEMBAST -> importSembast(inputFileDescriptor)
                }
            }.apply {
                invokeOnCompletion {
                    if (it == null) {
                        dataState.postValue(DataState.Success("Imported ${currentDataFormat.name}"))
                    }
                }
            }

            job.join()
        }

    private suspend fun importJson(inputFileDescriptor: FileDescriptor) = coroutineScope {
        val reader = FileReader(inputFileDescriptor).buffered()
        val jobs = mutableListOf<Job>()
        reader.forEachLine {
            val allAccountsWithTransactionsAsJson = JSONArray(it)
            for (i in 0 until allAccountsWithTransactionsAsJson.length()) {
                jobs.add(launch {
                    val currentAccountWithTransactions: AccountWithTransactions =
                        AccountWithTransactions.fromJson(
                            allAccountsWithTransactionsAsJson.getJSONObject(i),
                            false
                        )

                    val accountId = async {
                        repository.addAccount(currentAccountWithTransactions.account)
                    }

                    currentAccountWithTransactions.transactions.forEach { transaction ->
                        repository.addTransaction(transaction.copy(forAccountId = accountId.await()))
                    }
                })
            }
        }

        jobs.joinAll()
    }

    private suspend fun importCsv(inputFileDescriptor: FileDescriptor) = coroutineScope {
        CsvReader().open(FileInputStream(inputFileDescriptor)) {
            var row = readNext()
            var currentAccountId = 0L
            while (row != null) {
                if (row == accountHeaderRow) {
                    // account
                    val accountRow = readNext()!!
                    val accountMap = accountHeaderRow.zip(accountRow).toMap()
                    val account = Account(
                        name = accountMap[accountHeaderName]
                            ?: error("Missing $accountHeaderName"),
                        notes = accountMap[accountHeaderNotes]
                            ?: error("Missing $accountHeaderNotes"),
                        accountType = Account.AccountType.valueOf(
                            accountMap[accountHeaderType] ?: error("Missing $accountHeaderType")
                        ),
                        orderPosition = (accountMap[accountHeaderOrderPosition] as Int?) ?: -1
                    )

                    runBlocking {
                        currentAccountId = withContext(Dispatchers.Default) {
                            repository.addAccount(account)
                        }
                    }
                } else if (row[0] == "") {
                    // end of AccountWithTransactions
                    row = readNext()
                    continue
                } else if (row == transactionHeaderRow) {
                    row = readNext()
                    continue
                } else {
                    // transactions
                    val transactionRow = row
                    val transactionsMap = transactionHeaderRow.zip(transactionRow).toMap()
                    val transaction = Transaction(
                        name = transactionsMap[transactionHeaderName]
                            ?: error("Missing $transactionHeaderName"),
                        forAccountId = currentAccountId,
                        amount = transactionsMap[transactionHeaderAmount]?.toDouble()
                            ?: error("Missing $transactionHeaderAmount"),
                        date = try {
                            LocalDateTime.parse(transactionsMap[transactionHeaderDate])
                        } catch (e: RuntimeException) {
                            error("Missing $transactionHeaderDate")
                        },
                        notes = transactionsMap[transactionHeaderNotes]
                            ?: error("Missing $transactionHeaderNotes"),
                        media = ""
                    )

                    runBlocking {
                        repository.addTransaction(transaction)
                    }
                }

                row = readNext()
            }
        }
    }


    private suspend fun importSembast(inputFileDescriptor: FileDescriptor) = coroutineScope {
        val reader = FileReader(inputFileDescriptor).buffered()
        val allAccountsWithTransactions: MutableMap<Account, List<Transaction>> = mutableMapOf()
        val jobs = mutableListOf<Job>()
        reader.forEachLine { json ->
            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val jsonSembastAccount = jsonArray.getJSONObject(i)
                val accountWithTransactions = parseSembast(jsonSembastAccount)

                val currentTransactions = allAccountsWithTransactions[accountWithTransactions.first]
                if (currentTransactions != null) {
                    val mergedTransactions =
                        (accountWithTransactions.second + currentTransactions).distinctBy {
                            listOf(it.name, it.amount, it.forAccountId, it.notes, it.media, it.date)
                        }.toMutableList()

                    val startingBalanceTransactions = mergedTransactions.filter {
                        it.name == "Starting Balance" && it.notes == "Imported initial Transaction"
                    }

                    if (startingBalanceTransactions.size > 1) {
                        startingBalanceTransactions.takeLast(startingBalanceTransactions.size - 1)
                            .forEach {
                                mergedTransactions.remove(it)
                            }
                    }


                    allAccountsWithTransactions.replace(
                        accountWithTransactions.first,
                        mergedTransactions
                    )
                } else {
                    allAccountsWithTransactions[accountWithTransactions.first] =
                        accountWithTransactions.second
                }
            }

            allAccountsWithTransactions.forEach { currentAccountWithTransactions ->
                jobs.add(launch {
                    val accountId = async(defaultDispatcher) {
                        repository.addAccount(currentAccountWithTransactions.key)
                    }

                    val transactions = currentAccountWithTransactions.value.toMutableList()
//                    val startingBalance = transactions.sumOf {
//                        it.amount
//                    }

//                    val originalStartingBalance = transactions.find {
//                        it.name == "Starting Balance" &&
//                                it.notes == "Imported initial Transaction"
//                    }

//                    transactions.add(
//                        Transaction(
//                            name = "Starting Balance",
//                            notes = "Imported initial Transaction",
//                            amount = startingBalance,
//                            forAccountId = 0,
//                            media = "",
//                            date = LocalDateTime.now()
//                        )
//                    )

                    transactions.forEach {
                        repository.addTransaction(it.copy(forAccountId = accountId.await()))
                    }
                })
            }

        }

        jobs.joinAll()
    }

    private fun parseSembast(json: JSONObject): Pair<Account, List<Transaction>> {
        val sembastJsonAccount = json.getJSONObject("value")
        val accountWithBalance = Account.fromJsonSembast(sembastJsonAccount)
        val transactions = mutableListOf<Transaction>()
        val jsonTransactions =
            sembastJsonAccount.getJSONArray("transactions")

        for (i in 0 until jsonTransactions.length()) {
            val transaction = Transaction.fromJsonSembast(
                jsonTransactions.getJSONObject(i)
            )

            transactions.add(transaction)
        }

        val startingBalance = transactions.sumOf {
            it.amount
        }

        transactions.add(
            Transaction(
                name = "Starting Balance",
                notes = "Imported initial Transaction",
                amount = accountWithBalance.second - startingBalance,
                forAccountId = accountWithBalance.first.accountId,
                media = "",
                date = LocalDateTime.now()
            )
        )

        return accountWithBalance.first to transactions
    }

    fun export(outputFileDescriptor: FileDescriptor, context: Context) =
        viewModelScope.launch(defaultDispatcher) {
            repository.checkpoint()
            dataState.postValue(DataState.Loading)

            when (currentDataFormat) {
                DataFormat.JSON -> {
                    exportToJson(outputFileDescriptor)
                }
                DataFormat.CSV -> exportToCsv(outputFileDescriptor)
                else -> {
                }
            }

            dataState.postValue(DataState.Success("Exported ${currentDataFormat.name}"))
        }

    private fun exportToCsv(outputFileDescriptor: FileDescriptor) =
        viewModelScope.launch(defaultDispatcher) {
            try {
                val allAccountWithTransactions =
                    repository.getAllAccountsWithTransactionsSync()
                val allRows = mutableListOf<List<String>>()

                allAccountWithTransactions.forEach { currentAccountWithTransactions ->
                    val transactionsRows = mutableListOf<List<String>>()
                    var accountBalance = 0.0
                    currentAccountWithTransactions.transactions.forEach { currentTransaction ->
                        transactionsRows.add(currentTransaction.toCsvRow())
                        accountBalance += currentTransaction.amount
                    }

                    allRows.add(accountHeaderRow)
                    val accountRow =
                        currentAccountWithTransactions.account.toCsvRow()
                            .toMutableList().apply {
                                add(accountBalance.toString())
                            }
                    allRows.add(accountRow)
                    allRows.add(transactionHeaderRow)
                    transactionsRows.forEach {
                        allRows.add(it)
                    }

                    // blank line
                    allRows.add(listOf())
                }

                CsvWriter().writeAll(
                    allRows,
                    FileOutputStream(outputFileDescriptor)
                )
            } catch (e: RuntimeException) {
                dataState.postValue(DataState.Failed(e))
            }
        }

    private fun exportToJson(outputFileDescriptor: FileDescriptor) =
        viewModelScope.launch(defaultDispatcher) {
            var bufferedWriter: BufferedWriter? = null

            try {
                val allAccountWithTransactions =
                    repository.getAllAccountsWithTransactionsSync()

                val jsonArray = JSONArray()
                allAccountWithTransactions.forEach { currentAccountWithTransactions ->
                    jsonArray.put(currentAccountWithTransactions.toJson())
                }

                bufferedWriter =
                    BufferedWriter(FileWriter(outputFileDescriptor)).apply {
                        write(jsonArray.toString())
                        close()
                    }

            } finally {
                bufferedWriter?.close()
            }
        }

    fun deleteAll() = viewModelScope.launch(defaultDispatcher) {
        dataState.postValue(DataState.Loading)
        repository.deleteAll()
        dataState.postValue(DataState.Success("Successfully deleted ALL Accounts and Transactions"))
    }
}