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
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicInteger;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import de.hdodenhof.circleimageview.CircleImageView;

import vp.metagram.R;
import vp.metagram.ui.MainActivity;
import vp.metagram.ui.AccBrowser.AccntBrowserActivity;
import vp.metagram.ui.Dialogs.ConfirmationDialog;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.ui.Dialogs.ReportInfoDialog;
import vp.metagram.ui.MainFragments.ReportsFragment;
import vp.metagram.ui.Other.LogViewerActivity;
import vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor;
import vp.metagram.utils.instagram.executors.statistics.types.StatisticsOrder;
import vp.metagram.utils.instagram.types.ResponseStatus;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

import static vp.metagram.general.functions.predictStatisticsDataGatheringTime;
import static vp.metagram.general.functions.secondsToDurationStr;
import static vp.metagram.general.functions.setImageButtonEnabled;
import static vp.metagram.general.functions.setImageButtonEnabledForProgressFragment;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProcessFragment_Help;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProcessFragment_log;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProcessFragment_progress;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProcessFragment_time;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.isShowingHelp;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateReady;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateStop;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateWorking;


public class reProgressFragment extends Fragment
{
    View rootLayout;

    long IPK;
    int OrderID;

    StatisticsOrder Order;

    StatisticsExecutor executor;

    CircularProgressIndicator progressIndicator;

    CircleImageView profilePic;

    ImageButton stopButton;
    ImageButton playButton;
    ImageButton pauseButton;
    ImageButton reportType;

    TextView usernameTexView;
    TextView approxTimeTextView;
    TextView currentProcessTextView;

    final static int pictureSleep = 5000;
    final static int progressSleep = 20000;
    final static int animationDuration = 1200;

    boolean isPictureVisible = false;
    Animation fadeOut = new AlphaAnimation(1, 0);
    Animation fadeIn = new AlphaAnimation(0, 1);

    Animation currentProcessFadeOut = new AlphaAnimation(1, 0);
    Animation currentProcessFadeIn = new AlphaAnimation(0, 1);
    final static int currentProcessAnimationDuration = 500;
    String currentProcess;

    ResponseStatus responseStatus = ResponseStatus.ok;

    final static int approxSleepTime = 5000;

