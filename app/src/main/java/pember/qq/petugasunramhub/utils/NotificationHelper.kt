package pember.qq.petugasunramhub.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "unramhub_task_channel"
    private const val CHANNEL_NAME = "Disposisi Tugas Baru"

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Setting Kategori Channel agar muncul pop-up di atas layar (High Importance)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifikasi ketika admin memberikan tugas perbaikan baru"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Desain Visual Notifikasinya (Warna, Ikon, Teks)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Ganti pake icon aplikasi kamu nnti
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#003E7E")) // Warna Navy khas UnramHUB
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}