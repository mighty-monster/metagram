package vp.metagram.base;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import cat.ereza.customactivityoncrash.config.CaocConfig;

import vp.metagram.BuildConfig;
import vp.metagram.general.functions;
import vp.metagram.ui.MainActivity;
import vp.metagram.utils.LocaleManager;
import vp.metagram.utils.deviceInfo;
import vp.metagram.utils.instagram.MetagramAgent;
import vp.metagram.utils.instagram.executors.statistics.StatisticsWorker;
import vp.metagram.utils.VersionUtils;
import vp.tools.http.iEndPoints;
import vp.tools.http.iMetaCom;
import vp.tools.http.iServerAddress;
import vp.tools.io.iAPILogger;
import vp.tools.io.iDBMetagram;
import vp.tools.io.iLogger;

import static vp.metagram.general.channels.create_notification_channels;
import static vp.metagram.general.variables.APICacheAddress;
import static vp.metagram.general.variables.APILogger;
import static vp.metagram.general.variables.APILoggerDBName;
import static vp.metagram.general.variables.DBVersion;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.appVersion;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.deviceInfoJS;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.executionMode;
import static vp.metagram.general.variables.logger;
import static vp.metagram.general.variables.loggerDBName;
import static vp.metagram.general.variables.mainDatabaseName;
import static vp.metagram.general.variables.metaComGen;
import static vp.metagram.general.variables.metaComReg;
import static vp.metagram.general.variables.metaComSer;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.serverAddress;
import static vp.metagram.general.variables.threadPoolExecutor;

/**
 * Created by arash on 2/12/18.
 */


public class MetagramApp extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();

        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                .enabled(true)
                .showErrorDetails(true)
                .showRestartButton(true)
                .logErrorOnRestart(false)
                .trackActivities(true)
                .minTimeBetweenCrashesMs(2000)
                .restartActivity(MainActivity.class)
                .apply();

        try
        {

            appContext = getApplicationContext();
            logger = new iLogger(appContext, loggerDBName);
            APILogger = new iAPILogger(appContext,APILoggerDBName);

            if ( appVersion == null )
            {
                try
                {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    appVersion = VersionUtils.createFromVersionString(pInfo.versionName);


                }
                catch (Exception e)
                {
                    logger.logWTF(this.getClass().getName(),
                            "Version creation failed.\n", e);
                }
            }

            APICacheAddress = appContext.getCacheDir() + "/picasso-cache";

            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

            if ( BuildConfig.DEBUG )
            {
                executionMode = "debug";
                //executionMode = "release";
            }

            try
            {
                dbMetagram = new iDBMetagram(appContext, mainDatabaseName, DBVersion);

            }
            catch (Exception e)
            {
                logger.logWTF(this.getClass().getName(),
                        "Main database manager creation failed.\n", e);
            }


            serverAddress = new iServerAddress();

            metaComGen = new iMetaCom(this, serverAddress, iEndPoints.gen);
            metaComReg = new iMetaCom(this, serverAddress, iEndPoints.reg);
            metaComSer = new iMetaCom(this, serverAddress, iEndPoints.ser);


            try
            {
                deviceSettings = DeviceSetting.load();
                if (deviceSettings.firstRunTime == 0)
                { deviceSettings.firstRunTime = System.currentTimeMillis(); }
                deviceSettings.save();

                metagramAgent = new MetagramAgent(this, deviceSettings);

            }
            catch (Exception e)
            {
                logger.logWTF(this.getClass().getName(),
                        "Metagram Agent creation failed.\n", e);
            }


            try
            {
                if (deviceSettings.minVersion > appVersion.getSourceVersion())
                {
                    deviceSettings.mustUpgrade = true;
                    deviceSettings.save();
                }

            }
            catch (Exception e)
            {
                logger.logWTF(this.getClass().getName(),
                        "Setting upgrade flag failed.\n", e);
            }



            threadPoolExecutor.execute(functions::cleanupDirectories);


            try
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                }

            }
            catch (Exception e)
            {
                logger.logWTF(this.getClass().getName(),
                        "Disabling StrictMode failed.\n", e);
            }


            threadPoolExecutor.execute(()->
            {
                String key = "deviceInfo";
                try
                {
                    String deviceInfoStr = dbMetagram.getPair(key);
                    if ( deviceInfoStr.equals("") )
                    {
                        deviceInfoJS = deviceInfo.collect();
                        dbMetagram.setPair(key, deviceInfoJS.toString());
                    }
                    else
                    {
                        deviceInfoJS = new JSONObject(deviceInfoStr);
                    }
                }
                catch (Exception e)
                {
                    logger.logError(this.getClass().getName(), "error getting device info", e);
                }
            });

            create_notification_channels(this);

            threadPoolExecutor.execute(() ->
            {
                try
                {
                    metagramAgent.loadStatisticsExecutors();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

            });


            Constraints internet_constraint = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest schedule_work =
                new PeriodicWorkRequest.Builder(StatisticsWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(internet_constraint)
                        .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(StatisticsWorker.work_id,
                ExistingPeriodicWorkPolicy.KEEP,
                    schedule_work);

        }
        catch (Exception e)
        {
            Log.wtf(this.getClass().getName(), e.getMessage());

        }
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

}