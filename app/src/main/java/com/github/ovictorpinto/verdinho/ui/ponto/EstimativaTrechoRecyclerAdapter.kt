package com.github.ovictorpinto.verdinho.ui.ponto

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.tcsistemas.common.date.DataHelper
import br.com.tcsistemas.common.string.StringHelper
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.to.Estimativa
import com.github.ovictorpinto.verdinho.to.LinhaTO
import com.github.ovictorpinto.verdinho.to.PontoTO
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper
import com.inlocomedia.android.ads.AdView
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
        private val pontoDestino: PontoTO) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val ITEM = 11
        private val ADS = 12
    }

    private val analyticsHelper = AnalyticsHelper(context)
    private var adView: AdView? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == ITEM) {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.ly_item_trecho, viewGroup, false)
            return ItemViewHolder(view)
        } else {//ADS
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.ly_ads_ponto, viewGroup, false)
            adView = view.findViewById(R.id.adview)
            return HeadeHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ADS
            else -> ITEM
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

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

    internal inner class ItemViewHolder(mainView: View) : RecyclerView.ViewHolder(mainView) {

        var textviewHorarioOrigem = mainView.textview_horario_origem
        var textviewHorarioDestino = mainView.textview_horario_destino
        var textviewNumeroVeiculo = mainView.textview_numero_veiculo
        var textviewNome = mainView.textview_nome_linha
        var textviewNumero = mainView.textview_numero_linha
        var imageview = mainView.image_acessibilidade
    }

    internal inner class HeadeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    fun onResume() {
        if (adView != null) {
            adView!!.resume()
        }
    }

    fun onPause(isFinishing: Boolean) {
        if (adView != null) {
            // The pause method receives a boolean that you should fill with the activity isFinishing() method, or false if you do no
            // have the access to the method
            adView!!.pause(isFinishing)
        }
    }

    fun onDestroy() {
        if (adView != null) {
            adView!!.destroy()
        }
    }
}