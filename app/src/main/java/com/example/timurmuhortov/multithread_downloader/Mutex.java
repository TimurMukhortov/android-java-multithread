package com.example.timurmuhortov.multithread_downloader;

import java.util.concurrent.Semaphore;

/**
 * @author: timur.mukhortov
 * date: 31.01.2018
 * time: 21:16
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


public class Mutex {
    private Semaphore mutex = new Semaphore(1);

    public void lock() {
        try {
            mutex.acquire();
        } catch (Exception e){

        };
    }

    public void release() {
        mutex.release();
    }

}
