package vp.tools.io;

import android.content.Context;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class iDBAutoLike extends iDBWithCipher
{
    private static int version = 1001;

    private static String[] BuildQueries = {

            " Create Table if not Exists Configuration ( " +
                    " ID Integer Primary Key AutoIncrement, " +
                    " Name Text, " +
                    " Value Text, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " ChangeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            " CREATE TABLE if not Exists Like_Their_Posts(" +
                    " ID Integer Primary Key AutoIncrement, " +
                    " IPK Integer, " +
                    " Username Text, " +
                    " PicURL Text, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(IPK) ) ",

            " CREATE TABLE if not Exists Like_Their_Comments(" +
                    " ID Integer Primary Key AutoIncrement, " +
                    " IPK Integer, " +
                    " Username Text, " +
                    " PicURL Text, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(IPK) ) ",

            " CREATE TABLE if not Exists Liked_Posts(" +
                    " ID Integer Primary Key AutoIncrement, " +
                    " FIPK Integer, " +
                    " MPK Integer, " +
                    " URL Text, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(MPK) )",

            " CREATE TABLE if not Exists Liked_Comments(" +
                    " ID Integer Primary Key AutoIncrement, " +
                    " CPK Integer, " +
                    " FMPK Integer, " +
                    " Comment Text, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " UNIQUE(CPK) )"
    };


    public String indexesQueries[] =
            {
                    "\tCREATE INDEX IF NOT EXISTS Like_Their_Posts_IPK_Index ON Like_Their_Posts(IPK);\n" ,
                    "\tCREATE INDEX IF NOT EXISTS Like_Their_Posts_Username_Index ON Like_Their_Posts(Username);\n" ,

                    "\tCREATE INDEX IF NOT EXISTS Like_Their_Comments_IPK_Index ON Like_Their_Comments(IPK);\n" ,
                    "\tCREATE INDEX IF NOT EXISTS Like_Their_Comments_Username_Index ON Like_Their_Comments(Username);\n" ,

                    "\tCREATE INDEX IF NOT EXISTS Liked_Posts_FIPK_Index ON Liked_Posts(FIPK);\n" ,
                    "\tCREATE INDEX IF NOT EXISTS Liked_Posts_MPK_Index ON Liked_Posts(MPK);\n" ,

                    "\tCREATE INDEX IF NOT EXISTS Liked_Comments_FMPK_Index ON Liked_Comments(FMPK);\n" ,
                    "\tCREATE INDEX IF NOT EXISTS Liked_Comments_CPK_Index ON Liked_Comments(CPK);\n" ,
            };


    public iDBAutoLike(Context _context, String _dataBaseName) throws GeneralSecurityException, IOException
    {
        super(_context, _dataBaseName, version, BuildQueries);

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

    }
}
