package vp.igpapi;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import static vp.igpapi.IGWAException.could_not_find_csrftoken;
import static vp.igpapi.IGWAException.could_not_find_session_data;
import static vp.igpapi.IGWAException.media_not_found;


/**
 * Provides needed functionality to send anonymous calls to Instagram Web API
 * Can be instantiated as an anonymous client, but it is recommended to use IGWA class that
 * is a subclass of IGWABase and provide login functionality along
 * with calls that need a sessionid (need the client to be logged in)
 */
public class IGWABase implements IGWABaseInterface
{


    // HTTP method types used in this library
    final static String GET = "GET";
    final static String POST = "POST";


    // End Points
    final static String INIT_URL = "https://www.instagram.com/";
    final static String API_URL = "https://www.instagram.com/query/";
    final static String GRAPHQL_API_URL = "https://www.instagram.com/graphql/query/";
    final static String USERNAME_URL = "https://www.instagram.com/%s/";
    final static String MEDIA_URL = "https://www.instagram.com/p/%s/";
    final static String SEARCH_URL = "https://www.instagram.com/web/search/topsearch/";
    final static String LOGIN_URL = "https://www.instagram.com/accounts/login/ajax/";
    final static String LIKE_MEDIA_URL = "https://www.instagram.com/web/likes/%d/like/";
    final static String UNLIKE_MEDIA_URL = "https://www.instagram.com/web/likes/%d/unlike/";
    final static String FOLLOW_URL = "https://www.instagram.com/web/friendships/%d/follow/";
    final static String UNFOLLOW_URL = "https://www.instagram.com/web/friendships/%d/unfollow/";
    final static String ADD_COMMENT_URL = "https://www.instagram.com/web/comments/%d/add/";
    final static String DELETE_COMMENT_URL = "https://www.instagram.com/web/comments/%d/delete/%d/";
    final static String DELETE_MEDIA_URL = "https://www.instagram.com/create/%d/delete/";


    // Instagram Variables
    protected String username;
    String user_agent = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0";
    String mobile_user_agent = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1";
    String init_csrftoken;
    String rhx_gis;
    String csrftoken;
    String rollout_hash;


    // Internal States
    CookieManager cookieManager = new CookieManager();
    protected List<String> cookies = new ArrayList<>();
    protected String cookieString;
    int call_counter = 0;
    public boolean isLogedin = false;
    public boolean initialized = false;


    // Settings
    int timeout = 8000;
    int counterLimit = 50;
    int user_feed_count = 24;
    int comment_count = 40;
    int like_count = 40;
    int tag_feed_count = 24;
    int location_feed_count = 16;
    int following_count = 40;
    int follower_count = 40;
    int timefeed_count = 12;
    int IGTV_feed_count = 40;


   /**
    * This dynamic proxy is meant to provide further control over http calls,
    * Currently it is used to save the internal state, every @counterLimit calls
    */
    IGWABaseInterface proxy;
    IGWAInvocationHandler invocationHandler;


    /**
     * Saviour class is an instance of Implemented IGWAStorage interface
     * this class is used to save and load session data after login
     * So you have to implement this interface and send it to the constructor
     */
    private IGWAStorage saviour;


    /**
     * This constructor will be used by IGWA class to instantiate anonymous clients
     * It can be used directly, in order to prevent chance of using methods that need login
     * in anonymous client
     */
    public IGWABase () throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {

        this.invocationHandler = new IGWAInvocationHandler(this);

        this.proxy = (IGWABaseInterface) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {IGWABaseInterface.class}, this.invocationHandler);

