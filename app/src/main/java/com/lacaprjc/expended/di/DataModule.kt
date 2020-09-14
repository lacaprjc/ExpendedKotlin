package com.lacaprjc.expended.di

import android.content.Context
import androidx.room.Room
import com.lacaprjc.expended.R
import com.lacaprjc.expended.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        context.getString(R.string.database_name)
    )
        .build()

    @Singleton
    @Provides
    fun provideAccountDao(appDatabase: AppDatabase) = appDatabase.accountDao()
}