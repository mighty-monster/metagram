package vp.metagram.ui.ReportUI.chartViewer.adapter;

import android.content.Context;
import androidx.core.util.Pair;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.metagram.ui.ReportUI.chartViewer.types.ChartType;
import vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType;

import static vp.metagram.general.functions.getDateFromTimeStamp;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartType.barChart;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartType.lineChart;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateDone;


public class addedCommentsAdaptor extends ChartAdaptor
{
    long IPK;
    List<Pair<Long,Pair<Long,Long>>> results = new ArrayList<>();

    public addedCommentsAdaptor(Context context, ChartType chartType, QueryStepType stepType, long IPK) throws Exception
    {
        super(context, chartType, stepType);

        if (chartType != barChart && chartType != lineChart)
        {
            throw new Exception("invalid chart type");
        }

        this.IPK = IPK;
    }

    @Override
    public void queryData() throws Exception
    {
        QueryMutex.acquire();
        try
        {
            String sqlText = "";
            sqlText = String.format(Locale.ENGLISH, " Select Result,EndTime from Statistics_Jobs Left Join Statistics_Orders\n" +
                    "    on Statistics_Orders.StatOrderID = Statistics_Jobs.StatOrderID\n" +
                    "        Where Statistics_Orders.IPK = %d and Statistics_Jobs.Status = '%s' Order By EndTime asc", IPK, statusStateDone);

            queryResult = dbMetagram.selectQuery(sqlText);
        }
        finally
        {
            QueryMutex.release();
        }
    }


    @Override
    public List getEntries() throws Exception
    {
        queryData();
        fillResult();


        if (chartType == barChart)
        {
            List<BarEntry> barEntries = new ArrayList<>();
            for (Pair<Long,Pair<Long,Long>> pair:results)
            {
                barEntries.add(new BarEntry(pair.first,pair.second.second));
            }
            return barEntries;
        }
        else if (chartType == lineChart)
        {
            List<Entry> lineEntries = new ArrayList<>();
            for (Pair<Long,Pair<Long,Long>> pair:results)
            {
                lineEntries.add(new BarEntry(pair.first,pair.second.second));
            }
            return lineEntries;
        }

        return  null;
    }

    @Override
    public void prepareBarChart(BarChart barChart) throws Exception
    {
        super.prepareBarChart(barChart);
        BarData barData = barChart.getBarData();
        barData.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.format("%,d",(int)value));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setValueFormatter((value, axis) -> String.format("%,d", (int) value));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(getXAxisValueFormatter());
    }


    public void fillResult() throws Exception
    {
        long noOfComments = 0;
        long noOfAddedComments;
        if ( queryResult.moveToFirst() )
        {
            long index = 0;
            String objectName = "totalComments";
            while ( !queryResult.isAfterLast() )
            {
                String resultStr = dbMetagram.AESCipher.decryptFromHexToString(
                        queryResult.getString(queryResult.getColumnIndex("Result")));
                JSONObject resultObject = new JSONObject(resultStr);

                long endTime = queryResult.getLong(queryResult.getColumnIndex("EndTime"));

                long tubeComments = -1;
                try
                {
                    tubeComments = resultObject.getLong(objectName);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    queryResult.moveToNext();
                    continue;
                }

                if (noOfComments == 0)
                {
                    noOfComments = tubeComments;
                }
                else
                {
                    noOfAddedComments = tubeComments - noOfComments;
                    noOfComments = tubeComments;

                    Pair<Long,Pair<Long,Long>> newItem = new Pair(
                            new Long(index),
                            new Pair<>(new Long(endTime),noOfAddedComments));
                    results.add(newItem);
                    index++;
                }

                queryResult.moveToNext();

            }
        }
    }

    @Override
    public IAxisValueFormatter getXAxisValueFormatter() throws Exception
    {
        return (value, axis) ->
        {
            String returnValue = "";
            int index = (int) value;
            int resultSize = results.size();
            if (index >=0 && index < resultSize)
            {
                Pair<Long, Pair<Long, Long>> pair = results.get(index);
                if ( pair != null )
                {
                    returnValue = getDateFromTimeStamp(context, pair.second.first);
                }
            }

            return returnValue;
        };
    }

}
