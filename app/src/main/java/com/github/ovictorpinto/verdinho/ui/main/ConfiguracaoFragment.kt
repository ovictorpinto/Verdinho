package com.github.ovictorpinto.verdinho.ui.main

import android.app.Fragment
import android.app.FragmentManager
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11
import br.com.mobilesaude.androidlib.widget.DialogCarregandoV11
import com.github.ovictorpinto.verdinho.Constantes
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.ui.config.AjudaDialogFrag
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper
import com.github.ovictorpinto.verdinho.util.RatingHelper
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
            bundle.putString(AjudaDialogFrag.PARAM_TITULO, getString(R.string.reiniciar_pontos))
            bundle.putString(AjudaDialogFrag.PARAM_CONTEUDO, getString(R.string.reiniciar_pontos_help))
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
        view.avaliar.setOnClickListener({
            AnalyticsHelper(activity).openAvaliar()
            RatingHelper(activity).abreLoja()
        })
        view.reiniciar_ponto.setOnClickListener({
            //verifica se está online
            ProcessoLoadPontos().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        })
        return view
    }

    private inner class ProcessoLoadPontos : com.github.ovictorpinto.verdinho.ui.main.ProcessoLoadPontos(activity) {

        private val fragmentManager: FragmentManager

        init {
            this.fragmentManager = getFragmentManager()
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val carregando = DialogCarregandoV11()
            fragmentManager.beginTransaction().add(carregando, DialogCarregandoV11.FRAGMENT_ID).commitAllowingStateLoss()

        }
        override fun onPostExecute(success: Boolean) {

            var findFragmentByTag = fragmentManager.findFragmentByTag(DialogCarregandoV11.FRAGMENT_ID) as DialogCarregandoV11?
            findFragmentByTag?.dismiss()

            if (!isCancelled) {
                if (!success) {
                    //abrir uma nova janela de erro
                    val alert = AlertDialogFragmentV11.newInstance(null, null, R.string.falha_acesso_servidor)
                    fragmentManager.beginTransaction().add(alert, AlertDialogFragmentV11.FRAGMENT_ID).commitAllowingStateLoss()
                } else {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val editor = preferences.edit()
                    editor.putBoolean(Constantes.pref_loaded, true)
                    editor.apply()
                }
            }
        }
    }

}