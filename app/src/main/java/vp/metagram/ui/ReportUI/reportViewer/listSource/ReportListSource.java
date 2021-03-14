package vp.metagram.ui.ReportUI.reportViewer.listSource;

import java.util.List;

import vp.metagram.types.ListSource;

public abstract class ReportListSource extends ListSource
{
    public int count=0;
    public int pageCapacity = 100;
    public int index = 0;

    public String limitQuery = "  limit %d OFFSET %d  ";


    public ReportListSource(List sourceList)
    {
        super(sourceList);
    }

    abstract public void setSearchValue(String searchValue);

    abstract public void calculateCount() throws Exception;

    public void reset()
    {
        clearList();
        index = 0;
    }
}
