package com.example.timurmuhortov.multithread_downloader.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: timur.mukhortov
 * date: 30.01.2018
 * time: 22:10
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


public class DownloadFilePart extends AsyncTask<Void, Void, String> {
    private static final int BUFFER_SIZE = 4096;
    private String url;
    private long start;
    private long end;
    private File tmp_file;
    private OnTaskCompleted listener;


    DownloadFilePart(String url, Long start, Long end, File tmp_file, OnTaskCompleted listener) {
        this.url = url;
        this.start = start;
        this.end = end;
        this.tmp_file = tmp_file;
        this.listener = listener;
    }

    private String download(){
        try {
            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("range", String.format("bytes=%d-%d",this.start, this.end));
            urlConnection.setRequestProperty("accept-encoding", "identity");
            urlConnection.setRequestProperty("content-encoding", "identity");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            FileOutputStream outputStream = new FileOutputStream(this.tmp_file);

            byte[] b = new byte[BUFFER_SIZE];
            int len = inputStream.read(b);

            while(len > 0) {
                outputStream.write(b, 0, len);
                len = inputStream.read(b);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return "OK";

        } catch (Exception e){
            e.printStackTrace();
        }
        return "empty";
    }

    @Override
    protected String doInBackground(Void... voids) {
        return download();
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onTaskCompleted();
    }
}
