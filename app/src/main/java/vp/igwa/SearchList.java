package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vp.metagram.utils.instagram.types.User;
import vp.igpapi.IGWADigest;

public class SearchList implements IGWADigest
{
    public List<User> users = new ArrayList<>();


    @Override
    public SearchList digest(JSONObject jsonObject) throws JSONException
    {
        JSONArray users_array = jsonObject.getJSONArray("users");

        for (int i=0; i < users_array.length(); i++)
        {
            User newUser = new User();

            JSONObject userJSON = users_array.getJSONObject(i);
            userJSON = userJSON.getJSONObject("user");

            newUser.IPK = Long.parseLong( userJSON.getString("pk"));
            newUser.username = userJSON.getString("username");
            newUser.fullname = userJSON.getString("full_name");
            newUser.picURL = userJSON.getString("profile_pic_url");
            newUser.isPrivate = userJSON.getBoolean("is_private");

            users.add(newUser);

        }

        return this;
    }
}
