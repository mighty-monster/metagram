package vp.metagram.utils.instagram.executors.statistics.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor;

import static vp.metagram.general.functions.getThreadByName;

public class ExecutorPool
{
    int concurrentThreads;

    List<ExecutorThread> threads = new ArrayList<>();

    long delayLimitDuration = 3 * 60 * 1000; // 3 Minutes
    long lifeLimitDuration = 60 * 60 * 1000; // 60 Minutes

    private Semaphore mutex = new Semaphore(1);


    public ExecutorPool()
    {
        int noOfProcessors = Runtime.getRuntime().availableProcessors();
        concurrentThreads = noOfProcessors - 2;


        if (concurrentThreads <=0 ) {concurrentThreads = 1;}
        if (concurrentThreads > 4 ) {concurrentThreads = 4;}

        concurrentThreads = 3;

    }

    public boolean addThread(String threadName, long threadID, StatisticsExecutor executor) throws InterruptedException
    {
        mutex.acquire();
        try
        {
            boolean result = false;

            checkCurrentStatus();

            boolean isThere = false;
            for (ExecutorThread thread : threads)
            {
                if (thread.threadID == threadID)
                {
                    thread.refreshTime = System.currentTimeMillis();
                    result = true;
                    isThere = true;
                    break;
                }
            }

            if (isThere)
            {
                result = true;
            }
            else
            {
                if (threads.size() < concurrentThreads)
                {
                    threads.add(new ExecutorThread(threadName, threadID, executor));
                    result = true;
                }
            }

            return result;
        }
        finally
        {
            mutex.release();
        }
    }

    public void removeThread(long threadID)
    {
        for (ExecutorThread thread:threads)
        {
            if (thread.threadID == threadID)
            {
                threads.remove(thread);
                break;
            }
        }
    }

    public void checkCurrentStatus()
    {
        long now =  System.currentTimeMillis();
        Iterator<ExecutorThread> iterator = threads.listIterator();
        while  (iterator.hasNext())
        {
            ExecutorThread executorThread = iterator.next();

            long delayTime = now - executorThread.refreshTime ;
            long lifeTime =  now - executorThread.registerTime;

            if (delayTime > delayLimitDuration)
            {
                removeThread(executorThread.threadID);
                iterator.remove();
            }

            if (lifeTime > lifeLimitDuration)
            {
                removeThread(executorThread.threadID);
                iterator.remove();
            }

            if (!executorThread.isAlive() || executorThread.threadID == 0)
            {
                removeThread(executorThread.threadID);
                iterator.remove();
            }
        }
    }

    class ExecutorThread
    {
        String threadName;
        long threadID;
        long registerTime;
        long refreshTime;
        StatisticsExecutor executor;

        public ExecutorThread(String threadName, long threadID, StatisticsExecutor executor)
        {
            long now = System.currentTimeMillis();
            this.threadName = threadName;
            this.threadID = threadID;
            this.registerTime = now;
            this.refreshTime = now;
            this.executor = executor;

        }

        public boolean isAlive()
        {
            boolean result = false;
            Thread executorThread = getThreadByName(threadName);

            if ( executorThread != null )
            { if ( executorThread.getId() == threadID ) { result = true; } }

            return result;
        }

    }
}
