package vp.igwa;

import android.database.MatrixCursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import vp.metagram.utils.instagram.InstagramAgent;
import vp.igpapi.IGWAStorage;

import static vp.metagram.general.variables.dbMetagram;

public class UserSaviour implements IGWAStorage
{
    private InstagramAgent agent;

    public UserSaviour(InstagramAgent agent)
    {
        this.agent = agent;
    }

    @Override
    public void save(String username, String content) throws IOException
    {
        try
        {
            JSONObject jsonContent = new JSONObject(content);

            jsonContent.put("userID", agent.userID);
            jsonContent.put("pictureURL", agent.pictureURL);

            content = jsonContent.toString();

            int count;
            MatrixCursor result = dbMetagram.selectQuery(String.format(Locale.ENGLISH, "Select Count(*) as count from Accounts Where IPK = %d ", agent.userID));
            result.moveToFirst();
            count = result.getInt(result.getColumnIndex("count"));

            int ready;
            int registered;
            int enabled;

            ready = agent.isReady ? 1 : 0;
            registered = agent.isRegistered ? 1 : 0;
            enabled = agent.isEnabled ? 1 : 0;

            if (count > 0)
            {

                dbMetagram.execQuery(String.format(Locale.ENGLISH, "Update Accounts Set Content = '%s', Username = '%s', Enabled = %d, Ready = %d, Registered = %d,  ChangeDate = CURRENT_TIMESTAMP Where IPK = %d",
                        dbMetagram.AESCipher.encryptStringToHex(content),
                        username,
                        enabled,
                        ready,
                        registered,
                        agent.userID));


            } else
            {
                dbMetagram.execQuery(String.format(Locale.ENGLISH, "Insert Into Accounts( IPK, Content, Enabled, Ready, Registered, Username ) Values ( '%d', '%s', %d, %d, %d, '%s')",
                        agent.userID,
                        dbMetagram.AESCipher.encryptStringToHex(content),
                        enabled,
                        ready,
                        registered,
                        username));
            }
        }
        catch (GeneralSecurityException | JSONException  e )
        {
            e.printStackTrace();
        }

    }

    @Override
    public String load(String username) throws IOException
    {
        String result = "";
        try
        {
            String sqlText = String.format(Locale.ENGLISH, "Select Content, Ready, Registered, Enabled, Username from Accounts Where Username = '%s'", username);
            MatrixCursor queryResult = dbMetagram.selectQuery(sqlText);

            if ( queryResult.moveToFirst() )
            {
                result = queryResult.getString(queryResult.getColumnIndex("Content"));
                result = dbMetagram.AESCipher.decryptFromHexToString(result);

                int ready = queryResult.getInt(queryResult.getColumnIndex("Ready"));
                int registered = queryResult.getInt(queryResult.getColumnIndex("Registered"));
                int enabled = queryResult.getInt(queryResult.getColumnIndex("Enabled"));

                if ( ready == 1 ) {agent.isReady = true;}
                else {agent.isReady = false;}
                if ( registered == 1 ) {agent.isRegistered = true;}
                else {agent.isRegistered = false;}
                if ( enabled == 1 ) {agent.isEnabled = true;}
                else {agent.isEnabled = false;}

            }

            JSONObject jsonContent = new JSONObject(result);

            agent.username = username;
            agent.userID = jsonContent.getLong("userID");
            agent.pictureURL = jsonContent.getString("pictureURL");

        }
        catch (GeneralSecurityException | JSONException e)
        {
            e.printStackTrace();
        }

        return result;

    }
}
