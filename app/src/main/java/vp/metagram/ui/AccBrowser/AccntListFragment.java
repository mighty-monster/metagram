package vp.metagram.ui.AccBrowser;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;


import vp.metagram.R;
import vp.metagram.ui.AccBrowser.ListSource.ListSource;
import vp.metagram.ui.AccBrowser.ListSource.ListSource_Followers_Online;
import vp.metagram.ui.AccBrowser.ListSource.ListSource_Likes_Online;
import vp.metagram.ui.Dialogs.InformationDialog;


import static android.os.Looper.getMainLooper;
import static vp.metagram.general.variables.ScrollerDeltaMargin;
import static vp.metagram.general.variables.ScrollerPreFetchItems;
import static vp.metagram.general.variables.threadPoolExecutor;


public class AccntListFragment extends Fragment
{
    View rootLayout;

    ListSource listSource;

    boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private RecyclerView recycleView;
    public RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private SpinKitView loadingView;

    boolean isFirstTime = true;

    static public AccntListFragment newInstance(ListSource listSource)
    {
        AccntListFragment newListFragment = new AccntListFragment();
        newListFragment.listSource = listSource;
        return newListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_accnt_list, container, false);
        prepareUIElements();
        return rootLayout;
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



    private void prepareUIElements()
    {
        recycleView = rootLayout.findViewById(R.id.AccntList_RecycleView);
        loadingView = rootLayout.findViewById(R.id.AccntList_loadingView);


        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
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
        adapter = new FollowersAdaptor(listSource.sourceList,getActivity(),null);
        recycleView.setAdapter(adapter);


    }

    public void loadData()
    {

        loadingView.setVisibility(View.VISIBLE);
        threadPoolExecutor.execute(() ->
        {
            try
            {
                listSource.getNextList();
                new Handler(getMainLooper()).post(() ->
                        adapter.notifyDataSetChanged());
            }
            catch (IOException e)
            {
                e.printStackTrace();
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
                    e.printStackTrace();

                    if (listSource instanceof ListSource_Followers_Online || listSource instanceof ListSource_Likes_Online)
                    {
                        new Handler(getMainLooper()).post(() ->
                        {
                            if (!isAdded() || !isVisible()) {return;}
                            InformationDialog dialog = new InformationDialog();
                            dialog.showDialog(getActivity(),
                                    getString(R.string.addOrder_WarningTitle),
                                    getString(R.string.connectionInfo_noInternetContent),
                                    getString(R.string.button_ok), () -> getActivity().finish());
                        });
                    }
                }
            }
            finally
            {
                new Handler(getMainLooper()).post(() ->
                {
                    loadingView.setVisibility(View.GONE);
                    loading = true;
                });
            }
        });
    }
}
