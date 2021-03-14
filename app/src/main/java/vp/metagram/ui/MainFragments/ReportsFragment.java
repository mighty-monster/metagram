package vp.metagram.ui.MainFragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import vp.metagram.R;
import vp.metagram.ui.ReportUI.AddOrderActivity;
import vp.metagram.ui.ReportUI.reProgressFragment;
import vp.metagram.ui.ReportUI.reResultFragment;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

import static vp.metagram.general.functions.dpToPixels;
import static vp.metagram.general.functions.setImageButtonEnabled;
import static vp.metagram.general.functions.toggleImageButton;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ReportFragment_Help;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.ReportFragment_filter;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.isShowingHelp;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateDone;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateStop;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.statusStateWorking;



public class ReportsFragment extends Fragment implements ReportHolderInterface
{
    final int result_code = 1;

    View rootLayout;

    FloatingActionButton addOrderButton;

    LinearLayout fragmentsRootLayout;

    Map<Integer, Integer> frameIds = new HashMap<>();
    //Map<Integer, String> usernameIds = new HashMap<>();

    public SearchView searchView;
    SpinKitView loadingView;

    ImageButton allFilter;
    ImageButton finishedFilter;
    ImageButton runningFilter;
    ImageButton pausedFilter;
    ImageButton searchButton;

    boolean isAllFilter = true;
    boolean isFinishedFilter = false;
    boolean isRunningFilter = false;
    boolean isPausedFilter = false;

    String searchValue;

    List<ReportItem> reportItems = new ArrayList<>();

    int reportItemsIndex = 0;
    int reportItemsLoadNo = 6;

    ScrollView reportsScroll;

    ViewTreeObserver.OnScrollChangedListener scrollChangedListener;

    boolean isFirstTime = true;

