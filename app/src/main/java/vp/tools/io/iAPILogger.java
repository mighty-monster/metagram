package vp.tools.io;


import android.content.Context;
import android.util.Log;

import java.util.Locale;





public class iAPILogger
{
    public iDBManager logDatabase;
    int logDBVersion = 1;
    String logDBName;
    String[] logDBBuildQueries = new String[1];
    Context context;

    public iAPILogger(Context _Context, String _DBName)
    {
        logDBName = _DBName;
        context = _Context;

        logDBBuildQueries[0] = " Create Table if not Exists APILogs ( "+
                               " ID Integer Primary Key AutoIncrement, "+
                               " RequestType Text, "+
                               " ErrorCode Text, "+
                               " Message Text, "+
                               " Sent Integer Default 0,"+
                               " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP )";

        logDatabase = new iDBManager(_Context,_DBName,logDBVersion,logDBBuildQueries);
    }


    public void log(String requestType, Integer errorCode, String message)
    {

        try
        {
            String sqlText = String.format(Locale.ENGLISH, "Insert Into APILogs(RequestType, ErrorCode, Message) Values ('%s','%s','%s')", requestType,Integer.toString(errorCode),message);

            logDatabase.execQuery(sqlText);

        }
        catch (Exception e)
        {
            Log.e("Logger Internal Error",e.getStackTrace().toString());
        }
        Log.e("API Error",String.format(Locale.ENGLISH,"Error Code: %d\nRequest Type: %s\nMessage: %s",errorCode,requestType,message));
    }
}
