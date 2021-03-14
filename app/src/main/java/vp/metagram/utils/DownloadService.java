package vp.metagram.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.util.Random;


import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;

import static android.content.Context.DOWNLOAD_SERVICE;
import static vp.metagram.general.functions.createNotification;
import static vp.metagram.general.variables.appContext;


public class DownloadService
{
    static private DownloadManager downloadManager = null;
    private Context context;
    Runnable unregisterRunnable = null;

    public DownloadService(Context context)
    {
        this.context = context;
        if (downloadManager == null)
        {
            downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        }

    }


    public void download(String path, String filename, String URL, String title, String description, boolean needReceiver, String username)
    {

        Uri uri = Uri.parse(URL);

        String subPath = path.substring(path.indexOf("/files/")+"/files/".length());
        try
        {
            long lastDownload = downloadManager.enqueue(new DownloadManager.Request(uri)
                    .setDestinationInExternalFilesDir(context, subPath, filename)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(title)
                    .setDescription(description));

            if (needReceiver)
            {
                BroadcastReceiver onComplete = new BroadcastReceiver()
                {
                    public void onReceive(Context ctxt, Intent intent)
                    {
                        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                        if (lastDownload == id)
                        {
                            Intent notificationIntent = new Intent(appContext,MediaViewerActivity.class);
                            notificationIntent.putExtra("path",path);
                            notificationIntent.putExtra("isLocal",true);
                            createNotification(context,
                                    username ,
                                    "Download Completed",
                                    notificationIntent,
                                    true,
                                    new Random().nextInt(),
                                    false,
                                    true);

                            if (unregisterRunnable != null)
                            {
                                new Handler(Looper.getMainLooper()).post(unregisterRunnable);
                            }
                        }
                    }
                };

                unregisterRunnable = ()->
                {
                    if (context != null)
                    {
                        try
                        {
                            context.unregisterReceiver(onComplete);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };

                context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
