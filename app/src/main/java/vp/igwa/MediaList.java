package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import vp.metagram.utils.instagram.types.PostMedia;
import vp.igpapi.IGWADigest;

public class MediaList implements IGWADigest
{
    public Map<Long, PostMedia> medias = new LinkedHashMap<>(10,(float)0.75, true);

    public String next_hash;
    public int count;

    @Override
    public MediaList digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("user");
        jsonObject = jsonObject.getJSONObject("edge_owner_to_timeline_media");

        JSONArray edges = jsonObject.getJSONArray("edges");

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

        for (int i=0; i < edges.length(); i++)
        {
            PostMedia newMedia = new PostMedia();

            JSONObject mediaJSON = edges.getJSONObject(i);
            mediaJSON = mediaJSON.getJSONObject("node");

            newMedia.MPK = Long.parseLong(mediaJSON.getString("id"));
            newMedia.miniLink = mediaJSON.getString("shortcode");

            long user_id = Long.parseLong( mediaJSON.getJSONObject("owner").getString("id"));

            newMedia.ID = String.format(Locale.ENGLISH, "%d_%d", newMedia.MPK, user_id);


            newMedia.picURL = mediaJSON.getString("display_url");

            newMedia.likeCount = mediaJSON.getJSONObject("edge_media_preview_like").getInt("count");
            newMedia.commentCount = mediaJSON.getJSONObject("edge_media_to_comment").getInt("count");

            if (mediaJSON.getJSONObject("edge_media_to_caption").getJSONArray("edges").length() > 0)
            {newMedia.caption = mediaJSON.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");}
            else
            {newMedia.caption = "";}

            String typename = mediaJSON.getString("__typename");

            if (typename.equals("GraphImage"))
            {
                newMedia.type = 1;

                newMedia.urls.add(newMedia.picURL);
            }
            else if (typename.equals("GraphSidecar"))
            {
                newMedia.type = 8;

                JSONArray children = mediaJSON.getJSONObject("edge_sidecar_to_children").getJSONArray("edges");

                for (int j=0; j < children.length(); j++)
                {
                    JSONObject node = children.getJSONObject(j).getJSONObject("node");
                    String type = node.getString("__typename");
                    if (type.equals("GraphImage"))
                    {
                        newMedia.urls.add(node.getString("display_url"));
                    }
                    else if (type.equals("GraphVideo"))
                    {
                        newMedia.urls.add(node.getString("video_url"));
                    }

                }

            }
            else if (typename.equals("GraphVideo"))
            {
                newMedia.type = 2;

                newMedia.viewCount = mediaJSON.getInt("video_view_count");

                newMedia.urls.add(mediaJSON.getString("video_url"));
            }




            medias.put(newMedia.MPK ,newMedia);

        }

        return this;
    }
}
