package com.lacaprjc.expended.util

sealed class DataState<out R> {
    data class Success<out T>(val data: T?) : DataState<T>()
    data class Failed(val exception: Exception) : DataState<Exception>()
    object Loading : DataState<Nothing>()
    object Idle : DataState<Nothing>()
}