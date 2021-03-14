package vp.metagram.ui.MainFragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;


import vp.metagram.R;
import vp.metagram.ui.AccBrowser.DownloadHistory.DownloadAccountListActivity;
import vp.metagram.ui.AccBrowser.DownloadStory.DownloadStoryActivity;
import vp.metagram.ui.Dialogs.VersionDialog;
import vp.metagram.ui.MainActivity;
import vp.metagram.ui.MainMenu.HelpActivity;
import vp.metagram.ui.MainMenu.LegalActivity;
import vp.metagram.ui.MainMenu.MemoryActivity;
import vp.metagram.ui.MainMenu.SettingActivity;
import vp.metagram.ui.ReportUI.reProgressFragment;
import vp.metagram.ui.ReportUI.reResultFragment;
import vp.metagram.utils.instagram.types.AccountInfo;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appVersion;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProfileFragment_delete;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProfileFragment_downloadHighlight;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProfileFragment_downloadHistory;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ProfileFragment_downloads;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.isShowingHelp;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateDone;


public class ProfileFragment extends Fragment implements ReportHolderInterface
{

    View rootLayout;

    AccountInfo accountInfo;

    reProgressFragment progressFragment;
    reResultFragment resultFragment;

    SpinKitView spinKitView;

    ConstraintLayout downloadsLayout;
    TextView downloadsTitle;
    Button downloadHighlight;
    Button downloadHistory;
    ImageButton downloadsHelp;

    TextView settingsTitle;
    Button settingsReport;
    Button settingsMemory;


    Button helpText;

    TextView aboutTitle;
    Button aboutLicense;
    Button aboutShare;
    Button aboutVersion;

    boolean isFirstTime = true;

    boolean isReportReady = false;

    boolean isShowing = false;

    @Override
    public void onResume()
    {
        super.onResume();

        isShowing = true;

        if (isFirstTime)
        {
            threadPoolExecutor.execute(() ->
                    new Handler(Looper.getMainLooper()).post(() ->
                            checkActiveAccountReport(metagramAgent.activeAgent.userID)));
            isFirstTime = false;
        }
        else
        {
            showHelp();
        }

    }

    @Override
    public void onPause()
    {
        isShowing = false;
        dbMetagram.setItemStatus(isShowingHelp, 0);
        super.onPause();
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_profile, container, false);

        downloadsLayout = rootLayout.findViewById(R.id.profile_ConstraintLayout_download);
        downloadsTitle = rootLayout.findViewById(R.id.profile_download_title);
        downloadHighlight = rootLayout.findViewById(R.id.profile_download_highlight);
        downloadHistory = rootLayout.findViewById(R.id.profile_download_history);
        downloadsHelp = rootLayout.findViewById(R.id.profile_download_help);

        settingsTitle = rootLayout.findViewById(R.id.profile_setting_title);
        settingsReport = rootLayout.findViewById(R.id.profile_setting_reports);
        settingsMemory = rootLayout.findViewById(R.id.profile_setting_memory);

        helpText = rootLayout.findViewById(R.id.profile_help_text);

        aboutTitle = rootLayout.findViewById(R.id.profile_about_title);
        aboutLicense = rootLayout.findViewById(R.id.profile_about_license);
        aboutShare = rootLayout.findViewById(R.id.profile_about_share);
        aboutVersion = rootLayout.findViewById(R.id.profile_about_version);

        spinKitView = rootLayout.findViewById(R.id.profile_spinKit);

        configureButtons();

