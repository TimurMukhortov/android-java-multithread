package com.example.timurmuhortov.multithread_downloader.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import static java.lang.Math.min;

/**
 * @author: timur.mukhortov
 * date: 05.02.2018
 * time: 23:18
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


public class MakeRequestTask extends AsyncTask<String, Void, String> implements OnTaskCompleted {

    //Log
    private String tagTas = "Task";

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final String DOWNLOAD_SUCCESS = "DOWNLOAD SUCCESS!";

    private String fileName;
    private String filerPartNumber = "filerPartNumber";
    private Integer countThread;
    private Integer countReadyThread = 0;
    private Mutex mutex = new Mutex();

    private AsyncResponse delegate = null;

    private String responseCode;
    private String responseMessage;

    public MakeRequestTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    private void sendParams(String url, Integer countThread, Integer fileSize) {
        countReadyThread = 0;

        //get file name
        fileName = URLUtil.guessFileName(url, null, null);

        if (fileSize == -1) {
            responseMessage = ("Неккоректная ссылка. ");
        } else {
            Integer blockSize = fileSize / countThread + ((fileSize % countThread != 0) ? 1 : 0);
            Integer start;
            Integer end;
            for (int i = 0; i < countThread; i++) {
                start = (i - 1) * blockSize;
                end = min(start + blockSize - 1, fileSize - 1);
                File file = new File(Environment.getExternalStorageDirectory(), filerPartNumber + i);
                new DownloadFilePart(url, start, end, file, this).execute();
            }

        }
    }

    private void createResultFile(String fileName) {
        File resultFile = new File(Environment.getExternalStoragePublicDirectory("/"), fileName);
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(resultFile, false);
            for (int i = 0; i < countThread; i++) {
                File partFile = new File(Environment.getExternalStorageDirectory(), filerPartNumber + i);
                FileInputStream inputStream = new FileInputStream(partFile);

                byte[] b = new byte[4096];
                int len = inputStream.read(b);

                while (len > 0) {
                    Log.i("MainActivity", "Downloaded Size: " + len + " data: " + b);
                    outputStream.write(b, 0, len);
                    len = inputStream.read(b);
                }
                inputStream.close();
                //partFile.delete();

            }
            outputStream.flush();
            outputStream.close();
            responseMessage = DOWNLOAD_SUCCESS;
        } catch (FileNotFoundException e) {
            responseMessage = e.getMessage();
        } catch (IOException e) {
            responseMessage = e.getMessage();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        countThread = Integer.valueOf(params[1]);
        try {

            //Create a URL object holding our url
            URL urlConnection = new URL(url);

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
                Integer fileSize = connection.getContentLength();
                sendParams(url, countThread, fileSize);
                Log.i(tagTas, "File size = " + fileSize);
            } else {
                responseCode = String.valueOf(connection.getResponseCode()) + " : ";
                responseMessage = connection.getResponseMessage();
            }

            //Disconnect to out url
            connection.disconnect();

        } catch (UnknownHostException ignored) {
            responseMessage = ignored.getMessage();
            Log.i(tagTas, ignored.getMessage());
            return "";
        } catch (Exception e) {
            responseMessage = e.getMessage();
            Log.i(tagTas, "BOOM error!!!");
            return "";
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        String answerMsg = responseCode + responseMessage;
        delegate.responseServer(answerMsg);
    }

    @Override
    public void onTaskCompleted() {
        mutex.lock();
        countReadyThread++;
        mutex.release();
        if (countThread.equals(countReadyThread)) {
            Log.i(tagTas, "BOOM!");
            createResultFile(fileName);
        }
    }
}
