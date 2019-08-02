package com.koinwarga.android.models

data class History(
    val id: String,
    val type: String,
    val createdAt: String,
    val isIDR: Boolean,
    val amount: String
)