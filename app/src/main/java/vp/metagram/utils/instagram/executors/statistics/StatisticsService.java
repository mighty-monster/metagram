package vp.metagram.utils.instagram.executors.statistics;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;


import vp.metagram.R;
import vp.metagram.ui.MainActivity;

import static vp.metagram.general.channels.CHANNEL_ID_REPORTS;
import static vp.metagram.general.variables.metagramAgent;


public class StatisticsService extends Service
{

    public static int service_id = 1001;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int small_icon = R.drawable.ic_notification;

        String ContentText = null;
        try
        {
            ContentText = String.format(Locale.ENGLISH, "%d active report(s)", metagramAgent.getNoOfStatisticsActiveJobs());
        }
        catch (IOException | GeneralSecurityException e)
        {
            e.printStackTrace();
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_REPORTS)
                .setContentTitle("Collecting reports")
                .setContentText(ContentText)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(small_icon);

        mBuilder.setSound(null);
        mBuilder.setDefaults(0);

        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), small_icon));

        startForeground(service_id, mBuilder.build());

        return START_NOT_STICKY;
    }
}
