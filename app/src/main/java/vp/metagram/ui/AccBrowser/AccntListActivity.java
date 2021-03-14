package vp.metagram.ui.AccBrowser;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.LinkedHashMap;
import java.util.Map;


import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.AccBrowser.ListSource.ListSource;
import vp.metagram.ui.AccBrowser.ListSource.ListSource_Followers_Online;
import vp.metagram.ui.AccBrowser.ListSource.ListSource_Followings_Online;
import vp.metagram.utils.instagram.types.User;

import static vp.metagram.general.functions.setTextViewFontForMessage;


public class AccntListActivity extends BaseActivity
{

    long IPK;
    int position;
    String username;
    int noOfFollowers;
    int noOfFollowings;

    ImageButton backButton;

    TextView usernameTextView;
    ViewPager viewPager;
    TabLayout tabLayout;

    PagerAdapter pagerAdapter;
    ListSource followersListSource;
    ListSource followingsListSource;

    Map<Long,User> followersList = new LinkedHashMap<>(10, 0.75F, true);
    Map<Long,User> followingsList = new LinkedHashMap<>(10, 0.75F, true);


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accnt_list);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {finish();}

        IPK = bundle.getLong("ipk");
        position = bundle.getInt("position");
        username = bundle.getString("username");
        noOfFollowers = bundle.getInt("noOfFollowers");
        noOfFollowings = bundle.getInt("noOfFollowings");

        prepareUIElements();
    }

    private void prepareUIElements()
    {
        viewPager = findViewById(R.id.AccntList_viewPager);
        tabLayout = findViewById(R.id.AccntList_tabLayout);
        usernameTextView = findViewById(R.id.AccntList_title);

        backButton = findViewById(R.id.ReportSelector_backButton);
        backButton.setOnClickListener((View v)->{onBackPressed();});


        followingsListSource = new ListSource_Followings_Online(IPK,followingsList);
        followersListSource = new ListSource_Followers_Online(IPK,followersList);

        pagerAdapter = new AccntListPageAdapter(getSupportFragmentManager(), followersListSource, noOfFollowers, followingsListSource, noOfFollowings);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(position);

        usernameTextView.setText(username);
        setTextViewFontForMessage(this,usernameTextView);
    }
}
