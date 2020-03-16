package com.github.ovictorpinto.verdinho.ui.ponto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import br.com.tcsistemas.common.date.DataHelper
import br.com.tcsistemas.common.string.StringHelper
import com.github.ovictorpinto.verdinho.BuildConfig
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.to.Estimativa
import com.github.ovictorpinto.verdinho.to.LinhaTO
import com.github.ovictorpinto.verdinho.to.PontoTO
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.ly_item_trecho.view.*
import java.util.*

/**
 * Created by Suleiman on 14-04-2015.
 */
class EstimativaTrechoRecyclerAdapter(
        internal var context: Context,
        private val estimativas: List<Estimativa>,
        private val horarioDoServidor: Long,
        private val mapLinhas: Map<Int, LinhaTO>,
        private val pontoOrigem: PontoTO,
        private val pontoDestino: PontoTO) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private val ITEM = 11
        private val ADS = 12
    }

    private val analyticsHelper = AnalyticsHelper(context)
    private var adView: AdView? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {

        if (viewType == ITEM) {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.ly_item_trecho, viewGroup, false)
            return ItemViewHolder(view)
        } else {//ADS
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.ly_ads_ponto, viewGroup, false) as LinearLayout

            var unitId = context.getString(R.string.mob_unit_id_tests)
            if (!BuildConfig.DEBUG) {
                unitId = BuildConfig.AD_MOD_UNIT_ID
            }
            adView = AdView(context)
            adView?.adUnitId = unitId
            adView?.adSize = AdSize.BANNER
            view.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView?.loadAd(adRequest)
            return HeadeHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ADS
            else -> ITEM
        }
    }

    override fun onBindViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {

        if (position == 0) {
            return  //ad ocupa 1
        }

        //vai ser um item
        val item = estimativas[position - 1]

        val itemViewHolder = viewHolder as ItemViewHolder

        itemViewHolder.imageview.visibility = if (item.acessibilidade) View.VISIBLE else View.GONE

        var hora = item.getHorarioOrigemText(context, horarioDoServidor)
        var horario = DataHelper.format(Date(item.horarioNaOrigem!!), "HH:mm")

        val textView = itemViewHolder.textviewHorarioOrigem
        textView.text = StringHelper.mergeSeparator(" - ", horario, hora)
        textView.setBackgroundResource(item.getBackgroundConfiabilidade(horarioDoServidor))

        hora = item.getHorarioDestinoText(context, horarioDoServidor)
        horario = DataHelper.format(Date(item.horarioNoDestino!!), "HH:mm")
        itemViewHolder.textviewHorarioDestino.text = StringHelper.mergeSeparator(" - ", horario, hora)
        itemViewHolder.textviewHorarioDestino.setBackgroundResource(item.getBackgroundConfiabilidade(horarioDoServidor))

        itemViewHolder.textviewNumeroVeiculo.text = item.veiculo

        val linha = mapLinhas[item.itinerarioId]
        itemViewHolder.textviewNumero.text = linha!!.identificadorLinhaFiltrado
        itemViewHolder.textviewNome.text = linha.bandeira
    }

    override fun getItemCount(): Int {
        return estimativas.size + 1//a publicidade
    }

    internal inner class ItemViewHolder(mainView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mainView) {

        var textviewHorarioOrigem = mainView.textview_horario_origem
        var textviewHorarioDestino = mainView.textview_horario_destino
        var textviewNumeroVeiculo = mainView.textview_numero_veiculo
        var textviewNome = mainView.textview_nome_linha
        var textviewNumero = mainView.textview_numero_linha
        var imageview = mainView.image_acessibilidade
    }

    internal inner class HeadeHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    }

    fun onResume() {
        adView?.resume()
    }

    fun onPause() {
        // The pause method receives a boolean that you should fill with the activity isFinishing() method, or false if you do no
        // have the access to the method
        adView?.pause()
    }

    fun onDestroy() {
        adView?.destroy()
    }
}