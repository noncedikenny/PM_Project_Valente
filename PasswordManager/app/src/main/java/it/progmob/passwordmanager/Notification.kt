package it.progmob.passwordmanager

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

const val channelID = "channel1"

class Notification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Estrai il titolo e il testo della notifica dagli extra dell'intent
        val title = intent.getStringExtra("notification_title")
        val text = intent.getStringExtra("notification_text")
        val id = intent.getIntExtra("notification_id", System.currentTimeMillis().toInt())

        // Costruisci la notifica utilizzando il titolo e il testo estratti
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .build()

        // Ottieni il NotificationManager e mostra la notifica
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }
}
