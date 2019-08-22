package com.koinwarga.android.models

data class Account(
    val id: Int,
    val accountId: String,
    val secretKey: String,
    val accountName: String,
    var xlm: String? = null,
    var idr: String? = null,
    var lastPagingToken: String? = null
)