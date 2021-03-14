package vp.metagram.utils.instagram.types;

import android.database.MatrixCursor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;



import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.idleStatus;
import static vp.metagram.general.variables.updatedStatus;

/**
 * Created by arash on 2/26/18.
 */


public class User
{
    public long IPK;
    public String username;
    public String fullname;
    public boolean isPrivate;
    public String picURL;


    public void insertUserToDB() throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, " Insert Or Ignore Into Others(IPK,Username,PictureURL) Values (%d, '%s', '%s')", IPK, username, picURL);

        dbMetagram.execQuery(sqlText);
    }



    public void addToFollowersOfByIPK(long FIPK, int StatJobID) throws IOException, GeneralSecurityException
    {
        insertUserToDB();

        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No From Rel_Follower Where FIPK = %d and FollowerIPK = %d ", FIPK, IPK);
        MatrixCursor qResult = dbMetagram.selectQuery(sqlText);

        qResult.moveToFirst();
        int No = qResult.getInt(qResult.getColumnIndex("No"));

        if ( No > 0 )
        {
            sqlText = String.format(Locale.ENGLISH, "Update Rel_Follower Set Status = %d, CreationDate =  datetime('now') Where FIPK = %d and FollowerIPK = %d", updatedStatus, FIPK, IPK);
            dbMetagram.execQuery(sqlText);
        }
        else
        {
            sqlText = String.format(Locale.ENGLISH, "Insert Or Ignore Into Rel_Follower(FIPK, FollowerIPK, Status, StatJobID) Values (%d, %d, %d, %d) ", FIPK, IPK, idleStatus, StatJobID);
            dbMetagram.execQuery(sqlText);
        }

    }

    public void addToFollowingsOfByIPK(long FIPK, int StatJobID) throws IOException, GeneralSecurityException
    {
        insertUserToDB();

        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No From Rel_Following Where FIPK = %d and FollowingIPK = %d ", FIPK, IPK);
        MatrixCursor qResult = dbMetagram.selectQuery(sqlText);

        qResult.moveToFirst();
        int No = qResult.getInt(qResult.getColumnIndex("No"));

        if ( No > 0 )
        {
            sqlText = String.format(Locale.ENGLISH, "Update Rel_Following Set Status = %d, CreationDate =  datetime('now') Where FIPK = %d and FollowingIPK = %d", updatedStatus, FIPK, IPK);
            dbMetagram.execQuery(sqlText);
        }
        else
        {
            sqlText = String.format(Locale.ENGLISH, "Insert Or Ignore Into Rel_Following(FIPK, FollowingIPK, Status, StatJobID) Values (%d, %d, %d, %d) ", FIPK, IPK, idleStatus, StatJobID);
            dbMetagram.execQuery(sqlText);
        }
    }

    public void addToLikersOfByMPK(long MPK, int StatJobID) throws IOException, GeneralSecurityException
    {
        insertUserToDB();

        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No From Rel_Like Where FMPK = %d and LikerIPK = %d ", MPK, IPK);
        MatrixCursor qResult = dbMetagram.selectQuery(sqlText);

        qResult.moveToFirst();
        int No = qResult.getInt(qResult.getColumnIndex("No"));

        if ( No > 0 )
        {
            sqlText = String.format(Locale.ENGLISH, "Update Rel_Like Set Status = %d, CreationDate =  datetime('now') Where FMPK = %d and LikerIPK = %d", updatedStatus, MPK, IPK);
            dbMetagram.execQuery(sqlText);
        }
        else
        {
            sqlText = String.format(Locale.ENGLISH, "Insert Or Ignore Into Rel_Like(FMPK, LikerIPK, Status, StatJobID) Values (%d, %d, %d, %d) ", MPK, IPK, idleStatus, StatJobID);
            dbMetagram.execQuery(sqlText);
        }
    }

    public void updatePicURL() throws IOException
    {
        String sqlText = String.format(Locale.ENGLISH, "Update Others Set PictureURL = '%s' Where IPK = %d ", picURL, IPK);

        dbMetagram.execQuery(sqlText);
    }

}
