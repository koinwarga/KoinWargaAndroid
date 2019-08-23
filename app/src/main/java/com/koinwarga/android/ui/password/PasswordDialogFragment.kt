package com.koinwarga.android.ui.password


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import androidx.fragment.app.DialogFragment
import com.koinwarga.android.R
import kotlinx.android.synthetic.main.fragment_password_dialog.view.*


class PasswordDialogFragment(private val onOkClicked: (String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.fragment_password_dialog, null)

        view.txtPassword.setAnimationEnable(true)
        view.txtPassword.requestFocus()

        builder.setView(view)
        builder.setTitle("Masukkan PIN")
        builder.setPositiveButton("OK") { dialog, _ ->
            onOkClicked(view.txtPassword.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton("BATAL") { dialog, _ ->
            dialog.dismiss()
        }

        showKeyboard()

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        closeKeyboard()
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance(onOkClicked: (String) -> Unit) = PasswordDialogFragment(onOkClicked)
    }

    private fun showKeyboard() {
        val inputMethodManager =
            context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun closeKeyboard() {
        val inputMethodManager =
            context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(HIDE_IMPLICIT_ONLY, 0)
    }

}
