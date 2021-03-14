package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vp.igpapi.IGWADigest;

public class IGTVList implements IGWADigest
{
    public int count;
    public String next_hash = "";

    public List<IGTVSummery> IGTVs = new ArrayList();

    @Override
    public IGTVList digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("user");
        jsonObject = jsonObject.getJSONObject("edge_felix_video_timeline");

        count = jsonObject.getInt("count");
        if (jsonObject.getJSONObject("page_info").getBoolean("has_next_page"))
        {
            next_hash = jsonObject.getJSONObject("page_info").getString("end_cursor");
        }
        else
        {
            next_hash = "";
        }

        JSONArray edges = jsonObject.getJSONArray("edges");

        for (int i=0; i < edges.length(); i++)
        {
            IGTVSummery newIGTV = new IGTVSummery();

            JSONObject IGTVJson = edges.getJSONObject(i);
            IGTVJson = IGTVJson.getJSONObject("node");

            newIGTV.id = Long.parseLong(IGTVJson.getString("id"));

            newIGTV.display_url = IGTVJson.getString("display_url");

            newIGTV.noOfLikes = IGTVJson.getJSONObject("edge_liked_by").getInt("count");
            newIGTV.noOfComments = IGTVJson.getJSONObject("edge_media_to_comment").getInt("count");

            if (IGTVJson.getJSONObject("edge_media_to_caption").getJSONArray("edges").length() > 0)
            {newIGTV.caption = IGTVJson.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");}
            else
            {newIGTV.caption = "";}


            newIGTV.noOfViews = IGTVJson.getInt("video_view_count");

            newIGTV.title = IGTVJson.getString("title");

            newIGTV.short_code = IGTVJson.getString("shortcode");

            IGTVs.add(newIGTV);
        }

        return this;
    }
}
