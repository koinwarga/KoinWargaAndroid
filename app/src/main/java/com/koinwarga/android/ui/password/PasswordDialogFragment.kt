package com.koinwarga.android.ui.password


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.koinwarga.android.R
import kotlinx.android.synthetic.main.fragment_password_dialog.view.*


class PasswordDialogFragment(private val onOkClicked: (String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.fragment_password_dialog, null)

        builder.setView(view)
        builder.setTitle("Masukkan Password")
        builder.setPositiveButton("OK") { dialog, _ ->
            onOkClicked(view.txtPassword.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton("BATAL") { dialog, _ ->
            dialog.dismiss()
        }

        return builder.create()
    }

    companion object {
        fun newInstance(onOkClicked: (String) -> Unit) = PasswordDialogFragment(onOkClicked)
    }

}
