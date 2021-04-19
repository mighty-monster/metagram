package vp.metagram.utils.instagram.types;

import android.database.MatrixCursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.igpapi.IGWADigest;

import static vp.metagram.general.functions.StringListToJSON;
import static vp.metagram.general.functions.prepareStringForSQL;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.idleStatus;
import static vp.metagram.general.variables.updatedStatus;

/**
 * Created by arash on 2/27/18.
 */


public class PostMedia implements IGWADigest
{
    public long MPK;
    public String ID;
    public int type;
    public int viewCount;
    public int likeCount;
    public int commentCount;
    public String miniLink;
    public String caption;
    public String picURL;
    public List<String> urls = new ArrayList<>();


    public void insertMediaToDB(long FIPK, int statsJobID, int orderID) throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH," Select Count(*) as No From Posts Where MPK = %d", MPK);

        MatrixCursor qResult = dbMetagram.selectQuery(sqlText);
        qResult.moveToFirst();

        int No = qResult.getInt(qResult.getColumnIndex("No"));

        String URLsJson = StringListToJSON(urls);

        if (No > 0)
        {
            sqlText = String.format(Locale.ENGLISH," Update Posts Set Status = %d, MiniLink = '%s', MID = '%s', URLs = '%s', PicURL = '%s' Where MPK = %d", updatedStatus,miniLink, ID,prepareStringForSQL(URLsJson), prepareStringForSQL(picURL), MPK);

            dbMetagram.execQuery(sqlText);
        }
        else
        {

            String picURL = getInstagramPostLink();

            sqlText = String.format(Locale.ENGLISH, " Insert Into Posts(FIPK, MPK, MiniLink, Status, StatJobID, PostType, MID, OrderID, Caption, URLs, PicURL)" +
                    " Values (%d, %d, '%s', '%d', %d, %d, '%s', %d, '%s', '%s', '%s')", FIPK, MPK, miniLink, idleStatus, statsJobID, type, ID, orderID, prepareStringForSQL(caption), prepareStringForSQL(URLsJson), prepareStringForSQL(picURL));

            dbMetagram.execQuery(sqlText);
        }
    }

    public void insertMediaInfo(int statsJobID) throws IOException, GeneralSecurityException
    {
        String sqlText = String.format(Locale.ENGLISH," Select Count(*) as No From Posts_Info Where FMPK = %d and StatJobID = %d ", MPK , statsJobID);

        MatrixCursor qResult = dbMetagram.selectQuery(sqlText);
        qResult.moveToFirst();

        int No = qResult.getInt(qResult.getColumnIndex("No"));

        if (No > 0)
        {
            sqlText = String.format(Locale.ENGLISH," Update Posts_Info set LikeNo = %d, CommentNo = %d, ViewNo = %d Where FMPK = %d and StatJobID = %d "
                    ,likeCount,commentCount,viewCount, MPK, statsJobID );

            dbMetagram.execQuery(sqlText);
        }
        else
        {
            sqlText = String.format(Locale.ENGLISH," Insert Or Ignore Into Posts_Info(FMPK, StatJobID, LikeNo, CommentNo, ViewNo)" +
                    " Values (%d,%d,%d,%d,%d)" ,MPK, statsJobID,likeCount,commentCount,viewCount );

            dbMetagram.execQuery(sqlText);
        }
    }

    public String getInstagramImageLink_Medium()
    {
        return String.format(Locale.ENGLISH, "https://www.instagram.com/p/%s/media/?size=m", miniLink);
    }

    public String getInstagramPostLink()
    {
        return String.format(Locale.ENGLISH, "https://www.instagram.com/p/%s/", miniLink);
    }


    @Override
    public PostMedia digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("graphql");

        JSONObject mediaJSON = jsonObject.getJSONObject("shortcode_media");

        MPK = mediaJSON.getLong("id");
        miniLink = mediaJSON.getString("shortcode");

        long user_id = mediaJSON.getJSONObject("owner").getLong("id");

        ID = String.format(Locale.ENGLISH, "%d_%d", MPK, user_id);


        picURL = mediaJSON.getString("display_url");

        likeCount = mediaJSON.getJSONObject("edge_media_preview_like").getInt("count");
        commentCount = mediaJSON.getJSONObject("edge_media_preview_comment").getInt("count");

        if (mediaJSON.getJSONObject("edge_media_to_caption").getJSONArray("edges").length() > 0)
        {
            caption = mediaJSON.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
        }
        else
        {
            caption = "";
        }

        String typename = mediaJSON.getString("__typename");

        if (typename.equals("GraphImage"))
        {
            type = 1;

            urls.add(picURL);
        }
        else if (typename.equals("GraphSidecar"))
        {
            type = 8;

            JSONArray children = mediaJSON.getJSONObject("edge_sidecar_to_children").getJSONArray("edges");

            for (int j=0; j < children.length(); j++)
            {
                JSONObject node = children.getJSONObject(j).getJSONObject("node");
                String type = node.getString("__typename");
                if (type.equals("GraphImage"))
                {
                    urls.add(node.getString("display_url"));
                }
                else if (type.equals("GraphVideo"))
                {
                    urls.add(node.getString("video_url"));
                }

            }

        }
        else if (typename.equals("GraphVideo"))
        {
            type = 2;

            viewCount = mediaJSON.getInt("video_view_count");

            urls.add(mediaJSON.getString("video_url"));
        }



        return this;
    }
}
