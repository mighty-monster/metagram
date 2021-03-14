package vp.metagram.utils.instagram.executors.statistics.types;

import android.database.MatrixCursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateDone;

public class StatisticsJob
{
    public int OrderID;
    public int JobID;

    public long StartTime;
    public long ReStartTime;
    public long EndTime;

    public JSONObject Result;

    static public StatisticsJob getLastFinishedJob(int OrderID) throws IOException, GeneralSecurityException, JSONException
    {
        StatisticsJob statisticsJob = new StatisticsJob();
        statisticsJob.OrderID = OrderID;
        String sqlText = String.format(Locale.ENGLISH, "Select * From Statistics_Jobs Where Status = '%s' and StatOrderID = %d Order By StatJobID Desc Limit 1 ",
                statusStateDone,OrderID);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            statisticsJob.JobID = result.getInt(result.getColumnIndex("StatJobID"));

            statisticsJob.StartTime = result.getLong(result.getColumnIndex("StartTime"));
            statisticsJob.ReStartTime =  result.getLong(result.getColumnIndex("ReStartTime"));
            statisticsJob.EndTime = result.getLong(result.getColumnIndex("EndTime"));

            statisticsJob.Result = new JSONObject( dbMetagram.AESCipher.decryptFromHexToString(result.getString(result.getColumnIndex("Result"))) );

        }
        else
        {
            return null;
        }

        return statisticsJob;
    }

}
