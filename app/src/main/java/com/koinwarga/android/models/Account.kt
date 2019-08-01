package com.koinwarga.android.models

data class Account(
    val id: Int = -1,
    val accountId: String,
    val secretKey: String,
    var xlm: String? = null,
    var idr: String? = null,
    var lastPagingToken: String? = null
)