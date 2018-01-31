package com.example.timurmuhortov.multithread_downloader

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min


class MainActivity : AppCompatActivity(), OnTaskCompleted {

    private val tag = "MainActivity"
    private var countReadyThread = 0
    private var countThread = 0
    private lateinit var fileName: String
    private var mutex = Mutex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonDownload = findViewById<Button>(R.id.button_download)
        val buttonClear = findViewById<Button>(R.id.button_clear)
        val editText = findViewById<EditText>(R.id.editText_link)
        val numberPick = findViewById<NumberPicker>(R.id.numberPicker_thread)
        numberPick.minValue = 1
        numberPick.maxValue = 5
        numberPick.wrapSelectorWheel = false

        buttonDownload.setOnClickListener {
            sendParams(editText.text.toString(), numberPick.value)
        }

        buttonClear.setOnClickListener {
            editText.text.clear()
        }
    }


    private fun sendParams(link: String, countThread: Int) {
        this.countThread = countThread
        countReadyThread = 0

        Log.i(tag, "link: $link countThread = $countThread")
        var url: String = if (link.isEmpty()) "http://brandmark.io/logo-rank/random/pepsi.png"
        else link
        val fileName = URLUtil.guessFileName(url, null, null)
        this.fileName = fileName
        val fileSize = DownloadFileSize(url).execute().get()

        val blockSize = fileSize / countThread + (if (fileSize % countThread !== 0) 1 else 0)
        Log.i(tag, "FileSize = " + fileSize)

        var start: Int
        var end: Int
        for (i in 1..countThread) {
            start = (i - 1) * blockSize
            end = min(start + blockSize - 1, fileSize - 1)
            val file = File(this.getExternalFilesDir("/"), "tmp" + i)
            DownloadFilePart(url, start, end, file, this).execute()
        }

    }

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


    private fun createResultFile(fileName: String) {
        val resultFile = File(this.getExternalFilesDir("/"), fileName)
        val outputStream = FileOutputStream(resultFile, false)

        for (i in 1..countThread) {
            val inputStream: InputStream = File(this.getExternalFilesDir("/"), "tmp" + i).inputStream()

            val b = ByteArray(2048)
            var len = inputStream.read(b)

            while (len > 0) {
                outputStream.write(b, 0, len)
                len = inputStream.read(b)
            }
            inputStream.close()
        }

        outputStream.flush()
        outputStream.close()
        Toast.makeText(applicationContext, "DOWNLOAD SUCCESS!", Toast.LENGTH_LONG).show()
    }

    override fun onTaskCompleted() {
        mutex.lock()
        countReadyThread++
        mutex.release()
        Log.i(tag, "Thread number $countReadyThread ready!!!!")
        if (countThread == countReadyThread) {
            Log.i(tag, "BOOM!")
            createResultFile(fileName)
        }

    }

}
