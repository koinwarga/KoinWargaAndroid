package com.koinwarga.android.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.commons.BaseViewModel
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.IRepository
import kotlinx.coroutines.launch

class DashboardVM(
    private val repository: IRepository
) : BaseViewModel() {

    val viewState: LiveData<ViewState>
        get() = mutableViewState
    private val mutableViewState = MutableLiveData<ViewState>()

    val account: LiveData<Account>
        get() = mutableAccount
    private val mutableAccount = MediatorLiveData<Account>()

    init {
        loadAccount()
    }

    private fun loadAccount() {
        mutableViewState.value = ViewState.LOADING
        launch {
            mutableAccount.addSource(repository.getActiveAccount()) {
                mutableViewState.value = ViewState.ACCOUNT_LOADED_FULLY
                mutableAccount.value = it
            }
        }
    }

    enum class ViewState {
        LOADING,
        ACCOUNT_LOADED_NO_BALANCE,
        ACCOUNT_LOADED_XLM_ONLY,
        ACCOUNT_LOADED_FULLY
    }

}