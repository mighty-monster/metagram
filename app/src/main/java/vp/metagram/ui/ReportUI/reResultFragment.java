package vp.metagram.ui.ReportUI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;


import vp.metagram.R;
import vp.metagram.ui.MainActivity;
import vp.metagram.ui.AccBrowser.AccntBrowserActivity;
import vp.metagram.ui.Dialogs.ConfirmationDialog;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.ui.Dialogs.ReportInfoDialog;
import vp.metagram.ui.Other.ReportListActivity;
import vp.metagram.ui.ReportUI.chartViewer.ChartViewerActivity;
import vp.metagram.ui.ReportUI.reportViewer.ReportSelectorActivity;
import vp.metagram.utils.instagram.executors.statistics.types.StatisticsJob;
import vp.metagram.utils.instagram.executors.statistics.types.StatisticsOrder;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

import static vp.metagram.general.functions.convertRank;
import static vp.metagram.general.functions.getDateTimeFromTimeStampRevert;
import static vp.metagram.general.functions.setImageButtonEnabled;
import static vp.metagram.general.functions.setImageButtonEnabledWithGrayScale;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.functions.setTextViewFontRank;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ResultFragment_Help;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ResultFragment_chart;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ResultFragment_engagement;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ResultFragment_rank;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ResultFragment_refresh;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ResultFragment_report;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ResultFragment_setting;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.isShowingHelp;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;



public class reResultFragment extends Fragment
{

    View rootLayout;

    long IPK;
    int OrderID;
    String username;
    String rank;
    boolean isPrivate;

    StatisticsOrder Order;
    StatisticsJob lastJob;

    ImageButton deleteOrder;
    ImageButton editOrder;
    ImageButton refreshOrder;
    ImageButton reportType;

    ImageView profilePicture;

    TextView rankTextView;

    TextView datetimeTextView;
    TextView engagementTextView;

    ImageButton reportButton;
    ImageButton chartButton;

    TextView usernameTextView;

    final static int pictureSleep = 5000;
    final static int rankSleep = 20000;
    final static int animationDuration = 1200;

    boolean isPictureVisible = true;
    Animation fadeOut = new AlphaAnimation(1, 0);
    Animation fadeIn = new AlphaAnimation(0, 1);

    boolean chartsEnabled = false;
    boolean reportsEnabled = false;
    int noOfJobs = 0;
    int noOfJobsAfterReborn = 0;

    int noOfFollowers = 0;
    int noOfFollowings = 0;


