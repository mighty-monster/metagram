package vp.metagram.ui.AccBrowser.SearchAccount;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import vp.metagram.R;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.utils.instagram.InstagramAgent;

import vp.metagram.utils.instagram.types.User;

import static android.os.Looper.getMainLooper;
import static vp.metagram.general.variables.threadPoolExecutor;

public class SearchAccountFragment extends Fragment
{
    View rootLayout;

    InstagramAgent agent;

    SpinKitView loadingView;
    private RecyclerView recycleView;
    public RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;

    boolean isFriend = false;

    SearchView searchView;

    public List<User> suggestions = new ArrayList<>();

    private List<SearchRunnable> searchRunnableList = new ArrayList<>();

    public Runnable confirmRunnable;

    static public SearchAccountFragment newInstance(InstagramAgent agent, Runnable confirmRunnable)
    {
        SearchAccountFragment searchAccountFragment = new SearchAccountFragment();
        searchAccountFragment.agent = agent;
        searchAccountFragment.confirmRunnable = confirmRunnable;

        return searchAccountFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_search_account, container, false);
        prepareUIElements();

        return rootLayout;
    }

    public void prepareUIElements()
    {

        loadingView = rootLayout.findViewById(R.id.searchAccount_loadingView);

        searchView = rootLayout.findViewById(R.id.searchAccount_searchView);

        recycleView = rootLayout.findViewById(R.id.searchAccount_recyclerView);
        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recycleView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycleView.getContext(),
                layoutManager.getOrientation());
        recycleView.addItemDecoration(dividerItemDecoration);


        adapter = new SearchAccountAdapter(suggestions, getActivity(), confirmRunnable);

        recycleView.setAdapter(adapter);

        loadingView.bringToFront();


        searchView.setOnClickListener((View view) ->
                searchView.setIconified(false));

        searchView.onActionViewExpanded();
        searchView.clearFocus();

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


    }


    public void close()
    {
        Activity parentActivity = getActivity();
        parentActivity.finish();
    }

    public void loadData(String keyWord)
    {
        Iterator<SearchAccountFragment.SearchRunnable> iterator = searchRunnableList.iterator();
        while ( iterator.hasNext() )
        {
            SearchAccountFragment.SearchRunnable searchRunnable = iterator.next();
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
        SearchAccountFragment parent;

        boolean goOn = true;
        String keyWord;

        public SearchRunnable(SearchAccountFragment parent, String keyWord)
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
                getActivity().runOnUiThread(()->loadingView.setVisibility(View.VISIBLE));

                agent.proxy.search(keyWord,parent.suggestions);

                if (!goOn) {return;}

                if (searchView.getQuery().equals(""))
                {
                    suggestions.clear();
                }

                getActivity().runOnUiThread(()->
                {
                    if (!goOn) {return;}
                    ((SearchAccountAdapter)adapter).setDataSet(suggestions);
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
                        dialog.showDialog(getActivity(),
                                getString(R.string.addOrder_WarningTitle),
                                getString(R.string.connectionInfo_noInternetContent),
                                getString(R.string.button_ok),()->
                                {
                                    if (!parent.isAdded()){return;}
                                    parent.close();
                                });
                    });
                }
            }
            finally
            {
                getActivity().runOnUiThread(()->
                {
                    loadingView.setVisibility(View.INVISIBLE);
                    recycleView.setLayoutFrozen(false);
                    recycleView.smoothScrollToPosition(0);

                });
            }
            //TODO add error handling
        }
    }

}
