package vp.metagram.ui.AccBrowser.DownloadHistory;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import vp.metagram.R;
import vp.metagram.ui.AccBrowser.DownloadHistory.adaptors.DownloadFragmentAdaptor;
import vp.metagram.ui.AccBrowser.DownloadHistory.types.DownloadItem;

import static vp.metagram.general.variables.threadPoolExecutor;

public class DownloadViewerFragment extends Fragment
{

    String FilesPath;

    View rootLayout;

    Boolean isFirstTime = true;

    private RecyclerView recycleView;
    public DownloadFragmentAdaptor adapter;
    private LinearLayoutManager layoutManager;
    private SpinKitView loadingView;

    ImageView emptyBox;

    List<DownloadItem> items = new ArrayList<>();

    public DownloadViewerFragment(String FilesPath)
    {
        super();
        this.FilesPath = FilesPath;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_download_list, container, false);
        prepareUIElements();
        return rootLayout;
    }

    public void prepareUIElements()
    {
        recycleView = rootLayout.findViewById(R.id.DownloadList_RecycleView);
        loadingView = rootLayout.findViewById(R.id.DownloadList_loadingView);
        emptyBox = rootLayout.findViewById(R.id.DownloadList_emptyBox);

        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycleView.getContext(),
                layoutManager.getOrientation());
        recycleView.addItemDecoration(dividerItemDecoration);

        adapter = new DownloadFragmentAdaptor(getActivity(), items);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isFirstTime)
        {
            loadData();
            recycleView.setAdapter(adapter);
            isFirstTime = false;
        }
    }

    public void enableEmptyBox()
    {
        emptyBox.setVisibility(View.VISIBLE);
    }

    public void disableLoading()
    {
        loadingView.setVisibility(View.GONE);
    }

    public void notifyDataChanged()
    {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed())
        {
            activity.runOnUiThread(()->adapter.setDataSet(items));
        }
    }


    private void loadData()
    {
        threadPoolExecutor.execute(() ->
        {
            try
            {
                File directory = new File(FilesPath);
                File[] fList = directory.listFiles();

                if (fList != null)
                {
                    Arrays.sort(fList, (Comparator<File>) (o1, o2) -> Long.compare(((File) o2).lastModified(), ((File) o1).lastModified()));

                    for (File file : fList)
                    {
                        if (file.isDirectory())
                        {
                            DownloadItem newItem = new DownloadItem();

                            newItem.title = Long.toString(file.lastModified());

                            File newItemFile = new File(file.getAbsolutePath());
                            File[] newItemChildren = newItemFile.listFiles();

                            if (newItemChildren != null)
                            {
                                newItem.noOfItems = newItemChildren.length;
                                for (File child : newItemChildren)
                                {
                                    newItem.filesAddress.add(child.getAbsolutePath());
                                }
                            }

                            items.add(newItem);
                        }
                    }
                }

                if  (items.size() == 0)
                    getActivity().runOnUiThread(()->
                            {
                                if(isAdded())
                                    enableEmptyBox();
                            });
            }
            catch (Exception e)
            {
                e.printStackTrace();

            }
            finally
            {
                getActivity().runOnUiThread(() ->
                {
                    if (isAdded())
                    {
                        disableLoading();
                        notifyDataChanged();
                    }
                });
            }
        });
    }
}
