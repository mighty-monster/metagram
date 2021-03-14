package vp.igpapi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static vp.igpapi.IGWAException.invalid_comment_length;
import static vp.igpapi.IGWAException.not_logged_in;
import static vp.igpapi.IGWAException.username_password_needed;
import static vp.igpapi.IGWAException.wrong_password;
import static vp.igpapi.IGWAException.wrong_username;

/**
 * The main class that act as a http client of Instagram Web API
 * It can be used both anonymously or via login
 *
 * In order to use it for login, we need to first implement IGWAStorage interface to
 * handle saving and loading of the class internal states including session_id after login
 *
 * If we want an anonymous client we can just instantiate the class without any input
 */
public class IGWA extends IGWABase
{


    /**
     * This constructor will instantiate an anonymous client
     */
    public IGWA() throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        super();
    }


    /**
     * This constructor will an instantiate a login client, but will use session data saved
     * previously, so the implemented IGWAStorage class have to return a valid data
     * for the username, otherwise the constructor will throw IGWAException
     */
    public IGWA(String username, IGWAStorage saviour) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        super(username, saviour);
    }


    /**
     * This constructor will an instantiate a login client, using provided username and password
     * and after successful login will save the session data using provided subclass of IGWAStorage
     */
    public IGWA(String username, String password, IGWAStorage saviour) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        super(saviour);

        this.username = username;

        login(username, password);
    }


    /**
     * This method will make sure that rollout_hash has been initialized
     */
    private void _init_rollout_hash() throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        if (rollout_hash == null || rollout_hash.equals(""))
        {
            init();
        }
    }


    /**
     * All calls implemented in this subclass of IGWABase needs the client to be logged in,
     * so an IGWAException will be thrown otherwise, it was better to implement this by
     * dynamic proxies instead of calling this method in all the calls, but dynamic proxy
     * would add unnecessary complexity to the code, so this part can be implemented better
     */
    private void _check_if_logged_in() throws IGWAException
    {
        if (!isLogedin) {throw new IGWAException(not_logged_in, "You need to login first");}
    }


    /**
     * Error handling when http call was successful but, something went wrong in application
     * layer (eg. failed login, bad request, etc)
     * TODO - Implement application layer error handling
     */
    private void _check_post_respond(JSONObject respond) throws JSONException, IGWAException
    {
        String status = respond.getString("status");

        if (!status.equals("ok"))
        {
            throw new IGWAException(0,"");
        }
    }


    /**
     * Login using provided username and password and if successful save the session
     * Currently this is the only supported login methods, other functionality might be
     * add eg. handling checkpoints
     *
     * Also we can use a webview in order to login and use the cookies and headers to provide
     * load function of IGWAStorage, that way the library will use logedin sessionid, and we can
     * easily handle all kind of login
     *
     * @param username
     * @param password
     */
    public void login(String username, String password) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        if (username == null || password == null || username.equals("") || password.equals(""))
        {
            throw new IGWAException(username_password_needed, "Both username and password are needed to login");
        }

        long current_timestamp = (System.currentTimeMillis() / 1000);

        Map<String, String> params = new HashMap<>();
        params.put("username",username);
        params.put("enc_password",String.format(new Locale("en","EN"), "#PWD_INSTAGRAM_BROWSER:0:%d:%s",current_timestamp,password));
        params.put("queryParams","{}");

        _init_rollout_hash();

        String respond = _make_request(LOGIN_URL, params, null, null, POST);

        if (respond != null)
        {
            JSONObject result = new JSONObject(respond);
            _check_post_respond(result);

            boolean authenticated = result.getBoolean("authenticated");

            boolean user = result.getBoolean("user");

            if (authenticated)
            {
                isLogedin = true;

                save();
            }
            else if (user)
            {
                throw new IGWAException(wrong_password, "Wrong password");
            }
            else
            {
                throw new IGWAException(wrong_username, "Wrong username");
            }
        }

    }


    /**
     * Get user's followings, Login required
     *
     * @param user_id long integer used to identify each user in Instagram
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject user_followings(long user_id, String end_cursor) throws IGWAException, JSONException, IOException, NoSuchAlgorithmException
    {
        _check_if_logged_in();

        JSONObject variables = new JSONObject();
        variables.put("id", user_id);
        variables.put("first", following_count);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        //query.put("query_hash","c56ee0ae1f89cdbd1c89e2bc6b8f3d18");
        query.put("query_hash","d04b0a864b4b54837c0d870b0e77e076");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);

    }


    /**
     * Get user's followers, Login required
     *
     * @param user_id long integer used to identify each user in Instagram
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject user_followers(long user_id, String end_cursor) throws IGWAException, JSONException, IOException, NoSuchAlgorithmException
    {
        _check_if_logged_in();

        JSONObject variables = new JSONObject();
        variables.put("id", user_id);
        variables.put("first", follower_count);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        //query.put("query_hash","7dd9a7e2160524fd85f50317462cff9f");
        query.put("query_hash","c76146de99bb02f6415203be841dd25a");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        JSONObject result = new JSONObject(respond);
        _check_post_respond(result);

        return result;
    }


    /**
     * Like an media (Post), Login required
     *
     * @param media_id long integer used to identify each media in Instagram, this is not the
     *                 short_code, each media has both media_id and short_code
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject post_like(long media_id) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        _check_if_logged_in();

        String url = String.format(LIKE_MEDIA_URL, media_id);

        String respond = proxy._make_request(url, new HashMap<String, String>(), null,null, POST);

        JSONObject result = new JSONObject(respond);

        _check_post_respond(result);

        return result;

    }


    /**
     * Unlike an media (Post), Login required
     *
     * @param media_id long integer used to identify each media in Instagram, this is not the
     *                 short_code, each media has both media_id and short_code
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject delete_like(long media_id) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        _check_if_logged_in();

        String url = String.format(UNLIKE_MEDIA_URL, media_id);

        String respond = proxy._make_request(url, new HashMap<String, String>(), null,null, POST);

        JSONObject result = new JSONObject(respond);

        _check_post_respond(result);

        return result;

    }


    /**
     * Follow a user, Login required
     *
     * @param user_id long integer used to identify each user in Instagram
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject follow(long user_id) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        _check_if_logged_in();

        String url = String.format(FOLLOW_URL, user_id);

        String respond = proxy._make_request(url, new HashMap<String, String>(), null,null, POST);

        JSONObject result = new JSONObject(respond);

        _check_post_respond(result);

        return result;
    }


    /**
     * Unfollow a user, Login required
     *
     * @param user_id long integer used to identify each user in Instagram
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject unfollow(long user_id) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        _check_if_logged_in();

        String url = String.format(Locale.ENGLISH, UNFOLLOW_URL, user_id);

        String respond = proxy._make_request(url, new HashMap<String, String>(), null,null, POST);

        JSONObject result = new JSONObject(respond);

        _check_post_respond(result);

        return result;
    }


    /**
     * Post a new comment, Login required
     *
     * @param media_id long integer used to identify each media in Instagram, this is not the
     *                 short_code, each media has both media_id and short_code
     * @param comment the actual text of the comment, the length of the text does not exceeds
     *                300, All the letters can not be capital, can not have more than 4 hashtags
     *                and can not have more than one url
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject post_comment(long media_id, String comment) throws IGWAException, IOException, NoSuchAlgorithmException, JSONException
    {
        _check_if_logged_in();

        String url = String.format(Locale.ENGLISH, ADD_COMMENT_URL, media_id);

        if (comment.length() > 300)
        {
            throw new IGWAException(invalid_comment_length, "The total length of the comment cannot exceed 300 characters");
        }


        Map<String, String> params = new HashMap<>();
        params.put("comment_text",comment);

        String respond = proxy._make_request(url, params, null,null, POST);

        JSONObject result = new JSONObject(respond);

        _check_post_respond(result);

        return result;
    }


    /**
     * Delete a comment, Login required
     *
     * @param media_id long integer used to identify each media in Instagram, this is not the
     *                 short_code, each media has both media_id and short_code
     * @param comment_id long integer used to identify each comment in Instagram
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject delete_comment(long media_id, long comment_id) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        _check_if_logged_in();

        String url = String.format(DELETE_COMMENT_URL, media_id, comment_id);

        String respond = proxy._make_request(url, new HashMap<String, String>(), null,null, POST);

        JSONObject result = new JSONObject(respond);

        _check_post_respond(result);

        return result;
    }


    /**
     * Delete a media, Login required
     *
     * @param media_id long integer used to identify each media in Instagram, this is not the
     *                 short_code, each media has both media_id and short_code
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject delete_media(long media_id) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        _check_if_logged_in();

        String url = String.format(DELETE_MEDIA_URL, media_id);

        String respond = proxy._make_request(url, new HashMap<String, String>(), null,null, POST);

        JSONObject result = new JSONObject(respond);

        _check_post_respond(result);

        return result;
    }


    /**
     * Get logged-in user's timeline feed
     *
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject timeline_feed(String end_cursor) throws IGWAException, IOException, NoSuchAlgorithmException, JSONException
    {
        _check_if_logged_in();

        int fetch_comment_count = 4;
        int fetch_like = 10;
        boolean has_stories = false;

        JSONObject variables = new JSONObject();
        variables.put("fetch_media_item_count", timefeed_count);
        variables.put("fetch_comment_count", fetch_comment_count);
        variables.put("fetch_like", fetch_like);
        variables.put("has_stories", has_stories);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("fetch_media_item_cursor", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","3f01472fb28fb8aca9ad9dbc9d4578ff");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Get a logged-in users reels tray
     *
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject reels_tray() throws IGWAException, IOException, NoSuchAlgorithmException, JSONException
    {
        _check_if_logged_in();

        JSONObject variables = new JSONObject();
        variables.put("only_stories", false);

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","60b755363b5c230111347a7a4e242001");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Get the stories feed for the specified user IDs
     *
     * @param reels_ids An array of long integers containing user_ids of users that we want to
     *                  receive stories feed
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     * TODO - Investigate further -> what is reels_id ... and why user_ids can be passed?
     */
    public JSONObject reels_feed(Long[] reels_ids) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        _check_if_logged_in();

        String[] str_reels_ids = new String[reels_ids.length];

        for (int i=0 ; i <reels_ids.length; i++)
        {
            str_reels_ids[i]=Long.toString(reels_ids[i]);
        }

        return _story_feed(str_reels_ids, new String[] {}, new String[] {});
    }


    /**
     * Get the highlights for the specified user ID, Login required
     *
     * @param user_id long integer used to identify each user in Instagram
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject highlight_reels(long user_id) throws IGWAException, JSONException, IOException, NoSuchAlgorithmException
    {
        _check_if_logged_in();

        JSONObject variables = new JSONObject();
        variables.put("user_id", user_id);
        variables.put("include_chaining", false);
        variables.put("include_reel", true);
        variables.put("include_suggested_users", false);
        variables.put("include_logged_out_extras", false);
        variables.put("include_highlight_reels", true);


        Map<String, String> query = new HashMap<>();
        query.put("query_hash","7c16654f22c819fb63d1183034a5162f");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Get the IGTV Feed for the specified user ID, Login required
     *
     * @param user_id long integer used to identify each user in Instagram
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject IGTV_feed(long user_id, String end_cursor) throws IGWAException, JSONException, IOException, NoSuchAlgorithmException
    {
        _check_if_logged_in();

        JSONObject variables = new JSONObject();
        variables.put("id", user_id);
        variables.put("first", IGTV_feed_count);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }


        Map<String, String> query = new HashMap<>();
        query.put("query_hash","bc78b344a68ed16dd5d7f264681c4c76");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


}
