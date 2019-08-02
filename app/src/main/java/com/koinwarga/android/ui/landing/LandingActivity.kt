package com.koinwarga.android.ui.landing

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import com.koinwarga.android.repositories.Repository
import com.koinwarga.android.repositories.Response
import com.koinwarga.android.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LandingActivity : BaseActivity() {

    private val repository by lazy { Repository(this, this) }
    val viewState: MutableLiveData<ViewState> by lazy {
        MutableLiveData<ViewState>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        viewState.observe(this, Observer {
            when(it) {
                ViewState.LOADING -> onStateLoading()
                ViewState.EMPTY_ACCOUNT -> onStateEmptyAccount()
            }
        })

        btnNewAccount.setOnClickListener { createNewAccount() }

        checkAccountAvailability()
    }

    private fun goToMainPage() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun onStateLoading() {
        vLoading.visibility = View.VISIBLE
        vActionContainer.visibility = View.INVISIBLE
    }

    private fun onStateEmptyAccount() {
        vLoading.visibility = View.INVISIBLE
        vActionContainer.visibility = View.VISIBLE
    }

    private fun checkAccountAvailability() {
        viewState.value = ViewState.LOADING
        launch(Dispatchers.Main) {
            when(val response = repository.getAccount()) {
                is Response.Success -> goToMainPage()
                is Response.Error -> {
                    when(response.code) {
                        Response.ErrorCode.ERROR_EMPTY -> viewState.value = ViewState.EMPTY_ACCOUNT
                    }
                }
            }
        }
    }

    private fun createNewAccount() {
        launch(Dispatchers.Main) {
            repository.createAccount(true)
            goToMainPage()
        }
    }

    enum class ViewState {
        LOADING,
        EMPTY_ACCOUNT
    }
}
