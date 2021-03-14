package vp.metagram.utils.instagram.types;

import org.json.JSONException;
import org.json.JSONObject;


import vp.igpapi.IGWADigest;


public class UserFull extends User implements IGWADigest
{
    public int followerCount;
    public int followingCount;
    public int postsCount;

    public boolean followsViewer;
    public boolean followedByViewer;
    public boolean blockedByViewer;
    public boolean hasBlockedViewer;


    public String getAsJSON(boolean isFriend)
    {
        JSONObject userObject = new JSONObject();

        try
        {
            userObject.put("username", username);
            userObject.put("fullname", fullname);
            userObject.put("picURL", picURL);
            userObject.put("followerCount", followerCount);
            userObject.put("postsCount", postsCount);
            userObject.put("followingCount", followingCount);
            userObject.put("isFriend",isFriend);
            userObject.put("isPrivate",isPrivate);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return userObject.toString().trim();
    }

    @Override
    public UserFull digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("graphql");
        jsonObject = jsonObject.getJSONObject("user");


        IPK = jsonObject.getLong("id");
        username = jsonObject.getString("username");
        fullname = jsonObject.getString("full_name");
        isPrivate = jsonObject.getBoolean("is_private");
        picURL = jsonObject.getString("profile_pic_url");

        followerCount = jsonObject.getJSONObject("edge_followed_by").getInt("count");
        followingCount = jsonObject.getJSONObject("edge_follow").getInt("count");
        postsCount = jsonObject.getJSONObject("edge_owner_to_timeline_media").getInt("count");

        followsViewer = jsonObject.getBoolean("follows_viewer");
        followedByViewer = jsonObject.getBoolean("follows_viewer");
        blockedByViewer = jsonObject.getBoolean("follows_viewer");
        hasBlockedViewer = jsonObject.getBoolean("follows_viewer");


        return this;
    }
}
