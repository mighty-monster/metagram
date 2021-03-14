package vp.tools.io;


import android.content.Context;
import android.util.Log;

import java.util.Locale;



import static vp.metagram.general.functions.stackTraceToString;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.isReleaseMode;


public class iLogger
{
    public iDBManager logDatabase;
    int logDBVersion = 1;
    String logDBName;
    String[] logDBBuildQueries = new String[1];
    Context context;

    public iLogger(Context _Context, String _DBName)
    {
        logDBName = _DBName;
        context = _Context;

        logDBBuildQueries[0] = " Create Table if not Exists Logs ( "+
                               " ID Integer Primary Key AutoIncrement, "+
                               " Type Text, "+
                               " Tag Text, "+
                               " Message Text, "+
                               " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP )";

        logDatabase = new iDBManager(_Context,_DBName,logDBVersion,logDBBuildQueries);
    }

    public void logWTF(String _tag, String _message)
    {
        logWTF(_tag, _message,null);
    }

    public void logWTF(String _tag, String _message, Exception ex)
    {
        if (ex != null)
        {
            _message += "Message:"+ex.getMessage()+"\n"+
                        "StackTrace:" + stackTraceToString(ex);
        }

        String type = "wtf";
        try
        {
            String sqlText = String.format(Locale.ENGLISH, "Insert Into Logs(Type, Tag, Message) Values ('%s',%s','%s')", type,_tag,_message);

            logDatabase.execQuery(sqlText);
        }
        catch (Exception e)
        {Log.e("Logger Internal Error",e.getStackTrace().toString());}
        Log.wtf(_tag,_message);

        if (!isReleaseMode)
        {
            //Toast.makeText(context,_message,Toast.LENGTH_LONG).show();
        }
    }

    public void logError(String _tag, String _message)
    {
        logError(_tag,_message,null);
    }

    public void logError(String _tag, String _message, Exception ex)
    {
        if (deviceSettings!=null && !deviceSettings.logging) {return;}

        if (ex != null)
        {
            _message += "Message:"+ex.getMessage()+"\n"+
                    "StackTrace:" + stackTraceToString(ex);
        }

        String type = "error";
        try
        {
            String sqlText = String.format(Locale.ENGLISH, "Insert Into Logs(Type, Tag, Message) Values ('%s','%s','%s')", type,_tag,_message);

            logDatabase.execQuery(sqlText);

        }
        catch (Exception e)
        {
            Log.e("Logger Internal Error",e.getStackTrace().toString());
        }
        Log.e(_tag,_message);

        if (!isReleaseMode)
        {
            //Toast.makeText(context,_message,Toast.LENGTH_LONG).show();
        }
    }

}
