package vp.metagram.ui.ReportUI.reportViewer.listSource;

import android.database.MatrixCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.metagram.ui.ReportUI.reportViewer.types.ReportMedia;

import static vp.metagram.general.functions.JSONArrayToStringList;
import static vp.metagram.general.variables.dbMetagram;


public class MostViewedPostsSource extends ReportListSource
{
    long FIPK = -1;
    String searchValue;

    int pageCapacity = 10;

    public MostViewedPostsSource(long FIPK, List<ReportMedia> sourceList, String searchValue)
    {
        super(sourceList);
        this.FIPK = FIPK;
        this.searchValue = searchValue;
        clearList();
    }

    @Override
    public List<ReportMedia> getNextList() throws Exception
    {
        if (FIPK < 0)
        {
            clearList();
        }
        else
        {

            List<ReportMedia> newList = new ArrayList<>();

            String findLastIDQuery = String.format(Locale.ENGLISH, "Select Statistics_Jobs.StatJobID as lastID from Statistics_Orders left Join Statistics_Jobs\n" +
                    "          On Statistics_Jobs.StatJobID = \n" +
                    "        (Select StatJobID from Statistics_Jobs Where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID and Status = 'done' order by StatJobID desc limit 1) \n" +
                    "          where Statistics_Orders.IPK = %d", FIPK);

            MatrixCursor result = dbMetagram.selectQuery(findLastIDQuery);

            int lastID = -1;
            if (result.moveToFirst())
            {
                lastID = result.getInt(result.getColumnIndex("lastID"));
            }

            String sqlText = String.format(Locale.ENGLISH,"Select MPK, MID, MiniLink, PostType, ViewNo, LikeNo, CommentNo, ViewNo,PicUrl,Caption,URLs from Posts  Left Join Posts_Info \n" +
                    "    on Posts_Info.FMPK =  Posts.MPK and Posts_Info.StatJobID = %d" +
                    "     Where FIPK = %d AND PostType = 2  and Posts_Info.CommentNo Is Not NULL and Posts.Caption like '%%%s%%'   Order By ViewNo desc  limit %d OFFSET %d \n" +
                    "    ", lastID,FIPK, searchValue,pageCapacity,index);


            result = dbMetagram.selectQuery(sqlText);
            if (result.moveToFirst())
            {
                while ( !result.isAfterLast() )
                {
                    ReportMedia reportMedia = new ReportMedia();

                    reportMedia.MPK = result.getLong(result.getColumnIndex("MPK"));
                    reportMedia.extraInfo = result.getInt(result.getColumnIndex("ViewNo"));
                    reportMedia.miniLink = result.getString(result.getColumnIndex("MiniLink"));
                    reportMedia.type = result.getInt(result.getColumnIndex("PostType"));
                    reportMedia.MID = result.getString(result.getColumnIndex("MID"));
                    reportMedia.likeNo = result.getInt(result.getColumnIndex("LikeNo"));
                    reportMedia.commentNo = result.getInt(result.getColumnIndex("CommentNo"));
                    reportMedia.viewNo = result.getInt(result.getColumnIndex("ViewNo"));
                    reportMedia.PicURL = result.getString(result.getColumnIndex("PicUrl"));
                    reportMedia.caption = result.getString(result.getColumnIndex("Caption"));
                    reportMedia.urls = JSONArrayToStringList(result.getString(result.getColumnIndex("URLs")));

                    newList.add(reportMedia);

                    result.moveToNext();
                }
            }

            sourceList.addAll(newList);
            index += pageCapacity;
        }

        return sourceList;
    }

    @Override
    public void setSearchValue(String searchValue)
    {
        this.searchValue = searchValue;
    }

    @Override
    public void calculateCount() throws Exception
    {

    }
}
