package vp.metagram.utils.instagram.executors.statistics;

import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


import vp.metagram.R;
import vp.metagram.types.enumRank;
import vp.metagram.ui.MainActivity;
import vp.metagram.ui.ReportUI.reProgressFragment;
import vp.metagram.ui.ReportUI.reportViewer.ReportSelectorActivity;
import vp.metagram.utils.MemoryLogString;
import vp.metagram.utils.StatObservatory;
import vp.metagram.utils.instagram.InstagramAgent;
import vp.metagram.utils.instagram.InstagramAgentStatus;

import vp.metagram.utils.instagram.MetagramAgent;
import vp.metagram.utils.instagram.executors.statistics.execFlow.StatsAccountExecFlow;
import vp.metagram.utils.instagram.executors.statistics.execFlow.StatsPostExecFlow;
import vp.metagram.utils.instagram.executors.statistics.types.RankCalculator;
import vp.metagram.utils.instagram.executors.statistics.types.StatisticsJob;
import vp.metagram.utils.instagram.executors.statistics.types.StatisticsOrder;
import vp.metagram.utils.instagram.types.Comment;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.ResponseStatus;
import vp.metagram.utils.instagram.types.User;
import vp.metagram.utils.instagram.types.UserFull;
import vp.igpapi.IGWAException;

import static vp.metagram.general.functions.convertRank;
import static vp.metagram.general.functions.createNotification;
import static vp.metagram.general.functions.getRandomDelay;
import static vp.metagram.general.functions.isConnectedWifi;
import static vp.metagram.general.functions.repeatSingleQuotes;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.idleStatus;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.updatedStatus;

@SuppressWarnings("WeakerAccess")

public class StatisticsExecutor
{

    public int StatisticsJobID;
    public int StatisticsOrderID;

    Thread workerThread;
    public String threadName;
    public long threadID;

    MetagramAgent mAgent;
    List<String> listOfWorkerAccounts;
    int listOfAccountsIndex = 0;
    public long IPK;
    public long FIPK;
    public String username;

    public boolean F_Parameter;
    public boolean P_Parameter;
    public boolean D_Parameter;

    long startTime;

    public String status;
    volatile boolean halt = false;

    public StatsAccountExecFlow statsExecFlow;

    UserFull accountInfo;
    Map<Long, User> followers = new LinkedHashMap<>(10, (float) 0.75, true);
    Map<Long, User> followings = new LinkedHashMap<>(10, (float) 0.75, true);
    Map<Long, PostMedia> posts = new LinkedHashMap<>(10, (float) 0.75, true);

    static final public String statusStateReady = "ready";
    static final public String statusStateWorking = "working";
    static final public String statusStateStop = "stop";
    static final public String statusStateDone = "done";


    int mainThreadInterval = 5 * 1000; //5 second

    public static int engagementPostsNumber = 20;

    StatObservatory observatory;

    int percentCallCounter = 0;

    public String currentProcess;

    public MemoryLogString memoryLog = new MemoryLogString();

    ResponseStatus lastResponseStatus = ResponseStatus.ok;

    public Boolean advanced_logging = false;

    public StatisticsExecutor(MetagramAgent mAgent, int StatisticsJobID, String status, StatObservatory observatory)
    {
        this.mAgent = mAgent;
        this.StatisticsJobID = StatisticsJobID;
        this.status = status;
        this.observatory = observatory;

        startTime = System.currentTimeMillis();
        threadName = String.format(Locale.ENGLISH, "StatisticsExecutor_%d_%d", StatisticsJobID, startTime);

        workerThread = new Thread(() ->
        {
            memoryLog.append("Starting ...");

            mAgent.StartStatisticsService();

            PowerManager.WakeLock wakeLock = null;
            WifiManager.WifiLock wifiLock = null;
            try
            {
                if (deviceSettings.wakeLock)
                {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, threadName);
                    wakeLock.acquire(60 * 60 * 1000L);

                    memoryLog.append("CPU WakeLock activated");

                    if (isConnectedWifi(appContext))
                    {
                        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, threadName);
                        wifiLock.acquire();

                        memoryLog.append("WiFi WakeLock activated");
                    }
                }

                threadID = Thread.currentThread().getId();

                checkPoolStatus();

                initialization();

                execute();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {

                    mAgent.StopStatisticsService();

                    if (wifiLock != null)
                    {
                        wifiLock.release();
                    }

                    if (wakeLock != null)
                    {
                        wakeLock.release();
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                unregisterFromPool();

            }
        }, threadName);


    }

    public void startThread()
    {
        workerThread.start();

        memoryLog.append(String.format(Locale.ENGLISH, "Execution thread created with id: %d", workerThread.getId()));
    }


    public void initialization() throws Exception
    {

        memoryLog.append("Initialization started");

        String sqlText = String.format(Locale.ENGLISH, "Select IPK, F_Parameter, P_Parameter, D_Parameter, " +
                " FIPK,ExecAgents,JobDescriptor, StartTime, Username, Statistics_Jobs.StatOrderID as StatOrderID, Status from Statistics_Jobs left Join Statistics_Orders\n" +
                "  On Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID\n" +
                "    Where StatJobID = %d", StatisticsJobID);
        MatrixCursor jobInfoCursor = dbMetagram.selectQuery(sqlText);

        boolean firstRun;

        if (jobInfoCursor.moveToFirst())
        {
            F_Parameter = jobInfoCursor.getInt(jobInfoCursor.getColumnIndex("F_Parameter")) == 1;
            P_Parameter = jobInfoCursor.getInt(jobInfoCursor.getColumnIndex("P_Parameter")) == 1;
            D_Parameter = jobInfoCursor.getInt(jobInfoCursor.getColumnIndex("D_Parameter")) == 1;

            IPK = jobInfoCursor.getLong(jobInfoCursor.getColumnIndex("IPK"));
            FIPK = jobInfoCursor.getLong(jobInfoCursor.getColumnIndex("FIPK"));
            username = jobInfoCursor.getString(jobInfoCursor.getColumnIndex("Username"));
            StatisticsOrderID = jobInfoCursor.getInt(jobInfoCursor.getColumnIndex("StatOrderID"));
            status = jobInfoCursor.getString(jobInfoCursor.getColumnIndex("Status"));

            String listOfWorkers = jobInfoCursor.getString(jobInfoCursor.getColumnIndex("ExecAgents"));

            listOfWorkerAccounts = new ArrayList<String>(Arrays.asList(listOfWorkers.split("\\s*,\\s*")));

            firstRun = jobInfoCursor.getInt(jobInfoCursor.getColumnIndex("StartTime")) == -1;

        }
        else
        {
            throw new Exception("Can not find the statistics job by id, help me :(");
        }

        if (firstRun)
        {
            memoryLog.append(String.format(Locale.ENGLISH, "Order ID: %d", StatisticsOrderID));
            memoryLog.append(String.format(Locale.ENGLISH, "Job ID: %d", StatisticsJobID));
            memoryLog.append("No record in the database");
            memoryLog.append("This is a new report");

            sqlText = String.format(Locale.ENGLISH, " Update Statistics_Jobs Set ThreadName = '%s', ThreadID = %d, StartTime = %d, ReStartTime = %d Where StatJobID = %d ",
                    threadName, threadID, startTime, startTime, StatisticsJobID);
        }
        else
        {
            memoryLog.append(String.format(Locale.ENGLISH, "Order ID: %d", StatisticsOrderID));
            memoryLog.append(String.format(Locale.ENGLISH, "Job ID: %d", StatisticsJobID));
            memoryLog.append("This is a paused or interrupted report");
            memoryLog.append("Found execution flow in the database");
            memoryLog.append("Report`s execution flow loaded to memory");

            sqlText = String.format(Locale.ENGLISH, " Update Statistics_Jobs Set ThreadName = '%s', ThreadID = %d, ReStartTime = %d Where StatJobID = %d ",
                    threadName, threadID, startTime, StatisticsJobID);
        }
        
        dbMetagram.execQuery(sqlText);

        statsExecFlow = StatsAccountExecFlow.load(StatisticsJobID);
        statsExecFlow.save(StatisticsJobID);

        memoryLog.append("Initialization completed");
    }


