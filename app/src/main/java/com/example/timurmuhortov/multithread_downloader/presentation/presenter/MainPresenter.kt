package com.example.timurmuhortov.multithread_downloader.presentation.presenter

import com.example.timurmuhortov.multithread_downloader.presentation.view.IMainView
import com.example.timurmuhortov.multithread_downloader.utils.AsyncResponse
import com.example.timurmuhortov.multithread_downloader.utils.MakeRequestTask

/**
 * @author: timur.mukhortov
 * date: 05.02.2018
 * time: 22:40
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


class MainPresenter(private val view: IMainView): AsyncResponse {

    fun paramsRequest(url: String, countThread: Int){
        MakeRequestTask(this).execute(url, countThread.toString())
    }

    override fun processFinish(responseCode: Int, responseMessage: String) {
        view.createErrorAlertDialog(responseCode.toString() + " : " + responseMessage)
    }

}