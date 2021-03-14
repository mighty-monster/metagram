package vp.metagram.ui.ReportUI.reportViewer.listSource;

import android.database.MatrixCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.metagram.ui.ReportUI.reportViewer.types.ReportPostUser;

import static vp.metagram.general.functions.JSONArrayToStringList;
import static vp.metagram.general.variables.dbMetagram;


public class RemovedCommentsSource extends ReportListSource
{
    long FIPK = -1;
    int lastID = -1;

    String searchValue;

    String QueryFromPart = "Select MPK, MiniLink, MID, PostType, URLs, Caption, Others.Username, Others.PictureURL, Others.IPK, CommentText from Rel_Comment LEFT JOIN Posts\n" +
            "\tOn Rel_Comment.FMPK = Posts.MPK Left Join Others\n" +
            "\t\tOn Rel_Comment.CommenterIPK = Others.IPK \n" +
            "    Where Posts.FIPK = %d and Posts.StatJobID = %d and Rel_Comment.StatJobID < %d and Username like '%%%s%%'";

    String getLastIDQuery = "Select Statistics_Jobs.StatJobID as lastID from Statistics_Orders left Join Statistics_Jobs\n" +
            "      On Statistics_Jobs.StatJobID = \n" +
            "    (Select StatJobID from Statistics_Jobs Where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID and Status = 'done'  order by StatJobID desc limit 1) \n" +
            "      where Statistics_Orders.IPK = %d";

    public RemovedCommentsSource(long FIPK, List<ReportPostUser> sourceList, String searchValue)
    {
        super(sourceList);
        this.FIPK = FIPK;
        this.searchValue = searchValue;
        clearList();

    }

    public void getLastID() throws Exception
    {
        String sqlText = String.format(Locale.ENGLISH,getLastIDQuery,FIPK);
        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            lastID = result.getInt(result.getColumnIndex("lastID"));
        }
    }

    public void calculateCount() throws Exception
    {
        String sqlText = String.format(Locale.ENGLISH,"Select Count(*) as No from ( " + QueryFromPart + " )",FIPK,lastID,lastID,searchValue);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            count = result.getInt(result.getColumnIndex("No"));
        }

    }

    @Override
    public List<ReportPostUser> getNextList() throws Exception
    {
        getLastID();


        if (FIPK < 0)
        {
            clearList();
        }
        else
        {

            List<ReportPostUser> newList = new ArrayList<>();

            String sqlText = String.format(Locale.ENGLISH," Select * from ( " + QueryFromPart + " )",FIPK,lastID,lastID,searchValue);

            MatrixCursor result = dbMetagram.selectQuery(sqlText);
            if (result.moveToFirst())
            {
                while ( !result.isAfterLast() )
                {
                    ReportPostUser reportUser = new ReportPostUser();
                    reportUser.MPK = result.getLong(result.getColumnIndex("MPK"));
                    reportUser.MID = result.getString(result.getColumnIndex("MID"));
                    reportUser.type = result.getInt(result.getColumnIndex("PostType"));
                    reportUser.miniLink = result.getString(result.getColumnIndex("MiniLink"));
                    reportUser.urls = JSONArrayToStringList(result.getString(result.getColumnIndex("URLs")));
                    reportUser.accountIPK = result.getLong(result.getColumnIndex("IPK"));
                    reportUser.username = result.getString(result.getColumnIndex("Username"));
                    reportUser.accountPicUrl = result.getString(result.getColumnIndex("PictureURL"));
                    reportUser.comment = result.getString(result.getColumnIndex("CommentText"));
                    reportUser.caption = result.getString(result.getColumnIndex("Caption"));
                    newList.add(reportUser);
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
}
