package vp.metagram.ui.AccBrowser.ListSource;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;


import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.User;
import vp.igpapi.IGWAException;

import static vp.metagram.general.variables.metagramAgent;

public class ListSource_Likes_Online extends ListSource
{

    String short_code;

    public ListSource_Likes_Online(String short_code, Map<Long,User> sourceList)
    {
        super(sourceList);
        this.short_code = short_code;

    }

    @Override
    public Map<Long,PostMedia> getNextList() throws IOException, NoSuchAlgorithmException, JSONException, IGWAException
    {
        if (short_code.equals(""))
        {
            clearList();
        }
        else if (sourceList.size() <= 0)
        {
            Map<Long, User> newList = new LinkedHashMap<>(10, 0.75F, true);
            metagramAgent.activeAgent.proxy.getLikeList(short_code, newList, "");
            sourceList.putAll(newList);
        }

        return sourceList;
    }

}
