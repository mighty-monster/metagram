package vp.metagram.ui.AccBrowser.DownloadStory.highlight;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.util.List;

import vp.metagram.R;
import vp.metagram.utils.instagram.types.HighlightItem;

public class HighlightFragment extends Fragment
{
    View rootLayout;

    List<HighlightItem> itemList;

    private RecyclerView recycleView;
    public HighlightAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SpinKitView loadingView;

    ImageView emptyBox;

    String username;

    boolean isFirstTime = true;

    static public HighlightFragment newInstance(List<HighlightItem> itemList, String username)
    {
        HighlightFragment highlightFragment = new HighlightFragment();
        highlightFragment.itemList = itemList;
        highlightFragment.username = username;

        return highlightFragment;
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
        rootLayout = inflater.inflate(R.layout.fragment_highlight_list, container, false);
        prepareUIElements();
        return rootLayout;
    }

    public void prepareUIElements()
    {
        recycleView = rootLayout.findViewById(R.id.HighlightList_RecycleView);
        loadingView = rootLayout.findViewById(R.id.HighlightList_loadingView);
        emptyBox = rootLayout.findViewById(R.id.HighlightList_emptyBox);

        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycleView.getContext(),
                layoutManager.getOrientation());
        recycleView.addItemDecoration(dividerItemDecoration);

        adapter = new HighlightAdapter(itemList ,getActivity(), username);

    }

    public void enableEmptyBox()
    {
        emptyBox.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isFirstTime)
        {
            recycleView.setAdapter(adapter);
            isFirstTime = false;
        }

    }

    public void notifyDataChanged()
    {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed())
        {
            activity.runOnUiThread(()->adapter.setDataSet(itemList));
        }

    }

    public void disableLoading()
    {
        loadingView.setVisibility(View.GONE);
    }
}
