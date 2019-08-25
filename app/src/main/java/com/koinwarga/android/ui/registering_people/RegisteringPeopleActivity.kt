package com.koinwarga.android.ui.registering_people

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import com.koinwarga.android.repositories.RepositoryProvider
import com.koinwarga.android.ui.password.PasswordDialogFragment
import com.koinwarga.android.ui.scanner.ScannerActivity
import kotlinx.android.synthetic.main.activity_registering_people.*

class RegisteringPeopleActivity : BaseActivity() {

    private lateinit var viewModel: RegisteringPeopleVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registering_people)

        viewModel = RegisteringPeopleVM(
            RepositoryProvider.repository(this, this)
        )

        viewModel.viewState.observe(this, Observer {
            when(it) {
                RegisteringPeopleVM.ViewState.SENDING -> onStateSending()
                RegisteringPeopleVM.ViewState.SUCCESS -> onStateSuccess()
                RegisteringPeopleVM.ViewState.FAILED -> onStateFailed()
                else -> return@Observer
            }
        })

        btnScan.setOnClickListener {
            checkPermission()
        }

        btnRegister.setOnClickListener {
            showPinDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 300 && resultCode == 200 && data != null) {
            setAccountIdToForm(data.getStringExtra("accountId") ?: "")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onStateSending() {
        txtTo.isEnabled = false
        btnScan.isEnabled = false
        btnRegister.isEnabled = false
    }

    private fun onStateSuccess() {
        txtTo.isEnabled = true
        btnScan.isEnabled = true
        btnRegister.isEnabled = true
        showDialogMessage("Transaksi berhasil")
    }

    private fun onStateFailed() {
        txtTo.isEnabled = true
        btnScan.isEnabled = true
        btnRegister.isEnabled = true
        showDialogMessage("Transaksi gagal")
    }

    private fun showPinDialog() {
        val passwordDialogFragment = PasswordDialogFragment.newInstance {
            viewModel.registeringPeople(txtTo.text.toString(), it)
        }
        passwordDialogFragment.show(supportFragmentManager, "PasswordDialog")
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
}
