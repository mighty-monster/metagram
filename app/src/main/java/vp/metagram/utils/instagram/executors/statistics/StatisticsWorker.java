package vp.metagram.utils.instagram.executors.statistics;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;



import static vp.metagram.general.variables.logger;
import static vp.metagram.general.variables.metagramAgent;


public class StatisticsWorker extends Worker
{

    public static String work_id = "statistics_scheduler";

    public StatisticsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        try
        {
            metagramAgent.saveStats();
            metagramAgent.loadStatisticsExecutors();
            /*metagramAgent.loadUnfollowerRobots();
            metagramAgent.robotManager.refresh();*/
        }
        catch (Exception e)
        {
            logger.logWTF(this.getClass().getName(), "Fatal Error, Scheduler Error", e);
        }

        Log.d("metagram_work_manager","Statistics scheduler has been called");

        return Result.success();
    }
}
