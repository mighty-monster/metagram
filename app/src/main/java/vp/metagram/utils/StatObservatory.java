package vp.metagram.utils;



import android.util.Pair;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;


public class StatObservatory
{

    public Semaphore observatoryMutex = new Semaphore(1);

    List<Pair<Long, Fragment>> fragments = new ArrayList<>();


    public void addFragment(long orderID, Fragment fragment) throws InterruptedException
    {
        observatoryMutex.acquire();
        try
        {
            if (!isAvailable(orderID, fragment))
            {
                Pair<Long, Fragment> newPair = new Pair<>(orderID, fragment);
                fragments.add(newPair);
            }
        }
        finally
        {
            observatoryMutex.release();
        }
    }

    public boolean isAvailable(long orderID, Fragment fragment)
    {
        boolean result = false;
        Iterator<Pair<Long, Fragment>> iterator = fragments.iterator();
        while (iterator.hasNext())
        {
            Pair<Long, Fragment> pair = iterator.next();
            if (pair.first.equals(orderID) && pair.second.equals(fragment))
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
            Iterator<Pair<Long, Fragment>> iterator = fragments.iterator();
            while (iterator.hasNext())
            {
                Pair<Long, Fragment> pair = iterator.next();
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

    public List<Fragment> getFragments(long orderID) throws InterruptedException
    {
        List<Fragment> fragmentList = new ArrayList<>();
        observatoryMutex.acquire();
        try
        {
            Iterator<Pair<Long, Fragment>> iterator = fragments.iterator();
            while (iterator.hasNext())
            {
                Pair<Long, Fragment> pair = iterator.next();
                if (pair.first.equals(orderID))
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
