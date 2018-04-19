package com.github.ovictorpinto.verdinho.ui.main

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.ui.config.AjudaDialogFrag
import kotlinx.android.synthetic.main.ly_config.view.*
import kotlinx.android.synthetic.main.ly_toolbar.view.*

/**
 * Created by victorpinto on 18/04/18.
 */
class ConfiguracaoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.ly_config, null)
        view.toolbar.setTitle(R.string.informacoes)

        view.help_sobre.setOnClickListener({
            var dialog = AjudaDialogFrag()
            var bundle = Bundle()
            bundle.putString(AjudaDialogFrag.PARAM_TITULO, "Reiniciar ponto")
            bundle.putString(AjudaDialogFrag.PARAM_CONTEUDO, "Conteúdo do reiniciar ponto")
            dialog.arguments = bundle
            fragmentManager.beginTransaction().add(dialog, null).commitAllowingStateLoss()
        })

        view.sobre.setOnClickListener({
            val fragmentManager = fragmentManager
            val newFragment = SobreFragment()
            fragmentManager.beginTransaction().apply {
                setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                add(android.R.id.content, newFragment)
                addToBackStack(null)
                commit()
            }
        })
        return view
    }

}