package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat

// Notification ID.
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendFileDownloadedNotification(applicationContext: Context, downloadId: Long, title: String, text: String) {

    Log.i("DetailActivity", "Download Idd Notification: " + downloadId)
    val fileDownloadedIntent = Intent(applicationContext, DetailActivity::class.java)
    fileDownloadedIntent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId)
    val fileDownloadedPendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            fileDownloadedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(applicationContext, applicationContext.resources.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            // .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentTitle(title)
            .setContentText(text)
            //.setContentText(applicationContext.getString(R.string.notification_description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                    R.drawable.ic_assistant_black_24dp,
                    applicationContext.getString(R.string.notification_button),
                    fileDownloadedPendingIntent
            )

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}