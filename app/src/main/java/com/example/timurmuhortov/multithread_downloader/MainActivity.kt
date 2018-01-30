package com.example.timurmuhortov.multithread_downloader

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
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
        val button = findViewById<View>(R.id.button_download)
        val editText = findViewById<EditText>(R.id.editText_link)
        val numberPick = findViewById<NumberPicker>(R.id.numberPicker_thread)
        numberPick.minValue = 1
        numberPick.maxValue = 5
        numberPick.wrapSelectorWheel = false


        //
        button.setOnClickListener {
            sendParams(editText.text.toString(), numberPick.value)
        }
    }



    fun sendParams(link: String, countThread: Int) {
        var downloaders: ArrayList<AsyncTask<Void, Void, String>> = ArrayList()

        Log.i(tag, "link: $link counteThread = $countThread")
        val url = "https://www.google.ru/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"
        val fileSize = DownloadFileSize(url).execute().get()

        val blockSize = fileSize / countThread
        Log.i(tag, "FileSize = " + fileSize)

        for(i in 1..countThread) {
            val start = (i-1) * blockSize
            val end = min(start + blockSize, fileSize - 1)
            val file = File(this.filesDir, "tmp"+i)

            downloaders.add(DownloadFilePart(url, start, end, file).execute())


        }

        for (i in downloaders){
            i.get()
        }

        //todo delete old tmp3.png

        var res_file = File(this.getExternalFilesDir("/"), "tmp4.png")


        for(i in 1..countThread){
            var tmp_file = File(this.filesDir, "tmp"+i)
            tmp_file.copyTo(res_file)
//            val inputStream: InputStream = File(this.filesDir, "tmp"+i).inputStream()
//
//            val b = ByteArray(1024)
//            var len  = inputStream.read(b)
//
//            while (len > 0) {
//                res_file.appendBytes(b)
//                len = inputStream.read(b)
//            }
//
//            inputStream.close()
        }

        //val inputStream: InputStream = File(this.getExternalFilesDir("/"), "tmp.png").inputStream()
        //val inputString = inputStream.bufferedReader().use { it.readText() }
        //println("FILE: $inputString")

    }

    class DownloadFileSize(private val url: String) : AsyncTask<Void, Void, Int>() {

        override fun doInBackground(vararg params: Void?): Int {

            val url = URL(url)
            var urlConnection =  url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "HEAD"

            return urlConnection.contentLength
        }



    }

}
