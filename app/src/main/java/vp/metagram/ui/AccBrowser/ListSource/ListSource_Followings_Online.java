package vp.metagram.ui.AccBrowser.ListSource;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import vp.metagram.utils.instagram.InstagramAgent;

import vp.metagram.utils.instagram.types.User;
import vp.igpapi.IGWAException;

import static vp.metagram.general.variables.metagramAgent;

public class ListSource_Followings_Online extends ListSource
{
    long IPK = -1;
    String nextHash = "";

    InstagramAgent agent = null;

    Map<Long,User> followingsList = new LinkedHashMap<>(10, 0.75F, true);

    public ListSource_Followings_Online(long IPK, Map<Long,User> sourceList, InstagramAgent agent)
    {
        super(sourceList);
        this.IPK = IPK;
        this.agent = agent;
    }

    public ListSource_Followings_Online(long IPK, Map<Long,User> sourceList)
    {
        super(sourceList);
        this.IPK = IPK;

        if (agent == null)
        {
            agent = metagramAgent.activeAgent;
        }
    }

    @Override
    public Map<Long,User> getNextList() throws IOException, NoSuchAlgorithmException, JSONException, IGWAException
    {
        if (IPK < 0)
        {
            clearList();
        }
        else if (nextHash != null)
        {

            nextHash = agent.proxy.getFollowingList(IPK, followingsList, nextHash);
            sourceList.putAll(followingsList);
        }

        return sourceList;
    }


}
