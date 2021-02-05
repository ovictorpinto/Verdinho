package com.github.ovictorpinto.verdinho.ui.ponto

import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import com.github.ovictorpinto.verdinho.Constantes
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO
import com.github.ovictorpinto.verdinho.to.PontoTO
import kotlinx.android.synthetic.main.ly_rename_dialog.view.*

/**
 * Created by victorpinto on 16/04/18.
 */
class RenomearDialogFrag : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.renomear)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //remove fundo do dialog e obedece o shape
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val ponto: PontoTO = arguments.getSerializable(PontoTO.PARAM) as PontoTO
        val viewPrincipal = inflater.inflate(R.layout.ly_rename_dialog, null)
        viewPrincipal.edittext.setText(ponto.apelido)
        if (ponto.apelido != null) {
            viewPrincipal.edittext.setSelection(ponto.apelido.length)
        }
        viewPrincipal.edittext.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                salvar(ponto, viewPrincipal)
                true
            } else
                false
        }

        viewPrincipal.button.setOnClickListener {
            salvar(ponto, viewPrincipal)
        }

        return viewPrincipal
    }

    private fun salvar(ponto: PontoTO, viewPrincipal: View) {
        ponto.apelido = viewPrincipal.edittext.text.toString()
        //se deixar em branco gravo como null
        if (ponto.apelido.isEmpty())
            ponto.apelido = null
        PontoDAO(activity).update(PontoPO(ponto))
        LocalBroadcastManager.getInstance(activity)
                .sendBroadcast(Intent(Constantes.actionUpdatePontoFavorito))
        dismiss()
    }
}