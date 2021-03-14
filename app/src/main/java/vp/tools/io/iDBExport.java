package vp.tools.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;
import static vp.metagram.general.variables.ExportDBIV;


public class iDBExport extends SQLiteOpenHelper
{

    String databaseName;
    Context context;


    public iDBExport(Context _context, String _dataBaseName,String _databaseDirectory)
    {
        super(new ExportContext(_context,_databaseDirectory), _dataBaseName, null, 1);
        databaseName = _dataBaseName;
        context = _context;
        SQLiteDatabase.loadLibs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

        for ( String buildQuery : buildingQueries )
        {
            if ( buildQuery != null && !buildQuery.equals("") )
            {db.execSQL(buildQuery);}
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    { }


    public long insert(String _Table, String[] _Fields, String[] _Values)
    {
        SQLiteDatabase db;
        db = getWritableDatabase(ExportDBIV);

        ContentValues values = new ContentValues();
        for ( int i = 0; i < _Fields.length; i++ )
        {
            values.put(_Fields[i], _Values[i]);
        }
        return db.insert(_Table, null, values);
    }

    public int update(String _Table, String[] _Fields, String[] _Values, String _WhereClause, String[] _WhereArgs)
    {
        SQLiteDatabase db;
        db = getWritableDatabase(ExportDBIV);

        ContentValues values = new ContentValues();
        for ( int i = 0; i < _Fields.length; i++ )
        {
            values.put(_Fields[i], _Values[i]);
        }
        return db.update(_Table, values, _WhereClause, _WhereArgs);

    }

    public MatrixCursor selectQuery(String _Query)
    {

        SQLiteDatabase db;
        db = getWritableDatabase(ExportDBIV);

        Cursor queryResult = db.rawQuery(_Query, null);
        String[] columns = queryResult.getColumnNames();
        MatrixCursor result = new MatrixCursor(columns);

        try
        {
            if ( queryResult.moveToFirst() )
            {

                while ( !queryResult.isAfterLast() )
                {
                    MatrixCursor.RowBuilder rowBuilder = result.newRow();
                    for ( String col : columns )
                    {
                        switch ( queryResult.getType(queryResult.getColumnIndex(col)) )
                        {
                            case FIELD_TYPE_BLOB:
                            {
                                rowBuilder.add(queryResult.getBlob(queryResult.getColumnIndex(col)));
                                break;
                            }
                            case FIELD_TYPE_FLOAT:
                            {
                                rowBuilder.add(queryResult.getFloat(queryResult.getColumnIndex(col)));
                                break;
                            }
                            case FIELD_TYPE_INTEGER:
                            {
                                rowBuilder.add(queryResult.getLong(queryResult.getColumnIndex(col)));
                                break;
                            }
                            case FIELD_TYPE_STRING:
                            {
                                String content = queryResult.getString(queryResult.getColumnIndex(col));
                                rowBuilder.add(content);
                                break;
                            }
                            case FIELD_TYPE_NULL:
                            {
                                rowBuilder.add("");
                                break;
                            }
                        }

                    }
                    queryResult.moveToNext();
                }
            }
        }
        finally
        {
            if ( queryResult != null )
                queryResult.close();

        }

        return result;
    }

    public void execQuery(String _Query)
    {

        SQLiteDatabase db;
        db = getWritableDatabase(ExportDBIV);
        db.execSQL(_Query);
    }

    public String getDatabasePath()
    {
        return context.getDatabasePath(databaseName).getAbsolutePath();
    }

    public long getDatabaseSize()
    {
        File file = new File(getDatabasePath());
        return file.length();
    }

    public void removeDatabase() throws IOException
    {
        String databasePath = getDatabasePath();
        iFileSystemUtils.deleteFile(databasePath);
    }

    public void copyDatabase(String _path) throws IOException
    {
        String databasePath = getDatabasePath();
        iFileSystemUtils.copyFile(databasePath, _path);
    }

    final String[] buildingQueries = new String[] {

            " Create Table if not Exists Configuration ( " +
                    " Name Text, " +
                    " Value Text )",

            " Create Table if not Exists Account_Info ( " +
                    " ID Integer, " +
                    " StatisticsJobID Integer, " +
                    " FIPK Integer," +
                    " FollowersNo Integer," +
                    " FollowingNo Integer," +
                    " PostsNo Integer," +
                    " Used Integer  Default 0,"+
                    " CreationDate TIMESTAMP)",

            " Create Table if not Exists Posts ( " +
                    " FIPK Integer," +
                    " MPK Integer," +
                    " MiniLink Text," +
                    " MID Text," +
                    " OrderID Integer,"+
                    " PicUrl Text,"+
                    " Status Integer DEFAULT 0," +
                    " StatJobID Integer," +
                    " PostType Integer, " + // 1 Image - 2 Video
                    " CreationDate TIMESTAMP," +
                    " UNIQUE(MPK) ) ",

            " Create Table if not Exists Posts_Info ( " +
                    " FMPK Integer," +
                    " StatJobID Integer," +
                    " LikeNo Integer," +
                    " CommentNo Integer," +
                    " ViewNo Integer," +
                    " CreationDate TIMESTAMP," +
                    " UNIQUE(StatJobID, FMPK) ON CONFLICT REPLACE ) ",

            " Create Table if not Exists Statistics_Orders ( " +
                    " StatOrderID Integer," +
                    " FIPK Integer," +
                    " IPK Integer," +
                    " Username Text," +
                    " UserInfo Text," +
                    " ExecAgents Text, " +
                    " BoostSpeed Integer DEFAULT 0, " +
                    " stopFollowersTracking Integer, " +
                    " stopPostTracking Integer, " +
                    " autoRefresh Integer DEFAULT 1, " +
                    " intervalInSeconds Integer, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ",

            " Create Table if not Exists Statistics_Jobs ( " +
                    " StatJobID Integer," +
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
                    " CreationDate TIMESTAMP) ",

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
                    " CreationDate TIMESTAMP)",

            " Create Table if not Exists Rel_Following ( " +
                    " FIPK Integer," +
                    " FollowingIPK Integer," +
                    " Status Integer DEFAULT 0," + // 1 means updated
                    " StatJobID Integer, " +
                    " CreationDate TIMESTAMP)",

            " Create Table if not Exists Rel_Like ( " +
                    " FMPK Integer," +
                    " LikerIPK Integer," +
                    " Status Integer DEFAULT 0," + // 1 means updated
                    " StatJobID Integer, " +
                    " CreationDate TIMESTAMP) ",

            " Create Table if not Exists Rel_Comment ( " +
                    " CPK Integer," +
                    " FMPK Integer," +
                    " CommenterIPK Integer," +
                    " CommentText Text," +
                    " Status Integer," +
                    " StatJobID Integer, " +
                    " CreationDate TIMESTAMP) ",

            " Create Table if not Exists Engagement ( " +
                    " FIPK Integer," +
                    " EngageIPK Integer," +
                    " LikeNo Integer DEFAULT 0," +
                    " CommentNo Integer DEFAULT 0," +
                    " StatJobID Integer, " +
                    " ChangeDate TIMESTAMP) ",

    };


    static final public String[] indexesQueries = new String[]
            {
                    "    CREATE INDEX IF NOT EXISTS Account_Info_ID_Index ON Account_Info(ID);\n" ,
                    "    CREATE INDEX IF NOT EXISTS Account_Info_FIPK_Index ON Account_Info(FIPK);\n" ,

                    "    CREATE INDEX IF NOT EXISTS Statistics_Orders_StatOrderID_Index ON Statistics_Orders(StatOrderID);\n" ,

                    "    CREATE INDEX IF NOT EXISTS Posts_FIPK_Index ON Posts(FIPK);\n" ,
                    "    CREATE INDEX IF NOT EXISTS Posts_OrderID_Index ON Posts(OrderID);\n" ,

                    "    CREATE INDEX IF NOT EXISTS Engagement_FIPK_Index ON Engagement(FIPK);\n" ,
                    "    CREATE INDEX IF NOT EXISTS Engagement_EngageIPK_Index ON Engagement(EngageIPK);\n" ,
                    "    CREATE INDEX IF NOT EXISTS Engagement_LikeNo_Index ON Engagement(LikeNo);\n" ,
                    "    CREATE INDEX IF NOT EXISTS Engagement_CommentNo_Index ON Engagement(CommentNo);\n" ,
                    "    CREATE INDEX IF NOT EXISTS Engagement_StatJobID_Index ON Engagement(StatJobID);\n" ,

                    "    CREATE INDEX IF NOT EXISTS Statistics_Jobs_StatJobID_Index ON Statistics_Jobs(StatJobID);\n" ,
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
                    "    CREATE INDEX IF NOT EXISTS Rel_Comment_CreationDate_Index ON Rel_Comment(CreationDate);\n"
            };

}

