package vp.metagram.ui.ReportUI.reportViewer.listSource;

import android.database.MatrixCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.metagram.ui.ReportUI.reportViewer.types.ReportUser;

import static vp.metagram.general.variables.dbMetagram;


public class LazyFollowersSource extends ReportListSource
{
    long FIPK = -1;
    String searchValue;

    int lastID = -1;



    String QueryFromPart = " From Rel_Follower Left Join Others\n" +
            "   On Rel_Follower.FollowerIPK = Others.IPK\n" +
            "        where Rel_Follower.FIPK = %d and Username like '%%%s%%' and\n" +
            "        Rel_Follower.StatJobID =  \n" +
            "        (Select Statistics_Jobs.StatJobID as lastID from Statistics_Orders left Join Statistics_Jobs On Statistics_Jobs.StatJobID = \n" +
            "            (Select StatJobID from Statistics_Jobs Where              \n" +
            "                Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID and Status = 'done' order by StatJobID desc limit 1)\n" +
            "                 where Statistics_Orders.IPK = %d) AND IPK Not IN ( %s ) AND IPK Not IN ( %s ) ";

    String getLastIDQuery = "Select Statistics_Jobs.StatJobID as lastID from Statistics_Orders left Join Statistics_Jobs\n" +
            "      On Statistics_Jobs.StatJobID = \n" +
            "    (Select StatJobID from Statistics_Jobs Where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID and Status = 'done'  order by StatJobID desc limit 1) \n" +
            "      where Statistics_Orders.IPK = %d";

    public LazyFollowersSource(long FIPK, List<ReportUser> sourceList, String searchValue)
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

    public void calculateCount(String likerIPKListQuery, String commenterIPKListQuery) throws Exception
    {
        String sqlText = String.format(Locale.ENGLISH,"Select Count(*) as No " + QueryFromPart,FIPK , searchValue, FIPK, likerIPKListQuery, commenterIPKListQuery);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            count = result.getInt(result.getColumnIndex("No"));
        }

    }

    String readyLikerIPKListQuery;
    String readyCommenterIPKListQueryy;



    @Override
    public List<ReportUser> getNextList() throws Exception
    {

        getLastID();


        if (FIPK < 0)
        {
            clearList();
        }
        else
        {
            String MPKListQuery = String.format(Locale.ENGLISH,"Select MPK from Posts Left Join Posts_Info\n" +
                    "    On Posts_Info.FMPK = Posts.MPK and Posts_Info.StatJobID = %d \n" +
                    "    where Posts.FIPK = %d and \n" +
                    "    Posts.StatJobID = Posts_Info.StatJobID ",lastID,FIPK);


            String likerIPKListQuery = String.format(Locale.ENGLISH," Select LikerIPK from Rel_Like Where FMPK in\n" +
                    "     ( %s ) ",MPKListQuery);

            readyLikerIPKListQuery = likerIPKListQuery;

            String commenterIPKListQuery = String.format(Locale.ENGLISH," Select CommenterIPK from Rel_Comment Where FMPK in\n" +
                    "     ( %s ) ",MPKListQuery);

            readyCommenterIPKListQueryy = commenterIPKListQuery;

            List<ReportUser> newList = new ArrayList<>();

            String sqlText = String.format(Locale.ENGLISH," Select IPK,Username,PictureURL " + QueryFromPart + limitQuery
                     ,FIPK,searchValue,FIPK,likerIPKListQuery,commenterIPKListQuery,pageCapacity,index);

            MatrixCursor result = dbMetagram.selectQuery(sqlText);
            if (result.moveToFirst())
            {
                while ( !result.isAfterLast() )
                {
                    ReportUser reportUser = new ReportUser();
                    reportUser.IPK = result.getLong(result.getColumnIndex("IPK"));
                    reportUser.username = result.getString(result.getColumnIndex("Username"));
                    reportUser.picUrl = result.getString(result.getColumnIndex("PictureURL"));
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
        calculateCount(readyLikerIPKListQuery, readyCommenterIPKListQueryy);
    }
}
