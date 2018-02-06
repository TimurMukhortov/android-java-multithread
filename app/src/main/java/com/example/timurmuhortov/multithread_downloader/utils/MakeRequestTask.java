package com.example.timurmuhortov.multithread_downloader.utils;

import android.os.AsyncTask;
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


public class MakeRequestTask extends AsyncTask<Object, Void, String> implements OnTaskCompleted {

    //Log
    private String tagTas = "Task";

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final String DOWNLOAD_SUCCESS = "DOWNLOAD SUCCESS!";
    private static final String DOWNLOAD_START = "DOWNLOAD START!";

    private String fileName;
    private String filerPartNumber = "filerPartNumber";
    private Integer countThread;
    private Integer countReadyThread = 0;
    private Mutex mutex = new Mutex();
    private File filePath;

    private AsyncResponse delegate = null;

    private Integer responseCode;
    private String responseMessage;

    public MakeRequestTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    private void sendParams(String url, Integer countThread, Integer fileSize, File filePath) {
        countReadyThread = 0;

        //get file name
        fileName = URLUtil.guessFileName(url, null, null);

        if (fileSize == -1) {
            responseMessage = ("Неккоректная ссылка.");
        } else {
            Integer blockSize = fileSize / countThread + ((fileSize % countThread != 0) ? 1 : 0);
            Integer start;
            Integer end;
            for (int i = 0; i < countThread; i++) {
                start = i * blockSize;
                end = min(start + blockSize - 1, fileSize - 1);
                Log.i(tagTas, start + " " + end);
                File file = new File(filePath, filerPartNumber + i +".txt");
                new DownloadFilePart(url, start, end, file, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
    }

    private void createResultFile(String fileName) {
        File resultFile = new File(filePath, fileName);
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(resultFile, false);
            for (int i = 0; i < countThread; i++) {
                File partFile = new File(filePath, filerPartNumber + i + ".txt");
                FileInputStream inputStream = new FileInputStream(partFile);

                byte[] b = new byte[4096];
                int len = inputStream.read(b);

                while (len > 0) {
                    Log.i("MainActivity", "Downloaded Size: " + len + " data: " + b);
                    outputStream.write(b, 0, len);
                    len = inputStream.read(b);
                }
                inputStream.close();
                partFile.delete();

            }
            outputStream.flush();
            outputStream.close();
            responseMessage = DOWNLOAD_SUCCESS;
        } catch (FileNotFoundException e) {
            Log.i(tagTas, e.toString());

            responseMessage = e.getMessage();
        } catch (IOException e) {
            Log.i(tagTas, e.toString());

            responseMessage = e.getMessage();
        }
    }

    @Override
    protected String doInBackground(Object... params) {
        String url = params[0].toString();
        countThread = (Integer) params[1];
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
                filePath = (File)params[2];
                sendParams(url, countThread, fileSize, filePath);
                Log.i(tagTas, "File size = " + fileSize);
                responseMessage = DOWNLOAD_START;

                //Disconnect to out url
                connection.disconnect();
            } else {
                responseCode = connection.getResponseCode();
                responseMessage = connection.getResponseMessage();
            }

        } catch (UnknownHostException ignored) {
            responseMessage = ignored.getMessage();
            Log.i(tagTas, ignored.getMessage());
            return "";
        } catch (Exception e) {
            responseMessage = e.getMessage();
            Log.i(tagTas, e.getMessage());
            return "";
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        String answerMsg;
        if(responseCode != null) answerMsg = responseCode + " : " + responseMessage;
        else answerMsg = responseMessage;
        delegate.responseServer(answerMsg);
    }

    @Override
    public void onTaskCompleted() {
        mutex.lock();
        countReadyThread++;
        mutex.release();
        if (countThread.equals(countReadyThread)) {
            Log.i(tagTas, "DOING MERGE!");
            createResultFile(fileName);
        }
    }
}
