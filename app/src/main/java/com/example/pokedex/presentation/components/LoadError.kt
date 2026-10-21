package com.example.pokedex.presentation.components

import androidx.annotation.StringRes
import com.example.pokedex.R
import retrofit2.HttpException
import java.io.IOException

/** Why a load failed, as the UI tells the user; never the raw exception text. */
enum class LoadError(@StringRes val messageRes: Int) {
    NoConnection(R.string.error_no_connection),
    Server(R.string.error_server),
    Unknown(R.string.error_unknown)
}

fun Throwable.toLoadError(): LoadError = when (this) {
    is IOException -> LoadError.NoConnection
    is HttpException -> LoadError.Server
    else -> LoadError.Unknown
}
