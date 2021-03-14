package vp.metagram.ui.ReportUI.chartViewer.adapter;

import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import vp.metagram.R;
import vp.metagram.ui.ReportUI.chartViewer.types.ChartType;
import vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType;


public abstract class ChartAdaptor
{
    protected Context context;
    protected ChartType chartType;
    protected MatrixCursor queryResult;
    protected QueryStepType stepType;

    List<BarEntry> barEntries;
    List<Entry> lineEntries;

    public static Semaphore QueryMutex = new Semaphore(1);

    public ChartAdaptor(Context context, ChartType chartType, QueryStepType stepType)
    {
        this.context = context;
        this.chartType = chartType;
        this.stepType = stepType;
    }

    abstract public<DataList> List<DataList> getEntries() throws Exception;

    abstract public void queryData() throws Exception;

    abstract public IAxisValueFormatter getXAxisValueFormatter() throws Exception;


    Semaphore chartMutex = new Semaphore(1);

    public void prepareBarEntries() throws Exception
    {
        chartMutex.acquire();
        try
        {
            barEntries = getEntries();
        }
        finally
        {
            chartMutex.release();
        }

    }

    public void prepareLineEntries() throws Exception
    {

        chartMutex.acquire();
        try
        {
            lineEntries = getEntries();
        }
        finally
        {
            chartMutex.release();
        }
    }

    public void prepareBarChart(BarChart barChart) throws Exception
    {

        if (barEntries.size() == 0)
        { barEntries.add(new BarEntry(0,0)); }

        if (barEntries.size() <= 0) {return;}

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColor(R.color.sCoolGrayC1, 140);
        barDataSet.setBarBorderColor(R.color.sBlack);
        barDataSet.setBarBorderWidth(1);
        barDataSet.setValueTextSize(12f);


        BarData barData = new BarData(barDataSet);
        barData.setHighlightEnabled(false);
        barData.notifyDataChanged();

        barChart.setData(barData);
        barChart.notifyDataSetChanged();
        barChart.setDrawGridBackground(false);


        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(8f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-30);



        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);



        Legend legend = barChart.getLegend();
        legend.setEnabled(false);


        barChart.setDescription(null);
        barChart.setPinchZoom(true);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setHighlightPerTapEnabled(true);
        barChart.setScaleMinima((float) barData.getXMax() / 10f, 1f);
        barChart.moveViewToX(barData.getXMax());
        barChart.setTouchEnabled(true);


        if ( barEntries.size() < 10 )
        {
            barData.setBarWidth((float) barEntries.size() / 10);
        }


        barChart.moveViewToX(barData.getXMax());
        barChart.notifyDataSetChanged();
        barChart.invalidate();

    }

    public void prepareLineChart(LineChart lineChart) throws Exception
    {


        if (lineEntries.size() == 0)
        { lineEntries.add(new BarEntry(0,0)); }

        if (lineEntries.size() <= 0) {return;}

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "");
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(R.color.sCoolGrayC5);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setCircleColorHole(Color.BLACK);
        lineDataSet.setValueTextSize(12f);


        LineData lineData = new LineData(lineDataSet);
        lineData.setHighlightEnabled(false);
        lineData.notifyDataChanged();

        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(8f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-30);


        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);


        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        lineChart.setDescription(null);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setHighlightPerTapEnabled(true);
        lineChart.setScaleMinima((float) lineData.getXMax() / 10f, 1f);
        lineChart.moveViewToX(lineData.getXMax());
        lineChart.setTouchEnabled(true);

        lineChart.moveViewToX(lineData.getXMax());
        lineChart.invalidate();

    }

    public boolean dateChanged(long oldDatetime, long newDatetime)
    {
        int oldYear; int oldMonth; int oldDay;
        int newYear; int newMonth; int newDay;

        Calendar oldCalendar = Calendar.getInstance();
        oldCalendar.setTime(new Date(oldDatetime));
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(new Date(newDatetime));

        oldYear = oldCalendar.get(Calendar.YEAR);
        oldMonth = oldCalendar.get(Calendar.MONTH);
        oldDay = oldCalendar.get(Calendar.DATE);

        newYear = newCalendar.get(Calendar.YEAR);
        newMonth = newCalendar.get(Calendar.MONTH);
        newDay = newCalendar.get(Calendar.DATE);

        if (oldDay == newDay && oldMonth == newMonth &&  oldYear == newYear)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean monthChanged(long oldDatetime, long newDatetime)
    {
        int oldYear; int oldMonth;
        int newYear; int newMonth;

        Calendar oldCalendar = Calendar.getInstance();
        oldCalendar.setTime(new Date(oldDatetime));
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(new Date(newDatetime));

        oldYear = oldCalendar.get(Calendar.YEAR);
        oldMonth = oldCalendar.get(Calendar.MONTH);

        newYear = newCalendar.get(Calendar.YEAR);
        newMonth = newCalendar.get(Calendar.MONTH);

        if (oldMonth == newMonth &&  oldYear == newYear)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean weekChanged(long oldDatetime, long newDatetime)
    {
        int oldYear; int oldWeekOfYear;
        int newYear; int newWeekOfYear;

        Calendar oldCalendar = Calendar.getInstance();
        oldCalendar.setTime(new Date(oldDatetime));
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(new Date(newDatetime));

        oldYear = oldCalendar.get(Calendar.YEAR);
        oldWeekOfYear = oldCalendar.get(Calendar.WEEK_OF_YEAR);

        newYear = newCalendar.get(Calendar.YEAR);
        newWeekOfYear = newCalendar.get(Calendar.WEEK_OF_YEAR);

        if (oldWeekOfYear == newWeekOfYear &&  oldYear == newYear)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
