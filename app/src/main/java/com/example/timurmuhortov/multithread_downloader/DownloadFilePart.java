package com.example.timurmuhortov.multithread_downloader;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

/**
 * @author: timur.mukhortov
 * date: 30.01.2018
 * time: 22:10
 * @LinkedIn: linkedin.com/in/timurmukhortov
 **/


public class DownloadFilePart extends AsyncTask<Void, Void, String> {
    private String url;
    private long start;
    private long end;
    private File tmp_file;
    private OnTaskCompleted listener;
    private Activity activity;

    public DownloadFilePart(String url, int start, int end, File tmp_file, OnTaskCompleted listener) {
        this.url = url;
        this.start = start;
        this.end = end;
        this.tmp_file = tmp_file;
        this.listener = listener;
        this.activity = (Activity)listener;
    }

    private String download(){
        try {
            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("range", String.format("bytes=%d-%d",this.start, this.end));
            Log.i("MainActivity", String.format("bytes=%d-%d",this.start, this.end));
            urlConnection.connect();

            Log.i("MainActivity", "Response Code: " + urlConnection.getResponseCode());

            InputStream inputStream = urlConnection.getInputStream();

            FileOutputStream outputStream = new FileOutputStream(this.tmp_file);

            byte[] b = new byte[4096];
            int len = inputStream.read(b);

            while(len > 0) {
                Log.i("MainActivity", "Downloaded Size: " + len + " data: " + b);
                outputStream.write(b, 0, len);
                len = inputStream.read(b);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return "Downloaded Size: " + len + " data: " + b;

        }catch(MalformedURLException mue) {
            mue.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return "empty";
    }

    @Override
    protected String doInBackground(Void... voids) {
        return download();
    }

    @Override
    protected void onPostExecute(String s) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);
        builder.setMessage("sup bro!");
        builder.setInverseBackgroundForced(true);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.dismiss();
            }
        });

        if (!activity.isFinishing()) builder.show();
        listener.onTaskCompleted();
    }
}
