package vp.metagram.ui.AccBrowser.DownloadStory.igtv;

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

import vp.igwa.IGTVList;
import vp.metagram.R;

public class IGTVFragment extends Fragment
{
    View rootLayout;

    IGTVList itemList;

    private RecyclerView recycleView;
    public IGTVAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SpinKitView loadingView;

    ImageView emptyBox;

    String username;

    boolean isFirstTime = true;

    static public IGTVFragment newInstance(IGTVList itemList, String username)
    {
        IGTVFragment igtvFragment = new IGTVFragment();
        igtvFragment.itemList = itemList;
        igtvFragment.username = username;

        return igtvFragment;
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
    public void onResume()
    {
        super.onResume();

        if (isFirstTime)
        {
            recycleView.setAdapter(adapter);
            isFirstTime = false;
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

        adapter = new IGTVAdapter(itemList ,getActivity(), username);
    }


    public void enableEmptyBox()
    {
        emptyBox.setVisibility(View.VISIBLE);
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
