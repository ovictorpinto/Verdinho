package com.github.ovictorpinto.verdinho.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.support.v4.app.NotificationCompat
import com.github.ovictorpinto.verdinho.R
import com.github.ovictorpinto.verdinho.ui.main.MainActivity
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

/**
 * Created by victorpinto on 03/01/18.
 */
class VerdinhoMessageService : FirebaseMessagingService() {

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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, "default")
                .setContentText(mensagemTO.conteudo)
                .setContentTitle(mensagemTO.titulo)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notificationId = Random().nextInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
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