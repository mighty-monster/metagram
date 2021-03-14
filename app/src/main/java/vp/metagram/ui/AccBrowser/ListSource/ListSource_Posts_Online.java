package vp.metagram.ui.AccBrowser.ListSource;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import vp.metagram.utils.instagram.InstagramAgent;

import vp.metagram.utils.instagram.types.PostMedia;
import vp.igpapi.IGWAException;

public class ListSource_Posts_Online extends ListSource
{
    long IPK = -1;
    String nextHash = "";
    InstagramAgent agent;

    Map<Long, PostMedia> postsList = new LinkedHashMap<>(10, 0.75F, true);

    public ListSource_Posts_Online(long IPK, Map<Long, PostMedia> sourceList, InstagramAgent agent)
    {
        super(sourceList);
        this.IPK = IPK;
        this.agent = agent;
    }

    @Override
    public Map<Long, PostMedia> getNextList() throws IOException, NoSuchAlgorithmException, JSONException, IGWAException
    {
        if (IPK < 0)
        {
            clearList();
        }
        else if (nextHash != null)
        {

            nextHash = agent.proxy.getMediaList(IPK, postsList, nextHash,0);
            sourceList.putAll(postsList);
        }

        return sourceList;
    }

}
