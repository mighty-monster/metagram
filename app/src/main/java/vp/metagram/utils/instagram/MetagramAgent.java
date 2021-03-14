package vp.metagram.utils.instagram;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


import vp.igwa.IGWAExtractor;
import vp.metagram.R;
import vp.metagram.base.DeviceSetting;
import vp.metagram.ui.MainActivity;
import vp.metagram.utils.StatObservatory;
import vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor;
import vp.metagram.utils.instagram.executors.statistics.StatisticsService;
import vp.metagram.utils.instagram.executors.statistics.types.ExecutorPool;
import vp.metagram.utils.instagram.types.UserFull;
import vp.igpapi.IGWAException;

import static vp.metagram.general.channels.CHANNEL_ID_REPORTS;
import static vp.metagram.general.functions.getDateFromTimeStampRevert;
import static vp.metagram.general.functions.getThreadByName;
import static vp.metagram.general.functions.repeatSingleQuotes;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.logger;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateDone;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateReady;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateStop;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateWorking;


/**
 * Created by arash on 2/18/18.
 */


public class MetagramAgent
{
    public Context context;

    private Semaphore metagramMutex = new Semaphore(1);

    public List<InstagramAgent> instagramAgents = new ArrayList<>();
    public InstagramAgent activeAgent = null;

    DeviceSetting setting;

    public ExecutorPool executorPool = new ExecutorPool();

    public MetagramAgent(Context context, DeviceSetting setting) throws Exception
    {
        this.context = context;
        this.setting = setting;

        upgradeToNewAPIEngine();
        reloadInstagramAgents();
        loadSetting();
        saveSetting();
    }


    public int getNoOfAccounts()
    {
        return instagramAgents.size();
    }

    public int getNoOfRegisteredAccounts()
    {
        int count = 0;

        for ( InstagramAgent agent : instagramAgents )
        {
            if ( agent.isRegistered )
            {
                count++;
            }
        }

        return count;
    }

    public String addInstagramAccount(IGWAExtractor newIGClient) throws IOException, GeneralSecurityException, JSONException, IGWAException
    {
        String result;

        InstagramAgent newAgent = new InstagramAgent(newIGClient);

        boolean isDuplicate = false;
        Iterator<InstagramAgent> iterator = instagramAgents.iterator();
        while ( iterator.hasNext() )
        {
            InstagramAgent instagramAgent = iterator.next();
            if ( instagramAgent.userID == newAgent.userID )
            {
                isDuplicate = true;
                instagramAgents.remove(instagramAgent);
                break;
            }
        }

        instagramAgents.add(newAgent);

        if ( isDuplicate )
        {
            result = context.getResources().getString(R.string.login_duplicate);
        }
        else
        {
            result = context.getResources().getString(R.string.login_successful);
        }

        saveSetting();

        newAgent.GetAccountInfoAndSaveToDB();

        /*InstagramAgent finalNewAgent = newAgent;
        threadPoolExecutor.execute(() ->
        {
            try
            {
                finalNewAgent.GetAccountInfoAndSaveToDB();
            }
            catch (Exception e)
            {
                logger.logError(this.getClass().getName(),
                        "Error while getting account picture after login\n", e);
            }
        });*/

        return result;
    }

    public long getIPKByUsername(String username)
    {
        long result = -1;


        Iterator<InstagramAgent> iterator = instagramAgents.iterator();
        while ( iterator.hasNext() )
        {
            InstagramAgent instagramAgent = iterator.next();
            if ( instagramAgent.username.equals(username) )
            {
                result = instagramAgent.userID;
                break;
            }
        }

        return result;
    }

