package vp.metagram.utils.instagram.types;

import android.database.MatrixCursor;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;


import vp.igpapi.IGWADigest;

import static vp.metagram.general.functions.repeatSingleQuotes;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.idleStatus;
import static vp.metagram.general.variables.updatedStatus;

/**
 * Created by arash on 2/27/18.
 */


public class Comment implements Comparable<Comment>, IGWADigest
{
    public long CPK;

    public String message;
    public User commenter = new User();
    public int type;
    public long created_utc;
    public int noOfLikes;

    public void InsertCommentIntoDB(int StatJobID, long MPK) throws IOException, GeneralSecurityException
    {
        commenter.insertUserToDB();

        String sqlText = String.format(Locale.ENGLISH, "Select Count(*) as No From Rel_Comment Where FMPK = %d and CommenterIPK = %d and CPK = %d ", MPK, commenter.IPK, CPK);
        MatrixCursor qResult = dbMetagram.selectQuery(sqlText);

        qResult.moveToFirst();
        int No = qResult.getInt(qResult.getColumnIndex("No"));

        if ( No > 0 )
        {
            sqlText = String.format(Locale.ENGLISH, "Update Rel_Comment Set Status = %d, CreationDate =  datetime('now'), NoOfLikes = %d Where FMPK = %d and CommenterIPK = %d and CPK = %d ", updatedStatus,noOfLikes, MPK ,commenter.IPK, CPK);
            dbMetagram.execQuery(sqlText);
        }
        else
        {
            sqlText = String.format(Locale.ENGLISH, "Insert Into Rel_Comment(CPK, FMPK, CommenterIPK, Status, StatJobID, CommentText, Type, Created_UTC, NoOfLikes) Values (%d, %d, %d, %d, %d, '%s', %d, %d, %d) ", CPK, MPK, commenter.IPK, idleStatus, StatJobID , repeatSingleQuotes(message), type, created_utc,noOfLikes);
            dbMetagram.execQuery(sqlText);
        }
    }



    @Override
    public int compareTo(@NonNull Comment comment)
    {
        return Long.compare(this.created_utc,comment.created_utc);
    }

    @Override
    public Comment digest(JSONObject jsonObject) throws JSONException
    {
        CPK = Long.parseLong(jsonObject.getString("id"));
        message = jsonObject.getString("text");
        created_utc = Long.parseLong(jsonObject.getString("created_time"));

        JSONObject owner = jsonObject.getJSONObject("from");
        User user = new User();
        user.IPK = Long.parseLong(owner.getString("id"));
        user.username = owner.getString("username");
        user.picURL = owner.getString("profile_picture");

        commenter = user;

        return this;
    }
}
