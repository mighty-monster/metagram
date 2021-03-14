package vp.metagram.general;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;



import static androidx.core.content.ContextCompat.getSystemService;
import static vp.metagram.general.variables.appVersion;


public class channels
{
    public final static String CHANNEL_ID_REPORTS = "Reports Channel";
    public final static String CHANNEL_ID_FINISHED = "Finished Jobs Channel";


    public static void create_notification_channels(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String CHANNEL_NAME_REPORTS = String.format("%s Reports", appVersion.get_appName_en());
            String CHANNEL_NAME_FINISHED = String.format("%s Finished jobs", appVersion.get_appName_en());

            NotificationChannel reports_channel =
                    new NotificationChannel(
                            CHANNEL_ID_REPORTS,
                            CHANNEL_NAME_REPORTS,
                            NotificationManager.IMPORTANCE_LOW);

            NotificationChannel finished_channel =
                    new NotificationChannel(
                            CHANNEL_ID_FINISHED,
                            CHANNEL_NAME_FINISHED,
                            NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = getSystemService(context, NotificationManager.class);
            notificationManager.createNotificationChannel(reports_channel);
            notificationManager.createNotificationChannel(finished_channel);
        }
    }
}
