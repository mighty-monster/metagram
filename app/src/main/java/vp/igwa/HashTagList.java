package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vp.metagram.utils.instagram.types.PostMedia;
import vp.igpapi.IGWADigest;


public class HashTagList implements IGWADigest
{
    long id;
    String name;
    String picURL;


    int count;
    String next_hash;
    List<PostMedia> medias = new ArrayList<>();
    List<PostMedia> top_medias = new ArrayList<>();

    @Override
    public HashTagList digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("hashtag");

        id = jsonObject.getLong("id");
        name = jsonObject.getString("name");
        picURL = jsonObject.getString("profile_pic_url");

        JSONArray media_array = jsonObject.getJSONObject("edge_hashtag_to_media").getJSONArray("edges");
        JSONArray top_media_array = jsonObject.getJSONObject("edge_hashtag_to_top_posts").getJSONArray("edges");

        jsonObject = jsonObject.getJSONObject("edge_hashtag_to_media");
        count = jsonObject.getInt("count");

        jsonObject = jsonObject.getJSONObject("page_info");

        if (jsonObject.getBoolean("has_next_page"))
        {
            next_hash = jsonObject.getString("end_cursor");
        }
        else
        {
            next_hash = "";
        }

        for (int i=0; i < media_array.length(); i++)
        {
            PostMedia newMedia = new PostMedia();

            JSONObject mediaJSON = media_array.getJSONObject(i);
            mediaJSON = mediaJSON.getJSONObject("node");

            newMedia.MPK = Long.parseLong( mediaJSON.getString("id"));
            newMedia.miniLink = mediaJSON.getString("shortcode");

            long user_id = Long.parseLong( mediaJSON.getJSONObject("owner").getString("id"));

            newMedia.ID = String.format(Locale.ENGLISH, "%d_%d", newMedia.MPK, user_id);


            newMedia.picURL = mediaJSON.getString("display_url");

            newMedia.likeCount = mediaJSON.getJSONObject("edge_media_preview_like").getInt("count");
            newMedia.commentCount = mediaJSON.getJSONObject("edge_media_to_comment").getInt("count");

            newMedia.caption = mediaJSON.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");

            String typename = mediaJSON.getString("__typename");

            if (typename.equals("GraphImage"))
            {
                newMedia.type = 1;

                newMedia.urls.add(newMedia.picURL);
            }
            else if (typename.equals("GraphSidecar"))
            {
                newMedia.type = 8;

                newMedia.urls.add(newMedia.picURL);

            }
            else if (typename.equals("GraphVideo"))
            {
                newMedia.type = 2;

                newMedia.urls.add(newMedia.picURL);
            }


            medias.add(newMedia);

        }

        for (int i=0; i < top_media_array.length(); i++)
        {
            PostMedia newMedia = new PostMedia();

            JSONObject mediaJSON = top_media_array.getJSONObject(i);
            mediaJSON = mediaJSON.getJSONObject("node");

            newMedia.MPK = Long.parseLong( mediaJSON.getString("id"));
            newMedia.miniLink = mediaJSON.getString("shortcode");

            long user_id = Long.parseLong( mediaJSON.getJSONObject("owner").getString("id"));

            newMedia.ID = String.format(Locale.ENGLISH, "%d_%d", newMedia.MPK, user_id);


            newMedia.picURL = mediaJSON.getString("display_url");

            newMedia.likeCount = mediaJSON.getJSONObject("edge_media_preview_like").getInt("count");
            newMedia.commentCount = mediaJSON.getJSONObject("edge_media_to_comment").getInt("count");

            newMedia.caption = mediaJSON.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");

            String typename = mediaJSON.getString("__typename");

            if (typename.equals("GraphImage"))
            {
                newMedia.type = 1;

                newMedia.urls.add(newMedia.picURL);
            }
            else if (typename.equals("GraphSidecar"))
            {
                newMedia.type = 8;

                newMedia.urls.add(newMedia.picURL);

            }
            else if (typename.equals("GraphVideo"))
            {
                newMedia.type = 2;

                newMedia.urls.add(newMedia.picURL);
            }


            top_medias.add(newMedia);

        }


        return this;
    }
}
