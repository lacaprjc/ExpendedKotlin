package com.lacaprjc.expended.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lacaprjc.expended.common.TestCoroutineRule
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import com.lacaprjc.expended.model.AccountWithTransactions
import com.lacaprjc.expended.ui.settings.SettingsViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    private lateinit var mockedRepository: AccountsWithTransactionsRepository
    private lateinit var mockedLiveDataOfAccountsWithTranscations: Flow<List<AccountWithTransactions>>
    private lateinit var settingsViewModel: SettingsViewModel


    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        mockedRepository = mock()
        mockedLiveDataOfAccountsWithTranscations = mock()
        whenever(mockedRepository.getAllAccountsWithTransactions()).thenReturn(
            mockedLiveDataOfAccountsWithTranscations
        )
        settingsViewModel =
            SettingsViewModel(mockedRepository, testCoroutineRule.testDispatcher, mock())
    }

    @Test
    fun `test delete all accounts and transactions should call repository delete all`() =
        runBlockingTest {
            settingsViewModel.deleteAll()
            verify(mockedRepository).deleteAll()
        }
}