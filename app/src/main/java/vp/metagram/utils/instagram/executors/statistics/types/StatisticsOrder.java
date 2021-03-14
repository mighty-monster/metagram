package vp.metagram.utils.instagram.executors.statistics.types;

import android.database.MatrixCursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import static vp.metagram.general.variables.dbMetagram;

public class StatisticsOrder
{
    public int OrderID;
    public long FIPK;
    public long IPK;
    public JSONObject userInfo;
    public boolean F_Parameter;
    public boolean P_Parameter;
    public boolean D_Parameter;
    public boolean R_Parameter;

    public int interval;

    public StatisticsOrder(int OrderID) throws IOException, GeneralSecurityException, JSONException
    {
        this.OrderID = OrderID;

        String sqlText = String.format(Locale.ENGLISH, "Select * from Statistics_Orders Where StatOrderID = %d",OrderID);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);

        result.moveToFirst();

        FIPK = result.getLong(result.getColumnIndex("FIPK"));
        IPK = result.getLong(result.getColumnIndex("IPK"));
        userInfo = new JSONObject(result.getString(result.getColumnIndex("UserInfo")));

        F_Parameter = result.getInt(result.getColumnIndex("F_Parameter")) == 1;
        P_Parameter = result.getInt(result.getColumnIndex("P_Parameter")) == 1;
        D_Parameter = result.getInt(result.getColumnIndex("D_Parameter")) == 1;
        R_Parameter = result.getInt(result.getColumnIndex("autoRefresh")) == 1;

        interval = result.getInt(result.getColumnIndex("intervalInSeconds"));

    }
}
