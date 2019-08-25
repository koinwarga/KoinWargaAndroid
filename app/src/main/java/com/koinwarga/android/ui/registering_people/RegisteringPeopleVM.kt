package com.koinwarga.android.ui.registering_people

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.commons.BaseViewModel
import com.koinwarga.android.repositories.IRepository
import com.koinwarga.android.repositories.Response
import kotlinx.coroutines.launch

class RegisteringPeopleVM(
    private val repository: IRepository
) : BaseViewModel() {

    val viewState: LiveData<ViewState>
        get() = mutableViewState
    private val mutableViewState = MutableLiveData<ViewState>()

    fun registeringPeople(to: String, pin: String) {
        mutableViewState.value = ViewState.SENDING
        launch {
            when(repository.registeringNewMember(to, pin)) {
                is Response.Success -> mutableViewState.value = ViewState.SUCCESS
                is Response.Error -> mutableViewState.value = ViewState.FAILED
            }
        }
    }

    enum class ViewState {
        SENDING,
        SUCCESS,
        FAILED
    }

}