package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import vp.metagram.utils.instagram.types.Comment;
import vp.metagram.utils.instagram.types.User;
import vp.igpapi.IGWADigest;


public class CommentList implements IGWADigest
{
    public Map<Long, Comment> comments = new LinkedHashMap<>(10,(float)0.75, true);

    public String next_hash;
    public int count;

    @Override
    public CommentList digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("shortcode_media");
        jsonObject = jsonObject.getJSONObject("edge_media_to_parent_comment");

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
            Comment newComment = new Comment();

            JSONObject commentJSON = edges.getJSONObject(i);
            commentJSON = commentJSON.getJSONObject("node");

            newComment.CPK = Long.parseLong(commentJSON.getString("id"));
            newComment.message = commentJSON.getString("text");
            newComment.created_utc = Long.parseLong(commentJSON.getString("created_at"));

            JSONObject owner = commentJSON.getJSONObject("owner");
            User user = new User();
            user.IPK = Long.parseLong(owner.getString("id"));
            user.username = owner.getString("username");
            user.picURL = owner.getString("profile_pic_url");

            newComment.commenter = user;

            newComment.noOfLikes = commentJSON.getJSONObject("edge_liked_by").getInt("count");

            comments.put(newComment.CPK,newComment);
        }

        return this;
    }
}
