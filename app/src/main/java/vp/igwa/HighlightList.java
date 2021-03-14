package vp.igwa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vp.igpapi.IGWADigest;

public class HighlightList implements IGWADigest
{
    public List<HighlightSummery> items = new ArrayList<>();

    @Override
    public HighlightList digest(JSONObject jsonObject) throws JSONException
    {
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("user");
        jsonObject = jsonObject.getJSONObject("edge_highlight_reels");

        JSONArray edges = jsonObject.getJSONArray("edges");

        for (int i=0; i < edges.length(); i++)
        {
            HighlightSummery newItem = new HighlightSummery();

            jsonObject = edges.getJSONObject(i);
            jsonObject = jsonObject.getJSONObject("node");

            newItem.id = Long.parseLong(jsonObject.getString("id"));
            newItem.cover_media = jsonObject.getJSONObject("cover_media").getString("thumbnail_src");
            newItem.title = jsonObject.getString("title");

            items.add(newItem);
        }

        return this;
    }
}


