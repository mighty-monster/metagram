package vp.metagram.ui.AccBrowser;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import vp.metagram.ui.AccBrowser.ListSource.ListSource;

import static vp.metagram.general.functions.convertNumbersToHumanReadableFormat;


public class AccntListPageAdapter extends FragmentPagerAdapter
{
    Fragment followingsFragment;
    Fragment followersFragment;

    String followerCount;
    String followingCount;

    public AccntListPageAdapter(FragmentManager fm, ListSource followersListSource, int noOfFollowers, ListSource followingsListSource, int noOfFollowings)
    {
        super(fm);
        followingsFragment = AccntListFragment.newInstance(followingsListSource);
        followersFragment = AccntListFragment.newInstance(followersListSource);

        followerCount = convertNumbersToHumanReadableFormat(noOfFollowers);
        followingCount = convertNumbersToHumanReadableFormat(noOfFollowings);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return followingsFragment;
            case 1:
                return followersFragment;
            default:
                return followingsFragment;

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
                return followingCount + "\nFollowings";
            case 1:
                return followerCount + "\nFollowers";
            default:
                return "Followings";

        }
    }
}
