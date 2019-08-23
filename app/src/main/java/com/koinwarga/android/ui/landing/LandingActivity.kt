package com.koinwarga.android.ui.landing

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import com.koinwarga.android.repositories.RepositoryProvider
import com.koinwarga.android.ui.main.MainActivity
import com.koinwarga.android.ui.password.PasswordDialogFragment
import kotlinx.android.synthetic.main.activity_landing.*

class LandingActivity : BaseActivity() {

    private lateinit var viewModel: LandingVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        viewModel = LandingVM(
            RepositoryProvider.repository(this, this)
        )

        viewModel.viewState.observe(this, Observer {
            when(it) {
                LandingVM.ViewState.LOADING -> onStateLoading()
                LandingVM.ViewState.EMPTY_ACCOUNT -> onStateEmptyAccount()
                LandingVM.ViewState.ACCOUNT_AVAILABLE -> onStateAccountAvailable()
                else -> onStateLoading()
            }
        })

        btnNewAccount.setOnClickListener {
            showPasswordDialog()
        }
    }

    private fun goToMainPage() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun showPasswordDialog() {
        val passwordDialogFragment = PasswordDialogFragment.newInstance {
            viewModel.createNewAccount(it)
        }
        passwordDialogFragment.show(supportFragmentManager, "PasswordDialog")
    }

    private fun onStateLoading() {
        vLoading.visibility = View.VISIBLE
        vActionContainer.visibility = View.INVISIBLE
    }

    private fun onStateEmptyAccount() {
        vLoading.visibility = View.INVISIBLE
        vActionContainer.visibility = View.VISIBLE
    }

    private fun onStateAccountAvailable() {
        goToMainPage()
    }

}
