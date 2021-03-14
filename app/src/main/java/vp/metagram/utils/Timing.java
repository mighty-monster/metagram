package vp.metagram.utils;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.logger;

public class Timing
{
    long wakeupCounter = 1;
    long sleepCounter = 1;

    long firstTime = -1;
    long lastTime = -1;

    long lastWakeupTime = -1;
    long lastSleepTime = -1;

    long lastWakeupDuration = 0;
    long lastSleepDuration = 0;

    double averageWakeupDuration = 0;
    double averageSleepDuration = 0;

    double expectedSleepDuration = 0;

    long realPastTime = 0;
    long relativePastTime = 0;


    public void beforeSleep(long lastSleepTime) throws GeneralSecurityException, IOException, InterruptedException
    {
        if (this.firstTime < 0 )
        {this.firstTime = System.currentTimeMillis();}

        this.lastSleepTime = lastSleepTime;
        this.lastTime = lastSleepTime;

        if (lastWakeupTime < 0)
        {
            long duration = lastSleepTime - firstTime;
            relativePastTime += duration;
            return;
        }

        lastWakeupDuration = lastSleepTime - lastWakeupTime;
        relativePastTime += lastWakeupDuration;

        averageWakeupDuration = (averageWakeupDuration*(sleepCounter-1) + lastWakeupDuration) / sleepCounter;
        sleepCounter++;

        realPastTime = lastTime - firstTime;

        save();

    }

    public void afterSleep(long lastWakeupTime, long threadSleepTimeValue) throws GeneralSecurityException, IOException, InterruptedException
    {
        if (this.firstTime < 0 )
        {this.firstTime = System.currentTimeMillis();}

        this.lastWakeupTime = lastWakeupTime;
        this.lastTime = lastWakeupTime;

        lastSleepDuration = lastWakeupTime - lastSleepTime;
        relativePastTime += threadSleepTimeValue;

        averageSleepDuration = (averageSleepDuration*(wakeupCounter-1) + lastSleepDuration) / wakeupCounter;
        wakeupCounter++;

        realPastTime = lastTime - firstTime;

        expectedSleepDuration = (expectedSleepDuration*(wakeupCounter-1) + threadSleepTimeValue) /wakeupCounter;

        save();
    }



    public void save() throws GeneralSecurityException, IOException, InterruptedException
    {

        String key = "timing";
        Gson gson = new Gson();
        String json = gson.toJson(this);

        dbMetagram.setPair(key,json);
    }

    static public Timing load()
    {
        String key = "timing";
        Timing result;
        try
        {
            Gson gson = new Gson();
            result = gson.fromJson(dbMetagram.getPair(key), Timing.class);
        }
        catch (Exception e)
        {
            logger.logError("timing.Load()",
                    "Load timing from db failed.\n",e);
            result = new Timing();
        }
        if (result == null)
        {
            result = new Timing();
        }
        return result;
    }

}
