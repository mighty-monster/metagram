package vp.metagram.utils.instagram.executors.statistics.execFlow;

import android.database.MatrixCursor;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


import vp.metagram.utils.instagram.types.ResponseStatus;

import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.logger;


public class StatsAccountExecFlow
{
    public String deal = "trying";

    public boolean fetchAccountInfo = false;
    public boolean gotFollowers = false;
    public boolean gotFollowings = false;
    public boolean gotPostsList = false;
    public boolean calculateRank = false;

    public int followersCounter = 0;
    public int followingCounter = 0;
    public int postsCounter = 0;
    public int postsInfoCounter = 0;

    public String followersNextHash;
    public String followingsNextHash;
    public String postsNextHash;

    public int followersNo = 0;
    public int followingNo = 0;
    public int postsNo = 0;

    public boolean finalizationQueriesExecuted = false;

    public List<StatsPostExecFlow> postsStatsExecFlow = new ArrayList<>();

    public int postsOrderID = 0;

    public ResponseStatus lastResponseStatus = ResponseStatus.ok;

    public long noOfHttpCallsToInstagram = 0;

    public boolean isFinished( boolean F_Parameter, boolean P_Parameter, boolean D_Parameter )
    {
        boolean result = true;

        if (!fetchAccountInfo || (!gotFollowers && F_Parameter) || (!gotFollowings && F_Parameter) ||
                (!gotPostsList && P_Parameter) || !calculateRank) { result = false;}

        if (result && D_Parameter)
        {
            result = isPostsDetailsCollected();
        }

        return result;
    }

    public void addNewPostExecFlow(StatsPostExecFlow newPostExecFlow)
    {
        int size = postsStatsExecFlow.size();

        boolean found = false;
        for(int i=0; i < size; i++)
        {
            if (postsStatsExecFlow.get(i).MPK == newPostExecFlow.MPK)
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            postsStatsExecFlow.add(newPostExecFlow);
        }
    }

    public boolean isPostsDetailsCollected()
    {
        boolean result = true;

        if (!gotPostsList)
        {
            result = false;
        }
        else
        {

            Iterator<StatsPostExecFlow> iterator = postsStatsExecFlow.iterator();

            while (iterator.hasNext())
            {
                StatsPostExecFlow execFlow = iterator.next();

                if (!execFlow.fetchCommenter || (!execFlow.fetchLikers))
                {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    public void save(int StatJobID) throws GeneralSecurityException, IOException
    {

        Gson gson = new Gson();
        String json = gson.toJson(this);

        json = dbMetagram.AESCipher.encryptStringToHex(json);

        String sqlText = String.format(Locale.ENGLISH, "Update Statistics_Jobs Set JobDescriptor = '%s' Where StatJobID = %d",json,StatJobID);
        dbMetagram.execQuery(sqlText);
    }

    static public StatsAccountExecFlow load(int StatJobID) throws GeneralSecurityException, IOException
    {

        StatsAccountExecFlow result;

        String json = "";
        String sqlText = String.format(Locale.ENGLISH, "Select JobDescriptor From Statistics_Jobs Where StatJobID = %d", StatJobID);
        MatrixCursor qResult = dbMetagram.selectQuery(sqlText);

        if (qResult.moveToFirst())
        {
            json = qResult.getString(qResult.getColumnIndex("JobDescriptor"));

            if (!json.equals(""))
            {
                try
                {
                    json = dbMetagram.AESCipher.decryptFromHexToString(json);
                }
                catch (GeneralSecurityException e)
                {
                    e.printStackTrace();
                    //json = "";
                }

            }
        }

        if (!json.equals(""))
        {

            try
            {
                Gson gson = new Gson();
                result = gson.fromJson(json, StatsAccountExecFlow.class);
            }
            catch (Exception e)
            {
                logger.logError("StatsAccountExecFlow.Load()",
                        "Load setting from db failed.\n", e);
                result = new StatsAccountExecFlow();
            }
        }
        else
        {
            result = new StatsAccountExecFlow();
        }

        return result;
    }
}