    public void execute() throws IOException, GeneralSecurityException
    {
        while (!statsExecFlow.isFinished(F_Parameter, P_Parameter, D_Parameter) || !isStatusDone())
        {
            if (halt) {return;}

            try
            {
                if (status.equals(statusStateStop) || status.equals(statusStateDone))
                {
                    unregisterFromPool();
                    SystemClock.sleep(mainThreadInterval);
                    continue;
                }

                if (deviceSettings.mustUpgrade)
                {
                    memoryLog.append("Have to upgrade the engine, exit");
                    SystemClock.sleep(mainThreadInterval);
                    continue;
                }

                setResponseStatus(statsExecFlow.lastResponseStatus);


                memoryLog.append("Data collection initiated");

                getAccountInfo();

                getFollowersList();

                getFollowingList();

                getPostsList();

                getPostsDetails();

                calculateResult();

                finalizer();
            }
            catch (Exception ex)
            {
                Throwable e;

                if (ex instanceof UndeclaredThrowableException)
                { e = ((InvocationTargetException) ((UndeclaredThrowableException) ex).getUndeclaredThrowable()).getTargetException(); }
                else
                { e = ex; }

                if (e instanceof IOException)
                    setResponseStatus(ResponseStatus.noInternet);

                if (e instanceof IGWAException)
                {
                    IGWAException igwaException = (IGWAException) e;
                    if (igwaException.code == IGWAException.not_logged_in)
                    {
                        setResponseStatus(ResponseStatus.loginRequired);
                    }
                    if (igwaException.code == IGWAException.not_authorized)
                    {
                        setResponseStatus(ResponseStatus.notAuthorized);
                    }
                    if (igwaException.code == IGWAException.http_to_many_request)
                    {
                        setResponseStatus(ResponseStatus.rateLimit);

                        currentProcess = mAgent.context.getResources().getString(R.string.statisticsExecutor_rateLimit);

                        setCurrentProcess(currentProcess);

                        memoryLog.append("Instagram is angry, we have sent a lot of requests");

                        memoryLog.append(String.format(Locale.ENGLISH, "Delaying for %d Minutes", 36 * mainThreadInterval / 60000));

                        SystemClock.sleep(36 * mainThreadInterval);

                    }

                    if (igwaException.code == IGWAException.media_not_found)
                    {
                        memoryLog.append("Could not find the post, maybe it have been deleted");
                        memoryLog.append("Have to check whether post was deleted, or user unfollowed the page, or the page blocked the user");

                        mediaErrorSanityCheck(igwaException.respond);
                    }

                }
                else
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Error: Will try again in %d seconds", mainThreadInterval / 1000));
                }


                e.printStackTrace();
            }

            SystemClock.sleep(mainThreadInterval);
        }
    }

    public void mediaErrorSanityCheck(String short_code) throws IOException
    {
        try
        {
            UserFull user = mAgent.activeAgent.api.user_info(username, new UserFull());

            if (user.hasBlockedViewer)
            {
                //We are being blocked
                memoryLog.append("The page has blocked us");
                setResponseStatus(ResponseStatus.beingBlocked);
                stop();
            }
            else
            {
                //The post has been removed
                memoryLog.append("The post has been removed");

                deleteRemovedPostFromReport(short_code);
            }
        }
        catch (IGWAException e)
        {
            if (e.code == IGWAException.http_not_found)
            {
                //Username has changed
                //TODO test change of user in real world
                memoryLog.append("The page has changed it`s username");

                String newUsername = getUsernameFromUserFeed();

                if (newUsername != null && !newUsername.equals("") && !newUsername.equals(username))
                {
                    //Update new username
                    updateNewUsername(newUsername);

                }
                else
                {
                    //Inform user the error
                    memoryLog.append("Could not find new username, reporting the error");

                    setResponseStatus(ResponseStatus.changedUsername);
                    stop();
                }

            }
        }
        catch (IOException | JSONException | GeneralSecurityException e)
        {
            e.printStackTrace();
        }

    }

    public String getUsernameFromUserFeed()
    {
        memoryLog.append("Trying to find new username");
        String newUsername = "";
        try
        {
            JSONObject result = mAgent.activeAgent.api.user_feed(IPK, "");

            result = result.getJSONObject("data");
            result = result.getJSONObject("user");
            result = result.getJSONObject("edge_owner_to_timeline_media");
            JSONArray medias = result.getJSONArray("edges");
            result = medias.getJSONObject(0).getJSONObject("node");
            result = result.getJSONObject("owner");

            newUsername = result.getString("natgeo").trim();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return newUsername;

    }

    public void updateNewUsername(String newUsername) throws IOException
    {
        username = newUsername;

        String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Orders Set Username = '%s' Where StatOrderID = %d", newUsername, StatisticsOrderID);
        dbMetagram.execQuery(sqlText);

        memoryLog.append(String.format(Locale.ENGLISH, "New username = `%s`, have been found and report updated", username));
    }

    public void deleteRemovedPostFromReport(String short_code) throws IOException, GeneralSecurityException
    {
        memoryLog.append("Deleting the post from report");

        //Deleting from Database

        memoryLog.append("Deleting the post from database");

        long MPK;
        String sqlText = String.format(Locale.ENGLISH, "Select MPK From Posts Where MiniLink = '%s'", short_code);
        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            MPK = result.getLong(result.getColumnIndex("MPK"));

            memoryLog.append("Deleting the post`s likes from database");
            sqlText = String.format(Locale.ENGLISH, "Delete from Rel_Like Where FMPK = %d", MPK);
            dbMetagram.execQuery(sqlText);

            memoryLog.append("Deleting the post`s comments from database");
            sqlText = String.format(Locale.ENGLISH, "Delete from Rel_Comment Where FMPK = %d", MPK);
            dbMetagram.execQuery(sqlText);

            memoryLog.append("Deleting the post`s details from database");
            sqlText = String.format(Locale.ENGLISH, "Delete from Posts_Info Where FMPK = %d", MPK);
            dbMetagram.execQuery(sqlText);

            memoryLog.append("Deleting the post`s record from database");
            sqlText = String.format(Locale.ENGLISH, "Delete from Posts Where MPK = %d", MPK);
            dbMetagram.execQuery(sqlText);


            //Deleting from execution flow
            memoryLog.append("Deleting the post from execution flow");

            Iterator<StatsPostExecFlow> iterator = statsExecFlow.postsStatsExecFlow.iterator();
            while (iterator.hasNext())
            {
                StatsPostExecFlow postExecFlow = iterator.next();
                if (postExecFlow.shortCode.equals(short_code))
                {
                    statsExecFlow.postsStatsExecFlow.remove(postExecFlow);
                    break;
                }
            }

            statsExecFlow.save(StatisticsJobID);
        }
        else
        {
            memoryLog.append("Can not find post id to delete it, this is serious, the report is useless now");

            //Report To User and revert option //TODO handle revert unhandled report error
        }

    }


    public void getAccountInfo() throws  IOException, GeneralSecurityException, JSONException, IGWAException
    {
        if (checkStopStatus()) {return;}

        memoryLog.append("Entering getAccountInfo()");

        if (statsExecFlow.fetchAccountInfo)
        {
            memoryLog.append("Already finished, Skip ...");
            return;
        }

        if (!checkPoolStatus()) {return;}

        setCurrentProcess(mAgent.context.getResources().getString(R.string.statisticsExecutor_fetchingAccountInfo));


        InstagramAgent execAgent = mAgent.activeAgent;


        if (advanced_logging)
        { memoryLog.append(String.format(Locale.ENGLISH, "http call no %,d: gathering account`s basic info (following no, follower no, post no)", ++statsExecFlow.noOfHttpCallsToInstagram)); }

        accountInfo = execAgent.proxy.getUserInfo(username);


        if (mAgent.getIPKByUsername(username) > 0)
        {
            execAgent.GetAccountInfoAndSaveToDB(StatisticsJobID);
        }

        if (advanced_logging)
        {memoryLog.append(String.format("New information for '%s' received and stored in database", username));}

        long time_step1 = System.currentTimeMillis();
        updateUserInfoInOrder(accountInfo.getAsJSON(getUserInfo().getBoolean("isFriend")));
        long time_step2 = System.currentTimeMillis();
        execAgent.agentStatus.calculateAverageUpdateUserInfo(time_step2 - time_step1);

        statsExecFlow.followersNo = accountInfo.followerCount ;
        statsExecFlow.followingNo = accountInfo.followingCount ;
        statsExecFlow.postsNo = accountInfo.postsCount;
        statsExecFlow.fetchAccountInfo = true;
        statsExecFlow.save(StatisticsJobID);

        if (advanced_logging)
        {memoryLog.append(String.format("New information for '%s' received and stored in execution flow", username));}

        setResponseStatus(ResponseStatus.ok);

        int randomDelay = getRandomDelay();
        if (advanced_logging)
        {memoryLog.append(String.format(Locale.ENGLISH, "Sleeping for random delay: %,d milliseconds", randomDelay));}

        SystemClock.sleep(randomDelay);

        memoryLog.append("Exiting getAccountInfo()");

        mAgent.saveStats();
    }


    public void getFollowersList() throws  IOException, GeneralSecurityException, IGWAException, JSONException
    {

        if (!F_Parameter) {return;}

        if (!checkPoolStatus()) {return;}

        if (checkStopStatus()) {return;}

        memoryLog.append("Entering getFollowersList()");

        if (statsExecFlow.gotFollowers)
        {
            memoryLog.append("Already finished, Skip ...");
            return;
        }


        currentProcess = mAgent.context.getResources().getString(R.string.statisticsExecutor_collectingFollowers);

        memoryLog.append("Gathering account`s followers");

        setCurrentProcess(currentProcess);

        InstagramAgent execAgent = mAgent.activeAgent;


        if (statsExecFlow.followersNo > 0)
        {
            memoryLog.append(String.format(Locale.ENGLISH, "Start iteration, no of followers: %,d", statsExecFlow.followersNo));

            do
            {
                if (!checkPoolStatus()) {return;}

                followers.clear();

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "http call, %,d: gathering account`s followers", ++statsExecFlow.noOfHttpCallsToInstagram));
                }
                String nextHash = execAgent.proxy.getFollowerList(IPK, followers, statsExecFlow.followersNextHash);
                setResponseStatus(ResponseStatus.ok);
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Received list of %,d followers", followers.size()));
                }

                int counter = 0;

                for (Map.Entry<Long, User> entry : followers.entrySet())
                {
                    if (checkStopStatus()) {return;}

                    User user = entry.getValue();

                    long time_step1 = System.currentTimeMillis();

                    user.addToFollowersOfByIPK(IPK , StatisticsJobID );

                    long time_step2 = System.currentTimeMillis();
                    execAgent.agentStatus.calculateAverageAddFollowerToDB(time_step2 - time_step1);

                    counter++;
                }

                statsExecFlow.followersNextHash = nextHash;
                statsExecFlow.followersCounter += counter;
                statsExecFlow.save(StatisticsJobID);

                if (advanced_logging)
                { memoryLog.append(String.format(Locale.ENGLISH, "Process: %,d/%,d - %,d followers inserted to database", statsExecFlow.followersCounter, statsExecFlow.followersNo, counter)); }

                setUIPercentage(getGeneralPercentage(), percentCallCounter);

                int randomDelay = getRandomDelay();
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Sleeping for random delay: %,d milliseconds", randomDelay));
                }

                SystemClock.sleep(randomDelay);
            }
            while (statsExecFlow.followersNextHash != null && !statsExecFlow.followersNextHash.equals(""));

            memoryLog.append(String.format(Locale.ENGLISH, "End iteration, no of received followers: %,d", statsExecFlow.followersCounter));
        }

        if (statsExecFlow.followersNextHash == null) {statsExecFlow.followersNextHash = "";}

        statsExecFlow.gotFollowers = true;
        statsExecFlow.save(StatisticsJobID);

        memoryLog.append("Saving statistics on execution flow");

        memoryLog.append("Exiting getFollowersList()");

        mAgent.saveStats();
    }


    public void getFollowingList() throws  IOException, GeneralSecurityException, IGWAException, JSONException
    {
        if (!F_Parameter) {return;}

        if (!checkPoolStatus()) {return;}

        if (checkStopStatus()) {return;}

        memoryLog.append("Entering getFollowingList()");

        if (statsExecFlow.gotFollowings)
        {
            memoryLog.append("Already finished, Skip ...");
            return;
        }


        currentProcess = mAgent.context.getResources().getString(R.string.statisticsExecutor_collectingFollowings);

        memoryLog.append("Gathering account`s followings");

        setCurrentProcess(currentProcess);

        InstagramAgent execAgent = mAgent.activeAgent;

        if (statsExecFlow.followingNo > 0)
        {
            memoryLog.append(String.format(Locale.ENGLISH, "Start iteration, no of followings: %,d", statsExecFlow.followingNo));
            do
            {
                if (!checkPoolStatus()) {return;}

                followings.clear();

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "http call, %,d: gathering account`s followings", ++statsExecFlow.noOfHttpCallsToInstagram));
                }
                String nextHash = execAgent.proxy.getFollowingList(IPK, followings, statsExecFlow.followingsNextHash);
                setResponseStatus(ResponseStatus.ok);
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Received list of %,d followings", followings.size()));
                }

                int counter = 0;

                for (Map.Entry<Long, User> entry : followings.entrySet())
                {
                    if (checkStopStatus()) {return;}

                    User user = entry.getValue();

                    long time_step1 = System.currentTimeMillis();

                    user.addToFollowingsOfByIPK(IPK, StatisticsJobID);

                    long time_step2 = System.currentTimeMillis();
                    execAgent.agentStatus.calculateAverageAddFollowingToDB(time_step2 - time_step1);

                    counter++;
                }

                statsExecFlow.followingsNextHash = nextHash;
                statsExecFlow.followingCounter += counter;
                statsExecFlow.save(StatisticsJobID);

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Process: %,d/%,d - %,d followings inserted to database", statsExecFlow.followingCounter, statsExecFlow.followingNo, counter));
                }


                setUIPercentage(getGeneralPercentage(), percentCallCounter);

                int randomDelay = getRandomDelay();
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Sleeping for random delay: %,d milliseconds", randomDelay));
                }

                SystemClock.sleep(randomDelay);

            }
            while (statsExecFlow.followingsNextHash != null && !statsExecFlow.followingsNextHash.equals(""));

            memoryLog.append(String.format(Locale.ENGLISH, "End iteration, no of received followings: %,d", statsExecFlow.followingCounter));
        }

        if (statsExecFlow.followingsNextHash == null) {statsExecFlow.followingsNextHash = "";}

        statsExecFlow.gotFollowings = true;
        statsExecFlow.save(StatisticsJobID);

        memoryLog.append("Saving statistics on execution flow");

        memoryLog.append("Exiting getFollowingList()");

        mAgent.saveStats();
    }


    public void getPostsList() throws IOException, GeneralSecurityException, IGWAException, JSONException
    {

        if (!checkPoolStatus()) {return;}

        if (checkStopStatus()) {return;}

        memoryLog.append("Entering getPostsList()");

        if (statsExecFlow.gotPostsList)
        {
            memoryLog.append("Already finished, Skip ...");
            return;
        }

        currentProcess = mAgent.context.getResources().getString(R.string.statisticsExecutor_collectingPostsList);
        setCurrentProcess(currentProcess);

        memoryLog.append("Gathering account`s posts");


        InstagramAgent execAgent = mAgent.activeAgent;

        if (statsExecFlow.postsNo > 0)
        {
            memoryLog.append(String.format(Locale.ENGLISH, "Start iteration, no of posts: %,d", statsExecFlow.postsNo));
            do
            {
                posts.clear();

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "http call, %,d: gathering account`s posts", ++statsExecFlow.noOfHttpCallsToInstagram));
                }
                String nextHash = execAgent.proxy.getMediaList(IPK , posts, statsExecFlow.postsNextHash, 0);
                setResponseStatus(ResponseStatus.ok);
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Received list of %,d posts", posts.size()));
                }

                int counter = 0;

                for (Map.Entry<Long, PostMedia> entry : posts.entrySet())
                {

                    if (!checkPoolStatus())
                    {
                        return;
                    }

                    if (checkStopStatus())
                    {
                        return;
                    }


                    PostMedia post = entry.getValue();

                    long time_step1 = System.currentTimeMillis();

                    post.insertMediaToDB(IPK , StatisticsJobID , statsExecFlow.postsOrderID);
                    post.insertMediaInfo(StatisticsJobID);

                    long time_step2 = System.currentTimeMillis();
                    execAgent.agentStatus.calculateAverageAddPostMediaToDB(time_step2 - time_step1);

                    execAgent.agentStatus.calculateAverageCommentNo(post.commentCount);
                    execAgent.agentStatus.calculateAverageLikeNo(post.likeCount);

                    if (D_Parameter)
                    {
                        StatsPostExecFlow newPostExecFlow = new StatsPostExecFlow();
                        newPostExecFlow.MPK = post.MPK;
                        newPostExecFlow.MID = post.ID;
                        newPostExecFlow.likeNo = post.likeCount;
                        newPostExecFlow.commentNo = post.commentCount;
                        newPostExecFlow.postType = post.type;
                        newPostExecFlow.shortCode = post.miniLink;


                        statsExecFlow.addNewPostExecFlow(newPostExecFlow);
                    }

                    counter++;
                }


                statsExecFlow.postsNextHash = nextHash;
                statsExecFlow.postsCounter += counter;
                statsExecFlow.save(StatisticsJobID);

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Process: %,d/%,d - %,d posts inserted to database", statsExecFlow.postsCounter, statsExecFlow.postsNo, counter));
                }

                if (!P_Parameter && statsExecFlow.postsCounter >= engagementPostsNumber)
                {
                    break;
                }

                setUIPercentage(getGeneralPercentage(), percentCallCounter);

                int randomDelay = getRandomDelay();
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Sleeping for random delay: %,d milliseconds", randomDelay));
                }

                SystemClock.sleep(randomDelay);
            }
            while (statsExecFlow.postsNextHash != null && !statsExecFlow.postsNextHash.equals(""));

            memoryLog.append(String.format(Locale.ENGLISH, "End iteration, no of received posts: %,d", statsExecFlow.postsCounter));
        }

        if (statsExecFlow.postsNextHash == null) {statsExecFlow.postsNextHash = "";}

        statsExecFlow.gotPostsList = true;
        statsExecFlow.save(StatisticsJobID);

        memoryLog.append("Saving statistics on execution flow");

        memoryLog.append("Exiting getPostsList()");

        mAgent.saveStats();
    }


    public void getPostsDetails() throws GeneralSecurityException, IOException, IGWAException, JSONException
    {
        if (!P_Parameter || !D_Parameter) {return;}

        if (checkStopStatus()) {return;}

        if (!checkPoolStatus()) {return;}

        memoryLog.append("Entering getPostsDetails()");

        currentProcess = mAgent.context.getResources().getString(R.string.statisticsExecutor_collectingPostsDetails);

        setCurrentProcess(currentProcess);

        memoryLog.append(String.format(Locale.ENGLISH, "Start iteration, no of posts: %,d", statsExecFlow.postsCounter));
        statsExecFlow.postsInfoCounter = 0;
        for (int i = 0; i < statsExecFlow.postsStatsExecFlow.size(); i++)
        {
            if (checkStopStatus()) {return;}

            if (advanced_logging)
            {
                memoryLog.append(String.format(Locale.ENGLISH, "Post no: %,d", i));
            }
            if (advanced_logging)
            {
                memoryLog.append(String.format(Locale.ENGLISH, "Short Code: %s", statsExecFlow.postsStatsExecFlow.get(i).shortCode));
            }
            if (advanced_logging)
            {
                memoryLog.append(String.format(Locale.ENGLISH, "No of likes: %,d", statsExecFlow.postsStatsExecFlow.get(i).likeNo));
            }
            if (advanced_logging)
            {
                memoryLog.append(String.format(Locale.ENGLISH, "No of Comments: %,d", statsExecFlow.postsStatsExecFlow.get(i).commentNo));
            }

            getPostsLikers(i);

            getPostsComments(i);

            statsExecFlow.postsInfoCounter++;
        }
        memoryLog.append(String.format(Locale.ENGLISH, "End iteration, no of posts: %,d", statsExecFlow.postsCounter));

        memoryLog.append("Exiting getPostsDetails()");
    }


    public void getPostsLikers(int index) throws IOException, GeneralSecurityException, IGWAException, JSONException
    {
        if (!D_Parameter) {return;}

        if (!checkPoolStatus()) {return;}

        if (checkStopStatus()) {return;}

        memoryLog.append("Entering getPostsLikers()");

        if (statsExecFlow.postsStatsExecFlow.get(index).fetchLikers)
        {
            memoryLog.append("Already finished, Skip ...");
            return;
        }

        memoryLog.append(String.format(Locale.ENGLISH, "Gathering post(%,d/%,d)`s likes", index, statsExecFlow.postsNo));

        if (statsExecFlow.postsStatsExecFlow.get(index).likeNo > 0)
        {
            memoryLog.append(String.format(Locale.ENGLISH, "Start iteration, no of likes: %,d", statsExecFlow.postsStatsExecFlow.get(index).likeNo));

            String short_code = statsExecFlow.postsStatsExecFlow.get(index).shortCode;
            long MPK = statsExecFlow.postsStatsExecFlow.get(index).MPK ;

            InstagramAgent execAgent = mAgent.activeAgent;

            Map<Long, User> likers = new LinkedHashMap<>(10, (float) 0.75, true);

            do
            {
                if (!checkPoolStatus()) {return;}

                likers.clear();

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "http call, %,d: gathering post(%,d/%,d)`s likes", ++statsExecFlow.noOfHttpCallsToInstagram, index, statsExecFlow.postsNo));
                }
                String nextHash = execAgent.proxy.getLikeList(short_code, likers, statsExecFlow.postsStatsExecFlow.get(index).nextHashLiker);
                setResponseStatus(ResponseStatus.ok);
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Received list of %,d likes", likers.size()));
                }

                int counter = 0;
                for (Map.Entry<Long, User> entry : likers.entrySet())
                {

                    User user = entry.getValue();

                    long time_step1 = System.currentTimeMillis();

                    user.addToLikersOfByMPK(MPK, StatisticsJobID);

                    long time_step2 = System.currentTimeMillis();
                    execAgent.agentStatus.calculateAverageAddLikeToDB(time_step2 - time_step1);

                    counter++;
                }

                statsExecFlow.postsStatsExecFlow.get(index).nextHashLiker = nextHash;
                statsExecFlow.postsStatsExecFlow.get(index).likeCounter += counter;
                statsExecFlow.save(StatisticsJobID);

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "%,d Likes added to DB", likers.size()));
                    memoryLog.append(String.format(Locale.ENGLISH, "Process: %,d/%,d - %,d post(%,d/%,d)`s likes inserted to database",
                            statsExecFlow.postsStatsExecFlow.get(index).likeCounter,
                            statsExecFlow.postsStatsExecFlow.get(index).likeNo,
                            counter,
                            index,
                            statsExecFlow.postsNo));
                }


                int randomDelay = getRandomDelay();
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Sleeping for random delay: %,d milliseconds", randomDelay));
                }

                SystemClock.sleep(randomDelay);
            }
            while (statsExecFlow.postsStatsExecFlow.get(index).nextHashLiker != null && !statsExecFlow.postsStatsExecFlow.get(index).nextHashLiker.equals(""));

            memoryLog.append(String.format(Locale.ENGLISH, "End iteration, no of received post(%,d/%,d)`s likes: %,d", index, statsExecFlow.postsNo, statsExecFlow.postsStatsExecFlow.get(index).likeCounter));

            if (statsExecFlow.postsStatsExecFlow.get(index).nextHashLiker == null)
            {statsExecFlow.postsStatsExecFlow.get(index).nextHashLiker = "";}

        }

        setUIPercentage(getGeneralPercentage(), percentCallCounter);

        statsExecFlow.postsStatsExecFlow.get(index).fetchLikers = true;
        statsExecFlow.save(StatisticsJobID);

        memoryLog.append("Saving statistics on execution flow");

        memoryLog.append("Exiting getPostsLikers()");

        mAgent.saveStats();

    }


    public void getPostsComments(int index) throws  IOException, GeneralSecurityException, IGWAException, JSONException
    {
        if (!D_Parameter) {return;}

        if (!checkPoolStatus()) {return;}

        if (checkStopStatus()) {return;}

        memoryLog.append("Entering getPostsComments()");

        if (statsExecFlow.postsStatsExecFlow.get(index).fetchCommenter)
        {
            memoryLog.append("Already finished, Skip ...");
            return;
        }


        memoryLog.append(String.format(Locale.ENGLISH, "Gathering post(%,d/%,d)`s comments", index, statsExecFlow.postsNo));

        if (statsExecFlow.postsStatsExecFlow.get(index).commentNo > 0)
        {
            memoryLog.append(String.format(Locale.ENGLISH, "Start iteration, no of comments: %,d", statsExecFlow.postsStatsExecFlow.get(index).commentNo));

            String short_code = statsExecFlow.postsStatsExecFlow.get(index).shortCode;
            long MPK = statsExecFlow.postsStatsExecFlow.get(index).MPK;

            InstagramAgent execAgent = mAgent.activeAgent;

            Map<Long, Comment> comments = new LinkedHashMap<>(10, (float) 0.75, true);

            do
            {
                if (!checkPoolStatus()) {return;}

                comments.clear();

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "http call, %,d: gathering post(%,d/%,d)`s comments", ++statsExecFlow.noOfHttpCallsToInstagram, index, statsExecFlow.postsNo));
                }
                String nextHash = execAgent.proxy.getCommentList(short_code, comments, statsExecFlow.postsStatsExecFlow.get(index).nextHashCommenter);
                setResponseStatus(ResponseStatus.ok);

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Received list of %,d comments", comments.size()));
                }

                int counter = 0;
                for (Map.Entry<Long, Comment> entry : comments.entrySet())
                {


                    Comment comment = entry.getValue();

                    long time_step1 = System.currentTimeMillis();

                    comment.InsertCommentIntoDB(StatisticsJobID , MPK);

                    long time_step2 = System.currentTimeMillis();
                    execAgent.agentStatus.calculateAverageAddCommentToDB(time_step2 - time_step1);

                    counter++;
                }


                statsExecFlow.postsStatsExecFlow.get(index).nextHashCommenter = nextHash;
                statsExecFlow.postsStatsExecFlow.get(index).commentCounter += counter;
                statsExecFlow.save(StatisticsJobID);

                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "%,d Comments added to DB", comments.size()));
                    memoryLog.append(String.format(Locale.ENGLISH, "Process: %,d/%,d - %,d post(%,d/%,d)`s comments inserted to database",
                            statsExecFlow.postsStatsExecFlow.get(index).commentCounter,
                            statsExecFlow.postsStatsExecFlow.get(index).commentNo,
                            counter,
                            index,
                            statsExecFlow.postsNo));
                }

                int randomDelay = getRandomDelay();
                if (advanced_logging)
                {
                    memoryLog.append(String.format(Locale.ENGLISH, "Sleeping for random delay: %,d milliseconds", randomDelay));
                }

                SystemClock.sleep(randomDelay);
            }
            while (statsExecFlow.postsStatsExecFlow.get(index).nextHashCommenter != null && !statsExecFlow.postsStatsExecFlow.get(index).nextHashCommenter.equals(""));

            memoryLog.append(String.format(Locale.ENGLISH, "End iteration, no of received post(%,d/%,d)`s comments: %,d", index, statsExecFlow.postsNo, statsExecFlow.postsStatsExecFlow.get(index).commentCounter));


            if (statsExecFlow.postsStatsExecFlow.get(index).nextHashCommenter == null)
            {statsExecFlow.postsStatsExecFlow.get(index).nextHashCommenter = "";}
        }

        setUIPercentage(getGeneralPercentage(), percentCallCounter);

        statsExecFlow.postsStatsExecFlow.get(index).fetchCommenter = true;
        statsExecFlow.save(StatisticsJobID);


        memoryLog.append("Saving statistics on execution flow");

        memoryLog.append("Exiting getPostsComments()");

        mAgent.saveStats();
    }


    public void finalizer() throws IOException, GeneralSecurityException
    {
        if (!checkPoolStatus()) {return;}

        if (checkStopStatus()) {return;}

        memoryLog.append("Entering finalizer()");

        currentProcess = mAgent.context.getResources().getString(R.string.statisticsExecutor_finalizing);
        setCurrentProcess(currentProcess);

        InstagramAgent execAgent = mAgent.activeAgent;


        if (!statsExecFlow.finalizationQueriesExecuted)
        {
            memoryLog.append("We need to perform some calculation on our database`s data");

            int lastID = StatisticsJobID;

            String MPKListQuery = String.format(Locale.ENGLISH, "Select MPK from Posts Left Join Posts_Info\n" +
                    "    On Posts_Info.FMPK = Posts.MPK and Posts_Info.StatJobID = %d" +
                    "    where Posts.FIPK = %d and\n" +
                    "    Posts.StatJobID = Posts_Info.StatJobID ", lastID, IPK);

            long time_step1 = System.currentTimeMillis();

            String sqlText = String.format(Locale.ENGLISH, " Update Rel_Follower Set StatJobID = %d, Status = %d where Status = %d and FIPK = %d", StatisticsJobID, idleStatus, updatedStatus, IPK);
            dbMetagram.execQuery(sqlText);

            sqlText = String.format(Locale.ENGLISH, " Update Rel_Following Set StatJobID = %d, Status = %d where Status = %d and FIPK = %d", StatisticsJobID, idleStatus, updatedStatus, IPK);
            dbMetagram.execQuery(sqlText);

            sqlText = String.format(Locale.ENGLISH, " Update Posts Set StatJobID = %d, Status = %d where Status = %d and FIPK = %d", StatisticsJobID, idleStatus, updatedStatus, IPK);
            dbMetagram.execQuery(sqlText);

            sqlText = String.format(Locale.ENGLISH, " Update Rel_Like Set StatJobID = %d, Status = %d where Status = %d and FMPK in ( %s )", StatisticsJobID, idleStatus, updatedStatus, MPKListQuery);
            dbMetagram.execQuery(sqlText);

            sqlText = String.format(Locale.ENGLISH, " Update Rel_Comment Set StatJobID = %d, Status = %d where Status = %d and FMPK in ( %s )", StatisticsJobID, idleStatus, updatedStatus, MPKListQuery);
            dbMetagram.execQuery(sqlText);

            long time_step2 = System.currentTimeMillis();
            execAgent.agentStatus.calculateAverageFinalization(time_step2 - time_step1);

            statsExecFlow.finalizationQueriesExecuted = true;
            statsExecFlow.save(StatisticsJobID);

            memoryLog.append("Finalization queries executed on database");
        }

        calculateEngagements();

        memoryLog.append("Calculating account engagement and rank");

        setUIPercentage(getGeneralPercentage(), percentCallCounter);

        memoryLog.append("Exiting finalizer()");
        memoryLog.append("Finished");

        if (statsExecFlow.isFinished(F_Parameter, P_Parameter, D_Parameter))
        {
            long endTime = System.currentTimeMillis();
            String sqlText = String.format(Locale.ENGLISH, " Update Statistics_Jobs Set Status = '%s', EndTime = %d Where StatJobID = %d ",
                    statusStateDone, endTime, StatisticsJobID);
            dbMetagram.execQuery(sqlText);

            status = statusStateDone;


            sendNotification();

            loadResultFragment();

        }
    }


    public void calculateEngagements() throws GeneralSecurityException, IOException
    {
        if (!P_Parameter) {return;}

        MatrixCursor result;

        String sqlText = String.format(Locale.ENGLISH, "Select *  From ( Select  Others.IPK, Count(LikerIPK) as LikeNo  From Rel_Like Left Join Others\n" +
                        "  On LikerIPK = Others.IPK\n" +
                        " Where FMPK in \n" +
                        " ( Select MPK from Posts Left Join Posts_Info\n" +
                        "    On Posts_Info.FMPK = Posts.MPK and Posts_Info.StatJobID = %d \n" +
                        "    where Posts.FIPK = %d and \n" +
                        "    Posts.StatJobID = Posts_Info.StatJobID  ) and Rel_Like.StatJobID = %d Group By LikerIPK order by LikeNo desc )",
                StatisticsJobID, IPK, StatisticsJobID);

        result = dbMetagram.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while (!result.isAfterLast())
            {
                long engageIPK = result.getLong(result.getColumnIndex("IPK"));
                int likeNo = result.getInt(result.getColumnIndex("LikeNo"));
                insertLikersDataToDB(likeNo, engageIPK);
                result.moveToNext();
            }
        }

        sqlText = String.format(Locale.ENGLISH, "Select *  From ( Select  Others.IPK, Count(CommenterIPK) as CommentNo  From Rel_Comment Left Join Others\n" +
                        "  On CommenterIPK = Others.IPK\n" +
                        " Where FMPK in \n" +
                        " ( Select MPK from Posts Left Join Posts_Info\n" +
                        "    On Posts_Info.FMPK = Posts.MPK and Posts_Info.StatJobID = %d \n" +
                        "    where Posts.FIPK = %d and \n" +
                        "    Posts.StatJobID = Posts_Info.StatJobID  ) and Rel_Comment.StatJobID = %d Group By CommenterIPK order by CommentNo desc )",
                StatisticsJobID, IPK, StatisticsJobID);

        result = dbMetagram.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while (!result.isAfterLast())
            {
                long engageIPK = result.getLong(result.getColumnIndex("IPK"));
                int commentNo = result.getInt(result.getColumnIndex("CommentNo"));
                ;
                insertCommenterDataToDB(commentNo, engageIPK);
                result.moveToNext();
            }
        }

    }


    public void insertLikersDataToDB(int likeNo, long engageIPK) throws IOException, GeneralSecurityException
    {
        MatrixCursor result;
        String SQLText = String.format(Locale.ENGLISH, "Select Count(*) As No from Engagement Where FIPK = %d and EngageIPK = %d and StatJobID = %d", IPK, engageIPK, StatisticsJobID);
        result = dbMetagram.selectQuery(SQLText);

        if (result.moveToFirst())
        {
            int no = result.getInt(result.getColumnIndex("No"));
            if (no > 0)
            {
                SQLText = String.format(Locale.ENGLISH, "Update Engagement Set LikeNo = %d," +
                                " ChangeDate =  datetime('now') Where FIPK = %d and EngageIPK = %d and StatJobID = %d",
                        likeNo, IPK, engageIPK, StatisticsJobID);
            }
            else
            {
                SQLText = String.format(Locale.ENGLISH, " Insert Or Ignore into Engagement(FIPK,EngageIPK,LikeNo,StatJobID) " +
                        " Values (%d, %d, %d, %d)", IPK, engageIPK, likeNo, StatisticsJobID);
            }

            dbMetagram.execQuery(SQLText);
        }
    }


    public void insertCommenterDataToDB(int commentNo, long engageIPK) throws IOException, GeneralSecurityException
    {
        MatrixCursor result;
        String SQLText = String.format(Locale.ENGLISH, "Select Count(*) As No from Engagement Where FIPK = %d and EngageIPK = %d and StatJobID = %d", IPK, engageIPK, StatisticsJobID);
        result = dbMetagram.selectQuery(SQLText);

        if (result.moveToFirst())
        {
            int no = result.getInt(result.getColumnIndex("No"));
            if (no > 0)
            {
                SQLText = String.format(Locale.ENGLISH, "Update Engagement Set CommentNo = %d, " +
                                " ChangeDate =  datetime('now') Where FIPK = %d and EngageIPK = %d and StatJobID = %d",
                        commentNo, IPK, engageIPK, StatisticsJobID);
            }
            else
            {
                SQLText = String.format(Locale.ENGLISH, " Insert into Engagement(FIPK,EngageIPK,CommentNo,StatJobID) " +
                        " Values (%d, %d, %d, %d)", IPK, engageIPK, commentNo, StatisticsJobID);
            }

            dbMetagram.execQuery(SQLText);
        }
    }


    public void calculateResult() throws GeneralSecurityException, IOException, JSONException
    {
        long totalLikes = 0;
        long totalComments = 0;
        long totalViews = 0;

        double meanLikes = 0;
        double meanComments = 0;
        double meanViews = 0;

        int noOfFollowers = statsExecFlow.followersNo;
        int noOfFollowings = statsExecFlow.followingNo;
        int noOfPosts = statsExecFlow.postsNo;

        double alpha;
        double totalEngagement = 0;
        double meanEngagement = 0;

        double rankEngagement;

        if (noOfFollowers != 0)
        { alpha = (double) noOfFollowings / (double) noOfFollowers; }
        else
        { alpha = Double.MAX_VALUE;}


        rankEngagement = calculateRankEngagement();

        RankCalculator rankCalculator = new RankCalculator();
        enumRank rank = rankCalculator.calculateRank(noOfFollowers, alpha, noOfPosts, rankEngagement);

        String rankStr = rank.toString();

        JSONObject analyzeResult = new JSONObject();

        if (P_Parameter)
        {
            String sqlText = String.format(Locale.ENGLISH,
                    " Select LikeNo, CommentNo, ViewNo, PostType from Posts Left Join Posts_Info \n" +
                            "  On Posts.MPK = Posts_Info.FMPK\n" +
                            "    Where Posts.FIPK = %d and Posts_Info.StatJobID = %d",
                    IPK, StatisticsJobID);

            MatrixCursor infoCursor = dbMetagram.selectQuery(sqlText);

            //TODO Add Exception here
            if (!infoCursor.moveToFirst())
            {
            }

            int postsCounter = 0;
            int videoCounter = 0;


            while (!infoCursor.isAfterLast())
            {
                totalLikes += infoCursor.getInt(infoCursor.getColumnIndex("LikeNo"));
                totalComments += infoCursor.getInt(infoCursor.getColumnIndex("CommentNo"));
                totalViews += infoCursor.getInt(infoCursor.getColumnIndex("ViewNo"));

                postsCounter++;

                //ToDo add instagram post type as final value
                if (infoCursor.getInt(infoCursor.getColumnIndex("PostType")) == 2)
                {
                    videoCounter++;
                }

                infoCursor.moveToNext();
            }

            if (postsCounter != 0)
            {
                meanLikes = (double) totalLikes / (double) postsCounter;
                meanComments = (double) totalComments / (double) postsCounter;
            }

            if (videoCounter != 0)
            {
                meanViews = (double) totalViews / (double) videoCounter;
            }

            if (noOfFollowers != 0)
            {
                totalEngagement = (double) (totalLikes + totalComments) / (double) noOfFollowers;
            }

            if (postsCounter != 0)
            {
                meanEngagement = totalEngagement / (double) postsCounter;
            }

            analyzeResult.put("rankEngagement", rankEngagement);
            analyzeResult.put("noOfPosts", noOfPosts);
            analyzeResult.put("noOfFollowers", noOfFollowers);
            analyzeResult.put("noOfFollowings", noOfFollowings);
            analyzeResult.put("alpha", alpha);
            analyzeResult.put("totalEngagement", totalEngagement);
            analyzeResult.put("meanEngagement", meanEngagement);
            analyzeResult.put("meanViews", meanViews);
            analyzeResult.put("meanComments", meanComments);
            analyzeResult.put("meanLikes", meanLikes);
            analyzeResult.put("totalLikes", totalLikes);
            analyzeResult.put("totalComments", totalComments);
            analyzeResult.put("totalViews", totalViews);
            analyzeResult.put("rank", rankStr);


        }
        else
        {

            analyzeResult.put("rankEngagement", rankEngagement);
            analyzeResult.put("noOfPosts", noOfPosts);
            analyzeResult.put("noOfFollowers", noOfFollowers);
            analyzeResult.put("noOfFollowings", noOfFollowings);
            analyzeResult.put("alpha", alpha);
            analyzeResult.put("rank", rankStr);

        }

        String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Jobs Set Result = '%s' Where StatJobID = %d",
                dbMetagram.AESCipher.encryptStringToHex(analyzeResult.toString()), StatisticsJobID);

        dbMetagram.execQuery(sqlText);

        statsExecFlow.calculateRank = true;
        statsExecFlow.save(StatisticsJobID);
    }


    public double calculateRankEngagement() throws IOException, GeneralSecurityException
    {
        double result = 0;

        String sqlText = String.format(Locale.ENGLISH,
                " Select LikeNo, CommentNo, ViewNo, PostType from Posts Left Join Posts_Info \n" +
                        "  On Posts.MPK = Posts_Info.FMPK\n" +
                        "    Where Posts.FIPK = %d and Posts_Info.StatJobID = %d " +
                        " Order By Posts.OrderID DESC,Posts.rowid ASC Limit %d",
                IPK , StatisticsJobID, engagementPostsNumber);

        MatrixCursor infoCursor = dbMetagram.selectQuery(sqlText);

        //TODO Add Exception here
        if (!infoCursor.moveToFirst())
        { }
        while (!infoCursor.isAfterLast())
        {
            int postsCounter = 0;

            long totalLikes = 0;
            long totalComments = 0;

            int noOfFollowers = statsExecFlow.followersNo;

            while (!infoCursor.isAfterLast())
            {
                totalLikes += infoCursor.getInt(infoCursor.getColumnIndex("LikeNo"));
                totalComments += infoCursor.getInt(infoCursor.getColumnIndex("CommentNo"));

                postsCounter++;

                infoCursor.moveToNext();
            }

            result = (double) (totalLikes + totalComments) / (double) noOfFollowers;

            result = result / (double) postsCounter;
        }

        return result;
    }

    public double getNoOfTotalNeededCalls()
    {
        InstagramAgentStatus agentStatus = mAgent.activeAgent.agentStatus;

        if (statsExecFlow == null || !statsExecFlow.fetchAccountInfo)
        {return 0;}

        double followerRatio = statsExecFlow.followersNo / agentStatus.averageFollowerNo > 1 ? 1f : statsExecFlow.followersNo / agentStatus.averageFollowerNo;
        followerRatio = followerRatio < 0.2f ? 0.2f : followerRatio;

        int totalFollowers = statsExecFlow.followersNo;
        int totalFollowings = statsExecFlow.followingNo;
        int totalPosts;
        if (P_Parameter)
        {totalPosts = statsExecFlow.postsNo;}
        else
        {totalPosts = engagementPostsNumber;}


        double noOfCalls = 0;

        if (F_Parameter)
        {
            noOfCalls += totalFollowers / agentStatus.averageFollowerListLength;
            noOfCalls += totalFollowings / agentStatus.averageFollowingListLength;
        }

        noOfCalls += totalPosts / agentStatus.averagePostsListLength;

        if (D_Parameter)
        {
            for (StatsPostExecFlow postExecFlow : statsExecFlow.postsStatsExecFlow)
            {
                int total_likes = postExecFlow.likeNo;
                noOfCalls += total_likes / agentStatus.averageLikeListLength + 1;

                int total_comments = postExecFlow.commentNo;
                noOfCalls += total_comments / agentStatus.averageCommentNo + 1;
            }

            int unknownPosts = statsExecFlow.postsNo - statsExecFlow.postsStatsExecFlow.size();

            if (unknownPosts > 0)
            {
                noOfCalls += unknownPosts * agentStatus.averageCommentNo * followerRatio / agentStatus.averageCommentListLength;
                noOfCalls += unknownPosts * agentStatus.averageLikeNo * followerRatio / agentStatus.averageLikeListLength;
            }
        }

        return noOfCalls;
    }

    public double getNoOfRemainingNeededCalls()
    {
        InstagramAgentStatus agentStatus = mAgent.activeAgent.agentStatus;

        if (statsExecFlow == null || !statsExecFlow.fetchAccountInfo)
        {return 0;}

        double followerRatio = statsExecFlow.followersNo / agentStatus.averageFollowerNo > 1 ? 1f : statsExecFlow.followersNo / agentStatus.averageFollowerNo;
        followerRatio = followerRatio < 0.2f ? 0.2f : followerRatio;

        int remainingFollowers = statsExecFlow.followersNo - statsExecFlow.followersCounter;
        int remainingFollowings = statsExecFlow.followingNo - statsExecFlow.followingCounter;
        int remainingPosts;
        if (P_Parameter)
        {remainingPosts = statsExecFlow.postsNo - statsExecFlow.postsCounter;}
        else
        {remainingPosts = engagementPostsNumber - statsExecFlow.postsCounter > 0 ? engagementPostsNumber - statsExecFlow.postsCounter : 0;}


        double noOfCalls = 0;

        if (F_Parameter)
        {
            noOfCalls += remainingFollowers / agentStatus.averageFollowerListLength;
            noOfCalls += remainingFollowings / agentStatus.averageFollowingListLength;

        }

        noOfCalls += remainingPosts / agentStatus.averagePostsListLength;

        if (D_Parameter)
        {
            for (StatsPostExecFlow postExecFlow : statsExecFlow.postsStatsExecFlow)
            {
                if (!postExecFlow.fetchLikers)
                {
                    int remaining_likes = postExecFlow.likeNo - postExecFlow.likeCounter;
                    noOfCalls += remaining_likes / agentStatus.averageLikeListLength + 1;
                }

                if (!postExecFlow.fetchCommenter)
                {
                    int remaining_comments = postExecFlow.commentNo - postExecFlow.commentCounter;
                    noOfCalls += remaining_comments / agentStatus.averageCommentNo + 1;
                }
            }

            int unknownPosts = statsExecFlow.postsNo - statsExecFlow.postsStatsExecFlow.size();

            noOfCalls += unknownPosts * agentStatus.averageCommentNo * followerRatio / agentStatus.averageCommentListLength;
            noOfCalls += unknownPosts * agentStatus.averageLikeNo * followerRatio / agentStatus.averageLikeListLength;

        }

        return noOfCalls;
    }


    public int getGeneralPercentage()
    {
        double percent = 0;

        double totalCalls = getNoOfTotalNeededCalls();
        double remainingCalls = getNoOfRemainingNeededCalls();

        percent = (totalCalls - remainingCalls) / totalCalls * 100;


        if (D_Parameter && !statsExecFlow.gotPostsList)
        {
            double deductionRatio = (0.4 * Math.min((statsExecFlow.postsNo / mAgent.activeAgent.agentStatus.averagePostNo), 1));
            percent -= percent * deductionRatio;
        }


        int finalPercent;

        if (percent < 0) {finalPercent = 0;}
        else if (percent > 99.5) {finalPercent = 100;}
        else if (percent > 100) {finalPercent = 100;}
        else {finalPercent = (int) percent;}

        return finalPercent;
    }


    public void start() throws IOException
    {
        if (status.equals(statusStateDone))
        {return;}

        long restartTime = System.currentTimeMillis();
        String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Jobs Set Status = '%s', ReStartTime = %d Where StatJobID = %d ",
                statusStateWorking, restartTime, StatisticsJobID);
        dbMetagram.execQuery(sqlText);

        status = statusStateWorking;
    }


    public void stop() throws IOException
    {
        if (status == null || status.equals(statusStateDone))
        {return;}

        long stopTime = System.currentTimeMillis();
        String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Jobs Set Status = '%s', StopTime = %d Where StatJobID = %d ",
                statusStateStop, stopTime, StatisticsJobID);
        dbMetagram.execQuery(sqlText);

        status = statusStateStop;
    }


    public void halt() throws IOException
    {
        stop();
        halt = true;
    }


    public boolean isStatusDone() throws IOException, GeneralSecurityException
    {
        boolean result = false;
        String sqlText = String.format(Locale.ENGLISH, "Select Status From Statistics_Jobs Where StatJobID = %d ", StatisticsJobID);
        MatrixCursor queryResult = dbMetagram.selectQuery(sqlText);
        if (queryResult.moveToFirst())
        {
            String status = queryResult.getString(queryResult.getColumnIndex("Status")).trim();
            if (status.equals(statusStateDone))
            {
                result = true;
            }
        }
        return result;
    }


    public void updateUserInfoInOrder(String userInfo) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Orders Set UserInfo = '%s' Where StatOrderID = %d", repeatSingleQuotes(userInfo), StatisticsOrderID);
        dbMetagram.execQuery(sqlText);
    }


    public JSONObject getUserInfo() throws JSONException, IOException, GeneralSecurityException
    {
        JSONObject resultJSON = null;

        String sqlText = String.format(Locale.ENGLISH, "Select UserInfo From Statistics_Orders Where IPK = %d", IPK);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            resultJSON = new JSONObject(result.getString(result.getColumnIndex("UserInfo")));
        }

        return resultJSON;
    }


    public void setUIPercentage(double percent, int callCounter)
    {
        if (callCounter < percentCallCounter) {return;}
        percentCallCounter++;

        List<androidx.fragment.app.Fragment> fragmentList = null;
        try
        {
            fragmentList = observatory.getFragments(StatisticsOrderID);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        for (androidx.fragment.app.Fragment fragment : fragmentList)
        {
            if (fragment.isDetached() || fragment.isHidden() || fragment.isRemoving() || !fragment.isAdded())
            {continue;}
            if (fragment instanceof reProgressFragment)
            {
                reProgressFragment progressFragment = (reProgressFragment) fragment;
                progressFragment.setPercent(percent);
            }
        }
    }


    public void loadResultFragment()
    {
        List<androidx.fragment.app.Fragment> fragmentList = null;
        try
        {
            fragmentList = observatory.getFragments(StatisticsOrderID);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        for (androidx.fragment.app.Fragment fragment : fragmentList)
        {
            if (fragment.isDetached() || fragment.isHidden() || fragment.isRemoving() || !fragment.isAdded())
            {continue;}

            if (fragment instanceof reProgressFragment)
            {
                reProgressFragment progressFragment = (reProgressFragment) fragment;

                progressFragment.loadResultFragment(IPK);
            }
        }
    }


    public void sendNotification()
    {
        try
        {

            int notificationID = new Random().nextInt() & Integer.MAX_VALUE;

            boolean report_enabled = F_Parameter || D_Parameter || P_Parameter;

            if (!report_enabled)
            {
                Intent notificationIntent = new Intent(appContext, MainActivity.class);
                notificationIntent.putExtra("title", "ReportFinished");
                notificationIntent.putExtra("notificationID", notificationID);
                notificationIntent.putExtra("IPK", IPK);

                createNotification(appContext,
                        username,
                        "Report completed",
                        notificationIntent,
                        true,
                        notificationID,
                        false,
                        false);
            }
            else
            {
                StatisticsOrder Order = new StatisticsOrder(StatisticsOrderID);
                StatisticsJob lastJob = StatisticsJob.getLastFinishedJob(StatisticsOrderID);

                Intent notificationIntent = new Intent(appContext, ReportSelectorActivity.class);
                notificationIntent.putExtra("title", "ReportFinished");
                notificationIntent.putExtra("notificationID", notificationID);
                notificationIntent.putExtra("F_Parameter", Order.F_Parameter);
                notificationIntent.putExtra("P_Parameter", Order.P_Parameter);
                notificationIntent.putExtra("D_Parameter", Order.D_Parameter);
                notificationIntent.putExtra("R_Parameter", Order.R_Parameter);
                notificationIntent.putExtra("noOfJobs", metagramAgent.getNoOfStatisticsJobsAfterReborn(StatisticsOrderID));
                notificationIntent.putExtra("OrderID", StatisticsOrderID);
                notificationIntent.putExtra("username", username);
                notificationIntent.putExtra("IPK", IPK);
                notificationIntent.putExtra("rank", convertRank(lastJob.Result.getString("rank")));
                notificationIntent.putExtra("isPrivate", Order.userInfo.getBoolean("isPrivate"));
                notificationIntent.putExtra("noOfFollowers", Order.userInfo.getInt("followerCount"));
                notificationIntent.putExtra("noOfFollowings", Order.userInfo.getInt("followingCount"));
                notificationIntent.putExtra("OrderID", StatisticsOrderID);

                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


                createNotification(appContext,
                        username,
                        "Report completed",
                        notificationIntent,
                        true,
                        notificationID,
                        false,
                        false);
            }


        }
        catch (GeneralSecurityException | JSONException | IOException e)
        {
            e.printStackTrace();
        }
    }


    public void setCurrentProcess(String currentProcess)
    {
        List<androidx.fragment.app.Fragment> fragmentList = null;
        try
        {
            fragmentList = observatory.getFragments(StatisticsOrderID);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        for (androidx.fragment.app.Fragment fragment : fragmentList)
        {
            if (fragment.isDetached() || fragment.isHidden() || fragment.isRemoving() || !fragment.isAdded())
            {continue;}
            if (fragment instanceof reProgressFragment)
            {
                reProgressFragment progressFragment = (reProgressFragment) fragment;
                progressFragment.setCurrentProcess(currentProcess);
            }
        }
    }


    /*public void changeRubiesOnUI( int balance)
    {
        MainActivity mainActivity = MainActivity.getLastInstance();
        if (mainActivity != null)
        {
            new Handler(Looper.getMainLooper()).post(()->
                    mainActivity.changeRubyValue(balance));
        }
    }*/


    public void setResponseStatus(ResponseStatus responseStatus)
    {
        if (responseStatus == lastResponseStatus)
        {return;}

        lastResponseStatus = responseStatus;
        statsExecFlow.lastResponseStatus = responseStatus;


        try
        {
            statsExecFlow.save(StatisticsJobID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        List<androidx.fragment.app.Fragment> fragmentList = null;
        try
        {
            fragmentList = observatory.getFragments(StatisticsOrderID);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        for (androidx.fragment.app.Fragment fragment : fragmentList)
        {
            if (fragment.isDetached() || fragment.isHidden() || fragment.isRemoving() || !fragment.isAdded())
            {continue;}
            if (fragment instanceof reProgressFragment)
            {
                reProgressFragment progressFragment = (reProgressFragment) fragment;
                new Handler(Looper.getMainLooper()).post(() -> progressFragment.setResponseStatus(responseStatus));
            }
        }
    }


    public boolean checkPoolStatus()
    {
        boolean result = false;
        try
        {
            result = mAgent.executorPool.addThread(threadName, threadID, this);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        if (!result)
        {
            memoryLog.append("Waiting in queue for cpu time");
            setCurrentProcess(mAgent.context.getResources().getString(R.string.statisticsExecutor_waitingInQeue));
        }

        return result;
    }


    public void unregisterFromPool()
    {
        mAgent.executorPool.removeThread(threadID);
    }


    public boolean checkStopStatus()
    {
        boolean result = status.equals(statusStateStop);

        if (result) {unregisterFromPool();}

        return result;
    }


}
