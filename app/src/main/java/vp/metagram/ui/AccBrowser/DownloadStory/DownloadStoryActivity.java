package vp.metagram.ui.AccBrowser.DownloadStory;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

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
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.Dialogs.InformationDialog;

import vp.metagram.utils.instagram.types.User;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.DownloadStory_Search;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.isShowingHelp;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;

public class DownloadStoryActivity extends BaseActivity
{

    boolean isShowing = false;

    boolean isFriend = false;

    SearchView searchView;

    private Runnable confirmRunnable;

    private RecyclerView recycleView;
    public RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private List<DownloadStoryActivity.SearchRunnable> searchRunnableList = new ArrayList<>();
    public List<User> suggestions = new ArrayList<>();

    private SpinKitView loadingView;

    private TextView titleTextView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_story);

        prepareUIItems();
    }

    public void loadData(String keyWord)
    {
        Iterator<DownloadStoryActivity.SearchRunnable> iterator = searchRunnableList.iterator();
        while ( iterator.hasNext() )
        {
            DownloadStoryActivity.SearchRunnable searchRunnable = iterator.next();
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
            DownloadStoryActivity.SearchRunnable newSearchRunnable = new DownloadStoryActivity.SearchRunnable(this,keyWord);
            searchRunnableList.add(newSearchRunnable);
            threadPoolExecutor.schedule(newSearchRunnable,500, TimeUnit.MILLISECONDS);
        }
    }

    class SearchRunnable implements Runnable
    {
        DownloadStoryActivity parent;

        boolean goOn = true;
        String keyWord;

        public SearchRunnable(DownloadStoryActivity parent, String keyWord)
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
                    ((DownloadStoryAdaptor)adapter).setDataSet(suggestions);
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

    public void prepareUIItems()
    {
        searchView = findViewById(R.id.downloadStory_searchView);
        loadingView = findViewById(R.id.downloadStory_loading);
        recycleView = findViewById(R.id.downloadStory_recycleView);


        titleTextView = findViewById(R.id.downloadStory_title);
        setTextViewFontForMessage(this,titleTextView);

        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycleView.getContext(),
                layoutManager.getOrientation());
        recycleView.addItemDecoration(dividerItemDecoration);

        adapter = new DownloadStoryAdaptor(suggestions, this, confirmRunnable, this);
        recycleView.setAdapter(adapter);


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
    }

    @Override
    public void onResume()
    {
        super.onResume();

        isShowing = true;

        showHelp();
    }

    @Override
    public void onPause()
    {
        isShowing = false;
        super.onPause();
    }


    public void showHelp()
    {

        if (dbMetagram.getItemStatus(isShowingHelp) != 0 )
        { return; }

        if (dbMetagram.getItemStatus(DownloadStory_Search) == 0)
        {
            dbMetagram.setItemStatus(isShowingHelp,1);

            showInteractiveHelp(DownloadStory_Search,
                    this,
                    getResources().getString(R.string.i_DownloadStory_title),
                    getResources().getString(R.string.i_DownloadStory_content),
                    searchView,(View v1)->
                    {
                        dbMetagram.setItemStatus(isShowingHelp,0);
                    },
                    Gravity.auto);
        }
    }

}
