package com.github.ovictorpinto.verdinho.ui.ponto

import android.content.Context
import android.os.AsyncTask
import br.com.tcsistemas.common.net.HttpHelper
import com.github.ovictorpinto.ConstantesEmpresa
import com.github.ovictorpinto.verdinho.retorno.RetornoLinhasPonto
import com.github.ovictorpinto.verdinho.retorno.RetornoListarLinhas
import com.github.ovictorpinto.verdinho.to.Estimativa
import com.github.ovictorpinto.verdinho.to.LinhaTO
import com.github.ovictorpinto.verdinho.to.PontoTO
import com.github.ovictorpinto.verdinho.ui.main.MainActivity
import com.github.ovictorpinto.verdinho.util.FragmentExtended
import com.github.ovictorpinto.verdinho.util.LogHelper
import java.net.UnknownHostException
import java.util.*

abstract class ProcessoLoadLinhasTrecho(protected var context: Context,
                                        protected var pontoOrigemTO: PontoTO,
                                        protected var pontoDestinoTO: PontoTO) : AsyncTask<Void, String, Boolean>() {

    companion object {
        private val TAG = "ProcessoLoadLinhasTrechos"
    }

    protected lateinit var retornoLinhasPonto: RetornoLinhasPonto
    protected var mapLinhas: HashMap<Int, LinhaTO> = HashMap()
    val headers = ConstantesEmpresa(context).headers

    override fun doInBackground(vararg params: Void): Boolean {
        try {

            if (FragmentExtended.isOnline(context)) {
                try {

                    var url = ConstantesEmpresa.linhasTrecho
                    var urlParam = "{\"pontoDeOrigemId\": ${pontoOrigemTO.idPonto}, \"pontoDeDestinoId\": ${pontoDestinoTO.idPonto}}"

                    LogHelper.log(TAG, url)
                    LogHelper.log(TAG, urlParam)

                    var retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers)
                    LogHelper.log(TAG, retorno)

                    retornoLinhasPonto = MainActivity.mapper.readValue(retorno, RetornoLinhasPonto::class.java)
                    LogHelper.log(TAG, retornoLinhasPonto.estimativas.size.toString() + " item(s)")

                    val linhas = HashSet<Int>()

                    Collections.sort(retornoLinhasPonto.estimativas, Comparator<Estimativa> { lhs, rhs ->
                        var naOrigem = lhs.horarioNaOrigem!!.compareTo(rhs.horarioNaOrigem)
                        if (naOrigem == 0) {//se sair no mesmo horário, ordenada pela chegada no destino
                            lhs.horarioNoDestino!!.compareTo(rhs.horarioNoDestino)
                        } else {
                            naOrigem
                        }

                    })
                    retornoLinhasPonto.estimativas.forEach { linhas.add(it.itinerarioId) }

                    url = ConstantesEmpresa.listarLinhas
                    urlParam = "{\"listaIds\": " + linhas.toString() + " }"
                    LogHelper.log(TAG, url)
                    LogHelper.log(TAG, urlParam)

                    retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers)
                    LogHelper.log(TAG, retorno)

                    val retornoListarLinhas = MainActivity.mapper.readValue(retorno, RetornoListarLinhas::class.java)

                    for (i in 0 until retornoListarLinhas.linhas.size) {
                        val linha = retornoListarLinhas.linhas[i]
                        mapLinhas[linha.id] = linha
                    }

                    return true
                } catch (e: UnknownHostException) {
                    LogHelper.log(e)
                }

            }
        } catch (e: Exception) {
            LogHelper.log(e)
        }

        return false
    }
}