        init();
    }


    /**
     * This constructor will be used by IGWA class
     * Do not use this constructor
     *
     * @param  saviour is used to save internal state of client,
     *                 you need to implement IGWAStorage interface
     */
    public IGWABase (IGWAStorage saviour) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        this.saviour = saviour;

        this.invocationHandler = new IGWAInvocationHandler(this);

        this.proxy = (IGWABaseInterface) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {IGWABaseInterface.class}, this.invocationHandler);

        init();
    }


    /**
     * This constructor is used to load internal states of client, including sessionid, and is used
     * by IGWA class when previous login is saved persistently in previous run.
     * Do not use this constructor
     *
     * @param  username username of previously logged in account will be used as a key to retrieve
     *                  session information from saviour class
     * @param  saviour is used to save internal state of client,
     *                 you need to implement IGWAStorage interface
     */
    public IGWABase (String username, IGWAStorage saviour) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        this.saviour = saviour;

        this.invocationHandler = new IGWAInvocationHandler(this);

        this.proxy = (IGWABaseInterface) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {IGWABaseInterface.class}, this.invocationHandler);

        this.username = username;

        load();

    }


    /**
     * This method handle all the calls to Instagram WEB API, both GET and POST request will be
     * produced and sent by this method, it will handle cookie management, if not successful
     * will throw IGWAException containing code, message and error
     *
     * @param  _url is the end point address that we want to call, have to be a valid url
     * @param _params is used to add data to our post calls, all the data are in the form of pairs
     *                so a Map<String, String> is used to represent them
     * @param _headers the headers are handled by the library, but if your want to add something
     *                 this is th place, otherwise it have to be null
     * @param _query it is will be parsed and be added to url as the http call query, the map
     *               represent parameter`s names and values
     * @param _methodType will determine the http call type, it can be either GET or POST
     *                    it was possible to make this section automatic, but I prefer to be sure
     *                    and prevent bugs
     * @return      if successful returns the respond`s body
     */
    public String _make_request(String _url, Map<String, String> _params, Map<String, String>
            _headers, Map<String, String> _query, String _methodType) throws IGWAException, IOException, NoSuchAlgorithmException
    {

        if (_headers == null)
        {
            _headers = new LinkedHashMap<>(15,(float)1.5, true);
            _headers.put("User-Agent", user_agent);
            _headers.put("Accept", "*/*");
            _headers.put("Accept-Language", "en-US");
            _headers.put("Accept-Encoding", "gzip, deflate");
            _headers.put("'Connection'", "close");
        }

        if (_params != null)
        {
            _headers.put("x-csrftoken", csrftoken);
            _headers.put("x-requested-with", "XMLHttpRequest");
            _headers.put("x-instagram-ajax", rollout_hash);
            _headers.put("Referer", "https://www.instagram.com");
            _headers.put("Authority", "www.instagram.com");
            _headers.put("Origin", "https://www.instagram.com");
            _headers.put("Content-Type", "application/x-www-form-urlencoded");

        }

        if (_query != null)
        {
            if (_url.contains("?"))
            {
                _url += "&";
            } else
            {
                _url += "?";
            }

            _url += _parse_query(_query);

            String sig = _generate_request_signature(_query, _url);
            if (sig != null)
            {
                _headers.put("X-Instagram-GIS", sig);
            }
        }

        URL url = new URL(_url);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setConnectTimeout(timeout);
        connection.setRequestMethod(_methodType);

        cookieString = TextUtils.join(";", cookieManager.getCookieStore().getCookies());

        connection.setRequestProperty("Cookie", cookieString);


        for(Map.Entry<String,String> entry : _headers.entrySet())
        {
            connection.addRequestProperty(entry.getKey(), entry.getValue());
        }

        byte[] data = null;
        String data_str = null;
        if (_params != null)
        {
            data_str = _parse_query(_params);
        }

        if (data_str != null)
        {
            data = data_str.getBytes(StandardCharsets.US_ASCII);
        }


        if (_methodType.equals(POST) && data != null)
        {
            // Start

            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            // End
        }

        String message = connection.getResponseMessage();
        int responseCode = connection.getResponseCode();

        StringBuilder response = new StringBuilder();

        Map<String, List<String>> headerFields = connection.getHeaderFields();

        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            List<String> cookiesHeader = headerFields.get("Set-Cookie");

            if (cookiesHeader != null)
            {
                for (String cookie : cookiesHeader)
                {
                    HttpCookie httpCookie = HttpCookie.parse(cookie).get(0);

                    if (httpCookie.getName().equals("csrftoken"))
                    {
                        csrftoken = httpCookie.getValue();
                    }

                    cookieManager.getCookieStore().add(null,httpCookie );
                }
            }

            InputStream inputStream;
            List<String> EncodingList = headerFields.get("Content-Encoding");
            String ContentEncoding = null;
            if (EncodingList != null)
            {ContentEncoding = EncodingList.get(0);}


            if(ContentEncoding!= null && ContentEncoding.equals("gzip"))
            {
                inputStream = new GZIPInputStream(connection.getInputStream());
            }
            else
            {
                inputStream = connection.getInputStream();
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;

            while ((inputLine = input.readLine()) != null)
            {
                response.append(inputLine);
            }
            input.close();

        }
        else
        {
            InputStream inputStream = connection.getErrorStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;

            while ((inputLine = input.readLine()) != null)
            {
                response.append(inputLine);
            }
            throw new IGWAException(responseCode, message, response.toString());
        }


        return response.toString();
    }


    /**
     * All queries to Instagram API need to be signed with a MD5 hash of data as a header named
     * "X-Instagram-GIS", this method generate that MD5 hash, if there be _query data
     * in _make_request method
     */
    private String _generate_request_signature(Map<String, String> _query, String _url) throws MalformedURLException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        String variable = "";

        if (rhx_gis != null && _is_there(_query, "query_hash") && _is_there(_query, "variables"))
        {
            variable = _query.get("variables");
        } else if (rhx_gis != null && _url != null && !_url.equals("") && _is_there(_query, "__a"))
        {
            variable = new URL(_url).getPath();
        } else
            return null;

        byte[] bytesOfMessage = String.format("{%s}:{%s}", rhx_gis, variable).getBytes("UTF-8");

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(bytesOfMessage);

        return String.format("%032X", new BigInteger(1, digest));
    }


    /**
     * Checks the availability of the key inside the given Map
     */
    private boolean _is_there(Map<String, String> _map, String _key)
    {
        return _map.get(_key) != null && !_map.get(_key).equals("");
    }


    /**
     * Adds query`s parameters and values to hte url
     */
    private String _parse_query(Map<String, String> _query) throws UnsupportedEncodingException
    {
        String result = "";
        for (String key : _query.keySet())
        {
            final String encodedKey = URLEncoder.encode(key, "UTF-8");
            final String encodedValue = URLEncoder.encode(_query.get(key), "UTF-8");

            result += encodedKey + "=" + encodedValue;

            result += "&";


        }

        if (result.length() > 0)
        {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }


    /**
     * Previously rhx_gis had to be extracted from the response of an init call that sets
     * the cookies and headers, but now it can be produced from a random number as below
     */
    private String _extract_rhx_gis() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        Random random = new Random();

        int low = 10000000;
        int high = 99999999;
        int id = random.nextInt(high - low) + low;

        byte[] bytesOfMessage = String.format(":{\"id\":\"%d\"}", id).getBytes("UTF-8");

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(bytesOfMessage);

        return String.format("%032X", new BigInteger(1, digest));
    }


    /**
     * Using Regex extracts the rollout_hash from result of initial call to WEB API, that is a html
     */
    private String _extract_rollout_hash(String html)
    {
        String result = "";
        String pattern = "\"rollout_hash\":\"([A-Za-z0-9]+)\"";
        Pattern compiledPattern = Pattern.compile(pattern);

        Matcher matcher = compiledPattern.matcher(html);
        if (matcher.find( ))
        {
            result = matcher.group(0);

            result = result.substring(result.indexOf(":")+2,result.length()-1);
        }

        return result;
    }


    /**
     * Using Regex extracts the csrftoken from result of initial call to WEB API, that is a html
     * It can be collected from cookies as well, this is just a measure of safety
     */
    private String _extract_csrftoken(String html)
    {
        String result = "";
        String pattern = "\"csrf_token\":\"([A-Za-z0-9]+)\"";
        Pattern compiledPattern = Pattern.compile(pattern);

        Matcher matcher = compiledPattern.matcher(html);
        if (matcher.find( ))
        {
            result = matcher.group(0);

            result = result.substring(result.indexOf(":")+2,result.length()-1);
        }

        return result;
    }


    /**
     * Saves the internal state of the library including login info as json file
     * This method use savior class in order to save the the data persistently
     * The savior`s class have to implement IGWAStorage interface, the data will be saved
     * as <Name,Value> pair, name will be username, and value will be the data
     */
    public synchronized void save() throws JSONException, IOException
    {
        if ( saviour == null )
        {return;}


        String value;
        String name = username;

        cookieString = TextUtils.join(";", cookieManager.getCookieStore().getCookies());

        JSONObject jsonValue = new JSONObject();
        jsonValue.put("username", username);
        jsonValue.put("user_agent", user_agent);
        jsonValue.put("mobile_user_agent", mobile_user_agent);
        jsonValue.put("init_csrftoken", init_csrftoken);
        jsonValue.put("rhx_gis", rhx_gis);
        jsonValue.put("csrftoken", csrftoken);
        jsonValue.put("rollout_hash", rollout_hash);
        jsonValue.put("isLogedin", isLogedin);
        jsonValue.put("initialized", initialized);
        jsonValue.put("cookieString", cookieString);

        cookies = Arrays.asList(cookieString.split(";"));

        JSONArray cookiesArray = new JSONArray();
        for (String cookie : cookies)
        {
            cookiesArray.put(cookie);
        }

        jsonValue.put("cookies", cookiesArray);

        value = jsonValue.toString();
        saviour.save(name, value);

        call_counter = 0;
    }


    /**
     * Load the internal state using saviour class, the username will be used as the key
     * load() and save() methods will be used if the library is being used to login
     */
    public synchronized void load() throws IOException, JSONException, IGWAException
    {
        if ( saviour == null )
        {return;}

        String value;
        String name = username;

        value = saviour.load(name);

        if (value == null || value.equals(""))
        {
            throw new IGWAException(could_not_find_session_data, "Could not load the session data, check the username and load method implementation");
        }

        JSONObject jsonValue = new JSONObject(value);

        username = jsonValue.getString("username");
        user_agent = jsonValue.getString("user_agent");
        mobile_user_agent = jsonValue.getString("mobile_user_agent");
        init_csrftoken = jsonValue.getString("init_csrftoken");
        rhx_gis = jsonValue.getString("rhx_gis");
        rollout_hash = jsonValue.getString("rollout_hash");
        isLogedin = jsonValue.getBoolean("isLogedin");
        initialized = jsonValue.getBoolean("initialized");
        cookieString = jsonValue.getString("cookieString");
        try{csrftoken = jsonValue.getString("csrftoken");} catch (Exception ignored) {}
        if (csrftoken == null || csrftoken.equals("")) {csrftoken = init_csrftoken;}

        JSONArray cookiesArray = jsonValue.getJSONArray("cookies");
        cookies.clear();
        for (int i = 0; i < cookiesArray.length(); i++)
        {
            String cookie = cookiesArray.getString(i);
            HttpCookie httpCookie = HttpCookie.parse(cookie).get(0);
            cookieManager.getCookieStore().add(null, httpCookie);
            cookies.add(cookie);
        }

        cookieString = TextUtils.join(";", cookieManager.getCookieStore().getCookies());

    }


    /**
     * Initial GET request to get the first csrf token and rollout_hash and cookies
     */
    public void init() throws IOException, JSONException, IGWAException, NoSuchAlgorithmException
    {
        cookieManager.getCookieStore().add(null, HttpCookie.parse("ig_cb=1").get(0));

        String respond = proxy._make_request(INIT_URL, null, null, null, GET);

        rhx_gis = _extract_rhx_gis();

        rollout_hash = _extract_rollout_hash(respond);

        init_csrftoken = _extract_csrftoken(respond);

        if (csrftoken == null) {csrftoken = init_csrftoken;}
        if (init_csrftoken == null) {init_csrftoken = csrftoken;}

        if (csrftoken == null)
        {
            throw  new IGWAException(could_not_find_csrftoken, "Unable to get csrf from init request.");
        }


        cookieManager.getCookieStore().add(null, HttpCookie.parse("ig_pr=1").get(0));

        initialized = true;

        save();
    }


    /**
     * Gets user`s information using it`s username
     *
     * @param username this is an String, using the user_id is obsolete
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject user_info(String username) throws NoSuchAlgorithmException, IOException, IGWAException, JSONException
    {
        String url = String.format(USERNAME_URL,username);

        Map<String, String> query = new HashMap<>();
        query.put("__a","1");

        String respond = proxy._make_request(url, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Gets user`s feed
     *
     * @param user_id long integer used to identify each user in Instagram
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject user_feed(long user_id,  String end_cursor) throws JSONException, NoSuchAlgorithmException, IOException, IGWAException
    {
        JSONObject variables = new JSONObject();
        variables.put("id", user_id);
        variables.put("first", user_feed_count);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","003056d32c2554def87228bc3fd9668a");
        //query.put("query_hash","56a7068fea504063273cc2120ffd54f3");
        //query.put("query_hash","bfa387b2992c3a52dcbe447467b4b771");
        //query.put("query_hash","e7e2f4da4b02303f74f0841279e52d76");
        //query.put("query_hash","3913773caadd10357fba8b1ef4c89be3");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Gets media`s info, post images or videos, carousels, IGTV, story images or video, all will
     * be considered medias, and it might be common to be in a situation that you receive partial
     * information about a media, for example you might not get the resources links, this method
     * fills that gap
     *
     * @param short_code this is an string and not media_id called short_code, it is part of
     *                   media`s that you receive from the API
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject media_info(String short_code) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {

        Map<String,String> headers = new HashMap<>();
        headers.put("User-Agent", user_agent);
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "en-US");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Connection", "close");
        headers.put("'Referer", "https://www.instagram.com");
        headers.put("x-requested-with", "XMLHttpRequest");

        Map<String, String> query = new HashMap<>();
        query.put("__a","1");
        query.put("__b","1");


        String url = String.format(MEDIA_URL, short_code);

        String respond = proxy._make_request(url, null, headers,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Gets media`s comments
     *
     * @param short_code this is an string and not media_id called short_code, it is part of
     *                   media`s that you receive from the API
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject media_comments(String short_code, String end_cursor) throws JSONException, IOException, NoSuchAlgorithmException, IGWAException
    {
        JSONObject variables = new JSONObject();
        variables.put("shortcode", short_code);
        variables.put("first", comment_count);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        //query.put("query_hash","f0986789a5c5d17c2400faebf16efd0d");
        query.put("query_hash","bc3296d1ce80a24b1b6e40b1e72903f5"); // New query_hash will return comments replies as well

        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        if (respond.contains("shortcode_media\":null"))
        {
            throw new IGWAException(media_not_found,"media not found",short_code);
        }

        return new JSONObject(respond);
    }


    /**
     * Gets media`s likes
     *
     * @param short_code this is an string and not media_id called short_code, it is part of
     *                   media`s that you receive from the API
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject media_likers(String short_code, String end_cursor) throws JSONException, IOException, NoSuchAlgorithmException, IGWAException
    {
        JSONObject variables = new JSONObject();
        variables.put("shortcode", short_code);
        variables.put("first", like_count);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","d5d763b1e2acf209d62d22d184488e57");

        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null, query, GET);

        if (respond.contains("shortcode_media\":null"))
        {
            throw new IGWAException(media_not_found,"media not found", short_code);
        }

        return new JSONObject(respond);
    }


    /**
     * General search, returns list of users, tags, locations, matching the query_text
     *
     * @param query_text the key word to query about in our search
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject search(String query_text) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        Map<String, String> query = new HashMap<>();
        query.put("query",query_text);

        String respond = proxy._make_request(SEARCH_URL, null, null, query, GET);

        return new JSONObject(respond);

    }


    /**
     * Gets tag`s feed
     *
     * @param tag the tag that we want to receive feeds
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject tag_feed(String tag, String end_cursor) throws JSONException, IOException, NoSuchAlgorithmException, IGWAException
    {
        JSONObject variables = new JSONObject();
        variables.put("tag_name", tag.toLowerCase());
        variables.put("first", tag_feed_count);
        variables.put("show_ranked", false);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","f92f56d47dc7a55b606908374b43a314");

        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null, query, GET);

        return new JSONObject(respond);
    }


    /**
     * Gets a location`s feed
     *
     * @param location_id id of the location we want to receive it`s feeds
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject location_feed(long location_id, String end_cursor) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        JSONObject variables = new JSONObject();
        variables.put("id", location_id);
        variables.put("first", location_feed_count);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","1b84447a4d8b6d6d0426fefb34514485");

        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null, query, GET);

        return new JSONObject(respond);
    }


    /**
     * Get medias for the specified highlight IDs
     * When receiving list of highlights, it does not contain details of their medias
     * so we need to make another call to receive the information of medias belonging a highlight
     *
     * @param highlight_reel_ids highlights have a long integer as ids, the parameter is an
     *                          array of long integers, each for a distinct highlight
     *
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject highlight_reel_media(Long[] highlight_reel_ids) throws IOException, NoSuchAlgorithmException, IGWAException, JSONException
    {
        String[] str_highlight_reel_ids = new String[highlight_reel_ids.length];

        for (int i=0 ; i <highlight_reel_ids.length; i++)
        {
            str_highlight_reel_ids[i]=Long.toString(highlight_reel_ids[i]);
        }


        JSONObject variables = new JSONObject();
        variables.put("highlight_reel_ids", new JSONArray(str_highlight_reel_ids));
        variables.put("reel_ids", new JSONArray(new String[] {}));
        variables.put("location_ids", new JSONArray(new String[] {}));
        variables.put("precomposed_overlay", false);


        Map<String, String> query = new HashMap<>();
        query.put("query_hash","90709b530ea0969f002c86a89b4f2b8d");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Get the tagged feed for the specified user
     *
     * @param user_id id of the user
     * @param end_cursor it is used for pagination, have to retrieve this string from the respond
     *                   and resend it to API in order to get next page of data, for the first call
     *                   pass empty string ("") to the method
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject tagged_user_feed(long user_id, String end_cursor) throws JSONException, IOException, NoSuchAlgorithmException, IGWAException
    {
        JSONObject variables = new JSONObject();
        variables.put("id", user_id);
        variables.put("first", 50);

        if (end_cursor != null && !end_cursor.equals(""))
        {
            variables.put("after", end_cursor);
        }

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","ff260833edf142911047af6024eb634a");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Get the stories feed for the specified tags
     *
     * @param tag_names an array of strings for tags
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject tag_story_feed(String[] tag_names) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        return _story_feed(new String[] {}, tag_names, new String[] {});
    }


    /**
     * Get the stories feed for the specified location
     *
     * @param location_ids an array of longs for location_ids
     * @return JSONObject representing received answer from the API,
     * By implementing IGWADigest it is possible to parse the data if necessary
     */
    public JSONObject location_story_feed(Long[] location_ids) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        String[] str_location_ids = new String[location_ids.length];

        for (int i=0 ; i <location_ids.length; i++)
        {
            str_location_ids[i]=Long.toString(location_ids[i]);
        }

        return _story_feed(new String[] {}, new String[] {}, str_location_ids);
    }


    /**
     * Used by other functions to retrieve highlights and stories
     */
    public JSONObject _story_feed(String[] reel_ids, String[] tag_names, String[] location_ids) throws JSONException, IGWAException, IOException, NoSuchAlgorithmException
    {
        JSONObject variables = new JSONObject();
        variables.put("reel_ids", new JSONArray(reel_ids));
        variables.put("tag_names", new JSONArray(tag_names));
        variables.put("location_ids", new JSONArray(location_ids));
        variables.put("precomposed_overlay", false);
        variables.put("show_story_viewer_list", true);
        variables.put("story_viewer_fetch_count", 10);
        variables.put("story_viewer_cursor", 50);

        Map<String, String> query = new HashMap<>();
        query.put("query_hash","eb1918431e946dd39bf8cf8fb870e426");
        query.put("variables",variables.toString());

        String respond = proxy._make_request(GRAPHQL_API_URL, null, null,query, GET);

        return new JSONObject(respond);
    }


    /**
     * Setter function for saviour, it`s needed if we want to implement login through a
     * web view, first a special saviour will be used to inject internal stat to the client
     * and then we change the saviour
     */
    public void set_saviour(IGWAStorage saviour)
    {
        this.saviour = saviour;
    }

}
