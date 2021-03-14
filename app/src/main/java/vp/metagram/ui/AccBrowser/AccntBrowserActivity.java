package vp.metagram.ui.AccBrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


import me.grantland.widget.AutofitTextView;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.AccBrowser.DownloadStory.DownloadSelectionActivity;
import vp.metagram.ui.AccBrowser.ListSource.ListSource;
import vp.metagram.ui.AccBrowser.ListSource.ListSource_Posts_Online;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.ui.ReportUI.AddOrderActivity;
import vp.metagram.utils.instagram.InstagramAgent;

import vp.metagram.utils.instagram.types.AccountInfo;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.UserFull;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import vp.igpapi.IGWAException;

import static vp.metagram.general.functions.isNetworkAvailable;
import static vp.metagram.general.functions.openAccountPageOnInstagram;
import static vp.metagram.general.functions.setImageButtonEnabled;
import static vp.metagram.general.functions.setTextViewFontArvoRegular;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.ScrollerDeltaMargin;
import static vp.metagram.general.variables.ScrollerPreFetchItems;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.AccntBrowser_Post;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.AccntBrowser_order;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.AccntBrowser_story;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.isShowingHelp;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;


public class AccntBrowserActivity extends BaseActivity
{

    final int orderStatistics_requestCode = 1000;

    ConstraintLayout rootLayout;

    boolean isFirstTime = true;

    String username;
    String picURL;
    long IPK;
    boolean isPrivate;
    boolean isFriend = true;

    int followerCount;
    int followingCount;
    int postsCount;

    AutofitTextView usernameTextView;

    TextView postCountTitle;
    AutofitTextView postCountNumber;
    TextView followerCountTitle;
    AutofitTextView followerCountNumber;
    TextView followingCountTitle;
    AutofitTextView followingCountNumber;

    ImageView profilePicture;

    Map<Long, PostMedia> postsList = new LinkedHashMap<>(10, 0.75F, true);
    ListSource postsListSource;
    boolean postsLoading = true;
    private int postsPastVisibleItems, postsVisibleItemCount, postsTotalItemCount;
    private RecyclerView postsRecycleView;
    public RecyclerView.Adapter postsAdapter;
    private LinearLayoutManager postsLayoutManager;
    private SpinKitView postsLoadingView;

    ImageView privateIcon;
    TextView privateMessage;

    ImageButton orderButton;
    ImageButton downloadButton;

    boolean isShowing = false;

