package com.example.timurmuhortov.multithread_downloader

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import java.io.*
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections.min
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonDownload = findViewById<Button>(R.id.button_download)
        val buttonClear =findViewById<Button>(R.id.button_clear)
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



    fun sendParams(link: String, countThread: Int) {
        var downloaders: ArrayList<AsyncTask<Void, Void, String>> = ArrayList()

        Log.i(tag, "link: $link counteThread = $countThread")
        var url: String
        if (link.isEmpty()) url = "http://www.sample-videos.com/img/Sample-jpg-image-50kb.jpg"
        else url = link
        val fileName = URLUtil.guessFileName(url, null, null)
        val fileSize = DownloadFileSize(url).execute().get()

        val blockSize = fileSize / countThread
        Log.i(tag, "FileSize = " + fileSize)

        for(i in 1..countThread) {
            var start = (i-1) * blockSize
            var end = min(start + blockSize, fileSize - 1)
            val file = File(this.getExternalFilesDir("/"), "tmp"+i)
            file.delete()
            downloaders.add(DownloadFilePart(url, start, end, file).execute())


        }

        for (i in downloaders){
            i.get()
        }


        val resultFile = File(this.getExternalFilesDir("/"), fileName)
        val outputStream = FileOutputStream(resultFile, false);

        for(i in 1..countThread){
            val inputStream: InputStream = File(this.getExternalFilesDir("/"), "tmp"+i).inputStream()

            val b = ByteArray(2048)
            var len  = inputStream.read(b)

            while (len > 0) {
                outputStream.write(b, 0 ,len)
                len = inputStream.read(b)
            }


            inputStream.close()
        }

        outputStream.flush()
        outputStream.close()
        Toast.makeText(applicationContext, "DOWNLOAD SUCCESS!", Toast.LENGTH_LONG).show()

    }

    class DownloadFileSize(private val url: String) : AsyncTask<Void, Void, Int>() {

        override fun doInBackground(vararg params: Void?): Int {
            val url = URL(url)
            val urlConnection =  url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "HEAD"
            return urlConnection.contentLength
        }



    }

}
