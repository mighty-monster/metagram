package vp.tools.io;

import android.content.Context;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class iDBUnfollower extends iDBWithCipher
{
    private static int version = 1001;

    private static String[] BuildQueries = {

            " Create Table if not Exists Configuration ( " +
                    " ID Integer Primary Key AutoIncrement, " +
                    " Name Text, " +
                    " Value Text, " +
                    " CreationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " ChangeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            " CREATE TABLE if not Exists followings(" +
                    "\tID Integer Primary Key AutoIncrement, " +
                    "\tIPK Integer,\n" +
                    "\tUsername Text,\n" +
                    "\tFullName Text,\n" +
                    "\tStatus Integer DEFAULT 0,\n" +
                    "\tprofilePicURL Text,\n" +
                    "\tCreationDate DateTime DEFAULT CURRENT_TIMESTAMP,\n" +
                    "\tUNIQUE(IPK) )",

            " CREATE TABLE if not Exists white_list(" +
                    " ID Integer Primary Key AutoIncrement, " +
                    "\tIPK Integer,\n" +
                    "\tUsername Text,\n" +
                    "\tFullName Text,\n" +
                    "\tprofilePicURL Text,\n" +
                    "\tCreationDate DateTime DEFAULT CURRENT_TIMESTAMP,\n" +
                    "\tUNIQUE(IPK) )"

    };


    public String indexesQueries[] =
            {
                    "\tCREATE INDEX IF NOT EXISTS followings_IPK_Index ON followings(IPK);\n" ,
                    "\tCREATE INDEX IF NOT EXISTS followings_Username_Index ON followings(Username);\n",
                    "\tCREATE INDEX IF NOT EXISTS followings_Status_Index ON followings(Status);\n",

                    "\tCREATE INDEX IF NOT EXISTS white_list_IPK_Index ON white_list(IPK);\n" ,
                    "\tCREATE INDEX IF NOT EXISTS white_list_Username_Index ON white_list(Username);\n"
            };


    public iDBUnfollower(Context _context, String _dataBaseName) throws GeneralSecurityException, IOException
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
