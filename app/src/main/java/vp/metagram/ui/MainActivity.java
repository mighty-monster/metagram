package vp.metagram.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;


import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.types.CustomTypefaceSpan;
import vp.metagram.ui.Dialogs.ConfirmationDialog;
import vp.metagram.ui.Dialogs.ConnectionInfoDialog;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.ui.MainFragments.IdleFragment;
import vp.metagram.ui.MainFragments.LogInFragment;
import vp.metagram.ui.MainFragments.ProfileFragment;
import vp.metagram.ui.MainFragments.ReportsFragment;
import vp.metagram.ui.Other.DebugActivity;
import vp.metagram.ui.ReportUI.AddOrderActivity;
import vp.metagram.utils.VersionUtils;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;


import static vp.metagram.general.functions.callTracker;
import static vp.metagram.general.functions.configShimmer;
import static vp.metagram.general.variables.appVersion;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.isReleaseMode;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainFragments.LogInFragment.LOGIN_REQUEST;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProfileFragment_delete;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProfileFragment_edit;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProfileFragment_status;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.isShowingHelp;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;
import static vp.metagram.utils.instagram.types.ResponseStatus.loginRequired;
import static vp.metagram.utils.instagram.types.ResponseStatus.noInternet;
import static vp.metagram.utils.instagram.types.ResponseStatus.ok;



public class MainActivity extends BaseActivity
{

    public static MainActivity self;

    enum enum_fragmentMode
    {
        idle, profile, others, robot
    }


    DrawerLayout rootLayout;

    boolean backTouched = false;

    ShimmerFrameLayout menuExternalShimmer;
    TextView menuExternalShimmerTextView;
    ShimmerFrameLayout menuInternalShimmer;
    TextView menuInternalShimmerTextView;

    //TextView rubyCountTextView;

    //ImageButton mainMenuButton;
    //ImageButton rubyMenuButton;

    ImageButton statusButton;
    ImageButton deleteAccountButton;
    ImageButton editAccountButton;

    BottomNavigationView bottomMenu;

    FrameLayout topFrameLayout;
    FrameLayout bottomFrameLayout;

    public IdleFragment idleFragment;
    public LogInFragment logInFragment;
    public ProfileFragment profileFragment;
    public ReportsFragment reportsFragment;

    enum_fragmentMode fragmentMode = enum_fragmentMode.idle;

    View othersButton;
    View profileButton;
    View robotsButton;

    boolean isShowing = false;
    boolean isChecking = false;
    boolean firstTime = true;


    static public MainActivity getLastInstance()
    {
        return self;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        self = this;

        findUIElements();

        String shimmerText = "";
        if (appVersion.languagePartNo == 1)
            shimmerText = appVersion.get_appName_en();
        else if (appVersion.languagePartNo == 2)
            shimmerText = appVersion.get_appName_fa();

        configShimmer(this,
                shimmerText,
                menuExternalShimmer,
                menuInternalShimmer,
                menuExternalShimmerTextView,
                menuInternalShimmerTextView,
                3000);

        configureMenuButtons();
        checkLoginStatus();
        setFragments();

        dbMetagram.setItemStatus(isShowingHelp, 0);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        checkVersionValidity();

        enum_fragmentMode oldFragmentMode = fragmentMode;
        checkLoginStatus();
        if (fragmentMode != oldFragmentMode)
        {
            setFragments();
        }

        checkForSurvey();

        checkForAPIUpgrade();
        checkForFreeReport();
    }

    private void checkForFreeReport()
    {
        try
        {
            if (metagramAgent.activeAgent != null && metagramAgent.getNoOfStatisticsOrders() == 0 )
            {
                Intent orderActivity = new Intent(this, AddOrderActivity.class);
                orderActivity.putExtra("freeReport", true);
                startActivity(orderActivity);
            }
        }
        catch (IOException | GeneralSecurityException e)
        {
            e.printStackTrace();
        }
    }


