package com.koinwarga.android.ui.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.koinwarga.android.commons.BaseViewModel
import com.koinwarga.android.models.Contact
import com.koinwarga.android.repositories.IContactRepository
import com.koinwarga.android.repositories.Response
import com.koinwarga.android.ui.send.SendVM
import kotlinx.coroutines.launch

class ContactVM(
    private val contactRepository: IContactRepository
) : BaseViewModel() {

    val viewState: LiveData<ViewState>
        get() = mutableViewState
    private val mutableViewState = MutableLiveData<ViewState>()

    val contacts: LiveData<List<Contact>>
        get() = mutableContacts
    private val mutableContacts = MutableLiveData<List<Contact>>()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        launch {
            mutableViewState.value = ViewState.LOADING
            when(val contacts = contactRepository.getAllContact()) {
                is Response.Success -> {
                    mutableViewState.value = ViewState.LOADED
                    mutableContacts.value = contacts.body
                }
                is Response.Error -> mutableViewState.value = ViewState.FAILED
            }
        }
    }

    enum class ViewState {
        LOADING,
        LOADED,
        FAILED
    }

}