        return rootLayout;
    }

    private void configureButtons()
    {

        setTextViewFontForMessage(getActivity(), downloadsTitle);
        setTextViewFontForMessage(getActivity(), downloadHighlight);
        setTextViewFontForMessage(getActivity(), downloadHistory);

        setTextViewFontForMessage(getActivity(), settingsTitle);
        setTextViewFontForMessage(getActivity(), settingsReport);
        setTextViewFontForMessage(getActivity(), settingsMemory);

        setTextViewFontForMessage(getActivity(), helpText);

        setTextViewFontForMessage(getActivity(), aboutTitle);
        setTextViewFontForMessage(getActivity(), aboutLicense);
        setTextViewFontForMessage(getActivity(), aboutShare);
        setTextViewFontForMessage(getActivity(), aboutVersion);


        aboutShare.setOnClickListener((View v) ->
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.shareApp_title));
                String sAux = "\n" + getResources().getString(R.string.shareApp_content) + "\n\n";
                sAux = sAux + appVersion.getDownloadLink() + "\n\n";
                intent.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.shareApp_choose)));


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        helpText.setOnClickListener((View v) ->
        {
            Intent intent = new Intent(getActivity(), HelpActivity.class);
            startActivity(intent);
        });

        downloadHighlight.setOnClickListener((View V) ->
        {
            Intent intent = new Intent(getActivity(), DownloadStoryActivity.class);
            startActivity(intent);
        });

        settingsReport.setOnClickListener((View v) ->
        {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });

        downloadHistory.setOnClickListener((View v) ->
        {
            Intent intent = new Intent(getActivity(), DownloadAccountListActivity.class);
            startActivity(intent);
        });

        aboutVersion.setOnClickListener((View v) ->
        {
            VersionDialog versionDialog = VersionDialog.newInstance();
            versionDialog.show(getActivity().getFragmentManager(), "");
        });

        settingsMemory.setOnClickListener((View v) ->
        {
            Intent intent = new Intent(getActivity(), MemoryActivity.class);
            startActivity(intent);
        });

        aboutLicense.setOnClickListener((View v) ->
        {
            Intent intent = new Intent(getActivity(), LegalActivity.class);
            startActivity(intent);
        });

        downloadsHelp.setOnClickListener((View v) ->
                showDownloadsHelp());

    }

    public void checkActiveAccountReport(long IPK)
    {
        // Have reason for this ... do not remove it in future
        int OrderID = -1;
        try
        {
            OrderID = metagramAgent.getOrderIDByIPK(metagramAgent.activeAgent.userID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            String jobStatus = metagramAgent.checkJobStatus(metagramAgent.activeAgent.userID);

            if (OrderID < 0 || jobStatus == null || jobStatus.equals(""))
            {
                new Handler(Looper.getMainLooper()).postDelayed(() -> checkActiveAccountReport(IPK), 500);
            }
            else
            {
                spinKitView.setVisibility(View.GONE);
                if (jobStatus.equals(statusStateDone))
                {
                    changeFragmentToResult(IPK, OrderID);
                }
                else
                {
                    changeFragmentToProgress(IPK, OrderID);
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void changeFragmentToProgress(long IPK, int OrderID)
    {
        isReportReady = false;
        progressFragment = reProgressFragment.newInstance(IPK, OrderID);
        getChildFragmentManager().beginTransaction().replace(R.id.reportLinearLayout, progressFragment).commitAllowingStateLoss();
    }

    @Override
    public void changeFragmentToResult(long IPK, int OrderID)
    {
        isReportReady = true;
        resultFragment = reResultFragment.newInstance(IPK, OrderID);
        resultFragment.disableTheRemoveButton();
        getChildFragmentManager().beginTransaction().replace(R.id.reportLinearLayout, resultFragment).commitAllowingStateLoss();
    }


    public void showHelp()
    {

        if (dbMetagram.getItemStatus(isShowingHelp) != 0)
        { return; }

        if (dbMetagram.getItemStatus(ProfileFragment_delete) == 0)
        {
            dbMetagram.setItemStatus(isShowingHelp, 1);

            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null)
                mainActivity.showHelp_delete();
        }
    }

    public void showFragmentsHelp()
    {
        if (isReportReady)
            if (resultFragment != null)
                resultFragment.showHelp();
            else if (progressFragment != null)
                progressFragment.showHelp();
    }

    public void showDownloadsHelp()
    {
        if (dbMetagram.getItemStatus(isShowingHelp) != 0)
            return;

        dbMetagram.setItemStatus(isShowingHelp, 1);

        showInteractiveHelp(ProfileFragment_downloads,
                getActivity(),
                getResources().getString(R.string.i_Downloads_title),
                getResources().getString(R.string.i_Downloads_content),
                downloadsLayout,
                (View v) ->
                {
                    showDownloadHighlightHelp();
                    dbMetagram.setItemStatus(ProfileFragment_downloads, 0);
                },
                Gravity.auto);
    }

    public void showDownloadHighlightHelp()
    {
        showInteractiveHelp(ProfileFragment_downloadHighlight,
                getActivity(),
                getResources().getString(R.string.i_DownloadStory_title),
                getResources().getString(R.string.i_DownloadStory_content),
                downloadHighlight,
                (View v) ->
                {
                    showDownloadHistoryHelp();
                    dbMetagram.setItemStatus(ProfileFragment_downloadHighlight, 0);
                },
                Gravity.auto);
    }

    public void showDownloadHistoryHelp()
    {
        showInteractiveHelp(ProfileFragment_downloadHistory,
                getActivity(),
                getResources().getString(R.string.i_DownloadHistory_title),
                getResources().getString(R.string.i_DownloadHistory_content),
                downloadHistory,
                (View v) ->
                {
                    dbMetagram.setItemStatus(isShowingHelp, 0);
                    dbMetagram.setItemStatus(ProfileFragment_downloadHistory, 0);
                },
                Gravity.auto);
    }
}
