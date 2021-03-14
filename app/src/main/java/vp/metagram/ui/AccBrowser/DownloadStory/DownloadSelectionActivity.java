package vp.metagram.ui.AccBrowser.DownloadStory;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import vp.igwa.IGTVList;
import vp.igwa.MediaList;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.AccBrowser.DownloadStory.highlight.HighlightFragment;
import vp.metagram.ui.AccBrowser.DownloadStory.igtv.IGTVFragment;
import vp.metagram.utils.instagram.types.HighlightItem;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.igpapi.IGWAException;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.AccBrowser.DownloadStory.highlight.HighlightItemType.Story;

public class DownloadSelectionActivity extends BaseActivity
{
    long IPK;
    String username;
    String picURL;

    ImageButton backButton;

    TextView usernameTextView;
    ViewPager viewPager;
    TabLayout tabLayout;

    PagerAdapter pagerAdapter;

    boolean isFirstTime = true;

    HighlightFragment highlightFragment;
    IGTVFragment igtvFragment;

    List<HighlightItem> highlightList = new ArrayList<>();

    boolean isStoryCollected = false;
    boolean isHighlightCollected = false;

    IGTVList igtvList = new IGTVList();
    MediaList postsList = new MediaList();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_selection);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            IPK = extras.getLong("ipk");
            picURL = extras.getString("picURL");
            username = extras.getString("username");
        }
        else
        { finish(); }


        highlightFragment = HighlightFragment.newInstance(highlightList, username);
        igtvFragment = IGTVFragment.newInstance(igtvList, username);

        prepareUIItems();
    }


    public void prepareUIItems()
    {
        viewPager = findViewById(R.id.DownloadSelection_viewPager);
        tabLayout = findViewById(R.id.DownloadSelection_tabLayout);
        usernameTextView = findViewById(R.id.DownloadSelection_title);

        backButton = findViewById(R.id.DownloadSelection_backButton);
        backButton.setOnClickListener((View v)-> onBackPressed());

        pagerAdapter = new DownloadStoryPageAdaptor(getSupportFragmentManager(),highlightFragment,igtvFragment);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        usernameTextView.setText(username);
        setTextViewFontForMessage(this,usernameTextView);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (isFirstTime)
        {
            isFirstTime = false;
            loadData();
        }
    }

    public void loadData()
    {
        threadPoolExecutor.execute(()->
        {
            try
            {
                getStories();
                checkLoadingStatus();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                isStoryCollected = true;
                checkLoadingStatus();
            }

            try
            {
                getHighlights();
                checkLoadingStatus();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                isHighlightCollected = true;
                checkLoadingStatus();
            }

        });

        threadPoolExecutor.execute(()->
        {
            try
            {
                getIGTVs();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        });




    }

    public void getStories() throws IOException, IGWAException, NoSuchAlgorithmException, JSONException
    {
        List<PostMedia> mediaList = new ArrayList<>();
        metagramAgent.activeAgent.proxy.getStories(IPK, mediaList);

        if (mediaList.size() > 0)
        {
            HighlightItem item = new HighlightItem();
            item.itemType = Story;
            item.CoverURL = picURL;
            item.mediaList = mediaList;
            item.mediaCount = mediaList.size();
            item.title = getResources().getString(R.string.storyDownloader_storyTitle);

            highlightList.add(0, item);
            highlightFragment.notifyDataChanged();
        }

        isStoryCollected = true;

    }

    public void getHighlights() throws IGWAException, IOException, NoSuchAlgorithmException, JSONException
    {
        List<HighlightItem> newItems = new ArrayList<>();
        metagramAgent.activeAgent.proxy.getHighlights(IPK, newItems);

        highlightList.addAll(newItems);

        isHighlightCollected = true;

        highlightFragment.notifyDataChanged();

    }

    void checkLoadingStatus()
    {
        if (isHighlightCollected && isStoryCollected)
        {
            new Handler(getMainLooper()).postDelayed(()->
            {
                if (highlightList.size() == 0)
                {
                    highlightFragment.enableEmptyBox();
                }
                if (igtvList.IGTVs.size() == 0)
                {
                    igtvFragment.enableEmptyBox();
                }
                if (postsList.medias.size() == 0)
                {
                    //postsFragment.enableEmptyBox();
                }
                highlightFragment.disableLoading();
            }, 100);
        }

    }

    public void getIGTVs() throws IGWAException, IOException, NoSuchAlgorithmException, JSONException
    {

        metagramAgent.activeAgent.proxy.getIGTVList(IPK, igtvList);

        runOnUiThread(()->igtvFragment.notifyDataChanged());

        if (igtvList.next_hash == null || igtvList.next_hash.equals(""))
        {
            runOnUiThread(()->igtvFragment.disableLoading());
            return;
        }
        getIGTVs();
    }


}