    public void deleteInstagramAccount(long IPK) throws IOException, GeneralSecurityException, JSONException
    {

        for (StatisticsExecutor executor: statisticsExecutors)
        { executor.halt(); }

        String sqlText = "";
        MatrixCursor result = null;


        // Delete Statistics
        sqlText = String.format(Locale.ENGLISH, "Select StatOrderID,IPK From Statistics_Orders Where FIPK = %d", IPK);
        result = dbMetagram.selectQuery(sqlText);
        if ( result.moveToFirst() )
        {
            while ( !result.isAfterLast() )
            {
                int OrderID = result.getInt(result.getColumnIndex("StatOrderID"));
                long execIPK = result.getLong(result.getColumnIndex("IPK"));
                haltExecutorByIPK(execIPK);
                sqlText = String.format(Locale.ENGLISH, "delete from Statistics_Jobs Where StatOrderID = %d", OrderID);
                dbMetagram.execQuery(sqlText);
                result.moveToNext();
            }
        }
        sqlText = String.format(Locale.ENGLISH, "delete from Statistics_Orders Where FIPK = %d", IPK);
        dbMetagram.execQuery(sqlText);
        // Delete Statistics

        // Delete Account
        activeAgent = null;
        saveSetting();

        int agentsListSize = instagramAgents.size();
        for ( int i = agentsListSize - 1; i >= 0; i-- )
        {
            if ( instagramAgents.get(i).userID == IPK )
            {
                instagramAgents.remove(i);
                break;
            }
        }

        sqlText = String.format(Locale.ENGLISH, "delete from Account_Info Where FIPK = %d", IPK);
        dbMetagram.execQuery(sqlText);

        sqlText = String.format(Locale.ENGLISH, "delete from Accounts Where IPK = %d", IPK);
        dbMetagram.execQuery(sqlText);
        //Delete Account
    }

    public void setActiveAgentByUsername(String username) throws Exception
    {
        try
        {
            metagramMutex.acquire();
            boolean accountFound = false;
            try
            {

                Iterator<InstagramAgent> iterator = instagramAgents.iterator();
                while ( iterator.hasNext() )
                {
                    InstagramAgent instagramAgent = iterator.next();
                    if ( instagramAgent.username.equals(username) )
                    {
                        activeAgent = instagramAgent;
                        accountFound = true;
                        break;
                    }
                }

                if ( !accountFound )
                {
                    throw new Exception("username not found in the list of agents");
                }
            }
            finally
            {
                metagramMutex.release();
                if ( accountFound )
                {
                    saveSetting();
                }
            }
        }
        catch (InterruptedException e)
        {
            logger.logError(this.getClass().getName(),
                    "Metagram Agent setActiveAgentByUsername failed.\n", e);
        }
    }

    public void setAccountRegisteredByUsername(String username) throws Exception
    {
        long IPK;

        Iterator<InstagramAgent> iterator = instagramAgents.iterator();
        while ( iterator.hasNext() )
        {
            InstagramAgent instagramAgent = iterator.next();
            if ( instagramAgent.username.equals(username) )
            {
                IPK = instagramAgent.userID;
                instagramAgent.isRegistered = true;
                instagramAgent.saveAPIInternalsToDB();
                break;
            }
        }
    }

    public void reloadInstagramAgents()
    {

        try
        {
            metagramMutex.acquire();
            try
            {
                MatrixCursor queryResult = dbMetagram.selectQuery("Select IPK, Username from Accounts Where Registered = 1");
                if ( queryResult.moveToFirst() )
                {
                    while ( !queryResult.isAfterLast() )
                    {
                        long agentIPK = queryResult.getLong(queryResult.getColumnIndex("IPK"));
                        String username = queryResult.getString(queryResult.getColumnIndex("Username"));
                        boolean isThere = false;
                        Iterator<InstagramAgent> iterator = instagramAgents.iterator();
                        while ( iterator.hasNext() )
                        {
                            if ( iterator.next().userID == agentIPK )
                            {
                                isThere = true;
                                break;
                            }
                        }

                        if ( !isThere )
                        {
                            instagramAgents.add(new InstagramAgent(username));
                        }
                        queryResult.moveToNext();
                    }
                }
            }
            finally
            {
                metagramMutex.release();
            }
        }
        catch (Exception e)
        {
            logger.logError(this.getClass().getName(),
                    "Metagram Agent reload failed.\n", e);
        }
    }

    public void saveSetting() throws JSONException, IOException, GeneralSecurityException
    {
        try
        {
            metagramMutex.acquire();
            try
            {
                JSONObject json = new JSONObject();
                if ( activeAgent == null )
                {
                    json.put("activeAgent", 0);
                }
                else
                {
                    json.put("activeAgent", activeAgent.userID);
                }

                dbMetagram.setPair("metagramAgentSetting", json.toString());
            }
            finally
            {
                metagramMutex.release();
            }
        }
        catch (InterruptedException e)
        {
            logger.logError(this.getClass().getName(),
                    "Metagram Agent saveSetting failed.\n", e);
        }
    }

