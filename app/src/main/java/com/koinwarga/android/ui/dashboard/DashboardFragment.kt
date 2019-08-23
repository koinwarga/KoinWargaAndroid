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
import androidx.lifecycle.Observer
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseFragment
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.RepositoryProvider
import com.koinwarga.android.ui.password.PasswordDialogFragment
import com.koinwarga.android.ui.pay_scanner.PayScannerActivity
import com.koinwarga.android.ui.send.SendActivity
import kotlinx.android.synthetic.main.fragment_dashboard.*
import net.glxn.qrgen.android.QRCode


class DashboardFragment : BaseFragment() {

    private lateinit var viewModel: DashboardVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtAccountId.isClickable = true
        txtAccountId.setOnClickListener {
            copyAccountToClipboard(txtAccountId.text.toString())
        }

        btnActivateIDR.setOnClickListener {
            val passwordDialogFragment = PasswordDialogFragment.newInstance {
                trustIDR(it)
            }
            passwordDialogFragment.show(activity?.supportFragmentManager, "PasswordDialog")
        }

        btnSend.setOnClickListener { onSendClicked() }
        btnPay.setOnClickListener { onPayClicked() }
        btnRegisterPerson.setOnClickListener { onRegisteringPersonClicked() }

        viewModel = DashboardVM(
            RepositoryProvider.repository(context!!, this)
        )

        viewModel.viewState.observe(this, Observer {
            when(it) {
                DashboardVM.ViewState.LOADING -> onStateLoading()
                DashboardVM.ViewState.ACCOUNT_LOADED_FULLY -> onStateAccountDetailLoadedFully()
                else -> Log.d("test", "empty state")
            }
        })

        viewModel.account.observe(this, Observer {
            onStateReceiveAccount(it)
        })

//        viewState.observe(this, Observer {
//            when(it) {
//                ViewState.LOADING -> onStateLoading()
//                ViewState.ACCOUNT_LOADED -> onStateAccountLoaded()
//                ViewState.ACCOUNT_DETAIL_LOADED_NO_BALANCE -> onStateAccountDetailLoadedNoBalance()
//                ViewState.ACCOUNT_DETAIL_LOADED_XLM_ONLY -> onStateAccountDetailLoadedXLMOnly()
//                ViewState.ACCOUNT_DETAIL_LOADED_FULLY -> onStateAccountDetailLoadedFully()
//                ViewState.ERROR -> onStateError()
//                else -> Log.d("test", "empty state")
//            }
//        })
//
//        accountLiveData.observe(this, Observer {
//            if (it != null) {
//                txtIDRBalance.text = if (it.idr.isNullOrEmpty()) "" else it.idr
//            }
//        })



//        loadAccount()
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
    }

    private fun onStateAccountDetailLoadedNoBalance() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.GONE
    }

    private fun onStateAccountDetailLoadedXLMOnly() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.GONE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.VISIBLE
    }

    private fun onStateAccountDetailLoadedFully() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.GONE
    }

    private fun onStateError() {
        txtXLMBalance.visibility = View.VISIBLE
        vXLMBalanceLoading.visibility = View.GONE
        txtIDRBalance.visibility = View.VISIBLE
        vIDRBalanceLoading.visibility = View.GONE
        btnActivateIDR.visibility = View.GONE
    }

    private fun onStateReceiveAccount(account: Account) {
        txtAccountName.text = account.accountName
        txtAccountId.text = account.accountId
        txtXLMBalance.text = account.xlm
        txtIDRBalance.text = account.idr
        generateQRCode(account.accountId)
    }

    private fun onPayClicked() {
        Intent(context, PayScannerActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun onRegisteringPersonClicked() {
        Intent(context, SendActivity::class.java).apply {
            putExtra("isCreateAccount", true)
            startActivity(this)
        }
    }

    private fun onSendClicked() {
        Intent(context, SendActivity::class.java).apply {
            putExtra("isCreateAccount", false)
            startActivity(this)
        }
    }

    private fun generateQRCode(accountId: String) {
        val myBitmap = QRCode.from(accountId).withColor(0xFF000000.toInt(),
            0x00FFFFFF.toInt()
        ).bitmap()
        vQR.setImageBitmap(myBitmap)
    }

    private fun loadAccount() {
//        viewState.value = ViewState.LOADING
//        launch(Dispatchers.Main) {
//            when(val responseAccount = repository.getAccount()) {
//                is Response.Success -> {
//                    accountLiveData.postValue(responseAccount.body.value)
////                    viewState.value = ViewState.ACCOUNT_LOADED
////                    generateQRCode()
////                    loadAccountDetail()
//                }
//                is Response.Error -> {
//                    showToast(responseAccount.message)
//                    viewState.value = ViewState.ERROR
//                }
//            }
//        }
    }

    private fun loadAccountDetail() {
//        launch(Dispatchers.Main) {
//            when(val response = repository.getAccountDetail()) {
//                is Response.Success -> {
//                    account = response.body
//                    checkAccountAvailability(account)
//                }
//                is Response.Error -> {
//                    showToast(response.message)
//                    viewState.value = ViewState.ERROR
//                }
//            }
//        }
    }

    private fun trustIDR(password: String) {
//        viewState.value = ViewState.LOADING
//        launch(Dispatchers.Main) {
//            when(val response = repository.trustIDR(password)) {
//                is Response.Success -> {
//                    loadAccountDetail()
//                    showDialogMessage("Rupiah diaktifkan")
//                }
//                is Response.Error -> showDialogMessage("""Rupiah gagal diaktifkan. ${response.message}""")
//            }
//        }
    }

    private fun copyAccountToClipboard(accountId: String) {
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Source Text", accountId)
        clipboardManager.primaryClip = clipData
        showToast("Copy Account ID")
    }

    companion object {
        fun newInstance(): Fragment {
            return DashboardFragment()
        }
    }
}
