package vp.metagram.ui.ReportUI.reportViewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;


import me.relex.circleindicator.CircleIndicator;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;

import static vp.metagram.general.functions.convertRankToIndicator;
import static vp.metagram.general.functions.setCheckBoxFontForMessage;
import static vp.metagram.general.functions.setImageViewEnabled;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.functions.setTextViewFontRank;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.commentedButDidNotFollow;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.lazyFollowers;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.likedButDidNotFollow;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.notFollowedBack;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.notFollowingBack;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.peopleCommentedMost;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.peopleEngagedMost;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.peopleLikedMost;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.peopleUnfollowedYou;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.peopleYouUnfollowed;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.postsWithMostComments;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.postsWithMostLikes;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.postsWithMostViews;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.removedComments;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.removedLikes;



public class ReportSelectorActivity extends BaseActivity
{
    int OrderID;

    int noOfFollowers;
    int noOfFollowings;

    boolean F_Parameter;
    boolean D_Parameter;
    boolean P_Parameter;
    boolean R_Parameter;

    int noOfJobs;

    String username;
    long IPK;
    String rank;
    boolean isPrivate;

    ImageView f_parameter_icon;
    ImageView p_parameter_icon;
    ImageView d_parameter_icon;
    ImageView r_parameter_icon;

    TextView titleTextView;
    TextView rankTextView;
    RatingBar rankRateBar;

    ImageButton backButton;

    List<ReportType> validReports = new ArrayList<>();

    int noOfPages;
    int reportsPerPage = 6;

    ViewPager viewPager;

    ReportSelectorPageAdaptor adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_selector);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            F_Parameter = extras.getBoolean("F_Parameter");
            D_Parameter = extras.getBoolean("D_Parameter");
            P_Parameter = extras.getBoolean("P_Parameter");
            R_Parameter = extras.getBoolean("R_Parameter");
            noOfJobs = extras.getInt("noOfJobs");
            OrderID = extras.getInt("OrderID");
            username = extras.getString("username");
            IPK = extras.getLong("IPK");
            rank = extras.getString("rank");
            isPrivate = extras.getBoolean("isPrivate");
            noOfFollowers = extras.getInt("noOfFollowers");
            noOfFollowings = extras.getInt("noOfFollowings");

        }

        validateReports();

        findUIElements();

        prepareUIElements();

        arrangePages();

    }

    public void findUIElements()
    {
        f_parameter_icon = findViewById(R.id.ReportSelector_f_parameter_icon);
        p_parameter_icon = findViewById(R.id.ReportSelector_p_parameter_icon);
        d_parameter_icon = findViewById(R.id.ReportSelector_d_parameter_icon);
        r_parameter_icon = findViewById(R.id.ReportSelector_r_parameter_icon);

        titleTextView = findViewById(R.id.ReportSelector_titleTextView);
        rankTextView = findViewById(R.id.ReportSelector_rankTextView);

        rankRateBar = findViewById(R.id.ReportSelector_rankRateBar);

        backButton = findViewById(R.id.ReportSelector_backButton);

        viewPager = findViewById(R.id.ReportSelector_viewPager);


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

    }

    public void validateReports()
    {
        if (P_Parameter && D_Parameter)
        {
            validReports.add(peopleLikedMost);

            validReports.add(peopleCommentedMost);

            validReports.add(peopleEngagedMost);
        }

        if(P_Parameter)
        {
            validReports.add(postsWithMostLikes);
            validReports.add(postsWithMostComments);
            validReports.add(postsWithMostViews);
        }

        if (F_Parameter && D_Parameter && P_Parameter)
        {
            validReports.add(likedButDidNotFollow);

            validReports.add(commentedButDidNotFollow);

            validReports.add(lazyFollowers);
        }

        if (F_Parameter && R_Parameter && noOfJobs > 1)
        {
            validReports.add(peopleUnfollowedYou);
            validReports.add(peopleYouUnfollowed);
        }

        if (P_Parameter && D_Parameter && R_Parameter && noOfJobs > 1)
        {
            validReports.add(removedLikes);

            validReports.add(removedComments);
        }

        if (F_Parameter)
        {
            validReports.add(notFollowedBack);
            validReports.add(notFollowingBack);
        }

    }

    public void arrangePages()
    {
        noOfPages = (validReports.size() / reportsPerPage)+1;

        adapter = new ReportSelectorPageAdaptor(this);

        viewPager.setAdapter(adapter);

        CircleIndicator indicator =  findViewById(R.id.ReportSelector_indicator);
        indicator.bringToFront();
        indicator.setViewPager(viewPager);
    }

    class ReportSelectorPageAdaptor extends PagerAdapter
    {
        Context context;

        public ReportSelectorPageAdaptor(Context context)
        {
            this.context = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ConstraintLayout)inflater.inflate(R.layout.layout_report_selector,null);

            int id = position*reportsPerPage;


            prepareButton(layout.findViewById(R.id.imageButton_1), layout.findViewById(R.id.textView_1), id);
            id++;

            prepareButton(layout.findViewById(R.id.imageButton_2), layout.findViewById(R.id.textView_2), id);
            id++;

            prepareButton(layout.findViewById(R.id.imageButton_3), layout.findViewById(R.id.textView_3), id);
            id++;

            prepareButton(layout.findViewById(R.id.imageButton_4), layout.findViewById(R.id.textView_4), id);
            id++;

            prepareButton(layout.findViewById(R.id.imageButton_5), layout.findViewById(R.id.textView_5), id);
            id++;

            prepareButton(layout.findViewById(R.id.imageButton_6), layout.findViewById(R.id.textView_6), id);
            id++;

            collection.addView(layout);
            return layout;
        }

        public void prepareButton(ImageButton imageButton, TextView textView, int id)
        {
            View.OnClickListener onClickListener = (View v)->
            {
                Intent intent = new Intent(context, ReportViewerActivity.class);
                intent.putExtra("reportID",(Integer)v.getTag());
                intent.putExtra("IPK",IPK);
                intent.putExtra("username",username);
                intent.putExtra("isPrivate",isPrivate);
                intent.putExtra("noOfFollowers",noOfFollowers);
                intent.putExtra("noOfFollowings",noOfFollowings);

                context.startActivity(intent);

            };


            if (id < validReports.size())
            {
                imageButton.setImageResource(validReports.get(id).getIconID());
                imageButton.setTag(validReports.get(id).ordinal);
                setCheckBoxFontForMessage(context, textView);
                textView.setText(context.getResources().getString(validReports.get(id).getTitleID()));
                textView.setTag(validReports.get(id).ordinal);

                imageButton.setOnClickListener(onClickListener);
                textView.setOnClickListener(onClickListener);
            }
        }

        @Override
        public int getCount()
        {
            return noOfPages;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
        {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view)
        {
            collection.removeView((View) view);
        }
    }

}
