package vp.metagram.ui.ReportUI.reportViewer;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.ReportUI.reportViewer.adapter.ReportAccountAdapter;
import vp.metagram.ui.ReportUI.reportViewer.adapter.ReportPostUserAdapter;
import vp.metagram.ui.ReportUI.reportViewer.adapter.ReportPostsAdapter;
import vp.metagram.ui.ReportUI.reportViewer.listSource.CommentedWithoutFollowSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.LazyFollowersSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.LikedWithoutFollowSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.MostCommentedPostsSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.MostCommenterSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.MostEngageSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.MostLikedPostsSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.MostLikerSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.MostViewedPostsSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.NotFollowedBackSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.NotFollowingBackSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.RemovedCommentsSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.RemovedLikesSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.ReportListSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.SomeonesCommentsSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.SomeonesLikesSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.UnfollowedYouSource;
import vp.metagram.ui.ReportUI.reportViewer.listSource.YouUnfollowedSource;
import vp.metagram.ui.ReportUI.reportViewer.types.ReportMedia;
import vp.metagram.ui.ReportUI.reportViewer.types.ReportPostUser;
import vp.metagram.ui.ReportUI.reportViewer.types.ReportUser;

import static android.view.View.GONE;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.ScrollerDeltaMargin;
import static vp.metagram.general.variables.ScrollerPreFetchItems;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.peopleUnfollowedYou;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.removedComments;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.removedLikes;



public class ReportViewerActivity extends BaseActivity
{
    ReportType reportType;
    long IPK;
    String username;
    boolean isPrivate;
    int noOfFollowers;
    int noOfFollowings;
    int noOfOthers;

    // Commenter IPK
    long OIPK;

    TextView usernameTextView;
    ImageButton backButton;
    ImageView reportIcon;
    TextView reportTextView;

    ConstraintLayout searchLayout;
    TextView noOfItems;
    TextView validationTextView;

    private List<ReportUser> accountList = new ArrayList<>();
    private List<ReportMedia> mediaList = new ArrayList<>();
    private List<ReportPostUser> postUserList = new ArrayList<>();

    ReportListSource listSource;

    SpinKitView loadingView;
    boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private RecyclerView recycleView;
    public RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;
    SearchView searchView;

    String searchValue = "";

    boolean isFirstTime = true;
    private List<SearchRunnable> searchRunnableList = new ArrayList<>();

    boolean isInTheMiddle;
    boolean isCountCalculated;

    @Override
    public void onCreate(Bundle savedInstanceStat)
    {
        super.onCreate(savedInstanceStat);
        setContentView(R.layout.activity_report_viewer);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            int reportID = extras.getInt("reportID");
            reportType = ReportType.newInstance(reportID);
            IPK = extras.getLong("IPK");
            username = extras.getString("username");
            isPrivate = extras.getBoolean("isPrivate");
            noOfFollowers = extras.getInt("noOfFollowers");
            noOfFollowings = extras.getInt("noOfFollowings");

            noOfOthers = noOfFollowers + noOfFollowings;

            OIPK = extras.getLong("OIPK");
        }

