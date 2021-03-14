package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vp.igpapi.IGWADigest;

public class Highlight extends ReelsMedia implements  IGWADigest
{

    @Override
    public Highlight digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");

        JSONArray jsonArray = jsonObject.getJSONArray("reels_media");

        jsonObject = jsonArray.getJSONObject(0);

        id = Long.parseLong(jsonObject.getString("id"));

        jsonArray = jsonObject.getJSONArray("items");

        for (int i=0; i< jsonArray.length(); i++)
        {
            JSONObject highlight_item = jsonArray.getJSONObject(i);


            GraphReel new_item = new GraphReel();

            new_item.id = Long.parseLong(highlight_item.getString("id"));
            new_item.isVideo = highlight_item.getBoolean("is_video");
            new_item.display_url = highlight_item.getString("display_url");

            JSONArray display_array = highlight_item.getJSONArray("display_resources");

            new_item.resource_url = display_array.getJSONObject(display_array.length()-1).getString("src");

            if (new_item.isVideo)
            {
                JSONArray video_resources = highlight_item.getJSONArray("video_resources");

                new_item.video_url = video_resources.getJSONObject(video_resources.length() - 1).getString("src");
            }

            items.add(new_item);

        }

        return this;
    }
}
