package vp.metagram.ui.AccBrowser.ListSource;

import java.io.IOException;
import java.util.Map;



public abstract class ListSource<KeyType,ListResultType>
{
    public Map<KeyType,ListResultType> sourceList;

    public ListSource(Map<KeyType,ListResultType> sourceList)
    {
        this.sourceList = sourceList;
    }

    abstract public Map<KeyType,ListResultType> getNextList() throws IOException, Exception;

    public void clearList()
    {
        sourceList.clear();
    }
}
