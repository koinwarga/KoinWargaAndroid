package com.koinwarga.android.ui.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.commons.BaseViewModel
import com.koinwarga.android.repositories.IRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LandingVM(
    private val repository: IRepository
) : BaseViewModel() {

    val viewState: LiveData<ViewState>
        get() = mutableViewState
    private val mutableViewState = MutableLiveData<ViewState>()

    init {
        checkAccountAvailability()
    }

    private fun checkAccountAvailability() {
        mutableViewState.value = ViewState.LOADING
        launch {
            val isAccountAvailable = repository.isAccountAvailable()
            if (isAccountAvailable) {
                mutableViewState.value = ViewState.ACCOUNT_AVAILABLE
            } else {
                mutableViewState.value = ViewState.EMPTY_ACCOUNT
            }
        }
    }

    fun createNewAccount(password: String) {
        launch(Dispatchers.Main) {
            val createAccountSuccess = repository.createNewAccount("Akun Utama", password)
            if (createAccountSuccess) {
                mutableViewState.value = ViewState.ACCOUNT_AVAILABLE
            } else {
                mutableViewState.value = ViewState.EMPTY_ACCOUNT
            }
        }
    }

    enum class ViewState {
        LOADING,
        EMPTY_ACCOUNT,
        ACCOUNT_AVAILABLE
    }

}