    InstagramAgent agent = null;
    boolean isRobot ;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accnt_browser);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            username = bundle.getString("username");
            picURL = bundle.getString("picURL");
            IPK = bundle.getLong("ipk");
            isPrivate = bundle.getBoolean("isPrivate");
        }



        if (agent == null)
        { agent = metagramAgent.activeAgent; }


        findUIElements();

        prepareUIElements();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if(isFirstTime)
        {
            triggerDataCollection();
            isFirstTime = false;
        }

        isShowing = true;

        showHelp();
    }

    @Override
    public void onPause()
    {
        isShowing = false;
        super.onPause();
    }

    private void findUIElements()
    {
        rootLayout = findViewById(R.id.AccountBrowser_rootLayout);

        profilePicture = findViewById(R.id.AccountBrowser_profileImage);
        usernameTextView = findViewById(R.id.AccountBrowser_username_textView);
        postCountTitle = findViewById(R.id.AccountBrowser_postCountTitle);
        postCountNumber = findViewById(R.id.AccountBrowser_postCountNumber);
        followerCountTitle = findViewById(R.id.AccountBrowser_followerCountTitle);
        followerCountNumber = findViewById(R.id.AccountBrowser_followerCountNumber);
        followingCountTitle = findViewById(R.id.AccountBrowser_followingCountTitle);
        followingCountNumber = findViewById(R.id.AccountBrowser_followingCountNumber);

        postsLoadingView  = findViewById(R.id.AccountBrowser_loadingView);
        postsRecycleView = findViewById(R.id.AccountBrowser_postsLayout_RecycleView);

        privateIcon = findViewById(R.id.AccountBrowser_postsLayout_privateIcon);
        privateMessage = findViewById(R.id.AccountBrowser_postsLayout_privateTextView);

        orderButton = findViewById(R.id.AccountBrowser_order_report_imageButton);
        downloadButton = findViewById(R.id.AccountBrowser_save_story_imageButton);

    }

    private void prepareUIElements()
    {
        setTextViewFontForMessage(this,usernameTextView);
        setTextViewFontForMessage(this,postCountTitle);
        setTextViewFontForMessage(this,followerCountTitle);
        setTextViewFontForMessage(this,followingCountTitle);

        setTextViewFontArvoRegular(this, postCountNumber);
        setTextViewFontArvoRegular(this, followerCountNumber);
        setTextViewFontArvoRegular(this, followingCountNumber);

        setTextViewFontArvoRegular(this, privateMessage);

        profilePicture.setOnClickListener((View v)->
                openAccountPageOnInstagram(this, username));


        View.OnClickListener onFollowersClick = (View v) ->
        {

            Intent intent = new Intent(this, AccntListActivity.class);
            intent.putExtra("ipk",IPK);
            intent.putExtra("position",1);
            intent.putExtra("username",username);
            intent.putExtra("noOfFollowers",followerCount);
            intent.putExtra("noOfFollowings",followingCount);

            startActivity(intent);
        };
        followerCountNumber.setOnClickListener(onFollowersClick);
        followerCountTitle.setOnClickListener(onFollowersClick);

        View.OnClickListener onFollowingsClick = (View v) ->
        {
            Intent intent = new Intent(this, AccntListActivity.class);
            intent.putExtra("ipk",IPK);
            intent.putExtra("position",0);
            intent.putExtra("username",username);
            intent.putExtra("noOfFollowers",followerCount);
            intent.putExtra("noOfFollowings",followingCount);


            startActivity(intent);
        };
        followingCountNumber.setOnClickListener(onFollowingsClick);
        followingCountTitle.setOnClickListener(onFollowingsClick);



        postsListSource =  new ListSource_Posts_Online(IPK, postsList, agent);
        postsRecycleView.setHasFixedSize(true);
        postsLayoutManager = new LinearLayoutManager(this);
        postsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        postsRecycleView.setLayoutManager(postsLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(postsRecycleView.getContext(),
                postsLayoutManager.getOrientation());
        postsRecycleView.addItemDecoration(dividerItemDecoration);
        postsRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if ( dy > ScrollerDeltaMargin ) //check for scroll down
                {
                    postsVisibleItemCount = postsLayoutManager.getChildCount();
                    postsTotalItemCount = postsLayoutManager.getItemCount();
                    postsPastVisibleItems = postsLayoutManager.findFirstVisibleItemPosition();

                    if ( postsLoading )
                    {
                        if ( (postsVisibleItemCount + postsPastVisibleItems) + ScrollerPreFetchItems >= postsTotalItemCount )
                        {
                            postsLoading = false;
                            loadPostsData();
                        }
                    }
                }
            }
        });
        postsAdapter = new PostsAdaptor(postsList,this,null, isPrivate, IPK,username);
        postsRecycleView.setAdapter(postsAdapter);

        orderButton.setOnClickListener((View v)->
        {
            Intent intent = new Intent(this,AddOrderActivity.class);
            intent.putExtra("directOrder",true);
            intent.putExtra("ipk",IPK);
            intent.putExtra("noOfFollowers",followerCount);
            intent.putExtra("noOfFollowings",followingCount);
            intent.putExtra("noOfPosts",postsCount);
            intent.putExtra("isPrivate",isPrivate);
            intent.putExtra("isFriend",isFriend);
            intent.putExtra("username",username);
            intent.putExtra("picURL",picURL);


            startActivityForResult(intent,orderStatistics_requestCode);
        });

        setImageButtonEnabled(this,false,orderButton,R.drawable.ic_order_report);
        setImageButtonEnabled(this,false,downloadButton,R.drawable.ic_story);


        downloadButton.setOnClickListener((View v)->
        {
            Intent intent = new Intent(this,DownloadSelectionActivity.class);
            intent.putExtra("ipk",IPK);
            intent.putExtra("picURL",picURL);
            intent.putExtra("username",username);
            startActivity(intent);
        });

    }



    private void triggerDataCollection()
    {
        usernameTextView.setText(username);

        if (picURL != null && !picURL.equals(""))
        {
            agent.picasso.load( picURL)
                    .placeholder(getResources().getDrawable(R.drawable.ic_download_from_net))
                    .error(getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(profilePicture);
        }

        // Get User General Info
        threadPoolExecutor.execute(()->
        {
            if (IPK == agent.userID && !isRobot)
            {
                try
                {
                    agent.refreshAgentInfo();

                    AccountInfo accountInfo = agent.getAccountStatisticsFromDB();

                    postsCount = accountInfo.postsCount;
                    followingCount = accountInfo.followingCount;
                    followerCount = accountInfo.followerCount;
                    picURL = agent.pictureURL;

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    UserFull userInfo =  agent.proxy.getUserInfo(username);
                    postsCount = userInfo.postsCount;
                    followerCount = userInfo.followerCount;
                    followingCount = userInfo.followingCount;
                    picURL = userInfo.picURL;
                }
                catch (Exception e)
                {
                    UserFull userInfo = null;
                    try
                    {
                        userInfo = agent.proxy.getUserInfo(username);
                        postsCount = userInfo.postsCount;
                        followerCount = userInfo.followerCount;
                        followingCount = userInfo.followingCount;
                        picURL = userInfo.picURL;
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }

                }
            }

            new Handler(getMainLooper()).post(()->
            {
                if (followerCount !=0 || followingCount != 0 || postsCount != 0)
                {
                    followingCountNumber.setText(String.format(Locale.ENGLISH, "%,d", followingCount));
                    followerCountNumber.setText(String.format(Locale.ENGLISH, "%,d", followerCount));
                    postCountNumber.setText(String.format(Locale.ENGLISH, "%,d", postsCount));

                    agent.picasso.load(picURL)
                            .placeholder(getResources().getDrawable(R.drawable.ic_download_from_net))
                            .error(getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(profilePicture);
                }

            });
        });


        // Get User Posts List
        loadPostsData();
    }

    public void loadPostsData()
    {
        postsLoadingView.setVisibility(View.VISIBLE);
        threadPoolExecutor.execute(() ->
        {
            boolean isFinished = false;
            try
            {
                if (!isNetworkAvailable(this))
                { throw new IOException("No Internet");}


                postsListSource.getNextList();

                isFinished = true;
                new Handler(getMainLooper()).post(() ->
                        postsAdapter.notifyDataSetChanged());
            }
            catch (IGWAException e)
            {

                if (e.code == IGWAException.not_authorized)
                {
                    isFriend = false;

                    new Handler(getMainLooper()).post(()->
                    {
                        followerCountNumber.setOnClickListener(null);
                        followerCountTitle.setOnClickListener(null);
                        followingCountNumber.setOnClickListener(null);
                        followingCountTitle.setOnClickListener(null);

                        privateIcon.setVisibility(View.VISIBLE);
                        privateIcon.bringToFront();
                        privateMessage.setVisibility(View.VISIBLE);
                        privateMessage.bringToFront();

                    });
                }
                e.printStackTrace();
            }
            catch (Exception ex)
            {

                isFriend = false;

                Throwable e;

                if (ex instanceof UndeclaredThrowableException)
                {
                    e = ((InvocationTargetException)((UndeclaredThrowableException)ex).getUndeclaredThrowable()).getTargetException();
                }
                else
                {
                    e = ex;
                }


                if (e instanceof IOException)
                {
                    isFriend = false;
                    e.printStackTrace();

                    new Handler(getMainLooper()).post(()->
                    {
                        if (isShowing)
                        {
                            InformationDialog dialog = new InformationDialog();
                            dialog.showDialog(this,
                                    getString(R.string.addOrder_WarningTitle),
                                    getString(R.string.connectionInfo_noInternetContent),
                                    getString(R.string.button_ok),()->
                                    {
                                        if (!isShowing) {return;}
                                        finish();
                                    });
                        }
                    });
                }

            }
            finally
            {
                boolean finalIsFinished = isFinished;
                new Handler(getMainLooper()).post(() ->
                {
                    if (isFriend && finalIsFinished)
                    {
                        try
                        {
                            if (!metagramAgent.userExistsInStatisticsOrders(IPK))
                            {
                                setImageButtonEnabled(this,true, orderButton,R.drawable.ic_order_report);
                            }
                            setImageButtonEnabled(this,true, downloadButton,R.drawable.ic_story);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        catch (GeneralSecurityException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    postsLoadingView.setVisibility(View.GONE);
                    postsLoading = true;
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == orderStatistics_requestCode)
        {
            if (resultCode == Activity.RESULT_OK)
            {

                String message = data.getStringExtra("result");
                Snackbar snackbar = Snackbar
                        .make(rootLayout, message, Snackbar.LENGTH_LONG);
                snackbar.getView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                snackbar.show();
            }
        }
    }


    public void showHelp()
    {
        new Handler(getMainLooper()).postDelayed(()->
                showPostsHelp(),4000);

        if (dbMetagram.getItemStatus(isShowingHelp) != 0 )
        { return; }

        dbMetagram.setItemStatus(isShowingHelp,1);

        showInteractiveHelp(AccntBrowser_order,
                this,
                getResources().getString(R.string.i_AccntBrowser_order_title),
                getResources().getString(R.string.i_AccntBrowser_order_content),
                orderButton,
                (View v2) ->
                        showStoryHelp(),
                Gravity.auto);
    }


    public void showStoryHelp()
    {
        showInteractiveHelp(AccntBrowser_story,
                this,
                getResources().getString(R.string.i_AccntBrowser_story_title),
                getResources().getString(R.string.i_AccntBrowser_story_content),
                downloadButton,
                (View v2)->
                {
                    dbMetagram.setItemStatus(isShowingHelp,0);
                    showPostsHelp();
                },
                Gravity.auto);


    }

    public void showPostsHelp()
    {
        if (dbMetagram.getItemStatus(isShowingHelp) != 0 )
        { return; }

        dbMetagram.setItemStatus(isShowingHelp,1);

        View childView = postsRecycleView.getChildAt(0);

        if (childView != null)
        {
            showInteractiveHelp(AccntBrowser_Post,
                    this,
                    getResources().getString(R.string.i_AccntBrowser_Post_title),
                    getResources().getString(R.string.i_AccntBrowser_Post_content),
                    childView,
                    (View v2) -> dbMetagram.setItemStatus(isShowingHelp,0),
                    Gravity.auto);
        }
    }
}
