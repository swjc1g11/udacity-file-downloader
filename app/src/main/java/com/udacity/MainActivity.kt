package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException


data class FileToDownload(val fileName: String, val fileUrl: String)

class MainActivity : AppCompatActivity(), ButtonStateObservableChangeListener {

    private var downloadID: Long = 0
    private val filesToDownload = listOf(
            FileToDownload("Bumtech Glide", "https://github.com/bumptech/glide"),
            FileToDownload("ND940-C3 Advanced Android Project Starter Code", "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"),
            FileToDownload("Square RetroFit", "https://github.com/square/retrofit")
    )
    private var currentFile: FileToDownload? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Create notification channel for file downloads for later Android versions
        createChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name)
        )
        // Register the BroadcastReceive that will receive a notification once DownloadManager has downloaded a file.
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Observe a custom observable to change the button state to complete in case
        // this activity is still in the foreground when BroadCastReceiver completes
        ButtonStateObservable.getInstance().addObserver(this)

        // Set OnClick listener for custom button
        custom_button.setOnClickListener {
            download()
        }

        setUpRadioButtons()

        Toast.makeText(this, getString(R.string.toast_instructions), Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            download()
        } else {
            Toast.makeText(this, getString(R.string.requires_access_to_external_storage), Toast.LENGTH_LONG).show()
        }
    }

    override fun update(buttonState: ButtonState) {
        // Update the button state to a new value
        // Called when the DownloadManager is finished
        // And the BroadcastReceive updates the custom observable
        custom_button.setCustomButtonState(buttonState)
    }

    private fun setUpRadioButtons() {

        val radios = listOf(radio_option_1, radio_option_2, radio_option_3)

        for (i in 0..radios.size - 1) {
            val temp = radios.get(i)
            val file = filesToDownload.get(i)
            temp.setText(file.fileName)
            temp.setOnClickListener {
                currentFile = file
            }
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_channel_description)

            val notificationManager = getSystemService(
                    NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun download() {
        if (!getStoragePermissions()) return
        if (currentFile != null) {
            val folderPath = "/repos"
            val directory = File(getExternalFilesDir(null), folderPath)

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val sb = StringBuilder()
            sb.append(folderPath)
            sb.append("/" + currentFile!!.fileName)
            sb.append(".zip")

            val request =
                    DownloadManager.Request(Uri.parse(currentFile!!.fileUrl))
                            .setTitle(currentFile!!.fileName)
                            .setDescription(getString(R.string.app_description))
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sb.toString())
                            .setRequiresCharging(false)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)

            custom_button.setCustomButtonState(ButtonState.Loading)
        } else {
            Toast.makeText(this, getString(R.string.forgot_to_select_file), Toast.LENGTH_SHORT).show()
        }
    }

    fun getStoragePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                val permissionsArr = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissionsArr.toTypedArray(), 1)
                return false
            }
        }
        else {
            return true
        }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                ButtonStateObservable.getInstance().setButtonState(ButtonState.Completed)

                val notificationManager = ContextCompat.getSystemService(
                        context,
                        NotificationManager::class.java
                ) as NotificationManager
                notificationManager.cancelAll()
                notificationManager.sendFileDownloadedNotification(
                        context,
                        id,
                        getString(R.string.notification_title),
                        getString(R.string.notification_description)
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    }

}
