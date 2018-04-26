package com.github.ovictorpinto.verdinho.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v13.app.FragmentCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import br.com.tcsistemas.common.net.HttpHelper
import com.github.ovictorpinto.ConstantesEmpresa
import com.github.ovictorpinto.verdinho.Constantes
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO
import com.github.ovictorpinto.verdinho.retorno.RetornoPesquisarPontos
import com.github.ovictorpinto.verdinho.to.PontoTO
import com.github.ovictorpinto.verdinho.ui.ponto.DetalhePontoDialogFrag
import com.github.ovictorpinto.verdinho.util.LogHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.ly_map.view.*
import kotlinx.android.synthetic.main.ly_teste.view.*
import java.util.*

/**
 * Created by victorpinto on 09/04/18.
 */

class MapFragment : MapFragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DetalhePontoDialogFrag.PontoDialogoListener {

    var retornoPesquisarPontos: RetornoPesquisarPontos? = null

    override fun onDestino(pontoTO: PontoTO) {
        Toast.makeText(activity, "Procurando destinos do ponto ${pontoTO.getNomeApresentacao(activity)}", Toast.LENGTH_SHORT).show()
        ProcessoDestino(activity, pontoTO).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    inner class ProcessoDestino(var context: Context, var pontoTO: PontoTO) : AsyncTask<Void, String, Boolean>() {

        override fun doInBackground(vararg p0: Void?): Boolean {
            val mapper = MainActivity.mapper

            val url = ConstantesEmpresa.listarPontos
            val urlParam = "{\"pontoDeOrigemId\": ${pontoTO.idPonto}}"
            val headers = ConstantesEmpresa(context).headers
            LogHelper.log(TAG, url)
            LogHelper.log(TAG, urlParam)

            val retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers)
            LogHelper.log(TAG, retorno)

            retornoPesquisarPontos = mapper.readValue(retorno, RetornoPesquisarPontos::class.java)
            LogHelper.log(TAG, retornoPesquisarPontos!!.getPontosDeParada().size.toString() + " item(s)")

            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (!isCancelled) {

                fillMarkers()
                layout.button_cancelar.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        val ZOOM = 16f
        val POSICAO_SEDE = LatLng(-20.321367, -40.339607)//palacio anchieta
    }

    private val TAG = "MapaFragment"
    val PERMISSION_GPS_REQUEST_CODE = 201
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var map: GoogleMap? = null
    private var mClusterManager: ClusterManager<PontoTO>? = null
    private var moveuLocal = false
    private var setFavoritos = HashSet<Int>()
    private lateinit var favoriteReceive: BroadcastReceiver

    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        buildGoogleApiClient()
        getMapAsync(this)

        fillFavoritos()
        favoriteReceive = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                fillFavoritos()
                fillMarkers()
            }
        }
        LocalBroadcastManager.getInstance(activity).registerReceiver(favoriteReceive, IntentFilter(Constantes.actionUpdatePontoFavorito))
    }

    private lateinit var layout: LinearLayout

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater!!.inflate(R.layout.ly_map, container, false) as LinearLayout
        layout.toolbar.setTitle(R.string.app_name)

        val v = super.onCreateView(inflater, container, savedInstanceState)
        layout.addView(v)
        return layout
    }

    override fun onMapReady(mMap: GoogleMap) {
        map = mMap
        mClusterManager = ClusterManager(activity, mMap)
        mClusterManager!!.renderer = PontoRenderer(activity, mMap, mClusterManager!!)
        mClusterManager!!.setOnClusterItemClickListener(PontoPinClickListener(this))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POSICAO_SEDE, ZOOM))
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)

        mMap.isTrafficEnabled = true

        LogHelper.log("Mapa", "Mapa pronto")
        if (ActivityCompat
                        .checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat
                        .checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnCameraChangeListener({ fillMarkers() })
        fillMarkers()
    }

    private fun fillMarkers() {
        map?.let {
            val bounds = it.projection.visibleRegion.latLngBounds
            it.clear()
            val dao = PontoDAO(activity)
            val list = dao.findByRegiao(bounds)
            LogHelper.log(TAG, "Encontrados " + list.size)

            mClusterManager?.let {
                it.clearItems()
                for (i in list.indices) {
                    val pontoPO = list[i]
                    val to = pontoPO.pontoTO
                    //se não houver filtro ou o ponto estiver na lista de filtrados
                    if (retornoPesquisarPontos == null || retornoPesquisarPontos?.pontosDeParada?.contains(to.idPonto)!!)
                        it.addItem(to)
                }
                it.cluster()
            }
        }
    }


    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(activity).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient.disconnect()
    }

    override fun onConnected(connectionHint: Bundle?) {
        LogHelper.log(TAG, "Conectou no fused")

        if (!moveuLocal) {
            val hasPermission = ContextCompat
                    .checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                getLocation()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    FragmentCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_GPS_REQUEST_CODE)
                } else {
                    Toast.makeText(activity, getString(R.string.explicacao_gps_sem_permissao), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_GPS_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                @SuppressLint("MissingPermission")
                map?.isMyLocationEnabled = true
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (mLastLocation != null) {
            LogHelper.log(TAG, "Encontrou última posição!");
            val devicePosition = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            map?.moveCamera(CameraUpdateFactory.newLatLng(devicePosition))
            val update = CameraUpdateFactory.zoomTo(ZOOM)
            moveuLocal = true
            map?.animateCamera(update, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    fillMarkers()
                }

                override fun onCancel() {

                }
            })

        } else {
            Toast.makeText(activity, R.string.location_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        LogHelper.log(TAG, "onConnectionSuspended $i")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        LogHelper.log(TAG, "onConnectionFailed")
        Toast.makeText(activity, "Localização não disponível", Toast.LENGTH_SHORT).show()
    }

    fun fillFavoritos() {
        setFavoritos.clear()
        val favoritoDAO = PontoFavoritoDAO(activity)
        val allFavoritos = favoritoDAO.findAllFavoritos()
        for (i in allFavoritos.indices) {
            val po = allFavoritos[i]
            setFavoritos.add(po.pontoTO.idPonto)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(favoriteReceive)
    }

    /**
     * "Intercepta" a criação do marker no mapa
     */
    private inner class PontoRenderer(context: Context, map: GoogleMap, clusterManager: ClusterManager<PontoTO>) : DefaultClusterRenderer<PontoTO>(context, map, clusterManager) {

        override fun onBeforeClusterItemRendered(item: PontoTO?, markerOptions: MarkerOptions?) {
            val pin = if (setFavoritos.contains(item!!.idPonto)) R.drawable.pin_favorito else R.drawable.pin
            markerOptions!!.icon(BitmapDescriptorFactory.fromResource(pin))
            super.onBeforeClusterItemRendered(item, markerOptions)
        }

    }

    /**
     * Define a regra ao clicar no marker do mapa
     */
    private inner class PontoPinClickListener(var listener: Fragment) : ClusterManager
    .OnClusterItemClickListener<PontoTO> {

        override fun onClusterItemClick(clicado: PontoTO): Boolean {
            val bundle = Bundle()
            bundle.putSerializable(PontoTO.PARAM, clicado)

            val frag = DetalhePontoDialogFrag()
            frag.setTargetFragment(listener, 1)
            frag.arguments = bundle
            frag.show(fragmentManager, DetalhePontoDialogFrag.TAG_FRAG)
            return true
        }
    }
}