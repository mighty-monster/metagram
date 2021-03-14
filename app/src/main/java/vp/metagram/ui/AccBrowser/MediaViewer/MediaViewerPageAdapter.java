package vp.metagram.ui.AccBrowser.MediaViewer;

import android.content.Context;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;



import static vp.metagram.general.functions.getExtensionForPost;


public class MediaViewerPageAdapter extends FragmentStatePagerAdapter
{
    List<String> urls;
    Context context;
    boolean isLocal;

    public MediaViewerPageAdapter(FragmentManager fm, List<String> urls, Context context, boolean isLocal)
    {
        super(fm);
        this.urls = urls;
        this.context = context;
        this.isLocal = isLocal;
    }


    @Override
    public Fragment getItem(int position)
    {
        Fragment result = null;

        String url = urls.get(position);

        String extension = getExtensionForPost(url);

        if (extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png"))
        {
            result = MediaViewerImageFragment.newInstance(url, isLocal);
        }
        else if (extension.equals(".mp4") )
        {
            result = MediaViewerVideoFragment.newInstance(url, position, isLocal);
        }

        return result;
    }

    @Override
    public int getCount()
    {
        return urls.size();
    }
}
