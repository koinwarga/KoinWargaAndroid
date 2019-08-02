package com.koinwarga.android.ui.history

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.koinwarga.android.R
import com.koinwarga.android.commons.BaseFragment
import com.koinwarga.android.repositories.Repository
import com.koinwarga.android.repositories.Response
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryFragment : BaseFragment() {

    private val repository by lazy { Repository(context!!, this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadHistories()
    }

    private fun loadHistories() {
        launch(Dispatchers.Main) {
            when(val response = repository.accountHistories()) {
                is Response.Success -> {
                    Log.d("test", """Banyak history ${response.body.size}""")
                    vList.layoutManager = LinearLayoutManager(context)
                    vList.adapter = ListAdapter(response.body)
                    vList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                }
                is Response.Error -> Log.d("test", "error")
            }
        }
    }

    companion object {
        fun newInstance(): Fragment {
            return HistoryFragment()
        }
    }

}
