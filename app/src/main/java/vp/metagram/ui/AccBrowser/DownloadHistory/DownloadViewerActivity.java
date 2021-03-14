package vp.metagram.ui.AccBrowser.DownloadHistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.tools.io.iFileSystemUtils.GetDownloadDir;

public class DownloadViewerActivity extends BaseActivity
{
    String username;
    String downloadsAddress;

    ImageButton backButton;

    TextView usernameTextView;
    ViewPager viewPager;
    TabLayout tabLayout;

    PagerAdapter pagerAdapter;

    DownloadViewerFragment IGTV;
    DownloadViewerFragment Posts;
    DownloadViewerFragment Story;
    DownloadViewerFragment Highlight;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_viewer);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            username = extras.getString("username");
            downloadsAddress = GetDownloadDir(this) + username + "/";
        }

        findUIElements();
        configureUIElements();

        createFragmentsAndConfigurePager();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void findUIElements()
    {
        backButton = findViewById(R.id.DownloadViewer_backButton);
        usernameTextView = findViewById(R.id.DownloadViewer_title);
        viewPager = findViewById(R.id.DownloadViewer_viewPager);
        tabLayout = findViewById(R.id.DownloadViewer_tabLayout);
    }

    private void configureUIElements()
    {
        usernameTextView.setText(username);
        setTextViewFontForMessage(this,usernameTextView);

        backButton.setOnClickListener((View v)->finish());
    }

    private void createFragmentsAndConfigurePager()
    {
        IGTV = new DownloadViewerFragment(downloadsAddress + "IGTV/");
        Story = new DownloadViewerFragment(downloadsAddress + "Story/");
        Highlight = new DownloadViewerFragment(downloadsAddress + "Highlight/");
        Posts = new DownloadViewerFragment(downloadsAddress + "Post/");
        pagerAdapter = new DownloadViewerPageAdaptor(getSupportFragmentManager(), IGTV, Story, Highlight, Posts);

        viewPager.setOffscreenPageLimit(4);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

}
