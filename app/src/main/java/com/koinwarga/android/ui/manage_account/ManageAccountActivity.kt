package com.koinwarga.android.ui.manage_account

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseActivity
import com.koinwarga.android.models.Account
import com.koinwarga.android.repositories.RepositoryProvider
import com.koinwarga.android.repositories.Response
import com.koinwarga.android.services.ReceiverWorker
import kotlinx.android.synthetic.main.activity_manage_account.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class ManageAccountActivity : BaseActivity() {

    private val repository by lazy { RepositoryProvider.repository(this, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadAllAccount()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_manage_account, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.btnAdd -> createNewAccount()
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    private fun loadAllAccount() {
//        launch(Dispatchers.Main) {
//            when(val response = repository.getAllAccount()) {
//                is Response.Success -> {
//                    val listAdapter = ListAdapter(response.body)
//                    listAdapter.setOnAccountSelected = {
//                        onAccountSelected(it)
//                    }
//                    vList.layoutManager = LinearLayoutManager(this@ManageAccountActivity)
//                    vList.adapter = listAdapter
//                    vList.addItemDecoration(DividerItemDecoration(this@ManageAccountActivity, LinearLayoutManager.VERTICAL))
//                }
//                is Response.Error -> Log.d("test", "error")
//            }
//        }
    }

    private fun createNewAccount() {
//        launch(Dispatchers.Main) {
//            when(val response = repository.createAccount(false)) {
//                is Response.Success -> loadAllAccount()
//                is Response.Error -> Log.d("test", "error")
//            }
//        }
    }

    private fun onAccountSelected(account: Account) {
//        launch(Dispatchers.Main) {
//            when(val response = repository.setAccountDefault(account)) {
//                is Response.Success -> {
//                    WorkManager.getInstance(this@ManageAccountActivity).cancelAllWork()
//                    val receiverWorkRequest = PeriodicWorkRequestBuilder<ReceiverWorker>(
//                        15, TimeUnit.MINUTES)
//                        .build()
//                    WorkManager.getInstance(this@ManageAccountActivity).enqueue(receiverWorkRequest)
//                    finish()
//                }
//                is Response.Error -> Log.d("test", "error")
//            }
//        }
    }
}
