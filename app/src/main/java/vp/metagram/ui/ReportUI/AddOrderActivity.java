package vp.metagram.ui.ReportUI;

import android.app.Activity;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


import me.grantland.widget.AutofitTextView;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.MainActivity;
import vp.metagram.ui.Dialogs.ConfirmationDialog;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.ui.Other.ReportListActivity;

import vp.metagram.utils.instagram.types.User;
import vp.metagram.utils.instagram.types.UserFull;

import static vp.metagram.general.functions.predictOrderTime;
import static vp.metagram.general.functions.secondsToDurationStr;
import static vp.metagram.general.functions.setCheckBoxFontForMessage;
import static vp.metagram.general.functions.setImageViewEnabled;
import static vp.metagram.general.functions.setTextViewFontArvoRegular;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.logger;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.AddOrder_Help;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.AddOrder_Ruby;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;


public class AddOrderActivity extends BaseActivity
{

    final int MaxInterval = 14;

    SearchView searchView;

    private TextView searchTitle;

    private Runnable confirmRunnable;

    private RecyclerView recycleView;
    public RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private List<SearchRunnable> searchRunnableList = new ArrayList<>();
    public List<User> suggestions = new ArrayList<>();

    private SpinKitView loadingView;

    private LinearLayout searchLayout;

    private SpinKitView loadingView_AddOrder;
    private ConstraintLayout addOrderLayout;
    private ConstraintLayout addOrderProfileLayout;
    private ConstraintLayout addOrderConfigLayout;
    private ConstraintLayout addOrderPriceLayout;
    private ConstraintLayout rootLayout;


    public long IPK;
    public String username;
    UserFull userInfo;


    TextView postCountTitle;
    AutofitTextView postCountNumber;
    TextView followerCountTitle;
    AutofitTextView followerCountNumber;
    TextView followingCountTitle;
    AutofitTextView followingCountNumber;

    Button confirmButton;

    ImageView profilePicture;

    AutofitTextView usernameTextView;

    CheckBox fParameterCheckBox;
    CheckBox pParameterCheckBox;
    CheckBox dParameterCheckBox;
    CheckBox rParameterCheckBox;

    ImageView fParameterIcon;
    ImageView pParameterIcon;
    ImageView dParameterIcon;
    ImageView rParameterIcon;

    int interval = deviceSettings.statisticsDefaultJobInterval;
    SeekBar intervalSeekBar;
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener;
    TextView intervalDays;

    boolean isPrivate;
    boolean isFriend = false;

    int noOfFollowers;
    int noOfFollowings;
    int noOfPosts;

    TextView addOrder_Time;


    boolean freeReport = false;

    boolean directOrder = false;
    boolean isEdit = false;

    boolean overrideFollowLimits = false;
    boolean overridePostLimits = false;

    int OrderID;

    boolean old_fParameter;
    boolean old_dParameter;
    boolean old_pParameter;

    ImageButton helpButton;

    boolean isShowing = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            freeReport = extras.getBoolean("freeReport");
            directOrder = extras.getBoolean("directOrder");
            isEdit = extras.getBoolean("isEdit");

            if (directOrder)
            {
                IPK = extras.getLong("ipk");
                noOfFollowers = extras.getInt("noOfFollowers");
                noOfFollowings = extras.getInt("noOfFollowings");
                noOfPosts = extras.getInt("noOfPosts");
                isPrivate = extras.getBoolean("isPrivate");
                isFriend = extras.getBoolean("isFriend");
                username = extras.getString("username");

                userInfo = new UserFull();

                userInfo.picURL = extras.getString("picURL");
                userInfo.username = username;
                userInfo.followerCount = noOfFollowers;
                userInfo.followingCount = noOfFollowings;
                userInfo.postsCount = noOfPosts;
                userInfo.isPrivate = isPrivate;
                userInfo.IPK = IPK;
            }

