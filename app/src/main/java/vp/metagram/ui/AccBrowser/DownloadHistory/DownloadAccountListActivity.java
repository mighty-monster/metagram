package vp.metagram.ui.AccBrowser.DownloadHistory;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.AccBrowser.DownloadHistory.adaptors.DownloadAccountListAdaptor;
import vp.metagram.ui.AccBrowser.DownloadHistory.types.AccountItem;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.tools.io.iFileSystemUtils.GetDownloadDir;
import static vp.tools.io.iFileSystemUtils.getDirectorySize;

public class DownloadAccountListActivity extends BaseActivity
{

    TextView title;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    SearchView searchView;
    ImageButton backButton;
    SpinKitView loadingView;

    DownloadAccountListAdaptor adapter;

    List<AccountItem> accountsArray = new ArrayList<>();

    boolean isFirstTime = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_account_list);

        findUIElements();
        configureUIElements();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isFirstTime)
        {
            recyclerView.setAdapter(adapter);
            isFirstTime = false;
        }
        loadData(searchView.getQuery().toString());
    }

    private void findUIElements()
    {
        title = findViewById(R.id.downloadHistory_title);
        recyclerView = findViewById(R.id.downloadHistory_recyclerView);
        searchView = findViewById(R.id.downloadHistory_searchView);
        backButton = findViewById(R.id.downloadHistory_backButton);
        loadingView = findViewById(R.id.downloadHistory_loadingView);
    }

    private void configureUIElements()
    {
        setTextViewFontForMessage(this, title);

        searchView.setOnClickListener((View view)->
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

        backButton.setOnClickListener((View v)->finish());

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new DownloadAccountListAdaptor(this, accountsArray);
    }

    public void disableLoading()
    {
        loadingView.setVisibility(View.GONE);
    }


    public void loadData(String search_value)
    {
        threadPoolExecutor.execute(() ->
        {
            try
            {
                accountsArray.clear();
                String rootAddress = GetDownloadDir(this);

                File directory = new File(rootAddress);
                File[] fList = directory.listFiles();
                if (fList != null)
                    for (File file : fList)
                    {
                        if (file.isDirectory())
                        {

                            String name = file.getName();
                            String address = rootAddress + name + "/";

                            boolean shouldAdd = false;
                            if (!search_value.equals("") && name.contains(search_value))
                                shouldAdd = true;
                            else if (search_value.equals(""))
                                shouldAdd = true;

                            if (shouldAdd)
                            {
                                AccountItem newItem = new AccountItem();
                                newItem.name = name;
                                newItem.folderSize = getDirectorySize(new File(address));
                                accountsArray.add(newItem);
                            }
                        }
                    }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                runOnUiThread(() ->
                {
                    disableLoading();
                    adapter.setDataSet(accountsArray);
                });
            }
        });
    }
}
