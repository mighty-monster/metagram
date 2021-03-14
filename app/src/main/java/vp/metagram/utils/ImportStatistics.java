package vp.metagram.utils;

import android.content.Context;
import android.database.MatrixCursor;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import vp.tools.io.iDBExport;

import static vp.metagram.general.functions.repeatSingleQuotes;
import static vp.metagram.general.variables.dbMetagram;

public class ImportStatistics
{

    public static void importDB(Context context, long FIPK, String dbPath) throws IOException, GeneralSecurityException
    {
        iDBExport dbExport;
        File db = new File(dbPath);
        if (db.exists())
        {
            String dirPath = db.getParentFile().getAbsolutePath()+File.separator;
            String fileName = db.getName();

            dbExport = new iDBExport(context, fileName, dirPath);

            insertOthers(dbExport);

            String sqlText = "Select StatOrderID, IPK from Statistics_Orders ";
            MatrixCursor result = dbExport.selectQuery(sqlText);
            if (result.moveToFirst())
            {
                while ( !result.isAfterLast() )
                {
                    importOrder(dbExport,FIPK, result.getInt(result.getColumnIndex("StatOrderID")),result.getLong(result.getColumnIndex("IPK")));
                    result.moveToNext();
                }
            }
        }
    }


    public static void importOrder(iDBExport dbExport,long FIPK, int orderID, long IPK) throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH,"Select * from Statistics_Orders Where StatOrderID = %d", orderID);
        MatrixCursor result = dbExport.selectQuery(sqlText);

        result.moveToFirst();
        sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Statistics_Orders(FIPK, IPK, Username, UserInfo, ExecAgents, " +
                                         " BoostSpeed, stopFollowersTracking, stopPostTracking, autoRefresh, intervalInSeconds, CreationDate) " +
                                         " Values (%d, %d, '%s', '%s' , '%s', %d, %d, %d, %d, %d,  datetime('%s') ) ",
                                          FIPK,
                result.getLong(result.getColumnIndex("IPK")),
                result.getString(result.getColumnIndex("Username")),
                result.getString(result.getColumnIndex("UserInfo")),
                Long.toString(FIPK),
                result.getInt(result.getColumnIndex("BoostSpeed")),
                result.getInt(result.getColumnIndex("stopFollowersTracking")),
                result.getInt(result.getColumnIndex("stopPostTracking")),
                result.getInt(result.getColumnIndex("autoRefresh")),
                result.getInt(result.getColumnIndex("intervalInSeconds")),
                result.getString(result.getColumnIndex("CreationDate"))
                );

        dbMetagram.execQuery(sqlText);

        sqlText = " SELECT last_insert_rowid() as LastID";
        result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();
        int newOrderID = result.getInt(result.getColumnIndex("LastID"));


        sqlText = String.format(Locale.ENGLISH, "Select * from Statistics_Jobs Where StatOrderID = %d", orderID);
        result = dbExport.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                insertStatisticsJobs(dbExport,
                        result.getInt(result.getColumnIndex("StatJobID")),
                        newOrderID,
                        result.getString(result.getColumnIndex("JobDescriptor")),
                        result.getString(result.getColumnIndex("ThreadName")),
                        result.getInt(result.getColumnIndex("ThreadID")),
                        result.getString(result.getColumnIndex("Status")),
                        result.getLong(result.getColumnIndex("StartTime")),
                        result.getLong(result.getColumnIndex("ReStartTime")),
                        result.getLong(result.getColumnIndex("StopTime")),
                        result.getLong(result.getColumnIndex("EndTime")),
                        result.getString(result.getColumnIndex("Result")),
                        result.getString(result.getColumnIndex("CreationDate")),
                        IPK);

                result.moveToNext();
            }
        }



    }



    public static void insertStatisticsJobs(iDBExport dbExport,int oldJobID, int newOrderID, String JobDescriptor, String ThreadName, int ThreadID, String Status,
                                            long StartTime, long ReStartTime, long StopTime, long EndTime, String Result, String CreationDate, long IPK) throws GeneralSecurityException, IOException
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Statistics_Jobs(StatOrderID, JobDescriptor, ThreadName, ThreadID, Status, " +
                                                 " StartTime, ReStartTime, StopTime, EndTime, Result, Used, CreationDate) " +
                                                 " Values(%d, '%s', '%s', %d, '%s', %d, %d, %d, %d, '%s', 1, datetime('%s') ) ",
                                                  newOrderID,
                                                  dbMetagram.AESCipher.encryptStringToHex(JobDescriptor),
                                                  ThreadName, ThreadID, Status, StartTime, ReStartTime, StopTime, EndTime,
                                                  dbMetagram.AESCipher.encryptStringToHex(Result), CreationDate);
        dbMetagram.execQuery(sqlText);


        sqlText = " SELECT last_insert_rowid() as LastID";
        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();
        int newJobID = result.getInt(result.getColumnIndex("LastID"));

        insertAccountInfo(dbExport,oldJobID,IPK,newJobID);
        insertEngagements(dbExport,oldJobID,IPK,newJobID);
        insertFollowerRelations(dbExport,oldJobID,IPK,newJobID);
        insertFollowingRelations(dbExport,oldJobID,IPK,newJobID);
        insertPosts(dbExport,oldJobID,IPK,newJobID);


    }


    public static void insertOthers(iDBExport dbExport) throws IOException
    {
        int noOfItems;
        int perPage = 500;
        int index = 0;

        String sqlText = "Select Count(*) as No From Others";
        MatrixCursor result = dbExport.selectQuery(sqlText);
        result.moveToFirst();
        noOfItems = result.getInt(result.getColumnIndex("No"));

        while ( index < noOfItems )
        {
            sqlText = String.format(Locale.ENGLISH,"Select * from Others LIMIT %d OFFSET %s", perPage, index);
            result = dbExport.selectQuery(sqlText);
            if (result.moveToFirst())
            {
                while ( !result.isAfterLast() )
                {
                    sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Others(IPK, Username, PictureURL) " +
                                                     " Values(%d, '%s', '%s')",
                            result.getLong(result.getColumnIndex("IPK")),
                            result.getString(result.getColumnIndex("Username")),
                            result.getString(result.getColumnIndex("PictureURL"))
                            );
                    dbMetagram.execQuery(sqlText);
                    result.moveToNext();
                }
            }
            index += perPage;
        }

    }

    public static void insertAccountInfo(iDBExport dbExport, int oldJobID, long IPK, int newJobID) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * from Account_Info Where FIPK = %d and StatisticsJobID = %d", IPK, oldJobID);
        MatrixCursor result = dbExport.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Account_Info(StatisticsJobID, FIPK, FollowersNo, FollowingNo, PostsNo, Used, CreationDate) " +
                                                 " Values(%d, %d, %d, %d, %d, 1, datetime('%s'))",
                        newJobID, IPK,
                        result.getInt(result.getColumnIndex("FollowersNo")),
                        result.getInt(result.getColumnIndex("FollowingNo")),
                        result.getInt(result.getColumnIndex("PostsNo")),
                        result.getString(result.getColumnIndex("CreationDate"))
                        );

                dbMetagram.execQuery(sqlText);
                result.moveToNext();
            }
        }
    }

    public static void insertEngagements(iDBExport dbExport, int oldJobID, long IPK, int newJobID) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * from Engagement Where FIPK = %d and StatJobID = %d", IPK, oldJobID);
        MatrixCursor result = dbExport.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Engagement(FIPK, EngageIPK, LikeNo, CommentNo, StatJobID, ChangeDate) " +
                                " Values(%d, %d, %d, %d, %d, datetime('%s'))",
                        IPK,
                        result.getLong(result.getColumnIndex("EngageIPK")),
                        result.getInt(result.getColumnIndex("LikeNo")),
                        result.getInt(result.getColumnIndex("CommentNo")),
                        newJobID,
                        result.getString(result.getColumnIndex("ChangeDate"))
                );

                dbMetagram.execQuery(sqlText);
                result.moveToNext();
            }
        }
    }

    public static void insertFollowerRelations(iDBExport dbExport, int oldJobID, long IPK, int newJobID) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * From Rel_Follower Where FIPK = %d and StatJobID = %d", IPK, oldJobID);

        MatrixCursor result = dbExport.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Rel_Follower (FIPK, FollowerIPK, Status, StatJobID, CreationDate) " +
                                                 " Values (%d, %d, %d, %d, datetime('%s'))",
                                                 IPK,
                                                 result.getLong(result.getColumnIndex("FollowerIPK")),
                                                 result.getInt(result.getColumnIndex("Status")),
                                                 newJobID,
                                                 result.getString(result.getColumnIndex("CreationDate")) );

                dbMetagram.execQuery(sqlText);
                result.moveToNext();
            }
        }
    }

    public static void insertFollowingRelations(iDBExport dbExport, int oldJobID, long IPK, int newJobID) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * From Rel_Following Where FIPK = %d and StatJobID = %d", IPK, oldJobID);

        MatrixCursor result = dbExport.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Rel_Following (FIPK, FollowingIPK, Status, StatJobID, CreationDate) " +
                                " Values (%d, %d, %d, %d, datetime('%s'))",
                        IPK,
                        result.getLong(result.getColumnIndex("FollowingIPK")),
                        result.getInt(result.getColumnIndex("Status")),
                        newJobID,
                        result.getString(result.getColumnIndex("CreationDate")) );

                dbMetagram.execQuery(sqlText);
                result.moveToNext();
            }
        }
    }

    public static void insertPosts(iDBExport dbExport, int oldJobID, long IPK, int newJobID) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * from Posts Where FIPK = %d ", IPK, oldJobID);
        MatrixCursor result = dbExport.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {
                long MPK = result.getLong(result.getColumnIndex("MPK"));

                sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Posts(FIPK, MPK, MiniLink, MID, OrderID, PicUrl, Status, StatJobID, PostType, CreationDate) " +
                                                         " Values(%d, %d, '%s', '%s', %d, '%s', %d, %d, %d, datetime('%s'))",
                                                         IPK,
                        result.getLong(result.getColumnIndex("MPK")),
                        result.getString(result.getColumnIndex("MiniLink")),
                        result.getString(result.getColumnIndex("MID")),
                        result.getInt(result.getColumnIndex("OrderID")),
                        result.getString(result.getColumnIndex("PicUrl")),
                        result.getInt(result.getColumnIndex("Status")),
                        newJobID,
                        result.getInt(result.getColumnIndex("PostType")),
                        result.getString(result.getColumnIndex("CreationDate"))
                        );

                dbMetagram.execQuery(sqlText);

                insertPostInfo(dbExport,oldJobID,newJobID, MPK);
                insertPostLikes(dbExport,oldJobID,newJobID, MPK);
                insertPostComments(dbExport,oldJobID,newJobID, MPK);

                result.moveToNext();
            }
        }
    }

    public static void insertPostInfo(iDBExport dbExport, int oldJobID, int newJobID, long MPK) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * from Posts_Info Where FMPK = %d and StatJobID = %d", MPK, oldJobID);
        MatrixCursor result = dbExport.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {

                sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Posts_Info(FMPK, StatJobID, LikeNo, CommentNo, ViewNo, CreationDate) " +
                                                  " Values (%d, %d, %d, %d, %d, datetime('%s') )",
                        MPK, newJobID,
                        result.getInt(result.getColumnIndex("LikeNo")),
                        result.getInt(result.getColumnIndex("CommentNo")),
                        result.getInt(result.getColumnIndex("ViewNo")),
                        result.getString(result.getColumnIndex("CreationDate"))
                        );

                dbMetagram.execQuery(sqlText);



                result.moveToNext();
            }
        }
    }

    public static void insertPostLikes(iDBExport dbExport, int oldJobID,  int newJobID, long MPK) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * from Rel_Like Where FMPK = %d and StatJobID = %d", MPK, oldJobID);
        MatrixCursor result = dbExport.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {

                sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Rel_Like(FMPK, LikerIPK, Status, StatJobID, CreationDate) " +
                                " Values (%d, %d, %d,  %d, datetime('%s') )",
                        MPK,
                        result.getLong(result.getColumnIndex("LikerIPK")),
                        result.getInt(result.getColumnIndex("Status")),
                        newJobID,
                        result.getString(result.getColumnIndex("CreationDate"))
                );

                dbMetagram.execQuery(sqlText);



                result.moveToNext();
            }
        }
    }

    public static void insertPostComments(iDBExport dbExport, int oldJobID,  int newJobID, long MPK) throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Select * from Rel_Comment Where FMPK = %d and StatJobID = %d", MPK, oldJobID);
        MatrixCursor result = dbExport.selectQuery(sqlText);

        if (result.moveToFirst())
        {
            while ( !result.isAfterLast() )
            {

                sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Rel_Comment(CPK, FMPK, CommenterIPK, CommentText, Status, StatJobID, CreationDate) " +
                                " Values (%d, %d, %d, '%s', %d, %d, datetime('%s') )",
                        result.getLong(result.getColumnIndex("CPK")),
                        MPK,
                        result.getLong(result.getColumnIndex("CommenterIPK")),
                        repeatSingleQuotes(result.getString(result.getColumnIndex("CommentText"))),
                        result.getInt(result.getColumnIndex("Status")),
                        newJobID,
                        result.getString(result.getColumnIndex("CreationDate"))
                );

                dbMetagram.execQuery(sqlText);



                result.moveToNext();
            }
        }
    }




}


