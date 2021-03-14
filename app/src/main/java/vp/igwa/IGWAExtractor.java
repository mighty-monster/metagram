package vp.igwa;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import vp.igpapi.IGWA;
import vp.igpapi.IGWAException;
import vp.igpapi.IGWADigest;
import vp.igpapi.IGWAStorage;

public class IGWAExtractor extends IGWA
{

    public long user_id;

    public IGWAExtractor() throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        super();
    }

    public IGWAExtractor(String username, IGWAStorage saviour) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        super(username, saviour);

        for (String cookie : cookies)
        {
            if (cookie.contains("ds_user_id"))
            {

                String value = cookie.substring(cookie.indexOf("ds_user_id") + "ds_user_id".length() +1);

                user_id = Long.parseLong(value);

                break;
            }
        }
    }

    public IGWAExtractor(String username, String password, IGWAStorage saviour) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        super(username, password,  saviour);

    }


    public void set_username(String username)
    {
        this.username = username;
    }


    public String get_username()
    {
        return username;
    }


    public String get_cookie_string() {return cookieString;}

    public <T> T user_info(String username, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(user_info(username));
    }

    public <T> T user_feed(long user_id,  String end_cursor, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(user_feed(user_id, end_cursor));
    }

    public <T> T media_info(String short_code, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(media_info(short_code));
    }

    public <T> T media_comments(String short_code,  String end_cursor, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(media_comments(short_code, end_cursor));
    }

    public <T> T media_likers(String short_code,  String end_cursor, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(media_likers(short_code, end_cursor));
    }

    public <T> T search(String query_text, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(search(query_text));
    }

    public <T> T tag_feed(String tag, String end_cursor, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(tag_feed(tag,end_cursor));
    }

    public <T> T user_followings(long user_id, String end_cursor, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(user_followings(user_id, end_cursor));
    }

    public <T> T user_followers(long user_id,  String end_cursor, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(user_followers(user_id, end_cursor));
    }

    public <T> T post_comment(long media_id,  String comment, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(post_comment(media_id, comment));
    }

    public <T> T reels_feed(Long[] reels_ids, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(reels_feed(reels_ids));
    }

    public <T> T highlight_reels(long user_id, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(highlight_reels(user_id));
    }

    public <T> T highlight_reel_media(Long[] highlight_reel_ids, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(highlight_reel_media(highlight_reel_ids));
    }

    public <T> T IGTV_feed(long user_id, String end_cursor, IGWADigest result) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return result.digest(IGTV_feed(user_id, end_cursor));
    }
}


