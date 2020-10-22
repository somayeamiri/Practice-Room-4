package com.example.roomjava4;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ir.mahdi.mzip.zip.ZipArchive;

public class DownloadServices extends Service {


    private String DATABASE_NAME = "endb.db";
    private String FILE = "/data/data/com.example.roomjava4/databases/";
    private String CHANNNEL_ID = "ID_CHANNEL";
    private String CHANNNEL_NAME = "NAME_CHANNEL";

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        //creating CHANNEL for notification
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

         createNotificationChannel();
        }

        Log.i("DownloadServices", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("DownloadServices", "onStartCommand");

        String url = "";
        if (intent.getExtras() != null) {
            url = intent.getExtras().getString("URL");
        }
        Log.i("DownloadServices", url);

        final String finalurl = url;


        //creating new thread for doing some background works
        new Thread(new Runnable() {
            @Override
            public void run() {

                startForeground(123, startMyOwnForeground(0));
                DownLoadDatabase(finalurl, FILE, DATABASE_NAME);
                stopForeground(false);
                stopSelf();

            }


        }).start();


        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("DownloadServices", "onDestroy");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void DownLoadDatabase(String fileurl, String path, String filename) {

        String URL_ADDRESS = fileurl;
        String PATHNAME = path; // path for download and unzip

        //check folder exist
        File file = new File(PATHNAME);
        if (!file.exists()) {
            file.mkdirs();
        }


        try {
            // 1- get url and create connection
            URL url = new URL(URL_ADDRESS);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // 2 get file lenght
            int fileLength = connection.getContentLength();

            // 3- create input stream and output stream
            InputStream input = connection.getInputStream();
            OutputStream output = new FileOutputStream(PATHNAME + filename);

            //4- create byte array for read data
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {

                total += count;

                int percent = ((int) (total * 100 / fileLength));

                notificationManager.notify(123, startMyOwnForeground(percent));
                Log.i("DownloadServices", percent + "");
                output.write(data, 0, count);
            }

            Log.i("DownloadServices", "finished");
            output.close();
            input.close();
            connection.disconnect();

        } catch (Exception e) {

            Log.e("log", e.getMessage());
        }


        //7- Unzip
        ZipArchive zipArchive = new ZipArchive();
        zipArchive.unzip(PATHNAME + filename, PATHNAME, "");


    }


    private Notification startMyOwnForeground(int percent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
                .setContentTitle("App is running in background")
                .setContentText(percent + "Downloaded")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        return notification;
    }



    private void createNotificationChannel() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = CHANNNEL_ID;
            String channelName = CHANNNEL_NAME; // You should create a String resource for this instead of storing in a variable
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLockscreenVisibility(Notification.BADGE_ICON_SMALL);
            assert notificationManager != null;
            chan.setDescription("This is default channel used for all other notifications");
            notificationManager.createNotificationChannel(chan);
        }
    }

}
