package vp.metagram.utils;

import android.content.Context;
import android.database.MatrixCursor;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import vp.tools.io.iDBExport;
import vp.tools.io.iDBMetagram;

import static vp.metagram.general.functions.repeatSingleQuotes;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.deviceSettings;
import static vp.tools.io.iFileSystemUtils.GetExternalDir;

public class ExportStatistics
{


    public static void export(Context context, iDBMetagram db, String fileName, long IPK, long FIPK) throws IOException, GeneralSecurityException
    {
        if (FIPK < 0)
        {
            exportOrder(context,db,fileName,IPK);
        }
        else
        {
            String sqlText = String.format(Locale.ENGLISH,"Select IPK From Statistics_Orders Where FIPK = %d" , FIPK);
            MatrixCursor result = db.selectQuery(sqlText);
            if (result.moveToFirst())
            {
                while ( !result.isAfterLast() )
                {
                    exportOrder(context,db,fileName,result.getLong(result.getColumnIndex("IPK")));
                    result.moveToNext();
                }
            }
        }
    }


    public static void exportOrder(Context context, iDBMetagram db, String fileName, long IPK) throws IOException, GeneralSecurityException
    {
        String dbName = fileName;
        String dbPath = GetExternalDir(context)+ File.separator + "Metagram" +  File.separator + "exports" + File.separator;
        File dbDir = new File(dbPath);
        if (!dbDir.exists()) {dbDir.mkdirs();}
        iDBExport dbExport = new iDBExport(context,dbName,dbPath);
        for (String indexQuery : iDBExport.indexesQueries)
        { dbExport.execQuery(indexQuery); }

        String table;
        String[] fields;
        String[] values;

        String sqlText;
        MatrixCursor result;

        table = "Configuration";
        fields = new String[] {"name","value"};
        values = new String[] {"DeviceID", deviceSettings.DeviceUUI};

        dbExport.insert(table,fields,values);

        sqlText = String.format(Locale.ENGLISH, "Select * from Statistics_Orders Where IPK = %d",IPK);
        result = db.selectQuery(sqlText);

        int OrderID = -1;
        if (result.moveToFirst())
        {
            String creationDate = result.getString(result.getColumnIndex("CreationDate"));
            OrderID = result.getInt(result.getColumnIndex("StatOrderID"));

            sqlText = String.format(Locale.ENGLISH,"Insert Into Statistics_Orders(StatOrderID,FIPK, IPK, Username, UserInfo, ExecAgents, BoostSpeed, " +
                    "stopFollowersTracking, stopPostTracking, autoRefresh, intervalInSeconds, CreationDate) " +
                    "Values (%d, %d, %d, '%s', '%s', '%s',%d , %d, %d, %d, %d, datetime('%s')  )",
                    result.getInt(result.getColumnIndex("StatOrderID")),
                    result.getLong(result.getColumnIndex("FIPK")),
                    result.getLong(result.getColumnIndex("IPK")),
                    result.getString(result.getColumnIndex("Username")),
                    result.getString(result.getColumnIndex("UserInfo")),
                    result.getString(result.getColumnIndex("ExecAgents")),
                    result.getInt(result.getColumnIndex("BoostSpeed")),
                    result.getInt(result.getColumnIndex("stopFollowersTracking")),
                    result.getInt(result.getColumnIndex("stopPostTracking")),
                    result.getInt(result.getColumnIndex("autoRefresh")),
                    result.getInt(result.getColumnIndex("intervalInSeconds")),
                    creationDate
                    );
            dbExport.execQuery(sqlText);


        }

        sqlText = String.format(Locale.ENGLISH, "Select * from Statistics_Jobs Where StatOrderID = %d and Status = 'done' Order By StatJobID ASC ",OrderID);
        result = db.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                insertJobToExportedDB(dbExport,
                        result.getInt(result.getColumnIndex("StatJobID")),
                        result.getInt(result.getColumnIndex("StatOrderID")),
                        dbMetagram.AESCipher.decryptFromHexToString(result.getString(result.getColumnIndex("JobDescriptor"))),
                        result.getString(result.getColumnIndex("ThreadName")),
                        result.getInt(result.getColumnIndex("ThreadID")),
                        result.getString(result.getColumnIndex("Status")),
                        result.getLong(result.getColumnIndex("StartTime")),
                        result.getLong(result.getColumnIndex("ReStartTime")),
                        result.getLong(result.getColumnIndex("StopTime")),
                        result.getLong(result.getColumnIndex("EndTime")),
                        dbMetagram.AESCipher.decryptFromHexToString(result.getString(result.getColumnIndex("Result"))),
                        result.getInt(result.getColumnIndex("Used")),
                        result.getString(result.getColumnIndex("CreationDate")) );

                result.moveToNext();
            }
        }

        sqlText = String.format(Locale.ENGLISH, "Select * from Account_Info Where FIPK = %d ",IPK);
        result = db.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                insertAccountInfoToExportedDB(dbExport,
                        result.getInt(result.getColumnIndex("ID")),
                        result.getInt(result.getColumnIndex("StatisticsJobID")),
                        result.getLong(result.getColumnIndex("FIPK")),
                        result.getInt(result.getColumnIndex("FollowersNo")),
                        result.getInt(result.getColumnIndex("FollowingNo")),
                        result.getInt(result.getColumnIndex("PostsNo")),
                        result.getInt(result.getColumnIndex("Used")),
                        result.getString(result.getColumnIndex("CreationDate"))
                        );
                result.moveToNext();
            }
        }


        sqlText = String.format(Locale.ENGLISH, "Select * from Rel_Follower Where FIPK = %d ",IPK);
        result = db.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                insertOthersToExportedDB(dbExport, result.getLong(result.getColumnIndex("FollowerIPK")));
                insertFollowerRelationExportedDB(dbExport,
                        result.getLong(result.getColumnIndex("FIPK")),
                        result.getLong(result.getColumnIndex("FollowerIPK")),
                        result.getInt(result.getColumnIndex("Status")),
                        result.getInt(result.getColumnIndex("StatJobID")),
                        result.getString(result.getColumnIndex("CreationDate"))
                );
                result.moveToNext();
            }
        }

        sqlText = String.format(Locale.ENGLISH, "Select * from Rel_Following Where FIPK = %d ",IPK);
        result = db.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                insertOthersToExportedDB(dbExport, result.getLong(result.getColumnIndex("FollowingIPK")));
                insertFollowingRelationExportedDB(dbExport,
                        result.getLong(result.getColumnIndex("FIPK")),
                        result.getLong(result.getColumnIndex("FollowingIPK")),
                        result.getInt(result.getColumnIndex("Status")),
                        result.getInt(result.getColumnIndex("StatJobID")),
                        result.getString(result.getColumnIndex("CreationDate"))
                        );
                result.moveToNext();
            }
        }

        sqlText = String.format(Locale.ENGLISH, "Select * from Engagement Where FIPK = %d ",IPK);
        result = db.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                insertOthersToExportedDB(dbExport, result.getLong(result.getColumnIndex("EngageIPK")));
                insertEngagementToExportedDB(dbExport,
                        result.getLong(result.getColumnIndex("FIPK")),
                        result.getLong(result.getColumnIndex("EngageIPK")),
                        result.getInt(result.getColumnIndex("LikeNo")),
                        result.getInt(result.getColumnIndex("CommentNo")),
                        result.getInt(result.getColumnIndex("StatJobID")),
                        result.getString(result.getColumnIndex("ChangeDate"))
                );
                result.moveToNext();
            }
        }

        sqlText = String.format(Locale.ENGLISH, "Select * from Posts Where FIPK = %d ",IPK);
        result = db.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                long MPK = result.getLong(result.getColumnIndex("MPK"));
                insertPostToExportedDB(dbExport,
                        result.getLong(result.getColumnIndex("FIPK")),
                        result.getLong(result.getColumnIndex("MPK")),
                        result.getString(result.getColumnIndex("MiniLink")),
                        result.getString(result.getColumnIndex("MID")),
                        result.getInt(result.getColumnIndex("OrderID")),
                        result.getString(result.getColumnIndex("PicUrl")),
                        result.getInt(result.getColumnIndex("Status")),
                        result.getInt(result.getColumnIndex("StatJobID")),
                        result.getInt(result.getColumnIndex("PostType")),
                        result.getString(result.getColumnIndex("CreationDate"))
                        );

                String postSqlText = String.format(Locale.ENGLISH, "Select * from Posts_Info Where FMPK = %d", MPK);
                MatrixCursor postResult = dbMetagram.selectQuery(postSqlText);

                if (postResult.moveToFirst())
                {
                    while ( !postResult.isAfterLast() )
                    {
                        insertPostInfoToExportedDB(dbExport,
                                postResult.getLong(postResult.getColumnIndex("FMPK")),
                                postResult.getInt(postResult.getColumnIndex("StatJobID")),
                                postResult.getInt(postResult.getColumnIndex("LikeNo")),
                                postResult.getInt(postResult.getColumnIndex("CommentNo")),
                                postResult.getInt(postResult.getColumnIndex("ViewNo")),
                                postResult.getString(postResult.getColumnIndex("CreationDate"))
                        );
                        postResult.moveToNext();
                    }
                }

                postSqlText = String.format(Locale.ENGLISH,"Select * from Rel_Like where FMPK = %d", MPK);
                postResult = dbMetagram.selectQuery(postSqlText);

                if (postResult.moveToFirst())
                {
                    while ( !postResult.isAfterLast() )
                    {
                        insertOthersToExportedDB(dbExport, postResult.getLong(postResult.getColumnIndex("LikerIPK")));
                        insertLikeRelationExportedDB(dbExport,
                                postResult.getLong(postResult.getColumnIndex("FMPK")),
                                postResult.getLong(postResult.getColumnIndex("LikerIPK")),
                                postResult.getInt(postResult.getColumnIndex("Status")),
                                postResult.getInt(postResult.getColumnIndex("StatJobID")),
                                postResult.getString(postResult.getColumnIndex("CreationDate"))
                                );

                        postResult.moveToNext();
                    }
                }

                postSqlText = String.format(Locale.ENGLISH,"Select * from Rel_Comment where FMPK = %d", MPK);
                postResult = dbMetagram.selectQuery(postSqlText);
                if (postResult.moveToFirst())
                {
                    while ( !postResult.isAfterLast() )
                    {
                        insertOthersToExportedDB(dbExport, postResult.getLong(postResult.getColumnIndex("CommenterIPK")));
                        insertCommentRelationExportedDB(dbExport,
                                postResult.getLong(postResult.getColumnIndex("CPK")),
                                postResult.getLong(postResult.getColumnIndex("FMPK")),
                                postResult.getLong(postResult.getColumnIndex("CommenterIPK")),
                                postResult.getString(postResult.getColumnIndex("CommentText")),
                                postResult.getInt(postResult.getColumnIndex("Status")),
                                postResult.getInt(postResult.getColumnIndex("StatJobID")),
                                postResult.getString(postResult.getColumnIndex("CreationDate"))
                        );

                        postResult.moveToNext();
                    }
                }

                result.moveToNext();
            }
        }
    }

    public static void insertJobToExportedDB(iDBExport dbExport, int StatJobID, int StatOrderID, String JobDescriptor, String ThreadName, int ThreadID, String Status,
                                      long StartTime, long ReStartTime, long StopTime, long EndTime, String Result, int Used, String CreationDate)
    {
        String sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Statistics_Jobs(StatJobID,StatOrderID,JobDescriptor,ThreadName,ThreadID,Status," +
                                                " StartTime, ReStartTime, StopTime, EndTime, Result, Used, CreationDate)" +
                                                " Values (%d, %d, '%s', '%s', %d, '%s'," +
                                                " %d, %d, %d, %d, '%s', %d, datetime('%s') )",
                                                StatJobID,StatOrderID,JobDescriptor, ThreadName, ThreadID, Status,
                                                StartTime, ReStartTime, StopTime, EndTime, Result, Used, CreationDate);
        dbExport.execQuery(sqlText);

    }

    public static void insertAccountInfoToExportedDB(iDBExport dbExport, int ID, int StatisticsJobID, long FIPK, int FollowersNo, int FollowingNo, int PostsNo, int Used, String CreationDate )
    {
        String sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Account_Info(ID, StatisticsJobID, FIPK, FollowersNo, FollowingNo, PostsNo, Used, CreationDate) " +
                                                " Values(%d, %d, %d, %d, %d, %d, %d, datetime('%s') )",
                                                ID, StatisticsJobID, FIPK, FollowersNo, FollowingNo, PostsNo, Used, CreationDate);
        dbExport.execQuery(sqlText);
    }

    public static void insertPostToExportedDB(iDBExport dbExport, long FIPK, long MPK, String MiniLink, String MID, int OrderID, String PicUrl, int Status, int StatJobID, int PostType, String CreationDate)
    {
        String sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Posts(FIPK, MPK, MiniLink, MID, OrderID, PicUrl, Status, StatJobID, PostType, CreationDate) " +
                                                " Values (%d, %d, '%s', '%s', %d, '%s', %d, %d, %d, datetime('%s') ) ",
                                                 FIPK, MPK, MiniLink, MID, OrderID, PicUrl, Status, StatJobID, PostType, CreationDate);
        dbExport.execQuery(sqlText);
    }

    public static void insertPostInfoToExportedDB(iDBExport dbExport, long FMPK, int StatJobID, int LikeNo, int CommentNo, int ViewNo, String CreationDate)
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Posts_Info(FMPK, StatJobID, LikeNo, CommentNo, ViewNo, CreationDate) " +
                                                 " Values(%d, %d, %d, %d, %d, datetime('%s') )",
                                                 FMPK, StatJobID, LikeNo, CommentNo, ViewNo, CreationDate);
        dbExport.execQuery(sqlText);
    }

    public static void insertOthersToExportedDB(iDBExport dbExport, long IPK) throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * from Others Where IPK = %d",IPK);
        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToNext())
        {
            String Username = result.getString(result.getColumnIndex("Username"));
            String PictureURL = result.getString(result.getColumnIndex("PictureURL"));

            sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Others(IPK, Username, PictureURL) " +
                                              " Values(%d, '%s', '%s')",IPK, Username, PictureURL);
            dbExport.execQuery(sqlText);
        }
    }


    public static void insertLikeRelationExportedDB(iDBExport dbExport, long FMPK, long LikerIPK, int Status, int StatJobID, String CreationDate)
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Rel_Like(FMPK, LikerIPK, Status, StatJobID, CreationDate) " +
                                                 " Values(%d, %d, %d, %d, datetime('%s') )",
                                                 FMPK, LikerIPK, Status, StatJobID, CreationDate);
        dbExport.execQuery(sqlText);
    }

    public static void insertCommentRelationExportedDB(iDBExport dbExport, long CPK, long FMPK, long CommenterIPK, String CommentText, int Status, int StatJobID, String CreationDate)
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Rel_Comment(CPK, FMPK, CommenterIPK, CommentText, Status, StatJobID, CreationDate) " +
                                                 " Values(%d, %d, %d, '%s', %d, %d, datetime('%s')  )",
                                                  CPK, FMPK, CommenterIPK, repeatSingleQuotes(CommentText), Status, StatJobID, CreationDate );
        dbExport.execQuery(sqlText);
    }

    public static void insertFollowerRelationExportedDB(iDBExport dbExport, long FIPK, long FollowerIPK, int Status, int StatJobID, String CreationDate)
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Rel_Follower(FIPK, FollowerIPK, Status, StatJobID, CreationDate) " +
                                                 " Values(%d, %d, %d, %d, datetime('%s') )",
                                                 FIPK, FollowerIPK, Status, StatJobID, CreationDate);

        dbExport.execQuery(sqlText);
    }

    public  static void insertFollowingRelationExportedDB(iDBExport dbExport, long FIPK, long FollowingIPK, int Status, int StatJobID, String CreationDate)
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Rel_Following(FIPK, FollowingIPK, Status, StatJobID, CreationDate) " +
                                                         " Values(%d, %d, %d, %d, datetime('%s') )",
                                                         FIPK, FollowingIPK, Status, StatJobID, CreationDate);

        dbExport.execQuery(sqlText);
    }

    public static void insertEngagementToExportedDB(iDBExport dbExport, long FIPK, long EngageIPK, int LikeNo, int CommentNo, int StatJobID, String ChangeDate)
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Engagement(FIPK, EngageIPK, LikeNo, CommentNo, StatJobID, ChangeDate) " +
                                                 " Values(%d, %d, %d, %d, %d, datetime('%s') )",
                                                 FIPK, EngageIPK, LikeNo, CommentNo, StatJobID, ChangeDate);

        dbExport.execQuery(sqlText);
    }
}
