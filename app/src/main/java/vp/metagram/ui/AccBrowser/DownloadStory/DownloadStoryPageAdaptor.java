package vp.metagram.ui.AccBrowser.DownloadStory;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import vp.metagram.ui.AccBrowser.DownloadStory.highlight.HighlightFragment;
import vp.metagram.ui.AccBrowser.DownloadStory.igtv.IGTVFragment;

public class DownloadStoryPageAdaptor extends FragmentPagerAdapter
{
    Fragment highlightFragment;
    Fragment igtvFragment;

    public DownloadStoryPageAdaptor(FragmentManager fm, HighlightFragment highlightFragment, IGTVFragment igtvFragment)
    {
        super(fm);
        this.highlightFragment = highlightFragment;
        this.igtvFragment = igtvFragment;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return highlightFragment;
            case 1:
                return igtvFragment;
            default:
                return highlightFragment;

        }
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Highlight";
            case 1:
                return "IGTV";
            default:
                return "Highlights";

        }
    }
}
