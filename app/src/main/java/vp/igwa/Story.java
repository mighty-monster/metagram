package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vp.igpapi.IGWADigest;

public class Story extends ReelsMedia implements IGWADigest
{
    @Override
    public Story digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");

        JSONArray reels_media = jsonObject.getJSONArray("reels_media");
        if (reels_media.length() > 0)
        {
            jsonObject = reels_media.getJSONObject(0);
            id = Long.parseLong(jsonObject.getString("id"));

            JSONArray story_items = jsonObject.getJSONArray("items");

            for (int i =0; i < story_items.length(); i++)
            {
                JSONObject story_item = story_items.getJSONObject(i);

                GraphReel new_item = new GraphReel();

                new_item.id = Long.parseLong(story_item.getString("id"));
                new_item.isVideo = story_item.getBoolean("is_video");
                new_item.display_url = story_item.getString("display_url");

                JSONArray display_array = story_item.getJSONArray("display_resources");

                new_item.resource_url = display_array.getJSONObject(display_array.length()-1).getString("src");

                if (new_item.isVideo)
                {
                    JSONArray video_resources = story_item.getJSONArray("video_resources");

                    new_item.video_url = video_resources.getJSONObject(video_resources.length() - 1).getString("src");
                }

                items.add(new_item);

            }
        }

        return this;
    }
}