            if(isEdit)
            {
                IPK = extras.getLong("ipk");
                OrderID = extras.getInt("OrderID");
            }
        }

        findUIElements();

        prepareForEdit();

        prepareUIElements();

    }


    private void prepareForEdit()
    {
        if(isEdit)
        {

            String sqlText = String.format(Locale.ENGLISH, "Select UserInfo, F_Parameter, P_Parameter, D_Parameter, autoRefresh, " +
                    "intervalInSeconds,reBornDate from Statistics_Orders\n" +
                    "\tWhere IPK = %d and StatOrderID = %d ", IPK, OrderID);

            try
            {
                MatrixCursor result = dbMetagram.selectQuery(sqlText);
                if (result.moveToFirst())
                {
                    JSONObject  userJSON = new JSONObject(result.getString(result.getColumnIndex("UserInfo")));
                    boolean fParameter = result.getInt(result.getColumnIndex("F_Parameter")) == 1;
                    boolean pParameter = result.getInt(result.getColumnIndex("P_Parameter")) == 1;
                    boolean dParameter = result.getInt(result.getColumnIndex("d_Parameter")) == 1;
                    boolean rParameter = result.getInt(result.getColumnIndex("autoRefresh")) == 1;

                    int intervalInSeconds = result.getInt(result.getColumnIndex("intervalInSeconds"));
                    interval = intervalInSeconds/3600;

                    old_fParameter = fParameter;
                    old_dParameter = dParameter;
                    old_pParameter = pParameter;

                    fParameterCheckBox.setChecked(fParameter);
                    setImageViewEnabled(this,fParameter,fParameterIcon,R.drawable.ic_f_parameter);

                    pParameterCheckBox.setChecked(pParameter);
                    setImageViewEnabled(this,pParameter,pParameterIcon,R.drawable.ic_p_parameter);

                    dParameterCheckBox.setChecked(dParameter);
                    setImageViewEnabled(this,dParameter,dParameterIcon,R.drawable.ic_d_parameter);

                    rParameterCheckBox.setChecked(rParameter);
                    setImageViewEnabled(this,rParameter,rParameterIcon,R.drawable.ic_r_parameter);

                    noOfFollowers = userJSON.getInt("followerCount");
                    noOfFollowings = userJSON.getInt("followingCount");
                    noOfPosts = userJSON.getInt("postsCount");
                    isPrivate = userJSON.getBoolean("isPrivate");
                    isFriend = userJSON.getBoolean("isFriend");
                    username = userJSON.getString("username");

                    userInfo = new UserFull();

                    userInfo.picURL = userJSON.getString("picURL");
                    userInfo.username = username;
                    userInfo.followerCount = noOfFollowers;
                    userInfo.followingCount = noOfFollowings;
                    userInfo.postsCount = noOfPosts;
                    userInfo.isPrivate = isPrivate;
                    userInfo.IPK = IPK;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void findUIElements()
    {

        rootLayout = findViewById(R.id.addOrder_rootLayout);

        searchLayout = findViewById(R.id.addOrder_searchLayout);
        searchView = findViewById(R.id.addOrder_searchView);
        loadingView = findViewById(R.id.addOrder_loading);
        recycleView = findViewById(R.id.addOrder_recycleView);
        loadingView_AddOrder = findViewById(R.id.addOrder_addLayoutLoading);
        addOrderLayout = findViewById(R.id.addOrder_addLayout);
        addOrderProfileLayout = findViewById(R.id.AccountBrowserProfile_ConstraintLayout);
        addOrderConfigLayout = findViewById(R.id.addOrderConfiguration_ConstraintLayout);
        addOrderPriceLayout = findViewById(R.id.addOrder_priceLayout);
        searchTitle = findViewById(R.id.addOrder_searchTitle);

        profilePicture = findViewById(R.id.AccountBrowser_profileImage);

        usernameTextView = findViewById(R.id.AccountBrowser_username_textView);
        postCountTitle = findViewById(R.id.AccountBrowser_postCountTitle);
        postCountNumber = findViewById(R.id.AccountBrowser_postCountNumber);
        followerCountTitle = findViewById(R.id.AccountBrowser_followerCountTitle);
        followerCountNumber = findViewById(R.id.AccountBrowser_followerCountNumber);
        followingCountTitle = findViewById(R.id.AccountBrowser_followingCountTitle);
        followingCountNumber = findViewById(R.id.AccountBrowser_followingCountNumber);

        setTextViewFontForMessage(this,searchTitle);

        setTextViewFontForMessage(this,usernameTextView);
        setTextViewFontForMessage(this,postCountTitle);
        setTextViewFontForMessage(this,followerCountTitle);
        setTextViewFontForMessage(this,followingCountTitle);

        setTextViewFontArvoRegular(this, postCountNumber);
        setTextViewFontArvoRegular(this, followerCountNumber);
        setTextViewFontArvoRegular(this, followingCountNumber);

        confirmButton = findViewById(R.id.addOrder_confirmButton);
        setTextViewFontForMessage(this, confirmButton);

        fParameterCheckBox = findViewById(R.id.addOrder_fParameterCheckBox);
        setCheckBoxFontForMessage(this,fParameterCheckBox);
        pParameterCheckBox = findViewById(R.id.addOrder_pParameterCheckBox);
        setCheckBoxFontForMessage(this,pParameterCheckBox);
        dParameterCheckBox = findViewById(R.id.addOrder_dParameterCheckBox);
        setCheckBoxFontForMessage(this,dParameterCheckBox);
        rParameterCheckBox = findViewById(R.id.addOrder_rParameterCheckBox);
        setCheckBoxFontForMessage(this,rParameterCheckBox);

        fParameterIcon = findViewById(R.id.addOrder_fParameterIcon);
        pParameterIcon = findViewById(R.id.addOrder_pParameterIcon);
        dParameterIcon = findViewById(R.id.addOrder_dParameterIcon);
        rParameterIcon = findViewById(R.id.addOrder_rParameterIcon);

        intervalSeekBar = findViewById(R.id.addOrder_intervalSeekBar);
        intervalDays = findViewById(R.id.addOrder_intervalDays);
        setTextViewFontArvoRegular(this, intervalDays);

        addOrder_Time = findViewById(R.id.addOrder_time);
        setTextViewFontForMessage(this,addOrder_Time);

        helpButton = findViewById(R.id.addOrder_help);
    }

    public void decide()
    {
        if ( isPrivate && !isFriend )
        {
            InformationDialog dialog = new InformationDialog();
            dialog.showDialog(this,
                    getResources().getString(R.string.addOrder_WarningTitle),
                    getResources().getString(R.string.addOrder_PrivateMessage),
                    getResources().getString(R.string.button_ok), null);

            confirmButton.setEnabled(false);

        }

        boolean fParameter = !(noOfFollowings > deviceSettings.statisticsMaxFollowingValue ||
                noOfFollowers > deviceSettings.statisticsMaxFollowersValue ||
                (noOfFollowers + noOfFollowings) > deviceSettings.statisticsMaxCombinedValue);

        boolean pParameter = noOfPosts <= deviceSettings.statisticsMaxPostsValueLimit;

        boolean dParameter = noOfPosts <= deviceSettings.statisticsMaxPostsValue && fParameter && pParameter;

        fParameterCheckBox.setChecked(fParameter);
        setImageViewEnabled(this,fParameter,fParameterIcon,R.drawable.ic_f_parameter);

        pParameterCheckBox.setChecked(pParameter);
        setImageViewEnabled(this,pParameter,pParameterIcon,R.drawable.ic_p_parameter);

        dParameterCheckBox.setChecked(dParameter);
        setImageViewEnabled(this,dParameter,dParameterIcon,R.drawable.ic_d_parameter);

        rParameterCheckBox.setChecked(true);
        setImageViewEnabled(this,true,rParameterIcon,R.drawable.ic_r_parameter);

        getPredictedDuration();

        calculatePrice();

        addCheckBoxListeners();
    }

    private void addCheckBoxListeners()
    {
        fParameterCheckBox.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (b && (noOfFollowers > deviceSettings.statisticsMaxFollowersValueLimit ||
                    noOfFollowings > deviceSettings.statisticsMaxFollowingValueLimit ||
                    noOfFollowings + noOfFollowers > deviceSettings.statisticsMaxCombinedValueLimit) && !overrideFollowLimits)
            {


                ConfirmationDialog dialog = new ConfirmationDialog();
                dialog.showDialog(this,
                        getString(R.string.addOrder_WarningTitle),
                        getString(R.string.addOrder_Warning),
                        getString(R.string.addOrder_WarningGoOn), () ->
                        {
                            try
                            {
                                overrideFollowLimits = true;
                                fParameterCheckBox.setChecked(true);
                            }

                            catch (Exception e)
                            {
                                //TODO error handling
                                e.printStackTrace();
                            }
                        });

                b = !b;
                fParameterCheckBox.setChecked(b);
            }

            if (!b)
            {
                dParameterCheckBox.setChecked(false);
            }

            setImageViewEnabled(this,b,fParameterIcon,R.drawable.ic_f_parameter);
            getPredictedDuration();
            calculatePrice();
        });

        pParameterCheckBox.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (b && noOfPosts > deviceSettings.statisticsMaxPostsValueLimit && !overridePostLimits )
            {


                ConfirmationDialog dialog = new ConfirmationDialog();
                dialog.showDialog(this,
                        getString(R.string.addOrder_WarningTitle),
                        getString(R.string.addOrder_Warning),
                        getString(R.string.addOrder_WarningGoOn), () ->
                        {
                            try
                            {
                                overridePostLimits = true;
                                pParameterCheckBox.setChecked(true);
                            }

                            catch (Exception e)
                            {
                                //TODO error handling
                                e.printStackTrace();
                            }
                        });

                b = !b;
                pParameterCheckBox.setChecked(b);
            }

            if (!b)
            {
                dParameterCheckBox.setChecked(false);
            }

            setImageViewEnabled(this,b,pParameterIcon,R.drawable.ic_p_parameter);
            getPredictedDuration();
            calculatePrice();
        });

        dParameterCheckBox.setOnCheckedChangeListener((compoundButton, b) ->
        {

            if (b &&
                    (noOfPosts > deviceSettings.statisticsMaxPostsValueLimit && !overridePostLimits) ||
            ((noOfFollowers > deviceSettings.statisticsMaxFollowersValueLimit || noOfFollowings > deviceSettings.statisticsMaxFollowingValueLimit ||
                    noOfFollowings + noOfFollowers > deviceSettings.statisticsMaxCombinedValueLimit) &&!overrideFollowLimits ))
            {


                ConfirmationDialog dialog = new ConfirmationDialog();
                dialog.showDialog(this,
                        getString(R.string.addOrder_WarningTitle),
                        getString(R.string.addOrder_Warning),
                        getString(R.string.addOrder_WarningGoOn), () ->
                        {
                            try
                            {
                                overridePostLimits = true;
                                overrideFollowLimits = true;
                                dParameterCheckBox.setChecked(true);
                                pParameterCheckBox.setChecked(true);
                                fParameterCheckBox.setChecked(true);

                            }

                            catch (Exception e)
                            {
                                //TODO error handling
                                e.printStackTrace();
                            }
                        });

                b = !b;
                dParameterCheckBox.setChecked(b);
            }

            if (b) {pParameterCheckBox.setChecked(true);}
            if (b) {fParameterCheckBox.setChecked(true);}

            setImageViewEnabled(this,b,dParameterIcon,R.drawable.ic_d_parameter);
            getPredictedDuration();
            calculatePrice();
        });
    }

    private void getPredictedDuration()
    {
        boolean fParameter = fParameterCheckBox.isChecked();
        boolean pParameter = pParameterCheckBox.isChecked();
        boolean dParameter = dParameterCheckBox.isChecked();

        long predictedDuration = predictOrderTime(noOfFollowers, noOfFollowings, noOfPosts, metagramAgent.activeAgent, fParameter,pParameter,dParameter);

        String durationStr = secondsToDurationStr(predictedDuration, this);

        addOrder_Time.setText(String.format(getResources().getString(R.string.addOrder_predictedTime),durationStr));
    }

    private void prepareUIElements()
    {
        addOrderLayout.setVisibility(View.GONE);
        searchView.setOnClickListener((View view)->
                searchView.setIconified(false));

        searchView.onActionViewExpanded();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                loadData(newText);
                return false;
            }
        });

        Locale current = getResources().getConfiguration().locale;
        if (current.toString().equals("fa"))
        { helpButton.setRotationY(180f); }

        helpButton.setOnClickListener((View v)->
        {
            Intent intent = new Intent(this, ReportListActivity.class);
            startActivity(intent);
        });

        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycleView.getContext(),
                layoutManager.getOrientation());
        recycleView.addItemDecoration(dividerItemDecoration);


        confirmRunnable  = ()->
        {
            searchLayout.setVisibility(View.GONE);
            addOrderProfileLayout.setVisibility(View.GONE);
            addOrderConfigLayout.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);
            addOrderPriceLayout.setVisibility(View.GONE);

            loadingView_AddOrder.setVisibility(View.VISIBLE);
            addOrderLayout.setVisibility(View.VISIBLE);

            threadPoolExecutor.execute(()->{
                try
                {
                    userInfo = metagramAgent.activeAgent.proxy.getUserInfo(username);

                    isPrivate = userInfo.isPrivate;
                    noOfFollowers = userInfo.followerCount;
                    noOfFollowings = userInfo.followingCount;
                    noOfPosts = userInfo.postsCount;

                    if (username.equals(metagramAgent.activeAgent.username))
                    {
                        isFriend = true;
                    }
                    else
                    {
                        isFriend = metagramAgent.activeAgent.checkFriendship(username);
                    }

                    new Handler(getMainLooper()).post(()->
                            loadAccountDataToUI());


                }
                catch (Exception e)
                {
                    logger.logError(this.getClass().getName(),
                            "Getting chosen account info failed.\n",e);
                }

            });


        };

        adapter = new AddOrderAdaptor(suggestions, this, confirmRunnable, this);
        recycleView.setAdapter(adapter);

        if(!isEdit)
        {
            setImageViewEnabled(this, false, fParameterIcon, R.drawable.ic_f_parameter);
            setImageViewEnabled(this, false, pParameterIcon, R.drawable.ic_p_parameter);
            setImageViewEnabled(this, false, dParameterIcon, R.drawable.ic_d_parameter);
            setImageViewEnabled(this, false, rParameterIcon, R.drawable.ic_r_parameter);
        }

        if(isEdit && interval == 0)
        {
            intervalSeekBar.setEnabled(false);
        }

        rParameterCheckBox.setOnCheckedChangeListener((compoundButton, b) ->
        {
            intervalSeekBar.setEnabled(b);

            setImageViewEnabled(this,b,rParameterIcon,R.drawable.ic_r_parameter);

            if (b)
            { intervalDays.setTextColor(getResources().getColor(R.color.sBlack)); }
            else
            { intervalDays.setTextColor(getResources().getColor(R.color.sCoolGrayC5));}
        });

        fParameterIcon.setOnClickListener((View v)->
                fParameterCheckBox.setChecked(!fParameterCheckBox.isChecked()));

        pParameterIcon.setOnClickListener((View v)->
                pParameterCheckBox.setChecked(!pParameterCheckBox.isChecked()));

        dParameterIcon.setOnClickListener((View v)->
                dParameterCheckBox.setChecked(!dParameterCheckBox.isChecked()));

        rParameterIcon.setOnClickListener((View v)->
                rParameterCheckBox.setChecked(!rParameterCheckBox.isChecked()));

        onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                interval = (i+1)*24;
                intervalDays.setText(String.format(getResources().getString(R.string.addOrder_intervalDays),i+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }

        };

        intervalSeekBar.setMax(MaxInterval);
        intervalSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);



        confirmButton.setOnClickListener((View v)->
        {
            confirmButton.setEnabled(false);

            threadPoolExecutor.execute((() ->
            {
                try
                {
                    int f_parameter = 0;
                    int p_parameter = 0;
                    int d_parameter = 0;
                    int autoRefresh;
                    int interval;


                    if ( rParameterCheckBox.isChecked() )
                    {
                        autoRefresh = 1;
                        interval = (intervalSeekBar.getProgress()+1) *24*3600;
                    }
                    else
                    {
                        autoRefresh = 0;
                        interval = -1;
                    }

                    if ( fParameterCheckBox.isChecked() ) {f_parameter = 1;}
                    if ( pParameterCheckBox.isChecked() ) {p_parameter = 1;}
                    if ( dParameterCheckBox.isChecked() ) {d_parameter = 1;}

                    if(!isEdit)
                    {
                        metagramAgent.addStatisticsOrder(IPK, metagramAgent.activeAgent.userID, username, userInfo.getAsJSON(isFriend),
                                f_parameter, p_parameter, d_parameter, interval, autoRefresh);

                        metagramAgent.addStatisticsJob(IPK);

                        new Handler(getMainLooper()).post(()->
                        {
                           if (MainActivity.self != null && !MainActivity.self.isFinishing() && !MainActivity.self.isDestroyed())
                           {
                               if (MainActivity.self.reportsFragment!= null && MainActivity.self.reportsFragment.isAdded())
                               {
                                   try
                                   {
                                       MainActivity.self.reportsFragment.loadReports();
                                       MainActivity.self.reportsFragment.createReportsFragments();
                                   }
                                   catch (Exception e)
                                   {
                                       e.printStackTrace();
                                   }
                               }
                           }
                        });

                        new Handler(getMainLooper()).post(() ->
                        {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result", getResources().getString(R.string.addOrder_SuccessMessage));
                            returnIntent.putExtra("isEdit", isEdit);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        });
                    }
                    else
                    {
                        boolean isUpgrade = false;
                        if (!old_fParameter && f_parameter == 1) {isUpgrade = true;}
                        if (!old_pParameter && p_parameter == 1) {isUpgrade = true;}
                        if (!old_dParameter && d_parameter == 1) {isUpgrade = true;}



                        if (isUpgrade)
                        {
                            int finalF_parameter = f_parameter;
                            int finalP_parameter = p_parameter;
                            int finalD_parameter = d_parameter;

                            new Handler(getMainLooper()).post(()->
                            {
                                ConfirmationDialog dialog = new ConfirmationDialog();

                                dialog.showDialog(this,
                                        getResources().getString(R.string.addOrder_WarningTitle),
                                        getResources().getString(R.string.addOrder_UpgradeMessage),
                                        getResources().getString(R.string.button_ok), ()->
                                        {
                                            ConnectingDialog connectingDialog = ConnectingDialog.newInstance(getResources().getString(R.string.addOrder_UpgradeDialog));
                                            connectingDialog.show(getFragmentManager(), "");

                                            threadPoolExecutor.execute(()->
                                            {
                                                try
                                                {
                                                    metagramAgent.updateStatisticsOrder(OrderID, finalF_parameter, finalP_parameter, finalD_parameter, interval, autoRefresh);
                                                    metagramAgent.addStatisticsJob(IPK);
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                                finally
                                                {
                                                    new Handler(getMainLooper()).post(() ->
                                                    {
                                                        connectingDialog.close();
                                                        Intent returnIntent = new Intent();
                                                        returnIntent.putExtra("result", getResources().getString(R.string.addOrder_UpdateMessage));
                                                        returnIntent.putExtra("isEdit", isEdit);
                                                        returnIntent.putExtra("IPK", IPK);
                                                        returnIntent.putExtra("OrderID", OrderID);
                                                        setResult(Activity.RESULT_OK, returnIntent);
                                                        finish();
                                                    });
                                                }
                                            });
                                        });

                                confirmButton.setEnabled(true);
                            });

                        }
                        else
                        {
                            metagramAgent.updateStatisticsOrder(OrderID, f_parameter, p_parameter, d_parameter, interval, autoRefresh);

                            new Handler(getMainLooper()).post(() ->
                            {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("result", getResources().getString(R.string.addOrder_UpdateMessage));
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            });
                        }
                    }


                }
                catch (Exception e)
                {
                    logger.logError(this.getClass().getName(), "Error while adding statistics order and job", e);
                }
            }));

        });

        checkActivityFunctionality();

    }

    public void checkActivityFunctionality()
    {
        if (directOrder || isEdit )
        {
            searchLayout.setVisibility(View.GONE);
            addOrderLayout.setVisibility(View.VISIBLE);

            loadAccountDataToUI();
        }

        if (freeReport)
        {
            searchLayout.setVisibility(View.GONE);
            addOrderLayout.setVisibility(View.VISIBLE);

            ConnectingDialog dialog = ConnectingDialog.newInstance(getResources().getString(R.string.login_connectMessage));
            dialog.show(getFragmentManager(), "");

            threadPoolExecutor.execute(()->
            {
                try
                {
                    int noOfOrders = metagramAgent.getNoOfStatisticsOrdersByFIPK(metagramAgent.activeAgent.userID);
                    if (noOfOrders > 0) {return;}

                    userInfo = metagramAgent.activeAgent.proxy.getUserInfo(metagramAgent.activeAgent.username);

                    IPK = userInfo.IPK;
                    noOfFollowers = userInfo.followerCount;
                    noOfFollowings = userInfo.followingCount;
                    noOfPosts = userInfo.postsCount;
                    isPrivate = userInfo.isPrivate;
                    isFriend = true;
                    username = userInfo.username;

                    runOnUiThread(this::loadAccountDataToUI);

                }
                catch (Exception ignored)
                {
                }
                finally
                {
                    runOnUiThread(dialog::close);
                }
            });
        }

    }

    public void loadAccountDataToUI()
    {
        followerCountNumber.setText(String.format(Locale.ENGLISH,"%,d",userInfo.followerCount));
        followingCountNumber.setText(String.format(Locale.ENGLISH,"%,d",userInfo.followingCount));
        postCountNumber.setText(String.format(Locale.ENGLISH,"%,d",userInfo.postsCount));

        usernameTextView.setText(userInfo.username);

        metagramAgent.activeAgent.picasso.load( userInfo.picURL)
                .placeholder(getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(profilePicture);

        loadingView_AddOrder.setVisibility(View.GONE);
        addOrderProfileLayout.setVisibility(View.VISIBLE);
        addOrderConfigLayout.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.VISIBLE);
        addOrderPriceLayout.setVisibility(View.VISIBLE);


        if (!isEdit)
        {
            decide();
            showHelp();
        }
        else
        {
            addCheckBoxListeners();
            calculatePrice();
        }
    }

    public void calculatePrice()
    {
        int result = deviceSettings.S_ParameterPrice;

        if (fParameterCheckBox.isChecked()) {result += deviceSettings.F_ParameterPrice;}
        if (pParameterCheckBox.isChecked()) {result += deviceSettings.P_ParameterPrice;}
        if (dParameterCheckBox.isChecked()) {result += deviceSettings.D_ParameterPrice;}

        if(IPK == metagramAgent.activeAgent.userID) {result = 0;}

    }


    public void loadData(String keyWord)
    {
        Iterator<SearchRunnable> iterator = searchRunnableList.iterator();
        while ( iterator.hasNext() )
        {
            SearchRunnable searchRunnable = iterator.next();
            searchRunnable.exit();
            searchRunnableList.remove(searchRunnable);
        }

        if (keyWord.equals(""))
        {
            suggestions.clear();
            adapter.notifyDataSetChanged();
        }
        else
        {
            SearchRunnable newSearchRunnable = new SearchRunnable(this,keyWord);
            searchRunnableList.add(newSearchRunnable);
            threadPoolExecutor.schedule(newSearchRunnable,500, TimeUnit.MILLISECONDS);
        }
    }

    class SearchRunnable implements Runnable
    {
        AddOrderActivity parent;

        boolean goOn = true;
        String keyWord;

        public SearchRunnable(AddOrderActivity parent, String keyWord)
        {
            this.parent = parent;
            this.keyWord = keyWord;
        }

        public void exit()
        {
            goOn = false;
        }

        @Override
        public void run()
        {
            if (!goOn) {return;}
            parent.suggestions.clear();


            try
            {
                recycleView.setLayoutFrozen(true);
                runOnUiThread(()->loadingView.setVisibility(View.VISIBLE));

                metagramAgent.activeAgent.proxy.search(keyWord,parent.suggestions);

                if (!goOn) {return;}

                if (searchView.getQuery().equals(""))
                {
                    suggestions.clear();
                }

                runOnUiThread(()->
                {
                    if (!goOn) {return;}
                    ((AddOrderAdaptor)adapter).setDataSet(suggestions);
                });
            }
            catch (Exception ex)
            {
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
                        InformationDialog dialog = new InformationDialog();
                        dialog.showDialog(parent,
                                getString(R.string.addOrder_WarningTitle),
                                getString(R.string.connectionInfo_noInternetContent),
                                getString(R.string.button_ok),()->
                                {
                                    if (!parent.isShowing){return;}
                                    parent.finish();
                                });
                    });
                }
            }
            finally
            {
                runOnUiThread(()->
                {
                    loadingView.setVisibility(View.INVISIBLE);
                    recycleView.setLayoutFrozen(false);
                    recycleView.smoothScrollToPosition(0);

                });
            }
            //TODO add error handling
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        int process = 1;

        if (isEdit && interval > 0)
        {
            process = (interval / 24)-1;
        }
        intervalSeekBar.setProgress(process);
        onSeekBarChangeListener.onProgressChanged(intervalSeekBar, process,true);

        if (isEdit)
        {showHelp();}

        isShowing = true;
    }

    @Override
    public void onBackPressed()
    {
        if(!freeReport)
            super.onBackPressed();
    }

    @Override
    public void onPause()
    {
        isShowing = false;
        super.onPause();
    }

    public void showHelp()
    {
        showInteractiveHelp(AddOrder_Help,
                this,
                getResources().getString(R.string.i_AddOrder_Help_title),
                getResources().getString(R.string.i_AddOrder_Help_content),
                findViewById(R.id.addOrder_dummyLayout),
                null,
                smartdevelop.ir.eram.showcaseviewlib.config.Gravity.auto);
    }


}
