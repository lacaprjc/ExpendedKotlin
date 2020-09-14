package com.lacaprjc.expended.ui.settings

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.ui.model.Account
import com.lacaprjc.expended.ui.model.AccountWithTransactions
import com.lacaprjc.expended.ui.model.Transaction
import com.lacaprjc.expended.util.DataFormat
import com.lacaprjc.expended.util.DataState
import com.lacaprjc.expended.util.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.nio.channels.FileChannel

@ExperimentalCoroutinesApi
class SettingsViewModel @ViewModelInject constructor(
    private val repository: AccountsWithTransactionsRepository,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val SUCCESS_MESSAGE = "Successfully imported accounts."
    }

    private val dataState: MutableLiveData<DataState<Any>> =
        MutableLiveData<DataState<Any>>(DataState.Idle)

    private var currentDataFormat = DataFormat.JSON

    private val fileContents: MutableLiveData<List<String>> = MutableLiveData(emptyList())

    fun setCurrentDataFormat(format: DataFormat) {
        currentDataFormat = format
    }

    fun getDataState(): LiveData<DataState<Any>> = dataState

    fun getFileContents(): LiveData<List<String>> = fileContents

    private fun importAccount(accountWithTransactions: AccountWithTransactions) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val accountId = repository.addAccount(accountWithTransactions.account)
                accountWithTransactions.transactions.forEach {
                    repository.addTransaction(it.copy(forAccountId = accountId))
                }
            } catch (e: RuntimeException) {
                dataState.postValue(DataState.Failed(e))
            }
        }

    fun setDataState(dataState: DataState<Any>) {
        this.dataState.postValue(dataState)
    }

    fun importFromFile(inputStream: InputStream) =
        viewModelScope.launch(Dispatchers.IO) {
            dataState.postValue(DataState.Loading)

            val reader = BufferedReader(inputStream.bufferedReader())

            try {
                when (currentDataFormat) {
                    DataFormat.JSON -> TODO()
                    DataFormat.CSV -> TODO()
                    DataFormat.SQL -> TODO()
                    DataFormat.SEMBAST -> {
                        // skip the first line
                        if (reader.readLine() == null) {
                            return@launch
                        }

                        reader.forEachLine { json ->
                            val accountWithTransaction = parseSembast(json)
                            importAccount(accountWithTransaction)
                        }
                    }
                }

                dataState.postValue(DataState.Success(SUCCESS_MESSAGE))
            } catch (e: RuntimeException) {
                dataState.postValue(DataState.Failed(e))
            } finally {
                reader.close()
            }
        }

    private fun parseSembast(json: String): AccountWithTransactions {
        return try {
            val sembastJsonAccount = JSONObject(json).getJSONObject("value")
            val account = Account.fromJson(sembastJsonAccount)
            val transactions = mutableListOf<Transaction>()
            val jsonTransactions =
                sembastJsonAccount.getJSONArray("transactions")

            for (i in 0 until jsonTransactions.length()) {
                val transaction = Transaction.fromJson(
                    jsonTransactions.getJSONObject(i),
                    0
                )

                transactions.add(transaction)
            }

            AccountWithTransactions(account, transactions)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun export(outputFileDescriptor: FileDescriptor, context: Context? = null) =
        viewModelScope.launch(Dispatchers.IO) {
            dataState.postValue(DataState.Loading)

            when (currentDataFormat) {
                DataFormat.SQL -> {
                    val databaseFile: File =
                        context!!.getDatabasePath(context.getString(com.lacaprjc.expended.R.string.database_name))
                    exportToSql(outputFileDescriptor, databaseFile)
                }
                DataFormat.JSON -> {
                    exportToJson(outputFileDescriptor)
                }
                DataFormat.CSV -> TODO()
                DataFormat.SEMBAST -> TODO()
            }

            dataState.postValue(DataState.Success("Exported database!"))
        }

    private fun exportToSql(outputFileDescriptor: FileDescriptor, databaseFile: File) {
        var fileInputChannel: FileChannel? = null
        var fileOutputChannel: FileChannel? = null

        try {
            fileInputChannel = FileInputStream(databaseFile).channel
            fileOutputChannel = FileOutputStream(outputFileDescriptor).channel
            fileOutputChannel!!.transferFrom(
                fileInputChannel,
                0,
                fileInputChannel!!.size()
            )
        } catch (e: RuntimeException) {
            dataState.postValue(DataState.Failed(e))
        } finally {
            fileInputChannel?.close()
            fileOutputChannel?.close()
        }
    }

    private fun exportToJson(outputFileDescriptor: FileDescriptor) =
        viewModelScope.launch(Dispatchers.IO) {
            var bufferedWriter: BufferedWriter? = null

            try {
                val allAccountWithTransactions = repository.getAllAccountsWithTransactionsSync()

                val jsonArray = JSONArray()
                allAccountWithTransactions.forEach { currentAccountWithTransactions ->
                    jsonArray.put(currentAccountWithTransactions.toJson())
                }

                bufferedWriter = BufferedWriter(FileWriter(outputFileDescriptor)).apply {
                    write(jsonArray.toString())
                    close()
                }


            } catch (e: RuntimeException) {
                dataState.postValue(DataState.Failed(e))
            } finally {
                bufferedWriter?.close()
            }
        }
}