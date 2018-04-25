package com.github.ovictorpinto.verdinho.ui.main

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window

import com.github.ovictorpinto.verdinho.R
import kotlinx.android.synthetic.main.ly_sobre.view.*

class SobreFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.ly_sobre, null)
        view.bt_close.setOnClickListener({ dismiss() })
        return view
    }
}
