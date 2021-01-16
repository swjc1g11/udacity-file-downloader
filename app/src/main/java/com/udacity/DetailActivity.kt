package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private var fileName = ""
    private var status = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        ContextCompat.getSystemService(this, NotificationManager::class.java)?.cancelAll()

        done_button.setOnClickListener {
            finish()
        }
        loadDownloadInformation()
    }

    private fun loadDownloadInformation() {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (id > -1) {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
            if (cursor != null && cursor.moveToNext()) {
                fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                downloadStatusToString(fileName, status)

                cursor.close()
            }
        }
    }

    private fun downloadStatusToString(fileName: String, status: Int) {
        file_name_tv.text = fileName
        status_tv.text = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.successful)
            DownloadManager.STATUS_FAILED -> {
                status_tv.setTextColor(Color.RED)
                getString(R.string.failed)
            }
            DownloadManager.STATUS_PAUSED -> getString(R.string.paused)
            DownloadManager.STATUS_PENDING -> getString(R.string.pending)
            DownloadManager.STATUS_RUNNING -> getString(R.string.running)
            else -> getString(R.string.na)
        }
    }

    companion object {
        private const val TAG = "DetailActivity"
    }

}
