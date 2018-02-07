package com.example.timurmuhortov.multithread_downloader.utils

/**
 * @author: timur.mukhortov
 * date: 31.01.2018
 * time: 14:11
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


interface PartDownloadTask {

    fun onTaskCompleted()

    fun onTaskError(msg: String)
}