    public void loadSetting() throws IOException, GeneralSecurityException, JSONException
    {
        try
        {
            metagramMutex.acquire();
            try
            {
                String settingInfo = dbMetagram.getPair("metagramAgentSetting");
                if ( settingInfo.equals("") ) {return;}
                JSONObject json = new JSONObject(settingInfo);
                long activeID = json.getLong("activeAgent");
                if ( activeID == 0 )
                {
                    activeAgent = null;
                    return;
                }
                else
                {

                    for (InstagramAgent agent : instagramAgents)
                    {
                        if (agent.userID == activeID)
                        {
                            activeAgent = agent;
                            break;
                        }
                    }
                }
            }
            finally
            {
                metagramMutex.release();
            }
        }
        catch (InterruptedException e)
        {
            logger.logError(this.getClass().getName(),
                    "Metagram Agent loadSetting failed.\n", e);
        }
    }

    public InstagramAgent getAgentByUsername(String username)
    {

        Iterator iterator = instagramAgents.iterator();

        while ( iterator.hasNext() )
        {
            InstagramAgent instagramAgent = (InstagramAgent) iterator.next();
            if ( instagramAgent.username.equals(username) )
            {return instagramAgent;}
        }
        return null;
    }

    public InstagramAgent getAgentByIPK(long IPK)
    {

        Iterator iterator = instagramAgents.iterator();

        while ( iterator.hasNext() )
        {
            InstagramAgent instagramAgent = (InstagramAgent) iterator.next();
            if ( instagramAgent.userID == IPK )
            {return instagramAgent;}
        }
        return null;
    }

    public void loadExecutors() throws Exception
    {
        // Load Statistics Executors
        loadStatisticsExecutors();
    }


    // Statistics
    public Semaphore statisticsRestartMutex = new Semaphore(1);
    public Semaphore statisticsLoadMutex = new Semaphore(1);

    public List<StatisticsExecutor> statisticsExecutors = new ArrayList<>();

    public StatObservatory statObservatory = new StatObservatory();

    static public int orderFlagReady = 1;
    static public int orderFlagChanging = -1;

    public void StartStatisticsService()
    {
        Intent serviceIntent = new Intent(context, StatisticsService.class);
        ContextCompat.startForegroundService(context ,serviceIntent);
    }

