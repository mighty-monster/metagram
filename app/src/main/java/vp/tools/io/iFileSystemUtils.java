package vp.tools.io;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;


import static vp.metagram.general.variables.logger;


public class iFileSystemUtils
{

    public static void copy(String _source, String _destination) throws IOException
    {
        File file = new File(_source);
        if (!file.exists())
        {throw new IOException("Source do not exists.");}

        if (file.isFile())
        {copyFile(_source, _destination);}
        else if (file.isDirectory())
        {copyDir(_source, _destination);}

    }

    public static void move(String _source, String _destination) throws IOException
    {
        File file = new File(_source);
        if (!file.exists())
        {throw new IOException("Source do not exists.");}

        if (file.isFile())
        {moveFile(_source, _destination);}
        else if (file.isDirectory())
        {moveDir(_source, _destination);}
    }

    public static void delete(String _path) throws IOException
    {
        File file = new File(_path);
        if (file.exists() && file.isFile())
        {deleteFile(_path);}
        else if (file.exists() && file.isDirectory())
        {deleteDir(_path);}
    }

    public static void copyDir(String _source, String _destination) throws IOException
    {
        File sourceFile = new File(_source);
        if (sourceFile.isFile())
        {throw new IOException("Use copyFile function instead.");}
        if (!sourceFile.exists())
        {throw new IOException("Source do not exists");}

        File destinationFile = new File(_destination);
        if (!destinationFile.exists())
        {destinationFile.mkdir();}

        String[] children = sourceFile.list();

        for (String child : children)
        {
            File childFile = new File(child);
            if (childFile.isFile())
            {copyFile(_source, _destination);}
            else if (childFile.isDirectory())
            {copyDir(_source, _destination);}
        }
    }

    public static void copyFile(String _source, String _destination) throws IOException
    {
        File file = new File(_source);
        if (file.isDirectory())
        {throw new IOException("Use copyDir function instead.");}
        if (!file.exists())
        {throw new IOException("Source do not exists");}

        InputStream in = new FileInputStream(_source);
        OutputStream out = new FileOutputStream(_destination);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {out.write(buf, 0, len);}
        in.close();
        out.close();
    }

    public static void moveDir(String _source, String _destination) throws IOException
    {
        copyDir(_source, _destination);
        deleteDir(_source);
    }

    public static void moveFile(String _source, String _destination) throws IOException
    {
        copyFile(_source, _destination);
        deleteFile(_source);
    }

    public static void deleteDir(String _path) throws IOException
    {
        File file = new File(_path);
        if (file.exists() && file.isDirectory())
        {
            String[] children = file.list();
            for (String child : children)
            {
                File childFile = new File(_path + "/" + child);
                if (childFile.isFile())
                {deleteFile(_path + "/"  + child);}
                else if (childFile.isDirectory())
                {deleteDir(_path + "/"  + child);}
            }

            file.delete();
        }
        else if (file.isFile())
        {throw new IOException("Use deleteFile function instead.");}
    }

    public static void deleteFile(String _path) throws IOException
    {
        File file = new File(_path);
        if (file.exists() && file.isFile())
        {file.delete();}
        else if (file.isDirectory())
        {throw new IOException("Use deleteDir function instead.");}
    }

    public static String GetPrivateDir(Context context)
    {
        PackageManager m = context.getPackageManager();
        String packageName = context.getPackageName();
        PackageInfo packageInfo = null;
        try
        {
            packageInfo = m.getPackageInfo(packageName, 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            logger.logError("GetApplicationRootDir()",
                    "Error while getting app root directory.\n", e);
        }
        String appDir = packageInfo.applicationInfo.dataDir;
        return appDir;
    }

    public static String GetExternalDir(Context context)
    {
        return context.getExternalFilesDir(null).getAbsolutePath() + "/";
    }

    public static String GetTempDir(Context context)
    {
        String tempDir = GetExternalDir(context) + "temp/";
        File SDDir = new File(tempDir);
        if (!SDDir.exists()) { SDDir.mkdirs();}
        return tempDir;
    }

    public static String GetReportImportDir(Context context)
    {
        String tempDir = GetExternalDir(context) + "report_import/";
        File SDDir = new File(tempDir);
        if (!SDDir.exists()) { SDDir.mkdirs();}
        return tempDir;
    }

    public static String GetReportExportDir(Context context)
    {
        String tempDir = GetExternalDir(context) + "report_import/";
        File SDDir = new File(tempDir);
        if (!SDDir.exists()) { SDDir.mkdirs();}
        return tempDir;
    }

    public static String GetDownloadDir(Context context)
    {
        String tempDir = GetExternalDir(context) + "download/";
        File SDDir = new File(tempDir);
        if (!SDDir.exists())
        {
            if (!SDDir.mkdirs())
            {
                Log.d("FileSystemError", String.format(Locale.ENGLISH, "Cloud not create directory: %s", tempDir));
            }
        }
        return tempDir;
    }

    public static String getPathFromURI(Uri contentUri)
    {
        File file = new File(contentUri.getPath());
        return file.getAbsolutePath();
    }

    public static long getDirectorySize(File dir)
    {
        long size = 0;
        File[] fileList = dir.listFiles();
        if (fileList != null)
        {
            for (File file : fileList)
            {
                if (file.isFile())
                    size += file.length();
                else
                    size += getDirectorySize(file);
            }
        }
        return size;
    }

}


