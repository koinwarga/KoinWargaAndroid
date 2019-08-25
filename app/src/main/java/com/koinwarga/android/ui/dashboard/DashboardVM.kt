package com.koinwarga.android.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.commons.BaseViewModel
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.IRepository
import com.koinwarga.android.repositories.Response
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

    val msgPopup: LiveData<String>
        get() = mutableMsgPopup
    private val mutableMsgPopup = MutableLiveData<String>()

    init {
        loadAccount()
    }

    private fun loadAccount() {
        mutableViewState.value = ViewState.LOADING
        mutableAccount.addSource(repository.getActiveAccountLiveData()) {
            if (it != null) {
                if (it.xlm == null && it.idr == null) {
                    mutableViewState.value = ViewState.ACCOUNT_LOADED_NO_BALANCE
                } else if (it.xlm != null && it.idr == null) {
                    mutableViewState.value = ViewState.ACCOUNT_LOADED_XLM_ONLY
                } else {
                    mutableViewState.value = ViewState.ACCOUNT_LOADED_FULLY
                }
                mutableAccount.value = it
            } else {
                mutableViewState.value = ViewState.LOADING
            }
        }
    }

    fun trustIDR(password: String) {
        launch {
            when(val response = repository.trustIDR(password)) {
                is Response.Success -> {
                    mutableViewState.value = ViewState.ACCOUNT_LOADED_FULLY
                }
                is Response.Error -> mutableMsgPopup.value = """Rupiah gagal diaktifkan. ${response.message}"""
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