package vp.metagram.ui.ReportUI.chartViewer;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.ReportUI.chartViewer.types.ChartName;
import vp.metagram.ui.ReportUI.chartViewer.types.ChartType;
import vp.metagram.ui.ReportUI.chartViewer.types.QueryStepType;

import static vp.metagram.general.functions.convertRankToIndicator;
import static vp.metagram.general.functions.setImageViewEnabled;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.functions.setTextViewFontRank;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.addedComments;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.addedLikes;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.addedViews;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.engagementInTime;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.followersInTime;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.followingsInTime;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.totalCommentsInTime;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.totalLikesInTime;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartName.totalViewsInTime;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartType.barChart;
import static vp.metagram.ui.ReportUI.chartViewer.types.ChartType.lineChart;

public class ChartViewerActivity extends BaseActivity
{

    int OrderID;
    boolean F_Parameter;
    boolean D_Parameter;
    boolean P_Parameter;
    boolean R_Parameter;

    String username;
    long IPK;
    String rank;

    ImageButton backButton;

    ImageView f_parameter_icon;
    ImageView p_parameter_icon;
    ImageView d_parameter_icon;
    ImageView r_parameter_icon;

    TextView titleTextView;
    TextView rankTextView;
    RatingBar rankRateBar;

    int noOfPages;

    ViewPager viewPager;
    PagerAdapter adapter;

    List<ChartName> validCharts = new ArrayList<>();

    Button leftButton;
    Button rightButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_viewer);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            F_Parameter = extras.getBoolean("F_Parameter");
            D_Parameter = extras.getBoolean("D_Parameter");
            P_Parameter = extras.getBoolean("P_Parameter");
            R_Parameter = extras.getBoolean("R_Parameter");
            OrderID = extras.getInt("OrderID");
            username = extras.getString("username");
            IPK = extras.getLong("IPK");
            rank = extras.getString("rank");

        }

        validateCharts();

        findUIElements();

        prepareUIElements();


    }

    public void validateCharts()
    {
        validCharts.add(engagementInTime);
        validCharts.add(followersInTime);
        validCharts.add(followingsInTime);

        if (P_Parameter)
        {
            validCharts.add(totalLikesInTime);
            validCharts.add(totalCommentsInTime);
            validCharts.add(totalViewsInTime);
            validCharts.add(addedLikes);
            validCharts.add(addedComments);
            validCharts.add(addedViews);
        }

        noOfPages = validCharts.size();
    }

    public void findUIElements()
    {
        f_parameter_icon = findViewById(R.id.ChartViewer_f_parameter_icon);
        p_parameter_icon = findViewById(R.id.ChartViewer_p_parameter_icon);
        d_parameter_icon = findViewById(R.id.ChartViewer_d_parameter_icon);
        r_parameter_icon = findViewById(R.id.ChartViewer_r_parameter_icon);

        titleTextView = findViewById(R.id.ChartViewer_titleTextView);
        rankTextView = findViewById(R.id.ChartViewer_rankTextView);

        rankRateBar = findViewById(R.id.ChartViewer_rankRateBar);

        backButton = findViewById(R.id.ChartViewer_backButton);

        viewPager = findViewById(R.id.ChartViewer_viewPager);

        leftButton = findViewById(R.id.ChartViewer_leftButton);
        rightButton = findViewById(R.id.chartViewer_rightButton);
    }

    public void prepareUIElements()
    {
        setImageViewEnabled(this,F_Parameter,f_parameter_icon,R.drawable.ic_f_parameter);
        setImageViewEnabled(this,P_Parameter,p_parameter_icon,R.drawable.ic_p_parameter);
        setImageViewEnabled(this,D_Parameter,d_parameter_icon,R.drawable.ic_d_parameter);
        setImageViewEnabled(this,R_Parameter,r_parameter_icon,R.drawable.ic_r_parameter);

        setTextViewFontForMessage(this,titleTextView);
        titleTextView.setText(username);

        setTextViewFontRank(this, rankTextView);
        rankTextView.setText("Rank: " + rank);

        rankRateBar.setRating(convertRankToIndicator(rank));

        backButton.setOnClickListener((View v)-> onBackPressed());


        adapter = new ChartPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        CircleIndicator indicator =  findViewById(R.id.ChartViewer_indicator);
        indicator.bringToFront();
        indicator.setViewPager(viewPager);

        leftButton.setOnClickListener((View v)->
                viewPager.setCurrentItem(viewPager.getCurrentItem()-1));

        rightButton.setOnClickListener((View v)->
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1));
    }


    class ChartPagerAdapter extends FragmentPagerAdapter
    {
        public ChartPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            ChartName chartName = validCharts.get(position);
            ChartContainer chartContainer = null;
            switch ( chartName )
            {
                case addedLikes:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart},
                            new QueryStepType[]{QueryStepType.perCall},IPK);
                    break;
                case addedComments:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart},
                            new QueryStepType[]{QueryStepType.perCall},IPK);
                    break;
                case addedViews:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart},
                            new QueryStepType[]{QueryStepType.perCall},IPK);
                    break;
                case totalCommentsInTime:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart,lineChart},
                            new QueryStepType[]{QueryStepType.perCall, QueryStepType.perDay, QueryStepType.perWeek, QueryStepType.perMonth},IPK);
                    break;
                case totalViewsInTime:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart,lineChart},
                            new QueryStepType[]{QueryStepType.perCall, QueryStepType.perDay, QueryStepType.perWeek, QueryStepType.perMonth},IPK);
                    break;
                case totalLikesInTime:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart,lineChart},
                            new QueryStepType[]{QueryStepType.perCall, QueryStepType.perDay, QueryStepType.perWeek, QueryStepType.perMonth},IPK);
                    break;
                case followingsInTime:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart,lineChart},
                            new QueryStepType[]{QueryStepType.perCall, QueryStepType.perDay, QueryStepType.perWeek, QueryStepType.perMonth},IPK);
                    break;
                case engagementInTime:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart,lineChart},
                            new QueryStepType[]{QueryStepType.perCall, QueryStepType.perDay, QueryStepType.perWeek, QueryStepType.perMonth},IPK);
                    break;
                case followersInTime:
                    chartContainer = ChartContainer.newInstance(chartName,new ChartType[] {barChart,lineChart},
                            new QueryStepType[]{QueryStepType.perCall, QueryStepType.perDay, QueryStepType.perWeek, QueryStepType.perMonth},IPK);
                    break;


            }
            return chartContainer;
        }

        @Override
        public int getCount()
        {
            return noOfPages;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {

        }
    }



}
