package com.github.ovictorpinto.verdinho.ui.ponto

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11
import com.github.ovictorpinto.verdinho.BuildConfig
import com.github.ovictorpinto.verdinho.Constantes
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.to.PontoTO
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper
import com.github.ovictorpinto.verdinho.util.DividerItemDecoration
import kotlinx.android.synthetic.main.ly_trecho_detalhe.*
import java.util.*

class TrechoDetalheActivity : AppCompatActivity() {

    private lateinit var pontoTOOrigem: PontoTO
    private lateinit var pontoTODestino: PontoTO

    private var progress: View? = null
    private var emptyView: View? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var recyclerView: RecyclerView? = null
    private var appBarLayout: AppBarLayout? = null

    private var processo: ProcessoLoadLinhasPonto? = null
    private var analyticsHelper: AnalyticsHelper? = null

    private var timerAtual: Timer? = null
    private var task: TimerTask? = null
    private val handler = Handler()
    private var estimativaTrechoRecyclerAdapter: EstimativaTrechoRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ly_trecho_detalhe)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        analyticsHelper = AnalyticsHelper(this)
        pontoTOOrigem = intent.getSerializableExtra(PontoTO.PARAM) as PontoTO
        pontoTODestino = intent.getSerializableExtra(PontoTO.PARAM_DESTINO) as PontoTO

        appBarLayout = findViewById(R.id.app_bar)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.trecho)
        setSupportActionBar(toolbar)
        textview_origem.text = getString(R.string.origem__, pontoTOOrigem.getNomeApresentacaoComDescricao(this))
        textview_destino.text = getString(R.string.destino__, pontoTODestino.getNomeApresentacaoComDescricao(this))

        title = pontoTOOrigem.getNomeApresentacao(this)

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST)
        recyclerView!!.addItemDecoration(itemDecoration)

        progress = findViewById(R.id.layout_progress)
        progress!!.visibility = View.VISIBLE

        emptyView = findViewById(android.R.id.empty)
        (emptyView!!.findViewById<View>(R.id.image) as ImageView).setImageResource(R.drawable.error)
        (emptyView!!.findViewById<View>(R.id.textview_title) as TextView).setText(R.string.ponto_empty_title)
        (emptyView!!.findViewById<View>(R.id.textview_subtitle) as TextView).setText(R.string.ponto_empty_subtitle)

        swipeRefreshLayout = findViewById(R.id.swipe)

        swipeRefreshLayout!!.setOnRefreshListener {
            analyticsHelper!!.forceRefresh(pontoTOOrigem)
            refresh()
        }
        refresh()

        exibeLegenda()
    }

    override fun onPause() {
        super.onPause()
        timerAtual!!.cancel()
        timerAtual = null
        if (estimativaTrechoRecyclerAdapter != null) {
            estimativaTrechoRecyclerAdapter!!.onPause(isFinishing)
        }
    }

    override fun onResume() {
        super.onResume()
        if (timerAtual == null) {
            iniciaRefresh()
        }
        if (estimativaTrechoRecyclerAdapter != null) {
            estimativaTrechoRecyclerAdapter!!.onResume()
        }
    }

    private fun iniciaRefresh() {
        task = object : TimerTask() {
            override fun run() {
                handler.post {
                    swipeRefreshLayout!!.isRefreshing = true
                    refresh()
                }
            }
        }
        timerAtual = Timer()
        timerAtual!!.schedule(task, TIME_REFRESH_MILI, TIME_REFRESH_MILI)
    }

    private fun exibeLegenda() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val jaExibiu = sharedPreferences.getBoolean(Constantes.pref_show_legenda, false)
        if (!jaExibiu) {
            fragmentManager.beginTransaction().add(LegendaDialogFrag(), null).commitAllowingStateLoss()
            sharedPreferences.edit().putBoolean(Constantes.pref_show_legenda, true).apply()
        }
    }

    private fun refresh() {
        if (processo != null) {
            processo!!.cancel(true)
        }
        processo = ProcessoLoadLinhasPonto()
        processo!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onDestroy() {
        super.onDestroy()
        estimativaTrechoRecyclerAdapter?.onDestroy()
        task?.cancel()
        processo?.cancel(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        if (BuildConfig.USA_PRECO) {
            inflater.inflate(R.menu.menu_preco, menu)
        }
        inflater.inflate(R.menu.menu_legenda, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_legenda -> {
                analyticsHelper!!.clickLegenda()
                fragmentManager.beginTransaction().add(LegendaDialogFrag(), null).commitAllowingStateLoss()
                return true
            }
            R.id.menu_monetization -> {
                analyticsHelper!!.clickPreco()
                fragmentManager.beginTransaction().add(PrecoDialogFrag(), null).commitAllowingStateLoss()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private inner class ProcessoLoadLinhasPonto : com.github.ovictorpinto.verdinho.ui.ponto.ProcessoLoadLinhasTrecho(
            this@TrechoDetalheActivity,
            this@TrechoDetalheActivity.pontoTOOrigem,
            this@TrechoDetalheActivity.pontoTODestino) {

        override fun onPreExecute() {
            super.onPreExecute()
            val alert = fragmentManager.findFragmentByTag(AlertDialogFragmentV11.FRAGMENT_ID)
            if (alert != null) {
                fragmentManager.beginTransaction().remove(alert).commitAllowingStateLoss()
            }
        }

        override fun onPostExecute(success: Boolean) {

            if (!isCancelled) {
                if (!success) {
                    //abrir uma nova janela de erro
                    val alert = AlertDialogFragmentV11.newInstance(null, null, R.string.falha_acesso_servidor)
                    fragmentManager.beginTransaction().add(alert, AlertDialogFragmentV11.FRAGMENT_ID).commitAllowingStateLoss()
                } else {

                    val has = retornoLinhasPonto.estimativas != null && !retornoLinhasPonto.estimativas.isEmpty()
                    recyclerView!!.visibility = if (has) View.VISIBLE else View.GONE
                    emptyView!!.visibility = if (has) View.GONE else View.VISIBLE

                    if (has) {
                        estimativaTrechoRecyclerAdapter = EstimativaTrechoRecyclerAdapter(context, retornoLinhasPonto
                                .estimativas, retornoLinhasPonto.horarioDoServidor, mapLinhas, pontoTOOrigem, pontoTODestino)
                        recyclerView!!.adapter = estimativaTrechoRecyclerAdapter
                    }

                }
                swipeRefreshLayout!!.isRefreshing = false
                progress!!.visibility = View.GONE
            }
            processo = null
        }

    }

    companion object {

        val TIME_REFRESH_MILI = (30 * 1000).toLong()
    }
}
