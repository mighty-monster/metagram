package vp.tools.io;

import android.content.Context;
import android.database.MatrixCursor;
import android.util.Base64;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;
import java.util.concurrent.Semaphore;




/**
 * Created by arash on 2/12/18.
 */

public class iDBMetagram extends iDBWithCipher
{
    private static String[] BuildQueries = {

            " Create Table if not Exists Configuration ( " +
                    " ID Integer Primary Key AutoIncrement, " +
                    " Name Text, " +
                    " Value Text, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " ChangeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            " Create Table if not Exists Accounts ( " +
                    " ID Integer Primary Key AutoIncrement, " +
                    " IPK Integer," +
                    " Username Text," +
                    " Enabled Integer Default 1, " +
                    " Native Integer Default 1," +
                    " Content Text, " +
                    " Registered Integer Default 0, " +
                    " Ready Integer Default 0, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " ChangeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " UNIQUE(IPK) ) ",

            " Create Table if not Exists Account_Info ( " +
                    " ID Integer Primary Key AutoIncrement, " +
                    " StatisticsJobID Integer, " +
                    " FIPK Integer," +
                    " FollowersNo Integer," +
                    " FollowingNo Integer," +
                    " PostsNo Integer," +
                    " Used Integer  Default 0,"+
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            " Create Table if not Exists Posts ( " +
                    " FIPK Integer," +
                    " MPK Integer," +
                    " MiniLink Text," +
                    " MID Text," +
                    " OrderID Integer,"+
                    " PicURL Text,"+
                    " URLs Text,"+
                    " Caption Text,"+
                    " Status Integer DEFAULT 0," +
                    " StatJobID Integer," +
                    " PostType Integer, " + // 1 Image - 2 Video - 8 Slide
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(MPK) ) ",

