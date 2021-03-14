package vp.tools.io;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class ExportContext extends ContextWrapper
{

    String basePath = "";

    public ExportContext(Context base, String basePath)
    {
        super(base);
        this.basePath = basePath;
    }



    @Override
    public File getDatabasePath(String name)
    {
        String dbfile = basePath + name;
        if ( !dbfile.endsWith(".mef") )
        {
            dbfile += ".mef";
        }

        File result = new File(dbfile);

        if ( !result.getParentFile().exists() )
        {
            result.getParentFile().mkdirs();
        }


        return result;
    }


    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler)
    {
        return openOrCreateDatabase(name, mode, factory);
    }


}
