package com.koinwarga.android.ui.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.commons.BaseViewModel
import com.koinwarga.android.repositories.IRepository
import com.koinwarga.android.repositories.Response
import kotlinx.coroutines.launch

class SendVM(
    private val repository: IRepository
) : BaseViewModel() {

    val viewState: LiveData<ViewState>
        get() = mutableViewState
    private val mutableViewState = MutableLiveData<ViewState>()

    fun sendIDR(to: String, amount: Int, isNative: Boolean, password: String) {
        mutableViewState.value = ViewState.SENDING
        launch {
            when(repository.send(to, amount, password, isNative)) {
                is Response.Success -> mutableViewState.value = ViewState.SUCCESS
                is Response.Error -> mutableViewState.value = ViewState.SUCCESS
            }
        }
    }

    enum class ViewState {
        SENDING,
        SUCCESS,
        FAILED
    }

}