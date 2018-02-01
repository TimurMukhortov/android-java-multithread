package com.example.timurmuhortov.multithread_downloader.ui

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import com.example.timurmuhortov.multithread_downloader.utils.DownloadFilePart
import com.example.timurmuhortov.multithread_downloader.utils.Mutex
import com.example.timurmuhortov.multithread_downloader.utils.OnTaskCompleted
import com.example.timurmuhortov.multithread_downloader.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min


class MainActivity : AppCompatActivity(), OnTaskCompleted {

    private var fileName: String = ""
    private val filerPartNumber = "filerPartNumber"
    private var mutex = Mutex()
    private var countReadyThread = 0
    private var countThread = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonDownload = findViewById<Button>(R.id.button_download)
        val buttonClear = findViewById<Button>(R.id.button_clear)
        val editText = findViewById<EditText>(R.id.editText_link)
        val numberPick = findViewById<NumberPicker>(R.id.numberPicker_thread)
        numberPick.minValue = 1
        numberPick.maxValue = 99
        numberPick.wrapSelectorWheel = true
        countThread = numberPick.value

        buttonDownload.setOnClickListener {
            val url = editText.text.toString()
            if (checkURL(url)) {
                sendParams(url, countThread)
            }
        }

        buttonClear.setOnClickListener {
            editText.text.clear()
        }
    }

    override fun onTaskCompleted() {
        mutex.lock()
        countReadyThread++
        mutex.release()
        if (countThread == countReadyThread) {
            //Log.i(tag, "BOOM!")
            createResultFile(fileName)
        }

    }

    private fun checkURL(url: String): Boolean {

        if (url.isEmpty()) {
            createErrorAlertDialog("Введите URL!")
            return false
        }

        if (!URLUtil.isNetworkUrl(url)) {
            createErrorAlertDialog("Некоректная URL ссылка.")
            return false
        }


        return true
    }

    private fun sendParams(url: String, countThread: Int) {
        countReadyThread = 0

        //get file name
        fileName = URLUtil.guessFileName(url, null, null)

        //get file size
        val fileSize = DownloadFileSize(url).execute().get()
        if (fileSize == -1) {
            createErrorAlertDialog("Неккоректная ссылка. Введите ссылку на файл.")
        } else {

            val blockSize = fileSize / countThread + (if (fileSize % countThread !== 0) 1 else 0)

            var start: Int
            var end: Int
            for (i in 1..countThread) {
                start = (i - 1) * blockSize
                end = min(start + blockSize - 1, fileSize - 1)
                val file = File(this.getExternalFilesDir("/"), filerPartNumber + i)
                DownloadFilePart(url, start, end, file, this).execute()
            }

        }
    }

    private fun createResultFile(fileName: String) {
        val resultFile = File(this.getExternalFilesDir("/"), fileName)
        val outputStream = FileOutputStream(resultFile, false)

        for (i in 1..countThread) {
            val partFile = File(this.getExternalFilesDir("/"), filerPartNumber + i)
            val inputStream: InputStream = partFile.inputStream()

            val b = ByteArray(2048)
            var len = inputStream.read(b)

            while (len > 0) {
                outputStream.write(b, 0, len)
                len = inputStream.read(b)
            }
            inputStream.close()
            //partFile.delete()
        }

        outputStream.flush()
        outputStream.close()
        Toast.makeText(applicationContext, "DOWNLOAD SUCCESS!", Toast.LENGTH_LONG).show()
    }

    private fun createErrorAlertDialog(msg: String) =
            AlertDialog.Builder(this)
                    .setTitle("Ошибка:")
                    .setMessage(msg)
                    .setNegativeButton("OK", { dialog, _ -> dialog.dismiss() })
                    .create()
                    .show()


    class DownloadFileSize(private val url: String) : AsyncTask<Void, Void, Int>() {

        override fun doInBackground(vararg params: Void?): Int {
            val url = URL(url)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "HEAD"
            urlConnection.setRequestProperty("accept-encoding", "identity")
            urlConnection.setRequestProperty("content-encoding", "identity")
            return urlConnection.contentLength
        }

    }

}
