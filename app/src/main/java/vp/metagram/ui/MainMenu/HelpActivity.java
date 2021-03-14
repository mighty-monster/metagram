package vp.metagram.ui.MainMenu;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.Locale;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.MainMenu.HelpFragments.HelpErrorFragment;
import vp.metagram.ui.MainMenu.HelpFragments.HelpTrainingFragment;

import static vp.metagram.general.functions.configShimmer;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appVersion;


public class HelpActivity extends BaseActivity
{
    TabLayout tabLayout;
    ViewPager viewPager;
    PagerAdapter adapter;

    String externalReqError = "";

    ShimmerFrameLayout menuExternalShimmer;
    TextView menuExternalShimmerTextView;
    ShimmerFrameLayout menuInternalShimmer;
    TextView menuInternalShimmerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        if ( getResources().getBoolean(R.bool.portrait_only) )
        {setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);}

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.training_title)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.errorHandling_title)));

        tabLayout.setTabTextColors(getResources().getColor(R.color.black), getResources().getColor(R.color.black));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        viewPager = findViewById(R.id.pager);
        adapter = new HelpPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);


        if ( Locale.getDefault().getLanguage().trim().equals("fa"))
        {
            viewPager.setRotationY(180);
        }

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for ( int j = 0; j < tabsCount; j++ )
        {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();
            for ( int i = 0; i < tabChildCount; i++ )
            {
                View tabViewChild = vgTab.getChildAt(i);
                if ( tabViewChild instanceof TextView )
                {
                    setTextViewFontForMessage(this, (TextView) tabViewChild);

                }
            }
        }

        TabLayout.Tab tab = tabLayout.getTabAt(0);
        tab.select();

        Intent intent = getIntent();
        if ( intent != null )
        {
            externalReqError = intent.getStringExtra("error");
        }

        menuExternalShimmer = findViewById(R.id.shimmer_view_container_external);
        menuInternalShimmer =  findViewById(R.id.shimmer_view_container_internal);
        menuExternalShimmerTextView = findViewById(R.id.shimmer_text_exteral);
        menuInternalShimmerTextView = findViewById(R.id.shimmer_text_internal);

        String shimmerText = "";
        if (appVersion.languagePartNo == 1)
            shimmerText = appVersion.get_appName_en();
        else if(appVersion.languagePartNo == 2)
            shimmerText = appVersion.get_appName_fa();

        configShimmer(this,
                shimmerText ,
                menuExternalShimmer,
                menuInternalShimmer,
                menuExternalShimmerTextView,
                menuInternalShimmerTextView,
                2000);


    }

    @Override
    public void onResume()
    {
        super.onResume();


        if ( externalReqError != null && externalReqError.equals("login") )
        {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            tab.select();

            new Handler(Looper.getMainLooper()).postDelayed(()->
            {
                HelpPagerAdapter helpAdapter = (HelpPagerAdapter)adapter;
                HelpErrorFragment errorFragment = helpAdapter.errorFragment;
                if (errorFragment != null)
                {
                    errorFragment.selectLoginError();
                }
            },500);

        }

    }


    class HelpPagerAdapter extends FragmentStatePagerAdapter
    {
        int mNumOfTabs;

        public HelpErrorFragment errorFragment;

        public HelpPagerAdapter(FragmentManager fm, int NumOfTabs)
        {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position)
        {

            switch ( position )
            {
                case 0:
                    return  new HelpTrainingFragment();
                case 1:
                    errorFragment = new HelpErrorFragment();
                    return errorFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount()
        {
            return mNumOfTabs;
        }


    }
}
