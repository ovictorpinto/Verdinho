package com.github.ovictorpinto.verdinho.ui.config

import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.ovictorpinto.verdinho.R
import kotlinx.android.synthetic.main.ly_ajuda_dialog.view.*

/**
 * Created by victorpinto on 16/04/18.
 */
class AjudaDialogFrag : DialogFragment() {

    companion object {
        val PARAM_TITULO = "titulo_ajuda"
        val PARAM_CONTEUDO = "conteudo_ajuda"

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //remove fundo do dialog e obedece o shape
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        var viewPrincipal = inflater.inflate(R.layout.ly_ajuda_dialog, null)
        viewPrincipal.titulo.text = arguments.getString(PARAM_TITULO)
        viewPrincipal.conteudo.text = arguments.getString(PARAM_CONTEUDO)

        viewPrincipal.button.setOnClickListener {
            dismiss()
        }

        return viewPrincipal
    }
}