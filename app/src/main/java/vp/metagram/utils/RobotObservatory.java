package vp.metagram.utils;


import androidx.fragment.app.Fragment;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;


public class RobotObservatory
{

    public Semaphore observatoryMutex = new Semaphore(1);

    List<Pair<String, Fragment>> fragments = new ArrayList<>();


    public void addFragment(String RobotUUID, Fragment fragment) throws InterruptedException
    {
        observatoryMutex.acquire();
        try
        {
            if (!isAvailable(RobotUUID, fragment))
            {
                Pair<String, Fragment> newPair = new Pair<>(RobotUUID, fragment);
                fragments.add(newPair);
            }
        }
        finally
        {
            observatoryMutex.release();
        }
    }

    public boolean isAvailable(String RobotUUID, Fragment fragment)
    {
        boolean result = false;
        Iterator<Pair<String, Fragment>> iterator = fragments.iterator();
        while (iterator.hasNext())
        {
            Pair<String, Fragment> pair = iterator.next();
            if (pair.first.equals(RobotUUID) && pair.second.equals(fragment))
            {
                result = true;
                break;
            }
        }

        return result;
    }

    public void removeFragment(Fragment fragment) throws InterruptedException
    {
        observatoryMutex.acquire();
        try
        {
            Iterator<Pair<String, Fragment>> iterator = fragments.iterator();
            while (iterator.hasNext())
            {
                Pair<String, Fragment> pair = iterator.next();
                if (pair.second.equals(fragment))
                {
                    iterator.remove();
                }
            }
        }
        finally
        {
            observatoryMutex.release();
        }
    }

    public List<Fragment> getFragments(String RobotUUID) throws InterruptedException
    {
        List<Fragment> fragmentList = new ArrayList<>();
        observatoryMutex.acquire();
        try
        {
            Iterator<Pair<String, Fragment>> iterator = fragments.iterator();
            while (iterator.hasNext())
            {
                Pair<String, Fragment> pair = iterator.next();
                if (pair.first.equals(RobotUUID))
                {
                    fragmentList.add(pair.second);
                }
            }
        }
        finally
        {
            observatoryMutex.release();
        }
        return fragmentList;
    }

}