    public void StopStatisticsService() throws IOException, GeneralSecurityException
    {
        if (getNoOfStatisticsActiveJobs() == 0)
        {
            Intent serviceIntent = new Intent(context, StatisticsService.class);
            context.stopService(serviceIntent);
        }
        else
        {
            String ContentText = null;
            try
            {
                ContentText = String.format(Locale.ENGLISH, "%d active report(s)", metagramAgent.getNoOfStatisticsActiveJobs());
            }
            catch (IOException | GeneralSecurityException e)
            {
                e.printStackTrace();
            }

            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            int small_icon = R.drawable.ic_notification;


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_REPORTS)
                    .setContentTitle("Collecting reports")
                    .setContentText(ContentText)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(small_icon);

            mBuilder.setSound(null);
            mBuilder.setDefaults(0);


            mBuilder.setSmallIcon(small_icon);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), small_icon));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(StatisticsService.service_id, mBuilder.build());
        }
    }

    public StatisticsExecutor getStatisticsExecutorByJobID(int jobID)
    {
        Iterator iterator = statisticsExecutors.iterator();

        while ( iterator.hasNext() )
        {
            StatisticsExecutor executor = (StatisticsExecutor) iterator.next();
            if (executor.StatisticsJobID == jobID)
            {
                return executor;
            }
        }

        return null;
    }

    public StatisticsExecutor getStatisticsActiveExecutorByOrderID(int orderID)
    {
        Iterator iterator = statisticsExecutors.iterator();

        while ( iterator.hasNext() )
        {
            StatisticsExecutor executor = (StatisticsExecutor) iterator.next();
            if (executor.StatisticsOrderID == orderID &&  !executor.status.equals(statusStateDone))
            {
                return executor;
            }
        }

        return null;
    }

    public void loadUnfollowerRobots() throws Exception
    {
        if (deviceSettings.mustUpgrade) {return;}
    }

    public void loadStatisticsExecutors() throws InterruptedException
    {
        if (deviceSettings.mustUpgrade) {return;}

        statisticsLoadMutex.acquire();
        try
        {
            // Find Orders with out jobs
            String sqlText = String.format(Locale.ENGLISH,
                    " Select IPK from Statistics_Orders \n" +
                    "    Where 0 = (Select Count(*) from Statistics_Jobs Where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID And Statistics_Orders.Flag = %d)",
                    orderFlagReady);

            MatrixCursor joblessOrders = dbMetagram.selectQuery(sqlText);

            if ( joblessOrders.moveToFirst() )
            {
                while ( !joblessOrders.isAfterLast() )
                {
                    long IPK = joblessOrders.getLong(joblessOrders.getColumnIndex("IPK"));

                    if (!accountHaveActiveStatisticsJob(IPK))
                    {
                        addStatisticsJob(IPK);
                    }

                    joblessOrders.moveToNext();
                }
            }

            Thread.sleep(1000);

            // Adding new jobs for auto refreshing orders
            sqlText =   String.format(Locale.ENGLISH,
                    "Select Statistics_Orders.StatOrderID, IPK, intervalInSeconds, Status, " +
                    " StartTime, StopTime, ReStartTime, StopTime, EndTime from Statistics_Orders Left join Statistics_Jobs " +
                    "    on Statistics_Jobs.StatJobID = \n" +
                    "    (select StatJobID from Statistics_Jobs\n" +
                    "        where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID\n" +
                    "            order by Statistics_Jobs.StatJobID desc limit 1) where autoRefresh = 1 And Statistics_Orders.Flag = %d ", orderFlagReady);


            MatrixCursor previousJobs = dbMetagram.selectQuery(sqlText);

            if (previousJobs.moveToFirst())
            {

                while ( !previousJobs.isAfterLast() )
                {
                    String status = previousJobs.getString(previousJobs.getColumnIndex("Status"));

                    long IPK = previousJobs.getLong(previousJobs.getColumnIndex("IPK"));

                    long stopTime = previousJobs.getLong(previousJobs.getColumnIndex("StopTime"));
                    long reStartTime = previousJobs.getLong(previousJobs.getColumnIndex("ReStartTime"));
                    long endTime = previousJobs.getLong(previousJobs.getColumnIndex("EndTime"));

                    long interval = previousJobs.getInt(previousJobs.getColumnIndex("intervalInSeconds")) / 60;

                    long elapsedMinutesAfterEnd = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(endTime);

                    long elapsedMinutesBetweenRestart = 0;
                    if (stopTime > 0)
                    {
                        elapsedMinutesBetweenRestart = TimeUnit.MILLISECONDS.toMinutes(reStartTime) - TimeUnit.MILLISECONDS.toMinutes(stopTime);
                    }

                    if ( status.equals(statusStateDone) )
                    {
                        boolean needNewJob = false;

                        if ( stopTime != -1 && reStartTime != -1 )
                        {
                            if ( elapsedMinutesAfterEnd + elapsedMinutesBetweenRestart > interval )
                            {
                                needNewJob = true;
                            }
                        }
                        else
                        {
                            if ( elapsedMinutesAfterEnd > interval )
                            {
                                needNewJob = true;
                            }
                        }

                        if ( needNewJob )
                        {
                            addStatisticsJob(IPK);
                        }
                    }
                    previousJobs.moveToNext();
                }

            }

            Thread.sleep(1000);

            // Find unfinished jobs
            sqlText =   String.format(Locale.ENGLISH,
                        " select StatJobID,Status,ThreadName,ThreadID,Statistics_Orders.IPK as IPK from Statistics_Orders Left join Statistics_Jobs \n" +
                        "    on Statistics_Jobs.StatJobID = \n" +
                        "    (select StatJobID from Statistics_Jobs\n" +
                        "        where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID\n" +
                        "            order by Statistics_Jobs.StatJobID desc limit 1) where Statistics_Jobs.Status <> 'done' = 1 And Statistics_Orders.Flag = %d ", orderFlagReady);

            MatrixCursor jobCursor = dbMetagram.selectQuery(sqlText);

            if ( jobCursor.moveToFirst() )
            {
                while ( !jobCursor.isAfterLast() )
                {
                    int StatJobID = jobCursor.getInt(jobCursor.getColumnIndex("StatJobID"));
                    String status = jobCursor.getString(jobCursor.getColumnIndex("Status"));
                    String threadName = jobCursor.getString(jobCursor.getColumnIndex("ThreadName"));
                    long threadID = jobCursor.getLong(jobCursor.getColumnIndex("ThreadID"));

                    long IPK = jobCursor.getLong(jobCursor.getColumnIndex("IPK"));

                    if (  !isExecutorThreadActive(threadName, threadID) && !accountHaveActiveStatisticsJob(IPK) )
                    {
                        StatisticsExecutor newStatisticsExecutor = restartStatisticsJob(StatJobID, status);
                        statisticsExecutors.add(newStatisticsExecutor);
                        newStatisticsExecutor.startThread();

                        Thread.sleep(500);
                    }

                    jobCursor.moveToNext();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            statisticsLoadMutex.release();
        }
    }

    public void addStatisticsOrder(long IPK, long FIPK, String username, String userInfo,
                                   int F_Parameter, int P_Parameter, int D_Parameter, int interval, int R_Parameter) throws IOException, GeneralSecurityException
    {
        if ( userExistsInStatisticsOrders(IPK) ) {return;}

        String ExecAgents = Long.toString(FIPK);

        String sqlText = String.format(Locale.ENGLISH, " Insert  Or Ignore Into Statistics_Orders(IPK, FIPK, Username, UserInfo, ExecAgents,  F_Parameter, P_Parameter, D_Parameter, intervalInSeconds, autoRefresh) " +
                                                                                         " Values(  %d,  %d,   '%s',     '%s',     '%s',        %d,          %d,          %d,            %d,              %d  )",
                                                                    IPK, FIPK, repeatSingleQuotes(username), repeatSingleQuotes(userInfo), ExecAgents,  F_Parameter, P_Parameter, D_Parameter, interval, R_Parameter);
        dbMetagram.execQuery(sqlText);

    }

    public void updateStatisticsOrder(int OrderID, int F_Parameter, int P_Parameter, int D_Parameter, int interval, int R_Parameter) throws IOException
    {

        long reBornDate = System.currentTimeMillis();
        String reBornPart = String.format(Locale.ENGLISH," reBornDate = %d, ",reBornDate);

        String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Orders Set " + reBornPart +
                        " F_Parameter = %d, " +
                        " P_Parameter = %d, " +
                        " D_Parameter = %d, " +
                        " intervalInSeconds = %d, " +
                        " autoRefresh = %d " +
                        " Where StatOrderID = %d",
                F_Parameter,
                P_Parameter,
                D_Parameter,
                interval,
                R_Parameter,
                OrderID);

        dbMetagram.execQuery(sqlText);
    }

    public int addStatisticsJob(long IPK) throws Exception
    {
        statisticsRestartMutex.acquire();

        try
        {
            int newJobID = -1;

            MatrixCursor StatIDCursor = dbMetagram.selectQuery(String.format(Locale.ENGLISH, "Select StatOrderID From Statistics_Orders Where IPK = %d", IPK));
            int StatOrderID = -1;

            if ( StatIDCursor.moveToFirst() )
            {
                StatOrderID = StatIDCursor.getInt(StatIDCursor.getColumnIndex("StatOrderID"));
            }

            if ( StatOrderID < 0 )
            {
                throw new Exception("Cant not find  StatOrderID to create a job");
            }

            String sqlText = String.format(Locale.ENGLISH, " Insert Into Statistics_Jobs (StatOrderID)  Values(%d)", StatOrderID);
            dbMetagram.execQuery(sqlText);

            StatIDCursor = dbMetagram.selectQuery(String.format(Locale.ENGLISH, "Select StatJobID From Statistics_Jobs Where StatOrderID = %d Order By StatJobID Desc Limit 1", StatOrderID));

            if ( StatIDCursor.moveToFirst() )
            {
                newJobID = StatIDCursor.getInt(StatIDCursor.getColumnIndex("StatJobID"));
            }

            if ( newJobID < 0 )
            {
                throw new Exception("Cant not find created Job ID");
            }


            if (!accountHaveActiveStatisticsJob(IPK))
            {
                StatisticsExecutor newStatisticsExecutor = restartStatisticsJob(newJobID, statusStateReady);
                statisticsExecutors.add(newStatisticsExecutor);
                newStatisticsExecutor.startThread();
            }

            return newJobID;
        }
        finally
        {
            statisticsRestartMutex.release();
        }
    }

    public boolean accountHaveActiveStatisticsJob(long IPK)
    {
        boolean hasActiveJob = false;

        for (int i = 0; i < statisticsExecutors.size(); i++)
        {
            StatisticsExecutor statisticsExecutor = statisticsExecutors.get(i);

            if (statisticsExecutor.IPK == IPK && isExecutorThreadActive(statisticsExecutor.threadName, statisticsExecutor.threadID))
            {
                hasActiveJob = true;
            }
        }

        return hasActiveJob;
    }

    public void stopStatisticsJob(int StatJobID) throws IOException
    {

        StatisticsExecutor statisticsExecutor = getStatisticsExecutorByJobID(StatJobID);

        if (statisticsExecutor != null)
        {
            statisticsExecutor.stop();
        }

    }

    public StatisticsExecutor restartStatisticsJob(int StatJobID, String executorStatus) throws IOException
    {

        if ( executorStatus.equals(statusStateReady) )
        {
            String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Jobs Set Status = '%s' Where StatJobID = %d ",
                    statusStateWorking, StatJobID);
            dbMetagram.execQuery(sqlText);
        }

        StatisticsExecutor newStatisticsExecutor = new StatisticsExecutor(this, StatJobID, executorStatus, statObservatory);

        return newStatisticsExecutor;
    }

    public boolean isExecutorThreadActive(String threadName, long threadID)
    {
        boolean result = false;

        Thread executorThread = getThreadByName(threadName);

        if ( executorThread != null )
        {
            if ( executorThread.getId() == threadID )
            {
                result = true;
            }
        }

        return result;
    }

    public int getLastJobID(int OrderID) throws IOException, GeneralSecurityException
    {
        int JobID = -1;

        String sqlText = String.format(Locale.ENGLISH,
                "Select StatJobID from Statistics_Jobs Where Statistics_Jobs.StatOrderID = %d order by StatJobID desc limit 1", OrderID);


        MatrixCursor matrixCursor = dbMetagram.selectQuery(sqlText);

        if (matrixCursor.moveToFirst())
        {
            JobID = matrixCursor.getInt(matrixCursor.getColumnIndex("StatJobID"));
        }

        return JobID;
    }

    public void cancelStatisticsByOrderID(long IPK, int OrderID) throws IOException, GeneralSecurityException
    {
        stopExecutorByIPK(IPK);

        int JobID = getLastJobID(OrderID);

        deleteStatisticsJobInfoFromDB(JobID);

        String sqlText = String.format(Locale.ENGLISH, "Delete from Statistics_Jobs Where StatJobID = %d", JobID );
        dbMetagram.execQuery(sqlText);

        haltExecutorByIPK(IPK);

        StopStatisticsService();
    }


    public void deleteStatisticOrderByIPK(long IPK) throws IOException, GeneralSecurityException
    {

        stopExecutorByIPK(IPK);

        String sqlText = String.format(Locale.ENGLISH,"Select StatOrderID from Statistics_Orders Where IPK = %d", IPK);
        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        int StatisticOrderID = -1;
        if (result.moveToFirst())
        {
            StatisticOrderID = result.getInt(result.getColumnIndex("StatOrderID"));
        }

        sqlText = String.format(Locale.ENGLISH, "Select StatJobID From Statistics_Jobs Where StatOrderID = %d", StatisticOrderID);
        result = dbMetagram.selectQuery(sqlText);
        int JobID;
        if (result.moveToFirst())
        {
            while (!result.isAfterLast())
            {
                JobID = result.getInt(result.getColumnIndex("StatJobID"));
                deleteStatisticsJobInfoFromDB(JobID);
                result.moveToNext();
            }
        }

        if (StatisticOrderID > 0)
        {
            sqlText = String.format(Locale.ENGLISH, "Delete from Statistics_Jobs Where StatOrderID = %d", StatisticOrderID );
            dbMetagram.execQuery(sqlText);
            sqlText = String.format(Locale.ENGLISH, "Delete from Statistics_Orders Where StatOrderID = %d", StatisticOrderID );
            dbMetagram.execQuery(sqlText);
        }

        haltExecutorByIPK(IPK);
    }

    public void stopExecutorByIPK(long IPK) throws IOException
    {
        for (int i = 0; i < statisticsExecutors.size(); i++)
        {
            StatisticsExecutor statisticsExecutor = statisticsExecutors.get(i);

            if (statisticsExecutor.IPK == IPK )
            {
                statisticsExecutor.stop();
            }
        }
    }

    public String checkJobStatus(long IPK) throws IOException, GeneralSecurityException
    {

        String result = "";

        String sqlText = String.format(Locale.ENGLISH, "Select Status From Statistics_Orders Left Join Statistics_Jobs\n" +
                " On Statistics_Jobs.StatJobID = (Select StatJobID FROM Statistics_Jobs Where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID Order By StatJobID DESC Limit 1)\n" +
                "  Where Statistics_Orders.IPK = %s", IPK);

        MatrixCursor matrixCursor = dbMetagram.selectQuery(sqlText);

        if (matrixCursor.moveToFirst())
        {
            result = matrixCursor.getString(matrixCursor.getColumnIndex("Status"));
        }


        return result;
    }

    public boolean userExistsInStatisticsOrders(long IPK) throws IOException, GeneralSecurityException
    {
        boolean answer = true;
        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No from Statistics_Orders Where IPK = %d", IPK);
        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if ( result.moveToFirst() )
        {
            int no = result.getInt(result.getColumnIndex("No"));

            if ( no >= 1 )
            { answer = true; }
            else
            { answer = false; }
        }
        return answer;
    }

    public void haltExecutorByIPK(long IPK) throws IOException
    {
        for (int i = statisticsExecutors.size()-1; i >= 0; i--)
        {
            StatisticsExecutor statisticsExecutor = statisticsExecutors.get(i);

            if (statisticsExecutor.IPK == IPK )
            {
                statisticsExecutor.halt();
                statisticsExecutors.remove(i);
            }
        }
    }

    public int getNoOfStatisticsOrders() throws IOException, GeneralSecurityException
    {
        String sqlText = "Select Count(*) as No From Statistics_Orders";

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();

        return result.getInt(result.getColumnIndex("No"));
    }

    public int getNoOfStatisticsOrdersByFIPK(long FIPK) throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH,"Select Count(*) as No From Statistics_Orders Where FIPK = %d ",FIPK);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();

        return result.getInt(result.getColumnIndex("No"));
    }

    public int getNoOfStatisticsJobs(int orderID) throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No From Statistics_Jobs Where StatOrderID = %d",orderID);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();

        return result.getInt(result.getColumnIndex("No"));
    }

    public int getNoOfStatisticsActiveJobs() throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No From Statistics_Jobs Where Status = '%s' or Status = '%s'", statusStateWorking, statusStateStop);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();

        return result.getInt(result.getColumnIndex("No"));
    }

    public int getNoOfStatisticsJobsAfterReborn(int orderID) throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No From Statistics_Jobs Left Join Statistics_Orders\n" +
                "  On Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID Where Statistics_Jobs.StatOrderID = %d and StartTime >= ReBornDate",orderID);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();

        return result.getInt(result.getColumnIndex("No"));
    }

    public int getOrderIDByIPK(long IPK) throws IOException, GeneralSecurityException
    {
        int orderID =  -1;
        String sqlText = String.format(Locale.ENGLISH, "Select StatOrderID From Statistics_Orders Where IPK = %d",IPK);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            orderID =result.getInt(result.getColumnIndex("StatOrderID"));
        }

        return orderID;
    }

    public String getFirstOrderDateByIPK(Context context, long IPK) throws IOException, GeneralSecurityException
    {
        long EndTime;
        long ReBornDate;

        long resultTime = -1;

        String sqlText = String.format(Locale.ENGLISH, "Select EndTime, reBornDate from Statistics_Jobs Left Join Statistics_Orders \n" +
                "\tOn Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID\n" +
                "\t  Where IPK = %d and Status = 'done' Order By StatJobID asc Limit 1", IPK);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            EndTime = result.getLong(result.getColumnIndex("EndTime"));
            ReBornDate = result.getLong(result.getColumnIndex("reBornDate"));

            if (EndTime > ReBornDate) {resultTime = EndTime;}
            else {resultTime = ReBornDate;}
        }

        return getDateFromTimeStampRevert(context, resultTime);
    }

    public static String upgradeKey = "WebAPIUpgrade";
    public void upgradeToNewAPIEngine() throws IOException, GeneralSecurityException, InterruptedException
    {
        /*if (checkIfItIsOldVersion())
        {

            String sqlText = "Select StatJobID From Statistics_Jobs Where Status != 'done'";

            MatrixCursor cursor = dbMetagram.selectQuery(sqlText);

            if (cursor.moveToFirst())
            {
                while (!cursor.isAfterLast())
                {

                    int JobID = cursor.getInt(cursor.getColumnIndex("StatJobID"));

                    deleteStatisticsJobInfoFromDB(JobID);

                    cursor.moveToNext();
                }
            }

            dbMetagram.setPair(upgradeKey, "upgraded");

            dbMetagram.delPair("metagramAgentSetting");

            sqlText = "Delete From Accounts";
            dbMetagram.execQuery(sqlText);

        }*/
    }

    public void deleteStatisticsJobInfoFromDB(int JobID) throws IOException
    {
        String sqlText;

        // Delete Followers
        sqlText = String.format(Locale.ENGLISH, "Delete From Rel_Follower Where StatJobID = %d", JobID);
        dbMetagram.execQuery(sqlText);

        // Delete Followings
        sqlText = String.format(Locale.ENGLISH, "Delete From Rel_Following Where StatJobID = %d", JobID);
        dbMetagram.execQuery(sqlText);

        // Delete Likes
        sqlText = String.format(Locale.ENGLISH, "Delete From Rel_Like Where StatJobID = %d", JobID);
        dbMetagram.execQuery(sqlText);

        // Delete Comments
        sqlText = String.format(Locale.ENGLISH, "Delete From Rel_Comment Where StatJobID = %d", JobID);
        dbMetagram.execQuery(sqlText);

        // Delete PostInfo
        sqlText = String.format(Locale.ENGLISH, "Delete From Posts_Info Where StatJobID = %d", JobID);
        dbMetagram.execQuery(sqlText);

        // Delete PostInfo
        sqlText = String.format(Locale.ENGLISH, "Delete From Posts Where StatJobID = %d", JobID);
        dbMetagram.execQuery(sqlText);

        // Delete AccountInfo
        sqlText = String.format(Locale.ENGLISH, "Delete From Account_Info Where StatisticsJobID = %d", JobID);
        dbMetagram.execQuery(sqlText);
    }

    public boolean checkIfItIsOldVersion() throws IOException, GeneralSecurityException
    {
        boolean result = false;

        String sqlText = "Select Content from Accounts";

        MatrixCursor cursor = dbMetagram.selectQuery(sqlText);

        if (cursor.moveToFirst())
        {

            try
            {
                String password = "";
                String content = cursor.getString(cursor.getColumnIndex("Content"));
                content = dbMetagram.AESCipher.decryptFromHexToString(content);

                JSONObject accountContent = new JSONObject(content);

                password = accountContent.getString("password");

                if (password != null && !password.equals(""))
                {
                    result = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void createLogInUserOrder()
    {
        try
        {
            InstagramAgent agent = metagramAgent.activeAgent;

            int noOfOrders  = metagramAgent.getNoOfStatisticsOrdersByFIPK(agent.userID);
            if(noOfOrders > 0) {return;}

            UserFull userInfo = agent.proxy.getUserInfo(agent.username);

            boolean fParameter = !(userInfo.followingCount > deviceSettings.statisticsMaxFollowingValue ||
                    userInfo.followerCount > deviceSettings.statisticsMaxFollowersValue ||
                    (userInfo.followingCount + userInfo.followerCount) > deviceSettings.statisticsMaxCombinedValue) ;

            boolean dParameter = userInfo.postsCount <= deviceSettings.statisticsMaxPostsValue;
            boolean pParameter = userInfo.postsCount <= deviceSettings.statisticsMaxPostsValueLimit && fParameter && dParameter;

            int f_parameter = 0; if (fParameter) {f_parameter = 1;}
            int p_parameter = 0; if (pParameter) {p_parameter = 1;}
            int d_parameter = 0; if (dParameter) {d_parameter = 1;}


            metagramAgent.addStatisticsOrder(agent.userID, agent.userID, userInfo.username, userInfo.getAsJSON(true),
                    f_parameter, p_parameter, d_parameter, 24 * 3600, 1);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void saveStats()
    {
        activeAgent.agentStatus.save(activeAgent.userID);
    }

}
