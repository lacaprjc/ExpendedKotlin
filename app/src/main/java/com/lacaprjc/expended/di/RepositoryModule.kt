package com.lacaprjc.expended.di

import com.lacaprjc.expended.data.AccountDao
import com.lacaprjc.expended.data.AccountsWithTransactionsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAccountsWithTransacationsRepository(accountDao: AccountDao) = AccountsWithTransactionsRepository(accountDao)
}