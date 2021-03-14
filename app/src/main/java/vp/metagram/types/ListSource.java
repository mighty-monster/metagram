package vp.metagram.types;

import java.io.IOException;
import java.util.List;



public abstract class ListSource<ListResultType>
{
    public List<ListResultType> sourceList;

    public ListSource(List<ListResultType> sourceList)
    {
        this.sourceList = sourceList;
    }

    abstract public List<ListResultType> getNextList() throws IOException, Exception;

    public void clearList()
    {
        sourceList.clear();
    }
}
