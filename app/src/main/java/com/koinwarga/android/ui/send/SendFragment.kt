package com.koinwarga.android.ui.send

import android.Manifest.permission
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseFragment
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.Repository
import com.koinwarga.android.repositories.Response
import com.koinwarga.android.ui.scanner.ScannerActivity
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SendFragment : BaseFragment() {

    private val repository by lazy { Repository(context!!, this) }
    private var isCreateAccount = false
    private var isNative = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_send, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCreateAccount = arguments?.getBoolean("isCreateAccount") ?: false

        btnSend.setOnClickListener {
            val to = Account(
                accountId = txtTo.text.toString(),
                secretKey = ""
            )
            sendIDR(to, txtAmount.text.toString().toInt())
        }

        btnScan.setOnClickListener {
            checkPermission()
        }

        initChooseCurrency()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 300 && resultCode == 200 && data != null) {
            setAccountIdToForm(data.getStringExtra("accountId"))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initChooseCurrency() {
        if (isCreateAccount) {
            vChooseCurrency.visibility = View.GONE
        }
        val currencies = listOf( "XLM", "IDR" )
        val chooseCurrencyAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, currencies)
        chooseCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        vChooseCurrency.adapter = chooseCurrencyAdapter
        vChooseCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                isNative = currencies[position] == "XLM"
            }
        }
    }

    private fun setAccountIdToForm(accountId: String) {
        txtTo.setText(accountId)
    }

    private fun goToScannerPage() {
        Intent(context, ScannerActivity::class.java).apply {
            startActivityForResult(this, 300)
        }
    }

    private fun checkPermission() {
        Dexter.withActivity(activity)
            .withPermission(permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    goToScannerPage()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    showToast("Anda harus mengijinkan kamera untuk melakukan scan qrcode")
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun sendIDR(to: Account, amount: Int) {
        launch(Dispatchers.Main) {
            if (isCreateAccount) {
                when(repository.registerAccountToNetwork(to, amount)) {
                    is Response.Success -> {
                        showToast("Akun berhasil didaftarkan")
                    }
                    is Response.Error -> showDialogMessage("Gagal mendaftarkan akun")
                }
            }
            when(repository.send(to, amount, isNative)) {
                is Response.Success -> {
                    showToast("Uang terkirim")
                }
                is Response.Error -> showDialogMessage("Gagal mengirim uang")
            }
        }
    }

    companion object {
        fun newInstance(isCreateAccount: Boolean): Fragment {
            return SendFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isCreateAccount", isCreateAccount)
                }
            }
        }
    }
}
