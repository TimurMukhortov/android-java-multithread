package com.example.timurmuhortov.multithread_downloader.ui

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import com.example.timurmuhortov.multithread_downloader.R
import com.example.timurmuhortov.multithread_downloader.presentation.presenter.MainPresenter
import com.example.timurmuhortov.multithread_downloader.presentation.view.IMainView


class MainActivity : AppCompatActivity(), IMainView {

    private var mainPresenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        mainPresenter.attachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter.detachView()
    }

    override fun createAlertDialog(msg: String) =
            AlertDialog.Builder(this)
                    .setTitle("Сообщение:")
                    .setMessage(msg)
                    .setNegativeButton("OK", { dialog, _ -> dialog.dismiss() })
                    .create()
                    .show()

    private fun init() {
        val buttonDownload = findViewById<Button>(R.id.button_download)
        val buttonClear = findViewById<Button>(R.id.button_clear)
        val editText = findViewById<EditText>(R.id.editText_link)
        val numberPick = findViewById<NumberPicker>(R.id.numberPicker_thread)
        numberPick.minValue = 1
        numberPick.maxValue = 99
        numberPick.wrapSelectorWheel = true

        buttonDownload.setOnClickListener {
            val url = editText.text.toString()
            val countThread = numberPick.value
            mainPresenter.paramsRequest(url, countThread, this.getExternalFilesDir("/"))
        }

        buttonClear.setOnClickListener {
            editText.text.clear()
        }
    }

}
