package com.koinwarga.android.ui.send

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import com.koinwarga.android.repositories.Repository
import com.koinwarga.android.repositories.Response
import com.koinwarga.android.ui.password.PasswordDialogFragment
import com.koinwarga.android.ui.scanner.ScannerActivity
import kotlinx.android.synthetic.main.activity_send.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendActivity : BaseActivity() {

    private val repository by lazy { Repository(this, this) }
    private var isCreateAccount = false
    private var isNative = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        isCreateAccount = intent.getBooleanExtra("isCreateAccount", false)

        btnSend.setOnClickListener {
            showPasswordDialog()
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

    private fun showPasswordDialog() {
        val passwordDialogFragment = PasswordDialogFragment.newInstance {
            sendIDR(txtTo.text.toString(), txtAmount.text.toString().toInt(), it)
        }
        passwordDialogFragment.show(supportFragmentManager, "PasswordDialog")
    }

    private fun initChooseCurrency() {
        if (isCreateAccount) {
            vChooseCurrency.visibility = View.GONE
        }
        val currencies = listOf( "XLM", "IDR" )
        val chooseCurrencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
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
        Intent(this, ScannerActivity::class.java).apply {
            startActivityForResult(this, 300)
        }
    }

    private fun checkPermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
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

    private fun sendIDR(to: String, amount: Int, password: String) {
        launch(Dispatchers.Main) {
            if (isCreateAccount) {
                when(repository.registerAccountToNetwork(to, amount)) {
                    is Response.Success -> {
                        showToast("Akun berhasil didaftarkan")
                    }
                    is Response.Error -> showDialogMessage("Gagal mendaftarkan akun")
                }
            }
            when(repository.send(to, amount, password, isNative)) {
                is Response.Success -> {
                    showToast("Uang terkirim")
                }
                is Response.Error -> showDialogMessage("Gagal mengirim uang")
            }
        }
    }
}