    private void checkForAPIUpgrade()
    {
        try
        {
            String upgradeValue = dbMetagram.getPair("WebAPIUpgrade").trim();

            if (!upgradeValue.equals(""))
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(this, getResources().getString(R.string.addOrder_WarningTitle),
                        getResources().getString(R.string.mainMenu_APIUpgradeMessage),
                        getResources().getString(R.string.button_ok), null);
                dbMetagram.delPair("WebAPIUpgrade");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                String result = data.getStringExtra("result");
                Snackbar snackbar = Snackbar
                        .make(rootLayout, result, Snackbar.LENGTH_LONG);
                snackbar.getView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                snackbar.show();

                if (reportsFragment != null && reportsFragment.isAdded())
                {
                    long IPK = data.getLongExtra("IPK", -1);
                    int OrderID = data.getIntExtra("OrderID", -1);

                    boolean isEdit = data.getBooleanExtra("isEdit", true);

                    if (isEdit)
                    {
                        reportsFragment.changeFragmentToProgress(IPK, OrderID);
                    }
                    else
                    {
                        try
                        {
                            reportsFragment.loadReports();
                            reportsFragment.createReportsFragments();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (requestCode == 2)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                String message = data.getStringExtra("message");
                Snackbar snackbar = Snackbar
                        .make(rootLayout, message, Snackbar.LENGTH_LONG);
                snackbar.getView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                snackbar.show();

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void configureMenuButtons()
    {
        //Bottom Menu
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/FarBaseet.ttf");
        CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan("", typeface);
        for (int i = 0; i < bottomMenu.getMenu().size(); i++)
        {
            MenuItem menuItem = bottomMenu.getMenu().getItem(i);
            SpannableStringBuilder spannableTitle = new SpannableStringBuilder(menuItem.getTitle());
            spannableTitle.setSpan(typefaceSpan, 0, spannableTitle.length(), 0);
            menuItem.setTitle(spannableTitle);
        }

        profileButton = bottomMenu.findViewById(R.id.menu_profile);
        othersButton = bottomMenu.findViewById(R.id.menu_others);
        //robotsButton = bottomMenu.findViewById(R.id.menu_robots);

        profileButton.setOnClickListener((View v) ->
        {
            if (fragmentMode == enum_fragmentMode.profile) {return;}

            fragmentMode = enum_fragmentMode.profile;
            bottomMenu.setSelectedItemId(R.id.menu_profile);
            setFragments();
        });

        othersButton.setOnClickListener((View v) ->
        {
            if (fragmentMode == enum_fragmentMode.others) {return;}

            fragmentMode = enum_fragmentMode.others;
            bottomMenu.setSelectedItemId(R.id.menu_others);
            setFragments();
        });

        /*robotsButton.setOnClickListener((View v) ->
        {
            if (fragmentMode == enum_fragmentMode.robot) {return;}

            fragmentMode = enum_fragmentMode.robot;
            bottomMenu.setSelectedItemId(R.id.menu_robots);
            setFragments();
        });*/

        /*rubyMenuButton.setOnClickListener((View v) ->
        {
            AccountInfo accountInfo = metagramAgent.activeAgent.getAccountStatisticsFromDB();

            Intent intent = new Intent(this, BuyBazzarActivity.class);
            intent.putExtra("IPK", accountInfo.IPK);
            startActivity(intent);
        });*/


        deleteAccountButton.setOnClickListener((View v) ->
        {
            String username = metagramAgent.activeAgent.username;

            ConfirmationDialog dialog = new ConfirmationDialog();
            dialog.showDialog(this,
                    getString(R.string.removeAccount_title),
                    String.format(getString(R.string.removeAccount_content), username),
                    getString(R.string.button_confirmCaption), () ->
                    {
                        threadPoolExecutor.execute(() ->
                        {
                            try
                            {
                                metagramAgent.deleteInstagramAccount(metagramAgent.activeAgent.userID);
                                metagramAgent.activeAgent = null;
                                deviceSettings.save();


                                new Handler(Looper.getMainLooper()).post(() ->
                                {
                                    if (isShowing)
                                    {
                                        MainActivity mainActivity = this;
                                        mainActivity.firstTime = true;
                                        mainActivity.checkLoginStatus();
                                        mainActivity.setFragments();
                                    }
                                });

                            }

                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });

                    });
        });

        editAccountButton.setOnClickListener((View v) ->
        {
            Intent mIntent = new Intent(this, LogInActivity.class);
            mIntent.putExtra("editIPK", metagramAgent.activeAgent.userID);
            startActivityForResult(mIntent, LOGIN_REQUEST);
        });

        statusButton.setOnLongClickListener(view ->
        {
            if (!isReleaseMode)
            {
                Intent intent = new Intent(this, DebugActivity.class);
                startActivity(intent);
            }

            return false;
        });

        statusButton.setOnClickListener((View v) ->
        {
            if (metagramAgent.activeAgent.agentStatus.responseStatus == ok)
            {
                ConnectionInfoDialog connectionInfoDialog = ConnectionInfoDialog.newInstance(metagramAgent.activeAgent.username.trim());
                connectionInfoDialog.show(getFragmentManager(), "");
            }
            else if (metagramAgent.activeAgent.agentStatus.responseStatus == noInternet)
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(this,
                        getString(R.string.addOrder_WarningTitle),
                        getString(R.string.connectionInfo_noInternetContent),
                        getString(R.string.button_ok),
                        () ->
                        {
                            try
                            {
                                metagramAgent.activeAgent.getUserInfo(metagramAgent.activeAgent.username);
                            }
                            catch (Exception e)
                            {
                            }
                        });
            }
            else if (metagramAgent.activeAgent.agentStatus.responseStatus == loginRequired)
            {
                ConfirmationDialog dialog = new ConfirmationDialog();
                dialog.showDialog(this,
                        getString(R.string.addOrder_WarningTitle),
                        getString(R.string.connectionInfo_loginRequired),
                        getString(R.string.connectionInfo_loginRequiredButton),
                        () ->
                        {
                            if (!isShowing) {return;}
                            editAccountButton.callOnClick();
                        });
            }
        });

    }

    public void checkLoginStatus()
    {
        if (isFinishing()) { return;}
        if (isDestroyed()) {return;}

        if (metagramAgent.getNoOfAccounts() > 0)
        {
            if (fragmentMode == enum_fragmentMode.idle)
            {
                fragmentMode = enum_fragmentMode.profile;
            }
            bottomMenu.setVisibility(View.VISIBLE);
        }
        else
        {
            fragmentMode = enum_fragmentMode.idle;
            bottomMenu.setVisibility(View.GONE);
        }
    }


    public void setFragments()
    {
        topFrameLayout.setVisibility(View.GONE);

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragmentList)
        {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }

        switch (fragmentMode)
        {
            case idle:
                disableMenuButtons();
                topFrameLayout.setVisibility(View.VISIBLE);
                logInFragment = new LogInFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.topFragment_frameLayout, logInFragment).commitAllowingStateLoss();
                idleFragment = new IdleFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.bottomFragment_frameLayout, idleFragment).commitAllowingStateLoss();
                break;
            case others:
                enableMenuButtons();
                reportsFragment = new ReportsFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.bottomFragment_frameLayout, reportsFragment).commitAllowingStateLoss();
                break;
            case profile:
                enableMenuButtons();
                profileFragment = new ProfileFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.bottomFragment_frameLayout, profileFragment).commitAllowingStateLoss();
                break;
        }
    }

    private void findUIElements()
    {
        rootLayout = findViewById(R.id.mainActivity_rootLayout);

        menuExternalShimmer = findViewById(R.id.mainActivity_externalShimmer);
        menuExternalShimmerTextView = findViewById(R.id.mainActivity_externalShimmerTextView);
        menuInternalShimmer = findViewById(R.id.mainActivity_internalShimmer);
        menuInternalShimmerTextView = findViewById(R.id.mainActivity_internalShimmerTextView);

        //mainMenuButton = findViewById(R.id.mainActivity_menuButton);
        //rubyMenuButton = findViewById(R.id.mainActivity_rubyButton);
        //rubyCountTextView = findViewById(R.id.mainActivity_rubyCount);
        //setTextViewFontArvoRegular(this, rubyCountTextView);
        bottomMenu = findViewById(R.id.mainActivity_bottomMenu);

        topFrameLayout = findViewById(R.id.topFragment_frameLayout);
        bottomFrameLayout = findViewById(R.id.bottomFragment_frameLayout);

        statusButton = findViewById(R.id.mainActivity_statusButton);
        deleteAccountButton = findViewById(R.id.mainActivity_deleteAccount);
        editAccountButton = findViewById(R.id.mainActivity_editAccount);

    }

    @Override
    public void onBackPressed()
    {

        if (backTouched)
        {
            finishAffinity();
        }

        this.backTouched = true;

        Snackbar snackbar = Snackbar
                .make(rootLayout, getResources().getString(R.string.mainMenu_backButton), Snackbar.LENGTH_LONG);
        snackbar.getView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        snackbar.show();

        new Handler().postDelayed(() ->
        {
            backTouched = false;
        }, 2000);
    }

    public void enableMenuButtons()
    {
        editAccountButton.setVisibility(View.VISIBLE);
        deleteAccountButton.setVisibility(View.VISIBLE);
        statusButton.setVisibility(View.VISIBLE);
    }

    public void disableMenuButtons()
    {
        editAccountButton.setVisibility(View.GONE);
        deleteAccountButton.setVisibility(View.GONE);
        statusButton.setVisibility(View.GONE);
    }

    public void checkVersionValidity()
    {

        if (deviceSettings.mustUpgrade)
        {
            /*InformationDialog dialog = new InformationDialog();
            dialog.showDialog(this, getResources().getString(R.string.error_mustUpgradeTitle),
                    getResources().getString(R.string.error_mustUpgrade),
                    getResources().getString(R.string.button_ok), () -> finish());*/
        }
    }

    public void loadProfileResultFragment(long IPK, int OrderID)
    {
        if (IPK == metagramAgent.activeAgent.userID)
        {
            if (profileFragment.isAdded())
            {profileFragment.checkActiveAccountReport(IPK);}
        }
        else
        {
            reportsFragment.changeFragmentToResult(IPK, OrderID);
        }
    }


    public void reloadFragment(long IPK, int OrderID)
    {
        if (IPK == metagramAgent.activeAgent.userID)
        {
            profileFragment.checkActiveAccountReport(IPK);
        }
        else
        {
            if (reportsFragment != null && reportsFragment.isAdded())
            {reportsFragment.changeFragmentToProgress(IPK, OrderID);}
        }
    }


    public void checkForSurvey()
    {
        if (appVersion.supportedPayments.contains(VersionUtils.PaymentType.bazzar_rial))
        {

            threadPoolExecutor.execute(() ->
            {
                try
                {
                    String install_uuid = dbMetagram.getPair("install_uuid").trim();

                    if (install_uuid.equals(""))
                    {
                        install_uuid = UUID.randomUUID().toString().trim();

                        if (callTracker(this, install_uuid).equals("ok"))
                        {dbMetagram.setPair("install_uuid", install_uuid);}
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            });

            threadPoolExecutor.execute(() ->
            {
                String bazzar_survey = "";
                try
                {
                    bazzar_survey = dbMetagram.getPair("bazzar_survey");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                String finalBazzar_survey = bazzar_survey;
                new Handler(getMainLooper()).postDelayed(() ->
                {
                    if (deviceSettings.firstRunTime != 0 && !finalBazzar_survey.equals("done") &&
                            (System.currentTimeMillis() - deviceSettings.firstRunTime > 3 * 24 * 3600 * 1000))
                    {
                        try
                        {
                            new Handler(getMainLooper()).postDelayed(() ->
                            {
                                try
                                {
                                    Intent intent = new Intent(Intent.ACTION_EDIT);
                                    intent.setData(Uri.parse("bazaar://details?id=nava.metagram"));
                                    intent.setPackage("com.farsitel.bazaar");
                                    startActivity(intent);
                                }
                                catch (Exception e)
                                {

                                }
                            }, 500);


                            dbMetagram.setPair("bazzar_survey", "done");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, 1000);
            });
        }
    }



    public void showHelp_delete()
    {
        showInteractiveHelp(ProfileFragment_delete,
                this,
                getResources().getString(R.string.i_ProfileFragment_delete_title),
                getResources().getString(R.string.i_ProfileFragment_delete_content),
                deleteAccountButton, (View v) -> showHelp_edit(),
                Gravity.auto);

    }

    public void showHelp_edit()
    {
        showInteractiveHelp(ProfileFragment_edit,
                this,
                getResources().getString(R.string.i_ProfileFragment_edit_title),
                getResources().getString(R.string.i_ProfileFragment_edit_content),
                editAccountButton, (View v) -> showHelp_status(),
                Gravity.auto);
    }

    public void showHelp_status()
    {
        showInteractiveHelp(ProfileFragment_status,
                this,
                getResources().getString(R.string.i_ProfileFragment_status_title),
                getResources().getString(R.string.i_ProfileFragment_status_content),
                statusButton,
                (View v)->
                {
                    dbMetagram.setItemStatus(isShowingHelp, 0);

                    if (profileFragment != null && profileFragment.isAdded())
                        profileFragment.showFragmentsHelp();
                },
                Gravity.auto);
    }

}
