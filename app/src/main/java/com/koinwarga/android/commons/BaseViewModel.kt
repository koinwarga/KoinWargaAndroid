package com.koinwarga.android.commons

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

abstract class BaseViewModel : CoroutineScope by MainScope()