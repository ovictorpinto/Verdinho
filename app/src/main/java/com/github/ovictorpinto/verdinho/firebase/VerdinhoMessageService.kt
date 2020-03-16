package com.github.ovictorpinto.verdinho.firebase

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.VerdinhoApplication
import com.github.ovictorpinto.verdinho.ui.main.MainActivity
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper
import com.github.ovictorpinto.verdinho.util.LogHelper
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

/**
 * Created by victorpinto on 03/01/18.
 */
class VerdinhoMessageService : FirebaseMessagingService() {


    private val TAG = "VerdinhoMessageService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        LogHelper.log(TAG, token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        AnalyticsHelper(this).receiveNotification()

        var mensagem = MensagemTO(remoteMessage.data)

        //se já passou da validade não faz nada.
        if (mensagem.validade != null && mensagem.validade!!.before(Date())) {
            return
        }

        when (mensagem.tipo) {
            MensagemTO.MENSAGEM -> sendNotification(mensagem)
        }
    }

    private fun sendNotification(mensagemTO: MensagemTO) {
        val intent = when (mensagemTO.redireciona) {
            MensagemTO.REDIRECIONA_APLICATIVO -> {
                //se não tiver pra onde ir, ignora
                if (mensagemTO.endereco == null)
                    return

                //se tiver o app já abre
                if (hasApp(mensagemTO.endereco!!)) {
                    packageManager.getLaunchIntentForPackage(mensagemTO.endereco)
                } else {//se não tiver, manda pra loja
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mensagemTO.endereco))
                }
            }
            MensagemTO.REDIRECIONA_SITE -> {
                var intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(mensagemTO.endereco)
                intent
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,0)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, VerdinhoApplication.NOTIFICATION_CHANNEL_ID)
                .setContentText(mensagemTO.conteudo)
                .setContentTitle(mensagemTO.titulo)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(mensagemTO.conteudo))

        val notification = notificationBuilder.build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = System.currentTimeMillis().toInt()
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Some phones throw an exception for unapproved vibration
            notification.defaults = Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND
            notificationManager.notify(notificationId, notification)
        }

    }

    fun hasApp(packagename: String): Boolean {
        return try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

}