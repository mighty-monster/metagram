package vp.metagram.general;

import android.content.Context;

import org.json.JSONObject;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


import vp.metagram.base.DeviceSetting;
import vp.metagram.utils.instagram.MetagramAgent;
import vp.metagram.utils.VersionUtils;
import vp.tools.http.iMetaCom;
import vp.tools.http.iServerAddress;
import vp.tools.io.iAPILogger;
import vp.tools.io.iDBMetagram;
import vp.tools.io.iLogger;

/**
 * Created by arash on 2/12/18.
 */

public class variables
{
    static public Context appContext;
    static public iDBMetagram dbMetagram;
    static public iLogger logger;
    static public iAPILogger APILogger;

    static public int DBVersion = 1001;
    static public String mainDatabaseName = "metagram.db";
    static public String loggerDBName = "log.db";
    static public String APILoggerDBName = "APILog.db";
    static public MetagramAgent metagramAgent;
    static public DeviceSetting deviceSettings;

    static public JSONObject deviceInfoJS;

    static public String executionMode = "release";

    static public iServerAddress serverAddress;

    static public iMetaCom metaComReg;
    static public iMetaCom metaComGen;
    static public iMetaCom metaComSer;

    public static int updatedStatus = 1;
    public static int idleStatus = 0;

    public static boolean isReleaseMode = false;

    public static String AndroidClientDBIV = "idk83irujflovjdu";
    public static String ExportDBIV = "*C&H7s6tcg8^F%C&DS%$&^Y8hsabkausbd&^ISA%**D";

    public static String APICacheAddress;

    public static int ScrollerDeltaMargin = 20;
    public static int ScrollerPreFetchItems = 0;

    public static VersionUtils appVersion;

    public static ScheduledExecutorService threadPoolExecutor = new ScheduledThreadPoolExecutor(8);


}
