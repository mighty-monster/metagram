package vp.tools.io;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;
import static vp.metagram.general.variables.AndroidClientDBIV;


public class iDBManager extends SQLiteOpenHelper
{

    String[] buildingQueries;
    String databaseName;
    Context context;


    public iDBManager(Context _context, String _dataBaseName, int _dataBaseVersion, String[] _buildingQueries)
    {
        super(_context, _dataBaseName, null, _dataBaseVersion);
        buildingQueries = _buildingQueries;
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
    { if ( oldVersion < newVersion ) {} }


    public MatrixCursor selectQuery(String _Query) throws GeneralSecurityException, IOException
    {

        SQLiteDatabase db;
        db = getWritableDatabase(AndroidClientDBIV);

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

    public void execQuery(String _Query) throws IOException
    {

        SQLiteDatabase db;
        db = getWritableDatabase(AndroidClientDBIV);
        db.execSQL(_Query);
    }

    public String getDatabasePath()
    {
        return context.getDatabasePath(databaseName).getPath();
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

    public void shrinkDatabase() throws IOException
    {
        String sqlText = "VACUUM;";
        execQuery(sqlText);
    }


}