    public static reProgressFragment newInstance(long IPK, int OrderID)
    {
        reProgressFragment fragment = new reProgressFragment();

        fragment.IPK = IPK;
        fragment.OrderID = OrderID;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            metagramAgent.statObservatory.addFragment(OrderID, this);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        executor = metagramAgent.getStatisticsActiveExecutorByOrderID(OrderID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_statistics_progress, container, false);

        findUIElements();

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
                    progressIndicator.startAnimation(fadeIn);
                    profilePic.setVisibility(View.GONE);
                    progressIndicator.setVisibility(View.VISIBLE);
                    isPictureVisible = !isPictureVisible;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> startProgressPicAnimation(), progressSleep);
                } else
                {
                    profilePic.startAnimation(fadeIn);
                    progressIndicator.setVisibility(View.GONE);
                    profilePic.setVisibility(View.VISIBLE);
                    isPictureVisible = !isPictureVisible;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> startProgressPicAnimation(), pictureSleep);
                }


            }

            public void onAnimationRepeat(Animation animation)
            {
            }

            public void onAnimationStart(Animation animation)
            {
            }
        });


        currentProcessFadeOut.setInterpolator(new AccelerateInterpolator());
        currentProcessFadeOut.setDuration(currentProcessAnimationDuration);

        currentProcessFadeIn.setInterpolator(new AccelerateInterpolator());
        currentProcessFadeIn.setDuration(currentProcessAnimationDuration);

        currentProcessFadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationEnd(Animation animation)
            {
                currentProcessTextView.setText(currentProcess);
                currentProcessTextView.startAnimation(currentProcessFadeIn);
            }

            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        usernameTexView.setOnClickListener((View v) ->
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
        });

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

    @Override
    public void onResume()
    {
        super.onResume();

        AtomicInteger noOfJobs = new AtomicInteger();
        threadPoolExecutor.execute(() ->
        {
            try
            {
                Order = new StatisticsOrder(OrderID);
                new Handler(Looper.getMainLooper()).post(() -> configureUI());

                if (IPK == metagramAgent.activeAgent.userID)
                {
                    noOfJobs.set(metagramAgent.getNoOfStatisticsJobs(OrderID));
                    new Handler(Looper.getMainLooper()).post(()->
                            {
                                stopButton.setEnabled(false);
                                setImageButtonEnabled(getActivity(),false,stopButton , R.drawable.ic_stop);
                            });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        setApproxTime();

        if (executor != null )
        {
            setPercent(executor.getGeneralPercentage());

            if (executor.statsExecFlow != null)
            { setResponseStatus(executor.statsExecFlow.lastResponseStatus);}
        }

        showHelp();



    }

    public void findUIElements()
    {
        progressIndicator = rootLayout.findViewById(R.id.statisticsProgress_ProgressBar);
        progressIndicator.setMaxProgress(100);

        profilePic = rootLayout.findViewById(R.id.statisticsProgress_pic);

        stopButton = rootLayout.findViewById(R.id.statisticsProgress_stopOrder);
        playButton = rootLayout.findViewById(R.id.statisticsProgress_playButton);
        pauseButton = rootLayout.findViewById(R.id.statisticsProgress_pauseButton);

        reportType = rootLayout.findViewById(R.id.statisticsProgress_reportType);

        usernameTexView = rootLayout.findViewById(R.id.statisticsProgress_username);
        setTextViewFontForMessage(getActivity(), usernameTexView);

        approxTimeTextView = rootLayout.findViewById(R.id.statisticsProgress_approxTime);
        setTextViewFontForMessage(getActivity(), approxTimeTextView);

        currentProcessTextView = rootLayout.findViewById(R.id.statisticsProgress_currentProcess);
        setTextViewFontForMessage(getActivity(), currentProcessTextView);

    }

    void configureUI()
    {
        if (executor == null || executor.username == null || !isAdded())
        {
            executor = metagramAgent.getStatisticsActiveExecutorByOrderID(OrderID);
            new Handler(Looper.getMainLooper()).postDelayed(() -> configureUI(), 500);
            return;
        }

        usernameTexView.setText(executor.username);
        currentProcessTextView.setText(executor.currentProcess);

        try
        {
            metagramAgent.activeAgent.picasso.load(Order.userInfo.getString("picURL"))
                    .error(getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(profilePic);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        //new Handler(Looper.getMainLooper()).postDelayed(() -> startProgressPicAnimation(), progressSleep);


        if (executor.status.equals(statusStateStop))
        {
            setImageButtonEnabledForProgressFragment(getActivity(), true, playButton, R.drawable.ic_play);
            setImageButtonEnabledForProgressFragment(getActivity(), false, pauseButton, R.drawable.ic_pause);
        } else
        {
            setImageButtonEnabledForProgressFragment(getActivity(), false, playButton, R.drawable.ic_play);
            setImageButtonEnabledForProgressFragment(getActivity(), true, pauseButton, R.drawable.ic_pause);
        }

        setOnClicks();

    }


    public void setOnClicks()
    {
        reportType.setOnLongClickListener(view ->
        {

            Intent intent = new Intent(getActivity(), LogViewerActivity.class);
            intent.putExtra("Type", "Report");
            intent.putExtra("JobID", executor.StatisticsJobID);

            startActivity(intent);

            return true;
        });

        pauseButton.setOnClickListener((View v) ->
        {
            if (executor.status.equals(statusStateWorking) || executor.status.equals(statusStateReady))
            {
                try
                {
                    setImageButtonEnabledForProgressFragment(getActivity(), false, pauseButton, R.drawable.ic_pause);
                    executor.stop();

                    setImageButtonEnabledForProgressFragment(getActivity(), true, playButton, R.drawable.ic_play);

                    Fragment parent = getParentFragment();
                    if (parent instanceof ReportsFragment)
                    {
                        ((ReportsFragment) parent).setReportStatus(OrderID, statusStateStop);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });


        playButton.setOnClickListener((View v) ->
        {
            if (executor.status.equals(statusStateStop))
            {
                try
                {
                    setImageButtonEnabledForProgressFragment(getActivity(), false, playButton, R.drawable.ic_play);

                    executor.start();

                    setImageButtonEnabledForProgressFragment(getActivity(), true, pauseButton, R.drawable.ic_pause);

                    Fragment parent = getParentFragment();
                    if (parent instanceof ReportsFragment)
                    {
                        ((ReportsFragment) parent).setReportStatus(OrderID, statusStateWorking);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        stopButton.setOnClickListener((View v) ->
        {
            ConfirmationDialog dialog = new ConfirmationDialog();
            try
            {
                int noOfJobs = metagramAgent.getNoOfStatisticsJobs(OrderID);

                if (noOfJobs < 2)
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

                    return;
                }

                dialog.showDialog(getActivity(),
                        getResources().getString(R.string.cancelStatistics_title),
                        String.format(getResources().getString(R.string.cancelStatistics_content), Order.userInfo.getString("username").trim()),
                        getResources().getString(R.string.button_confirmCaption), () ->
                                threadPoolExecutor.execute(() ->
                                {
                                    try
                                    {
                                        metagramAgent.cancelStatisticsByOrderID(IPK, OrderID);

                                        new Handler(Looper.getMainLooper()).post(() ->
                                        {
                                            Activity activity = getActivity();
                                            if (activity instanceof MainActivity)
                                            {
                                                MainActivity mainActivity = (MainActivity) activity;

                                                if (metagramAgent.activeAgent.userID == IPK)
                                                {
                                                    mainActivity.profileFragment.changeFragmentToResult(IPK, OrderID);
                                                }
                                                else
                                                {
                                                    if (mainActivity.reportsFragment != null)
                                                    {
                                                        mainActivity.reportsFragment.changeFragmentToResult(IPK, OrderID);
                                                    }
                                                }
                                            }
                                        });

                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }));
            }
            catch (JSONException | GeneralSecurityException | IOException e)
            {
                e.printStackTrace();
            }
        });


        reportType.setOnClickListener((View v) ->
        {

            if (responseStatus == ResponseStatus.ok)
            {
                ReportInfoDialog reportInfoDialog = ReportInfoDialog.newInstance(executor.username, Order.F_Parameter, Order.P_Parameter, Order.D_Parameter, Order.R_Parameter,
                        (View view) -> reportType.performLongClick(), false);

                reportInfoDialog.show(getActivity().getFragmentManager(), "");
            }
            else if (responseStatus == ResponseStatus.noInternet)
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(getActivity(),
                        getActivity().getString(R.string.addOrder_WarningTitle),
                        getActivity().getString(R.string.connectionInfo_noInternetContent),
                        getActivity().getString(R.string.button_ok),null);
            }
            else if (responseStatus == ResponseStatus.loginRequired)
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(getActivity(),
                        getActivity().getString(R.string.addOrder_WarningTitle),
                        getActivity().getString(R.string.connectionInfo_loginRequired),
                        getActivity().getString(R.string.button_ok),null);
            }
            else if (responseStatus == ResponseStatus.notAuthorized)
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(getActivity(),
                        getActivity().getString(R.string.addOrder_WarningTitle),
                        getActivity().getString(R.string.reportInfo_notAutorized),
                        getActivity().getString(R.string.button_ok),null);
            }

            else if (responseStatus == ResponseStatus.rateLimit)
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(getActivity(),
                        getActivity().getString(R.string.addOrder_WarningTitle),
                        getActivity().getString(R.string.reportInfo_rateLimit),
                        getActivity().getString(R.string.button_ok),null);
            }
        });

    }


    public void startProgressPicAnimation()
    {
        if (isPictureVisible)
        {
            profilePic.startAnimation(fadeOut);
        } else
        {
            progressIndicator.startAnimation(fadeOut);
        }
    }

    public void setPercent(double percent)
    {
        if (isAdded())
        {
            new Handler(Looper.getMainLooper()).post(() -> progressIndicator.setCurrentProgress(percent));

        }
    }

    public void loadResultFragment(long IPK)
    {
        if (isAdded())
        {
            Activity activity = getActivity();

            if (activity == null)
            {
                return;
            }

            if (activity instanceof MainActivity)
            {
                new Handler(Looper.getMainLooper()).post(() ->
                {
                    if (Order != null)
                        ((MainActivity) activity).loadProfileResultFragment(IPK, Order.OrderID);
                });
            }

        }
    }

    public void setCurrentProcess(String currentProcess)
    {
        if (isAdded())
        {
            if (this.currentProcess == null)
            { this.currentProcess = new String(); }

            if (this.currentProcess.equals(currentProcess))
            { return; }

            this.currentProcess = currentProcess;
            new Handler(Looper.getMainLooper()).post(() -> currentProcessTextView.startAnimation(currentProcessFadeOut));
        }
    }

    public void setApproxTime()
    {
        if (isAdded())
        {
            if (approxTimeTextView != null)
            {

                if (executor != null && executor.statsExecFlow != null)
                {
                    String approxString = getResources().getString(R.string.statisticsProgress_Estimation) + " " +
                            secondsToDurationStr(predictStatisticsDataGatheringTime(
                                    executor.statsExecFlow,
                                    metagramAgent.activeAgent,
                                    executor.F_Parameter,
                                    executor.P_Parameter,
                                    executor.D_Parameter),
                                    getActivity());

                    approxTimeTextView.setText(approxString);
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> setApproxTime(), approxSleepTime);

            }
        }
    }

    public void setResponseStatus(ResponseStatus responseStatus)
    {
        this.responseStatus = responseStatus;
        if (responseStatus == ResponseStatus.ok)
        {
            reportType.setImageResource(R.drawable.ic_order_type_statistics);
        }
        else if (responseStatus == ResponseStatus.noInternet)
        {
            reportType.setImageResource(R.drawable.ic_no_internet);
        }
        else
        {
            reportType.setImageResource(R.drawable.ic_warning);
        }
    }


    public void showHelp()
    {
        if (!isAdded())
        { return; }

        if (dbMetagram.getItemStatus(isShowingHelp) != 0)
        { return; }

        dbMetagram.setItemStatus(isShowingHelp, 1);

        if (rootLayout != null)
        {
            showInteractiveHelp(ProcessFragment_Help,
                    getActivity(),
                    getResources().getString(R.string.i_ProcessFragment_Help_title),
                    getResources().getString(R.string.i_ProcessFragment_Help_content),
                    rootLayout,
                    (View v1) ->
                            showIndicatorHelp(),
                    Gravity.auto);
        } else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showIndicatorHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (progressIndicator != null)
        {

            showInteractiveHelp(ProcessFragment_progress,
                    getActivity(),
                    getResources().getString(R.string.i_ProcessFragment_percent_title),
                    getResources().getString(R.string.i_ProcessFragment_percent_content),
                    progressIndicator,
                    (View v2) ->
                            showApproxTimeHelp(),
                    Gravity.auto);
        } else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showApproxTimeHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (approxTimeTextView != null)
        {
            showInteractiveHelp(ProcessFragment_time,
                    getActivity(),
                    getResources().getString(R.string.i_ProcessFragment_time_title),
                    getResources().getString(R.string.i_ProcessFragment_time_content),
                    approxTimeTextView,
                    (View v3) ->
                    {
                        showReportTypeHelp();
                    },
                    Gravity.auto);
        } else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showReportTypeHelp()
    {
        if (!isAdded())
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
            return;
        }

        if (reportType != null)
        {
            showInteractiveHelp(ProcessFragment_log,
                    getActivity(),
                    getResources().getString(R.string.i_ProcessFragment_log_title),
                    getResources().getString(R.string.i_ProcessFragment_log_content),
                    reportType,
                    (View v4) ->
                            dbMetagram.setItemStatus(isShowingHelp, 0),
                    Gravity.auto);
        } else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }



}
