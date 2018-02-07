package com.example.timurmuhortov.multithread_downloader.utils;

import android.os.AsyncTask;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Math.min;

/**
 * @author: timur.mukhortov
 * date: 05.02.2018
 * time: 23:18
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


public class MakeRequestTask extends AsyncTask<Object, Void, String> implements PartDownloadTask {

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

    private void sendParams(String url, Integer countThread, Long fileSize, File filePath) {
        countReadyThread = 0;

        //get file name
        fileName = URLUtil.guessFileName(url, null, null);

        if (fileSize == -1) {
            responseMessage = ("Неккоректная ссылка.");
        } else {
            Long blockSize = fileSize / countThread + ((fileSize % countThread != 0) ? 1 : 0);
            Long start;
            Long end;
            for (int i = 0; i < countThread; i++) {
                start = i * blockSize;
                end = min(start + blockSize - 1, fileSize - 1);
                File file = new File(filePath, filerPartNumber + i);
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
                File partFile = new File(filePath, filerPartNumber + i);
                FileInputStream inputStream = new FileInputStream(partFile);

                byte[] b = new byte[4096];
                int len = inputStream.read(b);

                while (len > 0) {
                    outputStream.write(b, 0, len);
                    len = inputStream.read(b);
                }
                inputStream.close();
                partFile.delete();

            }
            outputStream.flush();
            outputStream.close();
            responseMessage = DOWNLOAD_SUCCESS;
            delegate.responseServer(responseMessage);
        } catch (Exception e) {
            e.printStackTrace();
            responseMessage = e.getMessage();
            delegate.responseServer(responseMessage);
        }
    }

    @Override
    protected String doInBackground(Object... params) {
        String url = params[0].toString();
        countThread = (Integer) params[1];
        HttpURLConnection connection = null;
        try {

            //Create a URL object holding our url
            URL urlConnection = new URL(url);

            //Create a connection
            connection = (HttpURLConnection)
                    urlConnection.openConnection();

            //Set methods, timeouts, property
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("accept-encoding", "identity");
            connection.setRequestProperty("content-encoding", "identity");

            //Connect to our url
            connection.connect();

            //Get content size
            if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                Long fileSize = connection.getContentLengthLong();
                filePath = (File) params[2];
                sendParams(url, countThread, fileSize, filePath);
                responseMessage = DOWNLOAD_START;
            } else {
                responseCode = connection.getResponseCode();
                responseMessage = connection.getResponseMessage();
            }

        } catch (Exception e) {
            responseMessage = e.getMessage();
            e.printStackTrace();
            return "";
        }
        finally {
            if (connection != null) connection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        String answerMsg;
        if (responseCode != null) answerMsg = responseCode + " : " + responseMessage;
        else answerMsg = responseMessage;
        delegate.responseServer(answerMsg);
    }

    @Override
    public void onTaskCompleted() {
        mutex.lock();
        countReadyThread++;
        mutex.release();
        if (countThread.equals(countReadyThread)) {
            createResultFile(fileName);
        }
    }

    @Override
    public void onTaskError(String msg) {
        responseMessage = msg;
        delegate.responseServer(responseMessage);
    }
}
