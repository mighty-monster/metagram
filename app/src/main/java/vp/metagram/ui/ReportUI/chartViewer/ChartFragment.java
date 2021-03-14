package vp.metagram.ui.ReportUI.chartViewer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import vp.metagram.ui.ReportUI.chartViewer.adapter.ChartAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.addedCommentsAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.addedLikesAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.addedViewsAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.engagementInTimeAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.followersInTimeAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.followingsInTimeAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.totalCommentsInTimeAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.totalLikesInTimeAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.adapter.totalViewsInTimeAdaptor;
import vp.metagram.ui.ReportUI.chartViewer.types.ChartName;
import vp.metagram.ui.ReportUI.chartViewer.types.ChartType;
import vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType;

import static vp.metagram.general.variables.threadPoolExecutor;

public class ChartFragment extends Fragment
{

    ChartType chartType;
    ChartName chartName;
    QueryStepType stepType;

    ChartAdaptor chartAdaptor;

    long IPK;

    ChartContainer parent;


    static public ChartFragment newInstance(ChartContainer parent, ChartType chartType, ChartName chartName, QueryStepType stepType, long IPK)
    {
        ChartFragment chartFragment = new ChartFragment();

        chartFragment.chartType = chartType;
        chartFragment.chartName = chartName;
        chartFragment.stepType = stepType;
        chartFragment.IPK = IPK;
        chartFragment.parent = parent;


        return chartFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View result = null;
        try
        {
            result = createTheChart();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //TODO Add error handling
        }

        return result;
    }

    public void prepareChartData() throws Exception
    {
        switch ( chartName )
        {
            case followersInTime:
                chartAdaptor = new followersInTimeAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case followingsInTime:
                chartAdaptor = new followingsInTimeAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case engagementInTime:
                chartAdaptor = new engagementInTimeAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case totalCommentsInTime:
                chartAdaptor = new totalCommentsInTimeAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case totalLikesInTime:
                chartAdaptor = new totalLikesInTimeAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case totalViewsInTime:
                chartAdaptor = new totalViewsInTimeAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case addedComments:
                chartAdaptor = new addedCommentsAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case addedLikes:
                chartAdaptor = new addedLikesAdaptor(getActivity(),chartType, stepType, IPK);
                break;
            case addedViews:
                chartAdaptor = new addedViewsAdaptor(getActivity(),chartType, stepType, IPK);
                break;

        }
    }

    public View createTheChart() throws Exception
    {
        View result = null;
        prepareChartData();
        switch ( chartType )
        {
            case barChart:
                result = createBarChart();
                break;
            case lineChart:
                result = createLineChart();
                break;
            case pieChart:
                result = createPieChart();
                break;
            case lineChartMultiple:
                result = createLineChartMultiple();
                break;
            case barChartMultiple:
                result = createBarChartMultiple();
                break;
            case barChartStacked:
                result = createBarChartStacked();
                break;
        }
        return result;
    }

    public BarChart createBarChart()
    {
        BarChart barChart = new BarChart(getActivity());
        barChart.setVisibility(View.INVISIBLE);
        threadPoolExecutor.execute(()->
        {
            try
            {
                chartAdaptor.prepareBarEntries();

                new Handler(Looper.getMainLooper()).post(()->
                {
                    try
                    {
                        chartAdaptor.prepareBarChart(barChart);
                        barChart.setVisibility(View.VISIBLE);
                        parent.loadingFinished();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });


        return barChart;
    }

    public LineChart createLineChart()
    {
        LineChart lineChart = new LineChart(getActivity());
        lineChart.setVisibility(View.INVISIBLE);
        threadPoolExecutor.execute(()->
        {
            try
            {
                chartAdaptor.prepareLineEntries();

                new Handler(Looper.getMainLooper()).post(()->
                {
                    try
                    {
                        chartAdaptor.prepareLineChart(lineChart);
                        lineChart.setVisibility(View.VISIBLE);
                        parent.loadingFinished();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        return lineChart;
    }

    public PieChart createPieChart()
    {
        return new PieChart(getActivity());
    }

    public LineChart createLineChartMultiple()
    {
        return new LineChart(getActivity());
    }

    public BarChart createBarChartMultiple()
    {
        return new BarChart(getActivity());
    }

    public BarChart createBarChartStacked()
    {
        return new BarChart(getActivity());
    }
}
