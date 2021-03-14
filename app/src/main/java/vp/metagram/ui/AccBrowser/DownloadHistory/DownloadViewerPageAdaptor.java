package vp.metagram.ui.AccBrowser.DownloadHistory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class DownloadViewerPageAdaptor extends FragmentPagerAdapter
{

    DownloadViewerFragment IGTV;
    DownloadViewerFragment Story;
    DownloadViewerFragment Highlight;
    DownloadViewerFragment Posts;

    public DownloadViewerPageAdaptor(@NonNull FragmentManager fm,
                                     DownloadViewerFragment IGTV,
                                     DownloadViewerFragment Story,
                                     DownloadViewerFragment Highlight,
                                     DownloadViewerFragment Posts)
    {
        super(fm);

        this.IGTV = IGTV;
        this.Story = Story;
        this.Highlight = Highlight;
        this.Posts = Posts;

    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return Posts;
            case 1:
                return Story;
            case 2:
                return Highlight;
            case 3:
                return IGTV;
            default:
                return Posts;

        }
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Posts";
            case 1:
                return "Stories";
            case 2:
                return "Highlights";
            case 3:
                return "IGTV";
            default:
                return "Posts";

        }
    }

    @Override
    public int getCount()
    {
        return 4;
    }
}
