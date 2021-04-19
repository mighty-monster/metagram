package vp.metagram.ui.ReportUI.reportViewer.listSource;

import android.database.MatrixCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.metagram.ui.ReportUI.reportViewer.types.ReportPostUser;

import static vp.metagram.general.functions.JSONArrayToStringList;
import static vp.metagram.general.variables.dbMetagram;



public class CommentedWithoutFollowSource extends ReportListSource
{
    long FIPK = -1;
    String searchValue;

    int lastID = -1;


    String QueryFromPart = " Select Others.IPK,Others.Username,Others.PictureURL,Posts.MPK,Posts.PostType, Posts.Caption,Posts.MID, Posts.MiniLink, Posts.URLs, Posts.PicURL, CommentText  From Rel_Comment Left Join Others\n" +
                        "  On CommenterIPK = Others.IPK  Left Join Posts On Posts.MPK = Rel_Comment.FMPK Where FMPK in ( %s ) " +
                        " and Others.IPK not in ( Select FollowerIPK from Rel_Follower Where FIPK = %d)\n" +
                        "    AND Username like '%%%s%%' and Others.IPK <> FIPK";

    String getLastIDQuery = "Select Statistics_Jobs.StatJobID as lastID from Statistics_Orders left Join Statistics_Jobs\n" +
            "      On Statistics_Jobs.StatJobID = \n" +
            "    (Select StatJobID from Statistics_Jobs Where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID and Status = 'done'  order by StatJobID desc limit 1) \n" +
            "      where Statistics_Orders.IPK = %d";


    public CommentedWithoutFollowSource(long FIPK, List<ReportPostUser> sourceList, String searchValue)
    {
        super(sourceList);
        this.FIPK = FIPK;
        this.searchValue = searchValue;
    }

    public void calculateCount(String MPKListQuery) throws Exception
    {

        String sqlText = String.format(Locale.ENGLISH,"Select Count(*) as No From ( " + QueryFromPart + " )"  , MPKListQuery,FIPK, searchValue);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            count = result.getInt(result.getColumnIndex("No"));
        }

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

    String readyMPK;

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


            String MPKListQuery = String.format(Locale.ENGLISH,"Select MPK from Posts Left Join Posts_Info\n" +
                    "    On Posts_Info.FMPK = Posts.MPK and Posts_Info.StatJobID = %d \n" +
                    "    where Posts.FIPK = %d and \n" +
                    "    Posts.StatJobID = Posts_Info.StatJobID ",lastID,FIPK);

            String sqlText = String.format(Locale.ENGLISH,"Select *  From ( " + QueryFromPart +" )" +limitQuery, MPKListQuery, FIPK, searchValue, pageCapacity, index);

            readyMPK = MPKListQuery;

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
                    reportUser.caption = result.getString(result.getColumnIndex("Caption"));
                    reportUser.comment = result.getString(result.getColumnIndex("CommentText"));
                    reportUser.PicURL = result.getString(result.getColumnIndex("PicURL"));
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

    @Override
    public void calculateCount() throws Exception
    {
        calculateCount(readyMPK);
    }
}
