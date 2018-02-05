package com.example.timurmuhortov.multithread_downloader.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author: timur.mukhortov
 * date: 05.02.2018
 * time: 23:18
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


public class MakeRequestTask extends AsyncTask<String, Void, String> {

    //Log
    private String tagTas = "Task";

    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    private String url;
    private Integer countThread;
    private Integer fileSize = 0;

    public AsyncResponse delegate = null;

    private Integer responseCode;
    private String responseMessage;

    public MakeRequestTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        url = params[0];
        countThread = Integer.valueOf(params[1]);
        try {

            //Create a URL object holding our url
            URL urlConnection = new URL(this.url);

            //Create a connection
            HttpURLConnection connection = (HttpURLConnection)
                    urlConnection.openConnection();

            //Set methods, timeouts, property
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setDoOutput(true);
            connection.setRequestProperty("accept-encoding", "identity");
            connection.setRequestProperty("content-encoding", "identity");

            //Connect to our url
            connection.connect();

            //Get content size
            if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                fileSize = connection.getContentLength();
                Log.i(tagTas, "File size = " + fileSize);
            } else {
                responseCode = connection.getResponseCode();
                responseMessage = connection.getResponseMessage();
            }


        } catch (UnknownHostException ignored) {
            Log.i(tagTas, ignored.getMessage());
        } catch (Exception e) {
            Log.i(tagTas, "BOOM error!!!");
            return "";
        }
        return null;
    }

    //Result from background method in params
    @Override
    protected void onPostExecute(String s) {
        if (responseCode != null && !responseMessage.isEmpty())
        delegate.processFinish(responseCode, responseMessage);
    }
}
