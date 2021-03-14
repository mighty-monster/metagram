package vp.metagram.ui.ReportUI.chartViewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import vp.metagram.R;
import vp.metagram.ui.ReportUI.chartViewer.types.ChartName;
import vp.metagram.ui.ReportUI.chartViewer.types.ChartType;
import vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.functions.shakeAnimation;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType.perCall;
import static vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType.perDay;
import static vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType.perMonth;
import static vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType.perWeek;

public class ChartContainer extends Fragment
{
    ChartType[] chartTypes;
    ChartType selectedType;
    ChartName chartName;
    QueryStepType[] stepTypes;
    QueryStepType selectedStepType;

    ImageButton barChartButton;
    ImageButton lineChartButton;
    TextView chartTitle;

    SpinKitView loadingView;

    View rootLayout;
    FrameLayout chartFrameLayout;

    long IPK = -1;

    Spinner selectTiming;

    ConstraintLayout controlLayout;

    String DBKey;

    static public ChartContainer newInstance(ChartName chartName, ChartType[] chartTypes, QueryStepType[] stepTypes, long IPK)
    {
        ChartContainer chartContainer = new ChartContainer();
        chartContainer.chartTypes = chartTypes;
        chartContainer.selectedType = chartTypes[0];
        chartContainer.chartName = chartName;
        chartContainer.stepTypes = stepTypes;
        chartContainer.selectedStepType = stepTypes[0];
        chartContainer.IPK = IPK;

        return chartContainer;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_chart_container, container, false);
        prepareUIElements();

        return rootLayout;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (chartTypes.length == 1)
        {
            // TODO  Handle chart types, and step types properly
            controlLayout.setVisibility(View.GONE);
        }

        try
        {
            loadChartInfo();

        }
        catch (Exception e)
        {
            setDefaults();
            e.printStackTrace();
        }


        loadTheChart();
    }

    public void setDefaults()
    {
        selectedType = chartTypes[0];
        selectedStepType = stepTypes[0];
        selectTiming.setSelection(0);
    }

    public void prepareUIElements()
    {
        chartFrameLayout = rootLayout.findViewById(R.id.chartContainer_chartFrameLayout);
        selectTiming = rootLayout.findViewById(R.id.chartContainer_selectTimingSpinner);
        controlLayout = rootLayout.findViewById(R.id.chartContainer_controlLayout);
        barChartButton = rootLayout.findViewById(R.id.chartContainer_barChartButton);
        lineChartButton = rootLayout.findViewById(R.id.chartContainer_lineChartButton);
        chartTitle = rootLayout.findViewById(R.id.chartContainer_chartTitle);
        loadingView = rootLayout.findViewById(R.id.chartContainer_loading);

        setTextViewFontForMessage(getActivity(),chartTitle);
        chartTitle.setText(getActivity().getResources().getString(chartName.getChartTitle()));

        List<String> spinnerData = new LinkedList<>(Arrays.asList(perCall.getStepTitle(),perDay.getStepTitle(),perWeek.getStepTitle(),perMonth.getStepTitle()));
        ArrayAdapter spinnerAdaptor = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,spinnerData);
        spinnerAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectTiming.setAdapter(spinnerAdaptor);

        selectTiming.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedStepType = QueryStepType.createFromTitle(spinnerData.get(position));
                saveChartInfo();
                loadTheChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });


        barChartButton.setOnClickListener((View v)->
        {
            selectedType = ChartType.barChart;
            shakeAnimation(getActivity(),v);
            saveChartInfo();
            loadTheChart();
        });

        lineChartButton.setOnClickListener((View v)->
        {
            selectedType = ChartType.lineChart;
            shakeAnimation(getActivity(),v);
            saveChartInfo();
            loadTheChart();
        });

        DBKey = String.format(Locale.ENGLISH,"%d_%s",Math.abs(IPK),
                getActivity().getResources().getString(chartName.getChartTitle()).replaceAll(" ", "_").trim());
    }

    public void loadTheChart()
    {

        ChartFragment chartFragment = ChartFragment.newInstance(this, selectedType,chartName,selectedStepType,IPK);
        getChildFragmentManager().beginTransaction().replace(R.id.chartContainer_chartFrameLayout, chartFragment).commit();
    }

    public void loadingFinished()
    {
        loadingView.setVisibility(View.GONE);
        chartFrameLayout.setVisibility(View.VISIBLE);
    }


    public void loadChartInfo() throws IOException, GeneralSecurityException, JSONException
    {
        JSONObject setting = new JSONObject( dbMetagram.getMiscellaneous(DBKey));
        selectedType = ChartType.valueOf(setting.getString("selectedType"));
        selectedStepType = QueryStepType.valueOf(setting.getString("selectedStepType"));
        selectTiming.setSelection(setting.getInt("selectedTypeID"));
    }

    public void saveChartInfo()
    {
        JSONObject setting = new JSONObject();
        try
        {
            setting.put("selectedType", selectedType.toString());
            setting.put("selectedStepType", selectedStepType.toString());
            setting.put("selectedTypeID", selectTiming.getSelectedItemPosition());
            dbMetagram.setMiscellaneous(DBKey, setting.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}