    public static reResultFragment newInstance(long IPK, int OrderID)
    {
        reResultFragment fragment = new reResultFragment();

        fragment.IPK = IPK;
        fragment.OrderID = OrderID;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        threadPoolExecutor.execute(() ->
        {
            try
            {
                Order = new StatisticsOrder(OrderID);
                lastJob = StatisticsJob.getLastFinishedJob(OrderID);


                noOfFollowers = Order.userInfo.getInt("followerCount");
                noOfFollowings = Order.userInfo.getInt("followingCount");
                new Handler(Looper.getMainLooper()).post(() -> configureUI());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        showHelp();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_statistics_result, container, false);

        findUIElements();

        prepareUIElements();

        return rootLayout;
    }

    @Override
    public void onDestroy()
    {
        try
        {
            metagramAgent.statObservatory.removeFragment(this);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    void configureUI()
    {
        if (!isAdded())
        {
            return;
        }

        try
        {
            isPrivate = Order.userInfo.getBoolean("isPrivate");

            metagramAgent.activeAgent.picasso.load(Order.userInfo.getString("picURL"))
                    .error(getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(profilePicture);

            if (lastJob == null) {return;}

            rank = convertRank(lastJob.Result.getString("rank"));
            rankTextView.setText(rank);

            datetimeTextView.setText(getDateTimeFromTimeStampRevert(getActivity(), lastJob.EndTime));


            username = Order.userInfo.getString("username");
            if (metagramAgent.activeAgent.userID == IPK)
            {
                //usernameTextView.setVisibility(View.GONE);
                usernameTextView.setText(username);
            }
            else
            {
                usernameTextView.setText(username);

            }

            engagementTextView.setText(String.format(Locale.ENGLISH, String.format(getResources().getString(R.string.statisticsResult_Engagement),
                    Float.parseFloat(lastJob.Result.getString("rankEngagement")) * 100)));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        setImageButtonEnabledWithGrayScale(getActivity(), false, chartButton, R.drawable.ic_charts);
        try
        {
            noOfJobs = metagramAgent.getNoOfStatisticsJobs(OrderID);
            noOfJobsAfterReborn = metagramAgent.getNoOfStatisticsJobsAfterReborn(OrderID);
            if (noOfJobs > 1)
            {
                chartsEnabled = true;
                setImageButtonEnabledWithGrayScale(getActivity(), true, chartButton, R.drawable.ic_charts);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }


        setImageButtonEnabledWithGrayScale(getActivity(), false, reportButton, R.drawable.ic_reports);
        if (Order.F_Parameter || Order.D_Parameter || Order.P_Parameter)
        {
            reportsEnabled = true;
            setImageButtonEnabledWithGrayScale(getActivity(), true, reportButton, R.drawable.ic_reports);
        }


        //new Handler(Looper.getMainLooper()).postDelayed(() -> startRankPicAnimation(), pictureSleep);
    }

    public void disableTheRemoveButton()
    {
        if (deleteOrder != null)
        {
            setImageButtonEnabled(getActivity(), false, deleteOrder, R.drawable.ic_delete_black);
        }
        else
        {
            new Handler(Looper.getMainLooper()).postDelayed(() -> disableTheRemoveButton(), 100);
        }
    }

    public void findUIElements()
    {
        deleteOrder = rootLayout.findViewById(R.id.statisticsProgress_deleteOrder);
        editOrder = rootLayout.findViewById(R.id.statisticsProgress_pauseButton);
        refreshOrder = rootLayout.findViewById(R.id.statisticsProgress_playButton);
        reportType = rootLayout.findViewById(R.id.statisticsProgress_reportType);
        profilePicture = rootLayout.findViewById(R.id.statisticsProgress_pic);

        rankTextView = rootLayout.findViewById(R.id.statisticsProgress_RankTextView);
        setTextViewFontRank(getActivity(), rankTextView);

        reportButton = rootLayout.findViewById(R.id.statisticsResult_reportButton);
        chartButton = rootLayout.findViewById(R.id.statisticsResult_chartButton);

        datetimeTextView = rootLayout.findViewById(R.id.statisticsResult_datetime);
        setTextViewFontForMessage(getActivity(), datetimeTextView);
        engagementTextView = rootLayout.findViewById(R.id.statisticsResult_engagement);
        setTextViewFontForMessage(getActivity(), engagementTextView);

        usernameTextView = rootLayout.findViewById(R.id.statisticsResult_username);
        setTextViewFontForMessage(getActivity(), engagementTextView);

    }

    public void prepareUIElements()
    {
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(animationDuration);

        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(animationDuration);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                if (isPictureVisible)
                {
                    rankTextView.startAnimation(fadeIn);
                    profilePicture.setVisibility(View.GONE);
                    rankTextView.setVisibility(View.VISIBLE);
                    isPictureVisible = !isPictureVisible;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> startRankPicAnimation(), rankSleep);
                }
                else
                {
                    profilePicture.startAnimation(fadeIn);
                    rankTextView.setVisibility(View.GONE);
                    profilePicture.setVisibility(View.VISIBLE);
                    isPictureVisible = !isPictureVisible;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> startRankPicAnimation(), pictureSleep);
                }


            }

            public void onAnimationRepeat(Animation animation)
            {
            }

            public void onAnimationStart(Animation animation)
            {
            }
        });

        try
        {
            metagramAgent.statObservatory.addFragment(OrderID, this);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        setOnClicks();
    }


    public void startRankPicAnimation()
    {
        if (isPictureVisible)
        {
            profilePicture.startAnimation(fadeOut);
        }
        else
        {
            rankTextView.startAnimation(fadeOut);
        }
    }


    public void setOnClicks()
    {
        refreshOrder.setOnClickListener((View view) ->
        {
            threadPoolExecutor.execute(() ->
            {
                try
                {
                    metagramAgent.activeAgent.refreshAgentInfo();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });

            threadPoolExecutor.execute(() ->
            {
                try
                {
                    new Handler(Looper.getMainLooper()).post(() -> setImageButtonEnabled(getActivity(), false, refreshOrder, R.drawable.ic_refresh_black));
                    metagramAgent.addStatisticsJob(IPK);

                    new Handler(Looper.getMainLooper()).postDelayed(() ->
                            ((MainActivity) getActivity()).reloadFragment(IPK, OrderID), 500);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        });

        deleteOrder.setOnClickListener((View v) ->
        {
            ConfirmationDialog dialog = new ConfirmationDialog();
            try
            {
                dialog.showDialog(getActivity(),
                        getResources().getString(R.string.removeStatistics_title),
                        String.format(getResources().getString(R.string.removeStatistics_content), Order.userInfo.getString("username").trim()),
                        getResources().getString(R.string.button_confirmCaption), () ->
                                threadPoolExecutor.execute(() ->
                                {
                                    try
                                    {
                                        metagramAgent.deleteStatisticOrderByIPK(IPK);

                                        new Handler(Looper.getMainLooper()).post(() ->
                                        {
                                            Activity activity = getActivity();
                                            if (activity instanceof MainActivity)
                                            {
                                                MainActivity mainActivity = (MainActivity) activity;

                                                mainActivity.reportsFragment.removeFragment(OrderID);
                                            }
                                        });

                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        });

        View.OnClickListener openAccntPage = (View v) ->
        {
            try
            {
                Intent intent = new Intent(getActivity(), AccntBrowserActivity.class);
                intent.putExtra("username", Order.userInfo.getString("username"));
                intent.putExtra("picURL", Order.userInfo.getString("picURL"));
                intent.putExtra("ipk", IPK);
                intent.putExtra("isPrivate", Order.userInfo.getBoolean("isPrivate"));
                startActivity(intent);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        };

        profilePicture.setOnClickListener(openAccntPage);

        usernameTextView.setOnClickListener(openAccntPage);


        reportButton.setOnClickListener((View v) ->
        {
            if (reportsEnabled)
            {
                Intent intent = new Intent(getActivity(), ReportSelectorActivity.class);
                intent.putExtra("F_Parameter", Order.F_Parameter);
                intent.putExtra("P_Parameter", Order.P_Parameter);
                intent.putExtra("D_Parameter", Order.D_Parameter);
                intent.putExtra("R_Parameter", Order.R_Parameter);
                intent.putExtra("noOfJobs", noOfJobsAfterReborn);
                intent.putExtra("OrderID", OrderID);
                intent.putExtra("username", username);
                intent.putExtra("IPK", IPK);
                intent.putExtra("rank", rank);
                intent.putExtra("isPrivate", isPrivate);
                intent.putExtra("noOfFollowers", noOfFollowers);
                intent.putExtra("noOfFollowings", noOfFollowings);

                getActivity().startActivity(intent);
            }
            else
            {
                ConfirmationDialog dialog = new ConfirmationDialog();
                dialog.showDialog(getActivity(),
                        getString(R.string.accntBrowser_downloadTitle),
                        getString(R.string.disabledReport_message),
                        getString(R.string.disabledReport_button), () ->
                        {
                            Intent intent = new Intent(getActivity(), ReportListActivity.class);
                            getActivity().startActivity(intent);
                        });
            }
        });

        chartButton.setOnClickListener((View v) ->
        {
            if (chartsEnabled)
            {
                Intent intent = new Intent(getActivity(), ChartViewerActivity.class);
                intent.putExtra("F_Parameter", Order.F_Parameter);
                intent.putExtra("P_Parameter", Order.P_Parameter);
                intent.putExtra("D_Parameter", Order.D_Parameter);
                intent.putExtra("R_Parameter", Order.R_Parameter);
                intent.putExtra("OrderID", OrderID);
                intent.putExtra("username", username);
                intent.putExtra("IPK", IPK);
                intent.putExtra("rank", rank);

                getActivity().startActivity(intent);
            }
            else
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(getActivity(),
                        getString(R.string.accntBrowser_downloadTitle),
                        getString(R.string.disabledChart_message),
                        getString(R.string.button_ok), null);
            }
        });


        editOrder.setOnClickListener((View v) ->
        {
            Intent intent = new Intent(getActivity(), AddOrderActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("ipk", IPK);
            intent.putExtra("OrderID", OrderID);

            getActivity().startActivityForResult(intent, 1);
        });

        reportType.setOnClickListener((View v) ->
        {
            ReportInfoDialog reportInfoDialog = ReportInfoDialog.newInstance(username, Order.F_Parameter, Order.P_Parameter, Order.D_Parameter, Order.R_Parameter,
                    (View view) -> editOrder.callOnClick(), true);

            reportInfoDialog.show(getActivity().getFragmentManager(), "");
        });
    }

    public void showHelp()
    {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
        {
            if (!isAdded())
            { return; }

            if (dbMetagram.getItemStatus(isShowingHelp) != 0)
            { return; }

            dbMetagram.setItemStatus(isShowingHelp, 1);

            if (rootLayout != null)
            {
                showInteractiveHelp(ResultFragment_Help,
                        getActivity(),
                        getResources().getString(R.string.i_ResultFragment_Help_title),
                        getResources().getString(R.string.i_ResultFragment_Help_content),
                        rootLayout,
                        (View v1) ->
                                showRankIconHelp(),
                        Gravity.auto);
            }
            else
            {
                dbMetagram.setItemStatus(isShowingHelp, 0);
            }
        }, 300);
    }

    public void showRankIconHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (rankTextView != null)
        {
            showInteractiveHelp(ResultFragment_rank,
                    getActivity(),
                    getResources().getString(R.string.i_ResultFragment_rank_title),
                    getResources().getString(R.string.i_ResultFragment_rank_content),
                    rankTextView,
                    (View v1) ->
                            showEngagementHelp(),
                    Gravity.auto);
        }
        else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showEngagementHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (engagementTextView != null)
        {
            showInteractiveHelp(ResultFragment_engagement,
                    getActivity(),
                    getResources().getString(R.string.i_ResultFragment_engagement_title),
                    getResources().getString(R.string.i_ResultFragment_engagement_content),
                    engagementTextView,
                    (View v1) ->
                            showReportIconHelp(),
                    Gravity.auto);
        }
        else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showReportIconHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (reportButton != null)
        {
            showInteractiveHelp(ResultFragment_report,
                    getActivity(),
                    getResources().getString(R.string.i_ResultFragment_report_title),
                    getResources().getString(R.string.i_ResultFragment_report_content),
                    reportButton,
                    (View v1) ->
                            showChartIconHelp(),
                    Gravity.auto);
        }
        else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showChartIconHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (chartButton != null)
        {
            showInteractiveHelp(ResultFragment_chart,
                    getActivity(),
                    getResources().getString(R.string.i_ResultFragment_chart_title),
                    getResources().getString(R.string.i_ResultFragment_chart_content),
                    chartButton,
                    (View v1) ->
                            showSettingIconHelp(),
                    Gravity.auto);
        }
        else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showSettingIconHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (editOrder != null)
        {
            showInteractiveHelp(ResultFragment_setting,
                    getActivity(),
                    getResources().getString(R.string.i_ResultFragment_setting_title),
                    getResources().getString(R.string.i_ResultFragment_setting_content),
                    editOrder,
                    (View v1) ->
                            showRefreshIconHelp(),
                    Gravity.auto);
        }
        else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showRefreshIconHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (refreshOrder != null)
        {
            showInteractiveHelp(ResultFragment_refresh,
                    getActivity(),
                    getResources().getString(R.string.i_ResultFragment_refresh_title),
                    getResources().getString(R.string.i_ResultFragment_refresh_content),
                    refreshOrder,
                    (View v1) ->
                            dbMetagram.setItemStatus(isShowingHelp, 0),
                    Gravity.auto);
        }
        else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

}
