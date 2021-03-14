package vp.metagram.ui.AccBrowser.ListSource;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;


import vp.metagram.utils.instagram.types.Comment;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.igpapi.IGWAException;

import static vp.metagram.general.variables.metagramAgent;

public class ListSource_Comments_Online extends ListSource
{
    String short_code = "";
    String nextHash = "";


    public ListSource_Comments_Online(String short_code, Map<Long,Comment> sourceList )
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
        else if (nextHash != null)
        {
            Map<Long,Comment> newList = new LinkedHashMap<>(10, 0.75F, true);

            nextHash = metagramAgent.activeAgent.proxy.getCommentList(short_code,sourceList,nextHash);

            sourceList.putAll(newList);
        }

        return sourceList;
    }

}
