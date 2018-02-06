package com.example.timurmuhortov.multithread_downloader.presentation.presenter

import android.webkit.URLUtil
import com.example.timurmuhortov.multithread_downloader.ui.MainActivity
import com.example.timurmuhortov.multithread_downloader.utils.AsyncResponse
import com.example.timurmuhortov.multithread_downloader.utils.MakeRequestTask
import java.io.File

/**
 * @author: timur.mukhortov
 * date: 05.02.2018
 * time: 22:40
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


class MainPresenter : AsyncResponse {

    private var view: MainActivity? = null

    fun paramsRequest(url: String, countThread: Int, path: File) {
        if (checkURL(url)) {
            MakeRequestTask(this).execute(url, countThread, path)
        }
    }

    fun attachView(mainActivity: MainActivity) {
        this.view = mainActivity
    }

    fun detachView() {
        this.view = null
    }

    private fun checkURL(url: String): Boolean {

        if (url.isEmpty()) {
            view!!.createAlertDialog("Введите URL!")
            return false
        }

        if (!URLUtil.isNetworkUrl(url)) {
            view!!.createAlertDialog("Некоректная URL ссылка.")
            return false
        }

        return true

    }

    override fun responseServer(responseRequest: String) {
        view!!.createAlertDialog(responseRequest)

    }

}