package com.koinwarga.android.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.Repository
import com.koinwarga.android.repositories.Response
import com.koinwarga.android.ui.send.SendActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode

class MainActivity : BaseActivity() {

    private val repository by lazy { Repository(this, this) }
    private lateinit var account: Account
    val viewState: MutableLiveData<ViewState> by lazy {
        MutableLiveData<ViewState>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewState.observe(this, Observer {
            when(it) {
                ViewState.LOADING -> onStateLoading()
                ViewState.ACCOUNT_LOADED -> onStateAccountLoaded()
                ViewState.ACCOUNT_DETAIL_LOADED -> onStateAccountDetailLoaded()
                ViewState.ERROR -> onStateError()
            }
        })

        btnActivateIDR.setOnClickListener { trustIDR() }
        btnSend.setOnClickListener { goToSendPage() }
        btnRegisteringAccount.setOnClickListener { goToSendPage(true) }

        loadAccount()
    }

    private fun onStateLoading() {
        txtXLMBalance.visibility = View.GONE
        vXLMBalanceLoading.visibility = View.VISIBLE
        txtIDRBalance.visibility = View.GONE
        vIDRBalanceLoading.visibility = View.VISIBLE
        txtAccountId.isClickable = false
    }

    private fun onStateAccountLoaded() {
        txtXLMBalance.visibility = View.GONE
        vXLMBalanceLoading.visibility = View.VISIBLE
        txtIDRBalance.visibility = View.GONE
        vIDRBalanceLoading.visibility = View.VISIBLE

        txtAccountId.text = account.accountId
        txtAccountId.isClickable = true
        txtAccountId.setOnClickListener {
            copyAccountToClipboard()
        }
    }

    private fun onStateAccountDetailLoaded() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE

        txtAccountId.text = account.accountId
        txtAccountId.isClickable = true
        txtAccountId.setOnClickListener {
            copyAccountToClipboard()
        }
        txtXLMBalance.text = account.xlm ?: "-"
        txtIDRBalance.text = account.idr ?: "-"
    }

    private fun onStateError() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE
    }

    private fun goToSendPage(isCreateAccount: Boolean = false) {
        Intent(this, SendActivity::class.java).apply {
            putExtra("isCreateAccount", isCreateAccount)
            startActivity(this)
        }
    }

    private fun generateQRCode() {
        val myBitmap = QRCode.from(account.accountId).withSize(200, 200).bitmap()
        vQR.setImageBitmap(myBitmap)
    }

    private fun loadAccount() {
        viewState.value = ViewState.LOADING
        launch(Dispatchers.Main) {
            when(val responseAccount = repository.getAccount()) {
                is Response.Success -> {
                    account = responseAccount.body
                    viewState.value = ViewState.ACCOUNT_LOADED
                    generateQRCode()
                    loadAccountDetail()
                }
                is Response.Error -> {
                    showToast(responseAccount.message)
                    viewState.value = ViewState.ERROR
                }
            }
        }
    }

    private fun loadAccountDetail() {
        launch(Dispatchers.Main) {
            when(val response = repository.getAccountDetail()) {
                is Response.Success -> {
                    account = response.body
                    viewState.value = ViewState.ACCOUNT_DETAIL_LOADED
                }
                is Response.Error -> {
                    showToast(response.message)
                    viewState.value = ViewState.ERROR
                }
            }
        }
    }

    private fun trustIDR() {
        launch(Dispatchers.Main) {
            when(val response = repository.trustIDR()) {
                is Response.Success -> {
                    loadAccountDetail()
                    showDialogMessage("Rupiah diaktifkan")
                }
                is Response.Error -> showDialogMessage("""Rupiah gagal diaktifkan. ${response.message}""")
            }
        }
    }

    private fun copyAccountToClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Source Text", account.accountId)
        clipboardManager.primaryClip = clipData
        showToast("Copy Account ID")
    }

    enum class ViewState {
        LOADING,
        ACCOUNT_LOADED,
        ACCOUNT_DETAIL_LOADED,
        ERROR
    }
}
