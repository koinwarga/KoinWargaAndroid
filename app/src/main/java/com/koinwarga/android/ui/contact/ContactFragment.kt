package com.koinwarga.android.ui.contact


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseFragment
import com.koinwarga.android.models.Contact
import com.koinwarga.android.repositories.RepositoryProvider
import kotlinx.android.synthetic.main.fragment_contact.*


class ContactFragment : BaseFragment() {

    private lateinit var viewModel: ContactVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ContactVM(
            RepositoryProvider.contactRepository(context!!, this)
        )

        viewModel.viewState.observe(this, Observer {
            when(it) {
                ContactVM.ViewState.LOADING -> onStateLoading()
                ContactVM.ViewState.LOADED -> onStateLoaded()
                ContactVM.ViewState.FAILED -> onStateFailed()
                else -> onStateLoading()
            }
        })

        viewModel.contacts.observe(this, Observer {
            if (it != null) {
                onContactsReceived(it)
            }
        })

        btnAdd.setOnClickListener {  }
    }

    private fun onStateLoading() {
        vList.visibility = View.INVISIBLE
    }

    private fun onStateLoaded() {
        vList.visibility = View.VISIBLE
    }

    private fun onStateFailed() {
        vList.visibility = View.INVISIBLE
    }

    private fun onContactsReceived(contacts: List<Contact>) {
        vList.layoutManager = LinearLayoutManager(context)
        vList.adapter = ListAdapter(contacts)
    }
}