        findUIElements();
        prepareUIElements();
        showValidationMessage();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if ( isFirstTime )
        {
            loadData();
            isFirstTime = false;
        }
    }

    public void findUIElements()
    {
        usernameTextView = findViewById(R.id.ReportViewer_userTitleTextView);
        backButton = findViewById(R.id.ReportViewer_backButton);
        reportIcon = findViewById(R.id.ReportViewer_reportIcon);
        reportTextView = findViewById(R.id.ReportViewer_reportTitleTextView);

        loadingView = findViewById(R.id.ReportViewer_loading);

        searchLayout = findViewById(R.id.ReportViewer_searchLayout);
        searchView = findViewById(R.id.ReportViewer_searchView);
        noOfItems = findViewById(R.id.ReportViewer_noOfItems);

        recycleView = findViewById(R.id.ReportViewer_recyclerView);

        validationTextView = findViewById(R.id.ReportViewer_validationTextView);

    }

    public void prepareUIElements()
    {
        setTextViewFontForMessage(this,usernameTextView);
        setTextViewFontForMessage(this,reportTextView);

        usernameTextView.setText(username);
        reportTextView.setText(getResources().getString(reportType.getTitleID()));

        reportIcon.setImageResource(reportType.getIconID());

        backButton.setOnClickListener((View v)-> onBackPressed());

        searchView.setOnClickListener((View view) ->
                searchView.setIconified(false));

        searchView.onActionViewExpanded();
        searchView.clearFocus();

        setTextViewFontForMessage(this,noOfItems);

        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycleView.getContext(),
                layoutManager.getOrientation());
        recycleView.addItemDecoration(dividerItemDecoration);
        recycleView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if ( dy > ScrollerDeltaMargin ) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ( loading )
                    {
                        if ( (visibleItemCount + pastVisibleItems) + ScrollerPreFetchItems >= totalItemCount )
                        {
                            loading = false;
                            loadData();
                        }
                    }
                }
            }
        });

        prepareAdapter();

        recycleView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                isCountCalculated = false;
                noOfItems.setText(getResources().getString(R.string.reportViewer_getCount));

                SearchRunnable newSearchRunnable = new SearchRunnable(query);
                searchRunnableList.add(newSearchRunnable);
                new Handler(getMainLooper()).postDelayed(newSearchRunnable,400);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                if (s.equals(""))
                {
                    onQueryTextSubmit("");
                    searchView.clearFocus();
                }
                return true;
            }

        });

        searchView.setInputType(InputType.TYPE_NULL);

        setTextViewFontForMessage(this,validationTextView);

        loadingView.bringToFront();

        noOfItems.setOnClickListener((View v)->
        {
            if(!isInTheMiddle && !isCountCalculated)
            {
                isInTheMiddle = true;
                loadingView.setVisibility(View.VISIBLE);
                searchView.setInputType(InputType.TYPE_NULL);

                threadPoolExecutor.execute(()->
                {
                    try
                    {
                        listSource.calculateCount();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        new Handler(getMainLooper()).post(()->
                        {
                            searchView.setInputType(InputType.TYPE_CLASS_TEXT);
                            loadingView.setVisibility(View.INVISIBLE);
                            isInTheMiddle = false;
                            isCountCalculated = true;

                            noOfItems.setText(String.format(Locale.ENGLISH,"%,d",listSource.count));
                        });
                    }
                });


            }
        });



    }

    public void loadData()
    {
        isInTheMiddle = true;
        loadingView.setVisibility(View.VISIBLE);
        threadPoolExecutor.execute(() ->
        {
            try
            {

                listSource.getNextList();
                new Handler(getMainLooper()).post(()->
                        adapter.notifyDataSetChanged());
            }
            catch (Exception e)
            {
                //TODO Add Error Handling
                e.printStackTrace();
            }
            finally
            {
                new Handler(getMainLooper()).post(()->
                {
                    searchView.setInputType(InputType.TYPE_CLASS_TEXT);
                    loadingView.setVisibility(View.INVISIBLE);
                    loading = true;
                    isInTheMiddle = false;;
                });
            }
        });
    }

    public void showValidationMessage()
    {
        if (reportType == peopleUnfollowedYou || reportType == removedLikes || reportType == removedComments)
        {

            try
            {
                validationTextView.setText(String.format(Locale.ENGLISH,getResources().getString(R.string.reportViewer_validationMessage),
                        metagramAgent.getFirstOrderDateByIPK(this, IPK)));
                validationTextView.setVisibility(View.VISIBLE);
            }
            catch (IOException | GeneralSecurityException e)
            {
                e.printStackTrace();
            }

        }
    }

    public void prepareAdapter()
    {
        noOfItems.setVisibility(View.VISIBLE);

        switch (reportType)
        {
            case notFollowedBack:
                adapter = new ReportAccountAdapter(accountList, this, reportType, IPK);
                listSource = new NotFollowedBackSource(IPK,accountList,searchValue);
                break;
            case notFollowingBack:
                adapter = new ReportAccountAdapter(accountList, this, reportType, IPK);
                listSource = new NotFollowingBackSource(IPK,accountList,searchValue);
                break;
            case postsWithMostLikes:
                adapter = new ReportPostsAdapter(mediaList, this, null,"Likes", isPrivate, IPK,username);
                listSource = new MostLikedPostsSource(IPK, mediaList, searchValue);
                noOfItems.setVisibility(GONE);
                break;
            case postsWithMostComments:
                adapter = new ReportPostsAdapter(mediaList, this, null,"Comments", isPrivate, IPK,username);
                listSource = new MostCommentedPostsSource(IPK, mediaList, searchValue);
                noOfItems.setVisibility(GONE);
                break;
            case postsWithMostViews:
                adapter = new ReportPostsAdapter(mediaList, this, null,"Views", isPrivate, IPK,username);
                listSource = new MostViewedPostsSource(IPK, mediaList, searchValue);
                noOfItems.setVisibility(GONE);
                break;
            case peopleLikedMost:
                adapter = new ReportAccountAdapter(accountList, this, reportType, IPK);
                listSource = new MostLikerSource(IPK,accountList,searchValue);
                break;
            case peopleCommentedMost:
                adapter = new ReportAccountAdapter(accountList, this, reportType, IPK);
                listSource = new MostCommenterSource(IPK,accountList,searchValue);
                break;
            case peopleEngagedMost:
                adapter = new ReportAccountAdapter(accountList,this,reportType, IPK);
                listSource = new MostEngageSource(IPK,accountList,searchValue);
                break;
            case lazyFollowers:
                adapter = new ReportAccountAdapter(accountList,this,reportType, IPK);
                listSource = new LazyFollowersSource(IPK,accountList,searchValue);
                break;
            case likedButDidNotFollow:
                adapter = new ReportPostUserAdapter(postUserList,this,null,isPrivate,IPK,username,reportType);
                listSource = new LikedWithoutFollowSource(IPK,postUserList,searchValue);
                break;
            case commentedButDidNotFollow:
                adapter = new ReportPostUserAdapter(postUserList,this,null,isPrivate,IPK,username,reportType);
                listSource = new CommentedWithoutFollowSource(IPK,postUserList,searchValue);
                break;
            case peopleUnfollowedYou:
                adapter = new ReportAccountAdapter(accountList,this,reportType, IPK);
                listSource = new UnfollowedYouSource(IPK,accountList,searchValue);
                break;
            case removedLikes:
                adapter = new ReportPostUserAdapter(postUserList,this,null,isPrivate,IPK,username,reportType);
                listSource = new RemovedLikesSource(IPK, postUserList,searchValue);
                break;
            case removedComments:
                adapter = new ReportPostUserAdapter(postUserList,this,null,isPrivate,IPK,username,reportType);
                listSource = new RemovedCommentsSource(IPK, postUserList,searchValue);
                break;
            case someonesComments:
                adapter = new ReportPostUserAdapter(postUserList,this,null,isPrivate,IPK,username,reportType);
                listSource = new SomeonesCommentsSource(IPK, postUserList,searchValue, OIPK);
                break;
            case someonesLikes:
                adapter = new ReportPostUserAdapter(postUserList,this,null,isPrivate,IPK,username,reportType);
                listSource = new SomeonesLikesSource(IPK, postUserList,searchValue, OIPK);
                break;
            case peopleYouUnfollowed:
                adapter = new ReportAccountAdapter(accountList,this,reportType, IPK);
                listSource = new YouUnfollowedSource(IPK,accountList,searchValue);
                break;
        }

    }

    class SearchRunnable implements Runnable
    {
        String keyword;
        public boolean goOn = true;

        public SearchRunnable(String keyword)
        {
            this.keyword = keyword;
        }

        @Override
        public void run()
        {
            if(goOn)
            {
                listSource.reset();
                listSource.setSearchValue(keyword);
                loadData();
            }
        }

        public void exit()
        {
            goOn = false;
        }
    }

}
