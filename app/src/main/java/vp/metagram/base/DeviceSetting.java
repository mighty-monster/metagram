package vp.metagram.base;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;


import vp.metagram.types.enumAccountRegisterStatus;
import vp.metagram.types.enumClientRegisterStatus;

import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.logger;

/**
 * Created by arash on 2/18/18.
 */

public class DeviceSetting
{


    // Local Settings
    public String DeviceUUI="";
    public enumClientRegisterStatus clientRegisterStatus = enumClientRegisterStatus.none;
    public enumAccountRegisterStatus accountRegisterStatus = enumAccountRegisterStatus.none;
    public String comAES="";

    // Robots Settings
    public int unfollowAfterDaysMin       = 1;
    public int unfollowAfterDaysMax       = 90;
    public int unfollowAfterDaysDefault   = 15;
    public int unfollowPerHourMin         = 1;
    public int unfollowPerHourMax         = 500;
    public int unfollowPerHourDefault     = 15;
    public int unfollowPerDayMin          = 1;
    public int unfollowPerDayMax          = 4000;
    public int unfollowPerDayDefault      = 360;
    public int autoLikeIntervalDefault    = 60;
    public int autoLikeIntervalMin        = 20;
    public int autoLikeIntervalMax        = 24*60;
    public int autoLikePostsPerDayDefault = 500;
    public int autoLikePostsPerDayMin     = 1;
    public int autoLikePostsPerDayMax     = 2000;
    public int autoLikeCommentsPerDayDefault = 300;
    public int autoLikeCommentsPerDayMin     = 1;
    public int autoLikeCommentsPerDayMax     = 1000;


    // Statistics Settings
    public int statisticsMaxFollowersValue = 10 * 1000;
    public int statisticsMaxFollowingValue = 10 * 1000;
    public int statisticsMaxCombinedValue  = 15 * 1000;
    public int statisticsMaxPostsValue     = 1000;
    public int statisticsDefaultJobInterval = 24*2; // Hours

    public int statisticsMaxFollowersValueLimit = 30 * 1000;
    public int statisticsMaxFollowingValueLimit = 30 * 1000;
    public int statisticsMaxCombinedValueLimit  = 50 * 1000;
    public int statisticsMaxPostsValueLimit     = 5000;

    public int S_ParameterPrice = 2;
    public int F_ParameterPrice = 2;
    public int P_ParameterPrice = 2;
    public int D_ParameterPrice = 4;

    public long UnfollowDelay = 40*1000;
    public int UnfollowerRobotPrice = 20;
    public int UnfollowerWorkerInterval = 60 * 1000; // 1 Minutes
    public int UnfollowerUnfollowMinutes = 20;
    public int UnfollowerGatheringMinutes = 6 * 60;

    public int AutoLikeRobotPrice = 30;
    public int AutoLikeWorkerInterval = 60 * 1000; // 1 Minutes

    public boolean wakeLock = true;
    public boolean stickyNotification = false;
    public int statisticsDelayTime = 1000;
    public boolean logging = true;

    public int minVersion = -1;
    public boolean mustUpgrade = false;

    public long firstRunTime = 0;

    public boolean powerSaving = false;

    public void save() throws IOException, GeneralSecurityException, InterruptedException
    {

        String key = "deviceSettings";
        Gson gson = new Gson();
        String json = gson.toJson(this);

        dbMetagram.setPair(key,json);
    }

    static public DeviceSetting load()
    {
        String key = "deviceSettings";
        DeviceSetting result;
        try
        {
            Gson gson = new Gson();
            String jsonValue = dbMetagram.getPair(key);
            result = gson.fromJson(jsonValue, DeviceSetting.class);
        }
        catch (Exception e)
        {
            logger.logError("DeviceSetting.Load()",
                    "Load setting from db failed.\n",e);
            result = new DeviceSetting();
        }
        if (result == null)
        {
            result = new DeviceSetting();
        }
        return result;
    }

}