    boolean isSearchButtonClicked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_reports, container, false);

        addOrderButton = rootLayout.findViewById(R.id.reports_addOrder);
        fragmentsRootLayout = rootLayout.findViewById(R.id.reports_fragmentRootLayout);
        searchView = rootLayout.findViewById(R.id.reports_searchView);
        loadingView = rootLayout.findViewById(R.id.reports_loading);

        allFilter = rootLayout.findViewById(R.id.reports_allFilter);
        finishedFilter = rootLayout.findViewById(R.id.reports_finishedFilter);
        runningFilter = rootLayout.findViewById(R.id.reports_playFilter);
        pausedFilter = rootLayout.findViewById(R.id.reports_pauseFilter);
        reportsScroll = rootLayout.findViewById(R.id.scrollView2);
        searchButton = rootLayout.findViewById(R.id.reports_searchFilter);

        scrollChangedListener = () ->
        {
            if (reportsScroll.getChildAt(0).getBottom()
                    == (reportsScroll.getHeight() + reportsScroll.getScrollY()))
            {
                if (reportItemsIndex >= reportItems.size())
                {
                    return;
                }
                threadPoolExecutor.execute(() ->
                {
                    try
                    {
                        createReportsFragments();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                });
            }
        };

        configureButtons();

        return rootLayout;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isFirstTime)
        {
            threadPoolExecutor.execute(() ->
            {
                try
                {
                    loadReports();
                    createReportsFragments();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    new Handler(Looper.getMainLooper()).post(() ->
                            reportsScroll.getViewTreeObserver().addOnScrollChangedListener(scrollChangedListener));
                }
            });

            new Handler(Looper.getMainLooper()).postDelayed(this::shakeActionButton, 3000);

            isFirstTime = false;
        }

        showHelp();

        setFilterIcons();


    }

    public void shakeActionButton()
    {
        if (!isAdded()) {return;}
        ObjectAnimator
                .ofFloat(addOrderButton, "translationX", 0, 25, -25, 25, -25,15, -15, 6, -6, 0)
                .setDuration(700)
                .start();
        new Handler(Looper.getMainLooper()).postDelayed(this::shakeActionButton,10000 );
    }

    @Override
    public void onPause()
    {
        dbMetagram.setItemStatus(isShowingHelp, 0);
        super.onPause();
    }

    private void configureButtons()
    {
        addOrderButton.setOnClickListener((View v) ->
        {
            Intent intent = new Intent(getActivity(), AddOrderActivity.class);
            getActivity().startActivityForResult(intent, result_code);
        });

        searchView.setOnClickListener((View view) ->
                searchView.setIconified(false));

        searchView.onActionViewExpanded();
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                searchValue = query;
                filterReports();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {

                if (reportItemsIndex >= reportItems.size())
                {
                    searchValue = newText;
                    performFiltering();
                }

                if (newText.equals(""))
                {
                    performFiltering();
                    searchView.clearFocus();
                }

                return true;
            }
        });


        allFilter.setOnClickListener((View v) ->
        {
            isAllFilter = true;
            isFinishedFilter = false;
            isRunningFilter = false;
            isPausedFilter = false;

            setFilterIcons();
            filterReports();
        });

        finishedFilter.setOnClickListener((View v) ->
        {
            isAllFilter = false;
            isFinishedFilter = true;
            isRunningFilter = false;
            isPausedFilter = false;

            setFilterIcons();
            filterReports();
        });

        runningFilter.setOnClickListener((View v) ->
        {
            isAllFilter = false;
            isFinishedFilter = false;
            isRunningFilter = true;
            isPausedFilter = false;

            setFilterIcons();
            filterReports();
        });

        pausedFilter.setOnClickListener((View v) ->
        {
            isAllFilter = false;
            isFinishedFilter = false;
            isRunningFilter = false;
            isPausedFilter = true;

            setFilterIcons();

            filterReports();
        });

        searchButton.setOnClickListener((View v)->
        {
            isSearchButtonClicked = true;
            searchButton.setVisibility(View.GONE);
            try
            {
                createReportsFragments();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        });
    }

    public void setFilterEnabled(boolean enabled)
    {
        if (enabled)
        {
            loadingView.setVisibility(View.GONE);

            if (isSearchButtonClicked)
            {
                searchView.setInputType(InputType.TYPE_CLASS_TEXT);
                searchView.setAlpha(1f);
            }
        }
        else
        {
            searchView.setAlpha(0.01f);
            loadingView.setVisibility(View.VISIBLE);
        }

        setImageButtonEnabled(getActivity(), enabled, allFilter, R.drawable.ic_all_reoprts);
        setImageButtonEnabled(getActivity(), enabled, finishedFilter, R.drawable.ic_finished_reports);
        setImageButtonEnabled(getActivity(), enabled, runningFilter, R.drawable.ic_play);
        setImageButtonEnabled(getActivity(), enabled, pausedFilter, R.drawable.ic_pause);

        if (enabled)
        {
            setFilterIcons();
        }

    }

    public void setFilterIcons()
    {
        toggleImageButton(getActivity(), isAllFilter, allFilter, R.drawable.ic_all_reoprts);
        toggleImageButton(getActivity(), isFinishedFilter, finishedFilter, R.drawable.ic_finished_reports);
        toggleImageButton(getActivity(), isRunningFilter, runningFilter, R.drawable.ic_play);
        toggleImageButton(getActivity(), isPausedFilter, pausedFilter, R.drawable.ic_pause);
    }

    public void filterReports()
    {
        if (!isAdded())
        {
            return;
        }


        if (reportItemsIndex < reportItems.size())
        {
            reportsScroll.getViewTreeObserver().removeOnScrollChangedListener(scrollChangedListener);

            threadPoolExecutor.execute(()->
            {
                reportItemsLoadNo = 10000;
                try
                {
                    createReportsFragments();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    new Handler(Looper.getMainLooper()).post(()->
                    {
                        reportsScroll.getViewTreeObserver().addOnScrollChangedListener(scrollChangedListener);
                        performFiltering();
                    });

                }
            });

        }
        else
        {
            performFiltering();
        }
    }

    public void performFiltering()
    {
        if (!isAdded()) {return;}

        if (searchValue == null)
        {
            searchValue = "";
        }

        for (ReportItem reportItem : reportItems)
        {
            boolean isVisible = true;

            if (reportItem == null)
            { return; }


            if (isFinishedFilter)
            { isVisible = reportItem.Status.equals(statusStateDone); }

            if (isRunningFilter)
            { isVisible = reportItem.Status.equals(statusStateWorking) || reportItem.Status.equals(statusStateStop); }

            if (isPausedFilter)
            { isVisible = reportItem.Status.equals(statusStateStop); }

            if (!reportItem.Username.contains(searchValue))
            { isVisible = false; }

            Integer frameID = frameIds.get(reportItem.OrderID);

            if (frameID == null)
            { return; }
            FrameLayout frameLayout = rootLayout.findViewById(frameID);

            if (frameLayout != null)
            {
                if (!isVisible)
                { frameLayout.setVisibility(View.GONE); }
                else
                { frameLayout.setVisibility(View.VISIBLE); }
            }
        }
    }

    public void loadReports() throws IOException, GeneralSecurityException
    {
        String sqlText = "Select * from Statistics_Orders Left Join Statistics_Jobs\n" +
                "  On Statistics_Jobs.StatJobID = \n" +
                "    (select StatJobID from Statistics_Jobs where \n" +
                "        Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID order by Statistics_Jobs.StatJobID desc limit 1)" +
                String.format(Locale.ENGLISH, " Where FIPK = %d and IPK <> %d", metagramAgent.activeAgent.userID, metagramAgent.activeAgent.userID);

        MatrixCursor ordersCursor = dbMetagram.selectQuery(sqlText);

        if (ordersCursor.moveToFirst())
        {

            while (!ordersCursor.isAfterLast())
            {
                String username = ordersCursor.getString(ordersCursor.getColumnIndex("Username"));
                String Status = ordersCursor.getString(ordersCursor.getColumnIndex("Status"));
                int orderID = ordersCursor.getInt(ordersCursor.getColumnIndex("StatOrderID"));
                long IPK = ordersCursor.getLong(ordersCursor.getColumnIndex("IPK"));

                boolean isThere = false;

                for (ReportItem item : reportItems)
                {
                    if (item.OrderID == orderID)
                    {
                        isThere = true;
                        break;
                    }
                }

                if (!isThere)
                {
                    reportItems.add(new ReportItem(IPK, orderID, username, Status));
                }

                /*if (usernameIds.get(orderID) != null)
                {
                    ordersCursor.moveToNext();
                    continue;
                }

                usernameIds.put(orderID, username);*/
                ordersCursor.moveToNext();
            }
        }

    }

    public void createReportsFragments() throws InterruptedException
    {
        if (!isAdded())
        {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> setFilterEnabled(false));
        try
        {

            int delay = 75;

            int counter = 0;
            while (counter < reportItemsLoadNo && reportItemsIndex < reportItems.size())
            {
                ReportItem reportItem = reportItems.get(reportItemsIndex);
                counter++;
                reportItemsIndex++;

                if (reportItem.Status.equals(statusStateDone))
                {
                    new Handler(Looper.getMainLooper()).post(() ->
                    {
                        addResultFragment(reportItem.IPK, reportItem.OrderID);
                    });
                } else
                {
                    new Handler(Looper.getMainLooper()).post(() ->
                    {
                        addProgressFragment(reportItem.IPK, reportItem.OrderID);
                    });
                }

                Thread.sleep(delay);
            }

            reportItemsLoadNo = 3;
        }
        finally
        {
            new Handler(Looper.getMainLooper()).post(() -> setFilterEnabled(true));
        }
    }

    public boolean checkRootLayoutHeight()
    {
        int reportScrollHeight = reportsScroll.getHeight();

        int linearLayoutHeight = fragmentsRootLayout.getHeight();

        return linearLayoutHeight <= reportScrollHeight;
    }

    public void removeFragment(int OrderID)
    {
        if (!isAdded())
        {
            return;
        }

        int frameID = frameIds.get(OrderID);

        FrameLayout frameLayout = fragmentsRootLayout.findViewById(frameID);

        fragmentsRootLayout.removeView(frameLayout);
    }

    public void addProgressFragment(long IPK, int OrderID)
    {
        if (!isAdded())
        {
            return;
        }

        reProgressFragment progressFragment = reProgressFragment.newInstance(IPK, OrderID);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        fragmentsRootLayout.addView(frameLayout);
        int frameId = frameLayout.generateViewId();
        frameLayout.setId(frameId);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) dpToPixels(getActivity(), 150));
        frameLayout.setLayoutParams(params);

        frameIds.put(OrderID, frameId);

        getChildFragmentManager().beginTransaction().replace(frameId, progressFragment).commitAllowingStateLoss();
    }

    public void addResultFragment(long IPK, int OrderID)
    {
        if (!isAdded())
        {
            return;
        }

        reResultFragment resultFragment = reResultFragment.newInstance(IPK, OrderID);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        fragmentsRootLayout.addView(frameLayout);
        int frameId = View.generateViewId();
        frameLayout.setId(frameId);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) dpToPixels(getActivity(), 150));
        frameLayout.setLayoutParams(params);

        frameIds.put(OrderID, frameId);

        getChildFragmentManager().beginTransaction().replace(frameId, resultFragment).commitAllowingStateLoss();
    }

    @Override
    public void changeFragmentToProgress(long IPK, int OrderID)
    {
        if(IPK < 0 || OrderID <0){return;}

        int frameID = frameIds.get(OrderID);

        reProgressFragment progressFragment = reProgressFragment.newInstance(IPK, OrderID);
        getChildFragmentManager().beginTransaction().replace(frameID, progressFragment).commitAllowingStateLoss();

        setReportStatus(OrderID, statusStateWorking);
    }

    @Override
    public void changeFragmentToResult(long IPK, int OrderID)
    {
        if(IPK < 0 || OrderID <0){return;}

        int frameID = frameIds.get(OrderID);

        reResultFragment resultFragment = reResultFragment.newInstance(IPK, OrderID);
        getChildFragmentManager().beginTransaction().replace(frameID, resultFragment).commitAllowingStateLoss();

        setReportStatus(OrderID, statusStateDone);
    }

    class ReportItem
    {
        long IPK;
        int OrderID;
        String Username;
        String Status;

        public ReportItem(long IPK, int OrderID, String Username, String Status)
        {
            this.OrderID = OrderID;
            this.Username = Username;
            this.Status = Status;
            this.IPK = IPK;
        }
    }

    public void setReportStatus(int OrderID, String Status)
    {
        for (ReportItem reportItem : reportItems)
        {
            if (reportItem.OrderID == OrderID)
            {
                reportItem.Status = Status;
                break;
            }
        }
        performFiltering();
    }

    public void showHelp()
    {
        if (!isAdded())
        {
            return;
        }

        if (dbMetagram.getItemStatus(isShowingHelp) != 0)
        {
            return;
        }

        dbMetagram.setItemStatus(isShowingHelp, 1);

        if (addOrderButton != null)
        {
            showInteractiveHelp(ReportFragment_Help,
                    getActivity(),
                    getResources().getString(R.string.i_ReportFragment_Help_title),
                    getResources().getString(R.string.i_ReportFragment_Help_content),
                    addOrderButton,
                    (View v1) ->
                            showFilterHelp(),
                    Gravity.auto);
        } else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }

    public void showFilterHelp()
    {
        LinearLayout dummyLayout = rootLayout.findViewById(R.id.reports_dummyLayout);
        if (dummyLayout != null)
        {
            showInteractiveHelp(ReportFragment_filter,
                    getActivity(),
                    getResources().getString(R.string.i_ReportFragment_filter_title),
                    getResources().getString(R.string.i_ReportFragment_filter_content),
                    dummyLayout,
                    (View v1) ->
                            dbMetagram.setItemStatus(isShowingHelp, 0),
                    Gravity.auto);
        } else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }


}