            " Create Table if not Exists Posts_Info ( " +
                    " FMPK Integer," +
                    " StatJobID Integer," +
                    " LikeNo Integer," +
                    " CommentNo Integer," +
                    " ViewNo Integer," +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(StatJobID, FMPK) ON CONFLICT REPLACE ) ",

            " Create Table if not Exists Statistics_Orders ( " +
                    " StatOrderID Integer Primary Key AutoIncrement, " +
                    " FIPK Integer," +
                    " IPK Integer," +
                    " Username Text," +
                    " UserInfo Text," +
                    " ExecAgents Text, " +
                    " Flag Integer DEFAULT 1, " + // 1: Ready, -1: Changing
                    " BoostSpeed Integer DEFAULT 0, " +
                    " F_Parameter Integer DEFAULT 0, " +
                    " P_Parameter Integer DEFAULT 0, " +
                    " D_Parameter Integer DEFAULT 0, " +
                    " autoRefresh Integer DEFAULT 1, " +
                    " intervalInSeconds Integer DEFAULT -1, " +
                    " reBornDate Integer DEFAULT -1, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(IPK) ) ",

            " Create Table if not Exists Statistics_Jobs ( " +
                    " StatJobID Integer Primary Key AutoIncrement, " +
                    " StatOrderID Integer," +
                    " JobDescriptor Text DEFAULT '', " +
                    " ThreadName text DEFAULT ''," +
                    " ThreadID Integer DEFAULT -1," +
                    " Status Text DEFAULT 'ready'," +
                    " StartTime Integer DEFAULT -1," +
                    " ReStartTime Integer DEFAULT -1," +
                    " StopTime Integer DEFAULT -1," +
                    " EndTime Integer DEFAULT -1," +
                    " Result Text," +
                    " Used Integer  Default 0,"+
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ",

            " Create Table if not Exists Others ( " +
                    " IPK Integer," +
                    " Username Text, " +
                    " PictureURL Text," +
                    " UNIQUE(IPK) ) ",

            " Create Table if not Exists Rel_Follower ( " +
                    " FIPK Integer," +
                    " FollowerIPK Integer," +
                    " Status Integer DEFAULT 0," + // 1 means updated
                    " StatJobID Integer, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(FIPK, FollowerIPK))",

            " Create Table if not Exists Rel_Following ( " +
                    " FIPK Integer," +
                    " FollowingIPK Integer," +
                    " Status Integer DEFAULT 0," + // 1 means updated
                    " StatJobID Integer, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(FIPK, FollowingIPK))",

            " Create Table if not Exists Rel_Like ( " +
                    " FMPK Integer," +
                    " LikerIPK Integer," +
                    " Status Integer DEFAULT 0," + // 1 means updated
                    " StatJobID Integer, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ",

            " Create Table if not Exists Rel_Comment ( " +
                    " CPK Integer," +
                    " FMPK Integer," +
                    " CommenterIPK Integer," +
                    " CommentText Text," +
                    " Type Integer, " + // 0-Original 2-Reply
                    " Created_UTC Integer,"+
                    " NoOfLikes Integer, "+
                    " Status Integer," +
                    " StatJobID Integer, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ",

            " Create Table if not Exists Engagement ( " +
                    " FIPK Integer," +
                    " EngageIPK Integer," +
                    " LikeNo Integer DEFAULT 0," +
                    " CommentNo Integer DEFAULT 0," +
                    " StatJobID Integer, " +
                    " ChangeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE(FIPK, EngageIPK, StatJobID) ) ",

            " Create Table if not Exists Miscellaneous ( " +
                    " ID Integer Primary Key AutoIncrement," +
                    " Name Text, " +
                    " Value Text) ",

            " Create Table if not Exists Reports ( " +
                    " ID Integer Primary Key AutoIncrement," +
                    " Name Text, " +
                    " Value Text," +
                    " Version Text) ",

            " Create Table if not Exists ItemStatus ( " +
                    " ID Integer Primary Key AutoIncrement," +
                    " Title Text, " +
                    " Status Integer DEFAULT 0) "

    };


    public String indexesQueries[] =
            {"    CREATE INDEX IF NOT EXISTS Account_Info_FIPK_Index ON Account_Info(FIPK);\n" ,

            "    CREATE INDEX IF NOT EXISTS Posts_FIPK_Index ON Posts(FIPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Posts_OrderID_Index ON Posts(OrderID);\n" ,
            "    CREATE INDEX IF NOT EXISTS Posts_MPK_Index ON Posts(MPK);\n" ,

            "    CREATE INDEX IF NOT EXISTS Posts_Info_FMPK_Index ON Posts_Info(FMPK);\n" ,

            "    CREATE INDEX IF NOT EXISTS Engagement_FIPK_Index ON Engagement(FIPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Engagement_EngageIPK_Index ON Engagement(EngageIPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Engagement_LikeNo_Index ON Engagement(LikeNo);\n" ,
            "    CREATE INDEX IF NOT EXISTS Engagement_CommentNo_Index ON Engagement(CommentNo);\n" ,
            "    CREATE INDEX IF NOT EXISTS Engagement_StatJobID_Index ON Engagement(StatJobID);\n" ,

            "    CREATE INDEX IF NOT EXISTS Statistics_Jobs_StatOrderID_Index ON Statistics_Jobs(StatOrderID);\n" ,
            "    CREATE INDEX IF NOT EXISTS Statistics_Jobs_CreationDate_Index ON Statistics_Jobs(CreationDate);\n" ,

            "    CREATE INDEX IF NOT EXISTS Rel_Follower_FollowerIPK_Index ON Rel_Follower(FollowerIPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Follower_StatJobID_Index ON Rel_Follower(StatJobID);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Follower_CreationDate_Index ON Rel_Follower(CreationDate);\n" ,

            "    CREATE INDEX IF NOT EXISTS Rel_Following_FollowingIPK_Index ON Rel_Following(FollowingIPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Following_StatJobID_Index ON Rel_Following(StatJobID);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Following_CreationDate_Index ON Rel_Following(CreationDate);\n" ,

            "    CREATE INDEX IF NOT EXISTS Rel_Like_FMPK_Index ON Rel_Like(FMPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Like_LikerIPK_Index ON Rel_Like(LikerIPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Like_StatJobID_Index ON Rel_Like(StatJobID);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Like_CreationDate_Index ON Rel_Like(CreationDate);\n" ,

            "    CREATE INDEX IF NOT EXISTS Rel_Comment_FMPK_Index ON Rel_Comment(FMPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Comment_CommenterIPK_Index ON Rel_Comment(CommenterIPK);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Comment_StatJobID_Index ON Rel_Comment(StatJobID);\n" ,
            "    CREATE INDEX IF NOT EXISTS Rel_Comment_CreationDate_Index ON Rel_Comment(CreationDate);\n",

            "    CREATE INDEX IF NOT EXISTS ItemStatus_Title_Index ON ItemStatus(Title);\n"
            };


    public iDBMetagram(Context _context, String _dataBaseName, int _dataBaseVersion) throws GeneralSecurityException, IOException
    {
        super(_context, _dataBaseName, _dataBaseVersion, BuildQueries);

        init();

        indexDB();

        upgradeDB();
    }

    public void indexDB() throws IOException
    {
        for (String query:indexesQueries)
        { execQuery(query); }
    }

    public void upgradeDB() throws IOException
    {
        String sqlText;

        /*sqlText = "drop table Robots";
        execQuery(sqlText);*/

        sqlText = " Create Table if not Exists Robots ( " +
                " RobotID Integer Primary Key AutoIncrement, " +
                " Type Integer, "+
                " Robot_UUID Text, "+
                " Username Text,"+
                " IPK Integer, " +
                " Content Text," +
                " Settings Text, "+
                " IsForeign Integer,"+
                " CreationVersion Text, "+
                " ExecVersion Text, "+
                " StartTime Integer DEFAULT -1," +
                " StopTime Integer DEFAULT -1," +
                " RobotStage Integer DEFAULT 0, "+
                " Enabled Integer DEFAULT 1,"+
                " Registered Integer DEFAULT 0,"+
                " ThreadName Text,"+
                " ThreadID Integer,"+
                " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        execQuery(sqlText);

        try
        {
            execQuery(sqlText);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void setAccountPicture(long IPK, String picHex) throws IOException
    {
        execQuery(String.format(Locale.ENGLISH, "Update Accounts Set Picture = '%s', ChangeDate = CURRENT_TIMESTAMP Where IPK = %d", picHex, IPK));
    }

    public byte[] getAccountPicture(long IPK) throws IOException, GeneralSecurityException
    {
        byte[] result = null;

        MatrixCursor cursor = selectQuery(String.format(Locale.ENGLISH, "Select Picture From Accounts Where IPK = %d ", IPK));
        if ( cursor.moveToFirst() )
        {
            String picHex = cursor.getString(cursor.getColumnIndex("Picture"));
            if ( !picHex.equals("") )
            {
                result = Base64.decode(picHex, Base64.DEFAULT);
            }
        }

        return result;
    }

    public void delPair(String key) throws IOException, GeneralSecurityException
    {
        execQuery(String.format(Locale.ENGLISH, "Delete From Configuration Where name = '%s' ", AESCipher.encryptStringToHex(key)));
    }

    public Semaphore pairMutex = new Semaphore(1);
    public void setPair(String key, String value) throws IOException, GeneralSecurityException, InterruptedException
    {
        pairMutex.acquire();
        try
        {
            int count;
            MatrixCursor result = selectQuery(String.format(Locale.ENGLISH, "Select Count(*) as count from Configuration Where Name = '%s' ", AESCipher.encryptStringToHex(key)));
            result.moveToFirst();
            count = result.getInt(result.getColumnIndex("count"));

            if (count > 0)
            {
                execQuery(String.format(Locale.ENGLISH, "Update Configuration Set Value = '%s', ChangeDate = CURRENT_TIMESTAMP Where Name = '%s'", AESCipher.encryptStringToHex(value), AESCipher.encryptStringToHex(key)));
            } else
            {
                execQuery(String.format(Locale.ENGLISH, "Insert Into Configuration(Name, Value) Values ('%s','%s')", AESCipher.encryptStringToHex(key), AESCipher.encryptStringToHex(value)));
            }
        }
        finally
        {
            pairMutex.release();
        }
    }

    public String getPair(String key) throws IOException, GeneralSecurityException, InterruptedException
    {
        pairMutex.acquire();
        try
        {
            String result = "";

            MatrixCursor queryResult = selectQuery(String.format(Locale.ENGLISH, "Select Value from Configuration Where Name = '%s'", AESCipher.encryptStringToHex(key)));

            if ( queryResult.moveToFirst() )
            {
                result = queryResult.getString(queryResult.getColumnIndex("Value"));
                result = AESCipher.decryptFromHexToString(result);
            }

            return result;
        }
        finally
        {
            pairMutex.release();
        }
    }

    public void delMiscellaneous(String key) throws IOException, GeneralSecurityException
    {
        execQuery(String.format(Locale.ENGLISH, "Delete From Miscellaneous Where name = '%s' ", AESCipher.encryptStringToHex(key)));
    }

    public void setMiscellaneous(String key, String value) throws IOException, GeneralSecurityException
    {

        int count;
        MatrixCursor result = selectQuery(String.format(Locale.ENGLISH, "Select Count(*) as count from Miscellaneous Where Name = '%s' ", AESCipher.encryptStringToHex(key)));
        result.moveToFirst();
        count = result.getInt(result.getColumnIndex("count"));

        if ( count > 0 )
        {
            execQuery(String.format(Locale.ENGLISH, "Update Miscellaneous Set Value = '%s' Where Name = '%s'", AESCipher.encryptStringToHex(value), AESCipher.encryptStringToHex(key)));
        }
        else
        {
            execQuery(String.format(Locale.ENGLISH, "Insert Into Miscellaneous(Name, Value) Values ('%s','%s')", AESCipher.encryptStringToHex(key), AESCipher.encryptStringToHex(value)));
        }
    }

    public String getMiscellaneous(String key) throws IOException, GeneralSecurityException
    {
        String result = "";

        MatrixCursor queryResult = selectQuery(String.format(Locale.ENGLISH, "Select Value from Miscellaneous Where Name = '%s'", AESCipher.encryptStringToHex(key)));

        if ( queryResult.moveToFirst() )
        {
            result = queryResult.getString(queryResult.getColumnIndex("Value"));
            result = AESCipher.decryptFromHexToString(result);
        }

        return result;
    }

    public void setBazzarPayment(String key, String value, String version) throws IOException, GeneralSecurityException
    {

        int count;
        MatrixCursor result = selectQuery(String.format(Locale.ENGLISH, "Select Count(*) as count from Reports Where Name = '%s' ", AESCipher.encryptStringToHex(key)));
        result.moveToFirst();
        count = result.getInt(result.getColumnIndex("count"));

        if ( count > 0 )
        {
            execQuery(String.format(Locale.ENGLISH, "Update Reports Set Value = '%s', Version = '%s' Where Name = '%s'",
                    AESCipher.encryptStringToHex(value),
                    AESCipher.encryptStringToHex(version),
                    AESCipher.encryptStringToHex(key)));
        }
        else
        {
            execQuery(String.format(Locale.ENGLISH, "Insert Into Reports(Name, Value, Version) Values ('%s','%s','%s')",
                    AESCipher.encryptStringToHex(key),
                    AESCipher.encryptStringToHex(value),
                    AESCipher.encryptStringToHex(version)));
        }
    }

    public String getBazzarPayment(String key) throws IOException, GeneralSecurityException
    {
        String result = "";

        MatrixCursor queryResult = selectQuery(String.format(Locale.ENGLISH, "Select Value from Reports Where Name = '%s'", AESCipher.encryptStringToHex(key)));

        if ( queryResult.moveToFirst() )
        {
            result = queryResult.getString(queryResult.getColumnIndex("Value"));
            result = AESCipher.decryptFromHexToString(result);
        }

        return result;
    }

    public void setItemStatus(String key, int status)
    {
        try
        {
            int count;
            MatrixCursor result = selectQuery(String.format(Locale.ENGLISH, "Select Count(*) as count from ItemStatus Where Title = '%s' ", key));
            result.moveToFirst();
            count = result.getInt(result.getColumnIndex("count"));

            if ( count > 0 )
            {
                execQuery(String.format(Locale.ENGLISH, "Update ItemStatus Set Status = %d Where Title = '%s'", status, key));
            }
            else
            {
                execQuery(String.format(Locale.ENGLISH, "Insert Into ItemStatus(Title, Status) Values ('%s',%d)", key, status));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public int getItemStatus(String key)
    {
        int result = 0;

        try
        {
            MatrixCursor queryResult = selectQuery(String.format(Locale.ENGLISH, "Select Status from ItemStatus Where Title = '%s'", key));

            if ( queryResult.moveToFirst() )
            {
                result = queryResult.getInt(queryResult.getColumnIndex("Status"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
