package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import vp.metagram.utils.instagram.types.User;
import vp.igpapi.IGWADigest;

public class FollowerList implements IGWADigest
{
    public Map<Long, User> followers = new LinkedHashMap<>(10,(float)0.75, true);
    public String next_hash;
    public int count;

    @Override
    public FollowerList digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("user");
        jsonObject = jsonObject.getJSONObject("edge_followed_by");

        count = jsonObject.getInt("count");

        JSONArray edges = jsonObject.getJSONArray("edges");

        jsonObject = jsonObject.getJSONObject("page_info");

        if (jsonObject.getBoolean("has_next_page"))
        {
            next_hash = jsonObject.getString("end_cursor");
        }
        else
        {
            next_hash = "";
        }

        for (int i=0; i < edges.length(); i++)
        {
            User newUser = new User();

            JSONObject userJSON = edges.getJSONObject(i);
            userJSON = userJSON.getJSONObject("node");

            newUser.IPK = Long.parseLong(userJSON.getString("id"));
            newUser.username = userJSON.getString("username");
            newUser.fullname = userJSON.getString("full_name");
            newUser.picURL = userJSON.getString("profile_pic_url");
            newUser.isPrivate = userJSON.getBoolean("is_private");

            followers.put(newUser.IPK, newUser);
        }

        return this;
    }
}
