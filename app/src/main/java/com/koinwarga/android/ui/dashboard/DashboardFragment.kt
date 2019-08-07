package com.koinwarga.android.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseFragment
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.Repository
import com.koinwarga.android.repositories.Response
import com.koinwarga.android.ui.pay_scanner.PayScannerActivity
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode

class DashboardFragment : BaseFragment() {

    private val repository by lazy { Repository(context!!, this) }
    private lateinit var account: Account
    val viewState: MutableLiveData<ViewState> by lazy {
        MutableLiveData<ViewState>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewState.observe(this, Observer {
            when(it) {
                ViewState.LOADING -> onStateLoading()
                ViewState.ACCOUNT_LOADED -> onStateAccountLoaded()
                ViewState.ACCOUNT_DETAIL_LOADED_NO_BALANCE -> onStateAccountDetailLoadedNoBalance()
                ViewState.ACCOUNT_DETAIL_LOADED_XLM_ONLY -> onStateAccountDetailLoadedXLMOnly()
                ViewState.ACCOUNT_DETAIL_LOADED_FULLY -> onStateAccountDetailLoadedFully()
                ViewState.ERROR -> onStateError()
                else -> Log.d("test", "empty state")
            }
        })

        txtAccountId.isClickable = true
        txtAccountId.setOnClickListener {
            copyAccountToClipboard()
        }

        btnActivateIDR.setOnClickListener {
            trustIDR()
        }

        btnPay.setOnClickListener { onPayClicked() }

        loadAccount()
    }

    private fun onStateLoading() {
        txtXLMBalance.visibility = View.GONE
        vXLMBalanceLoading.visibility = View.VISIBLE
        txtIDRBalance.visibility = View.GONE
        vIDRBalanceLoading.visibility = View.VISIBLE
        btnActivateIDR.visibility = View.INVISIBLE
    }

    private fun onStateAccountLoaded() {
        txtXLMBalance.visibility = View.GONE
        vXLMBalanceLoading.visibility = View.VISIBLE
        txtIDRBalance.visibility = View.GONE
        vIDRBalanceLoading.visibility = View.VISIBLE
        btnActivateIDR.visibility = View.GONE

        txtAccountId.text = account.accountId
    }

    private fun onStateAccountDetailLoadedNoBalance() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.GONE

        txtAccountId.text = account.accountId
        txtXLMBalance.text = "-"
        txtIDRBalance.text = "-"
    }

    private fun onStateAccountDetailLoadedXLMOnly() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.GONE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.VISIBLE

        txtAccountId.text = account.accountId
        txtXLMBalance.text = account.xlm
    }

    private fun onStateAccountDetailLoadedFully() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.GONE

        txtAccountId.text = account.accountId
        txtXLMBalance.text = account.xlm
        txtIDRBalance.text = account.idr
    }

    private fun onStateError() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.GONE
    }

    private fun onPayClicked() {
        Intent(context, PayScannerActivity::class.java).apply {
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
                    checkAccountAvailability(account)
                }
                is Response.Error -> {
                    showToast(response.message)
                    viewState.value = ViewState.ERROR
                }
            }
        }
    }

    private fun checkAccountAvailability(account: Account) {
        if(account.xlm == null && account.idr == null) {
            viewState.value = ViewState.ACCOUNT_DETAIL_LOADED_NO_BALANCE
        } else if (account.idr == null) {
            viewState.value = ViewState.ACCOUNT_DETAIL_LOADED_XLM_ONLY
        } else {
            viewState.value = ViewState.ACCOUNT_DETAIL_LOADED_FULLY
        }
    }

    private fun trustIDR() {
        viewState.value = ViewState.LOADING
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
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Source Text", account.accountId)
        clipboardManager.primaryClip = clipData
        showToast("Copy Account ID")
    }

    enum class ViewState {
        LOADING,
        ACCOUNT_LOADED,
        ACCOUNT_DETAIL_LOADED_NO_BALANCE,
        ACCOUNT_DETAIL_LOADED_XLM_ONLY,
        ACCOUNT_DETAIL_LOADED_FULLY,
        ERROR
    }

    companion object {
        fun newInstance(): Fragment {
            return DashboardFragment()
        }
    }
}
