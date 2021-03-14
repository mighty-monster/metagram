package vp.metagram.utils.instagram;

import com.google.gson.Gson;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import vp.metagram.utils.instagram.types.ResponseStatus;

import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.logger;
import static vp.metagram.utils.instagram.types.ResponseStatus.ok;

public class InstagramAgentStatus
{
    final static String dbKey = "agentStatus_";

    public ResponseStatus responseStatus = ok;
    public long refactorIndex = 0;

    long IPK;

    public long requestsTotal = 0;
    public long requestsPerDay = 0;
    public long requestsPerHour = 0;

    public long requestsTotal_getUserInfo = 0;
    public long requestsPerDay_getUserInfo = 0;
    public long requestsPerHour_getUserInfo = 0;

    public long requestsTotal_follow = 0;
    public long requestsPerDay_follow = 0;
    public long requestsPerHour_follow = 0;

    public long requestsTotal_unfollow = 0;
    public long requestsPerDay_unfollow = 0;
    public long requestsPerHour_unfollow = 0;

    public long requestsTotal_like = 0;
    public long requestsPerDay_like = 0;
    public long requestsPerHour_like = 0;

    public  long requestsTotal_comment = 0;
    public long requestsPerDay_comment = 0;
    public long requestsPerHour_comment = 0;

    public  long requestsTotal_search = 0;
    public long requestsPerDay_search = 0;
    public long requestsPerHour_search = 0;

    public  long requestsTotal_getFollowerList = 0;
    public  long requestsPerDay_getFollowerList = 0;
    public   long requestsPerHour_getFollowerList = 0;

    public  long requestsTotal_getFollowingList = 0;
    public long requestsPerDay_getFollowingList = 0;
    public long requestsPerHour_getFollowingList = 0;

    public long requestsTotal_getMediaList = 0;
    public long requestsPerDay_getMediaList = 0;
    public long requestsPerHour_getMediaList = 0;

    public long requestsTotal_getCommentList = 0;
    public  long requestsPerDay_getCommentList = 0;
    public  long requestsPerHour_getCommentList = 0;

    public  long requestsTotal_getLikeList = 0;
    public  long requestsPerDay_getLikeList = 0;
    public  long requestsPerHour_getLikeList = 0;

    long lastRequestTime = 0;


    public void count_getUserInfo()
    {
        count_Total();
        requestsTotal_getUserInfo++;
        requestsPerDay_getUserInfo++;
        requestsPerHour_getUserInfo++;
    }

    public void count_follow()
    {
        count_Total();
        requestsTotal_follow++;
        requestsPerDay_follow++;
        requestsPerHour_follow++;
    }

    public void count_unfollow()
    {
        count_Total();
        requestsTotal_unfollow++;
        requestsPerDay_unfollow++;
        requestsPerHour_unfollow++;
    }

    public void count_like()
    {
        count_Total();
        requestsTotal_like++;
        requestsPerDay_like++;
        requestsPerHour_like++;
    }

    public void count_comment()
    {
        count_Total();
        requestsTotal_comment++;
        requestsPerDay_comment++;
        requestsPerHour_comment++;
    }

    public void count_search()
    {
        count_Total();
        requestsTotal_search++;
        requestsPerDay_search++;
        requestsPerHour_search++;
    }

    public void count_getFollowerList()
    {
        count_Total();
        requestsTotal_getFollowerList++;
        requestsPerDay_getFollowerList++;
        requestsPerHour_getFollowerList++;
    }

    public void count_getFollowingList()
    {
        count_Total();
        requestsTotal_getFollowingList++;
        requestsPerDay_getFollowingList++;
        requestsPerHour_getFollowingList++;
    }

    public void count_getMediaList()
    {
        count_Total();
        requestsTotal_getMediaList++;
        requestsPerDay_getMediaList++;
        requestsPerHour_getMediaList++;
    }

    public void count_getCommentList()
    {
        count_Total();
        requestsTotal_getCommentList++;
        requestsPerDay_getCommentList++;
        requestsPerHour_getCommentList++;
    }

    public void count_getLikeList()
    {
        count_Total();
        requestsTotal_getLikeList++;
        requestsPerDay_getLikeList++;
        requestsPerHour_getLikeList++;
    }

    public void count_Total()
    {
        resetCounters();
        requestsTotal++;
        requestsPerDay++;
        requestsPerHour++;
    }

    public void save(long IPK)
    {
        String key = String.format(Locale.ENGLISH,"%s%d",dbKey, IPK);
        Gson gson = new Gson();
        String json = gson.toJson(this);

        try
        {
            dbMetagram.setPair(key,json);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void delete(long IPK)
    {
        String key = String.format(Locale.ENGLISH,"%s%d",dbKey, IPK);
        try
        {
            dbMetagram.delPair(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    static public InstagramAgentStatus load(long IPK)
    {

        String key = String.format(Locale.ENGLISH,"%s%d",dbKey, IPK);
        InstagramAgentStatus result;
        try
        {
            Gson gson = new Gson();
            String jsonValue = dbMetagram.getPair(key);
            result = gson.fromJson(jsonValue, InstagramAgentStatus.class);
        }
        catch (Exception e)
        {
            logger.logError("DeviceSetting.Load()",
                    "Load setting from db failed.\n",e);
            result = new InstagramAgentStatus();
        }
        if (result == null)
        {
            result = new InstagramAgentStatus();
        }
        result.IPK = IPK;
        return result;
    }

    public void resetCounters()
    {
        long currentTime = System.currentTimeMillis();

        if ( TimeUnit.MILLISECONDS.toDays(currentTime) != TimeUnit.MILLISECONDS.toDays(lastRequestTime))
        {
            resetHourlyCounters();
            resetDailyCounters();
        }
        else
        {
            if ( TimeUnit.MILLISECONDS.toHours(currentTime) != TimeUnit.MILLISECONDS.toHours(lastRequestTime))
            {
                resetHourlyCounters();
            }
        }

        lastRequestTime = currentTime;
    }

    public void resetHourlyCounters()
    {
        requestsPerHour = 0;
        requestsPerHour_getUserInfo = 0;
        requestsPerHour_follow = 0;
        requestsPerHour_unfollow = 0;
        requestsPerHour_like = 0;
        requestsPerHour_comment = 0;
        requestsPerHour_search = 0;
        requestsPerHour_getFollowerList = 0;
        requestsPerHour_getFollowingList = 0;
        requestsPerHour_getMediaList = 0;
        requestsPerHour_getCommentList = 0;
        requestsPerHour_getLikeList = 0;
    }

    public void resetDailyCounters()
    {
        requestsPerDay = 0;
        requestsPerDay_getUserInfo = 0;
        requestsPerDay_follow = 0;
        requestsPerDay_unfollow = 0;
        requestsPerDay_like = 0;
        requestsPerDay_comment = 0;
        requestsPerDay_search = 0;
        requestsPerDay_getFollowerList = 0;
        requestsPerDay_getFollowingList = 0;
        requestsPerDay_getMediaList = 0;
        requestsPerDay_getCommentList = 0;
        requestsPerDay_getLikeList = 0;
    }


    /*Instagram Request Times*/

    public double averageRequestDuration = 2000;
    public long averageTotalRequestsCounter = 1;

    public void calculateAverageRequestDuration(long newDuration)
    {
        averageRequestDuration = (averageRequestDuration*(requestsTotal-1) + newDuration) /requestsTotal;
        averageTotalRequestsCounter++;
    }

    public double averageFollowerListLength = 40;
    public long averageTotalFollowerListCounter = 1;

    public void calculateAverageFollowerListLength(long newLength)
    {
        averageFollowerListLength = (averageFollowerListLength*(averageTotalFollowerListCounter -1) + newLength) / averageTotalFollowerListCounter;
        averageTotalFollowerListCounter++;
    }

    public double averageFollowingListLength = 1;
    public long averageTotalFollowingListCounter = 1;

    public void calculateAverageFollowingListLength(long newLength)
    {
        averageFollowingListLength = (averageFollowingListLength*(averageTotalFollowingListCounter -1) + newLength) / averageTotalFollowingListCounter;
        averageTotalFollowingListCounter++;
    }

    public double averagePostsListLength = 24;
    public long averageTotalPostsListCounter = 1;

    public void calculateAveragePostsListLength(long newLength)
    {
        averagePostsListLength = (averagePostsListLength*(averageTotalPostsListCounter -1) + newLength) / averageTotalPostsListCounter;
        averageTotalPostsListCounter++;
    }

    public double averageCommentListLength = 40;
    public long averageTotalCommentListCounter = 1;

    public void calculateAverageCommentListLength(long newLength)
    {
        if (newLength > 15)
        {
            averageCommentListLength = (averageCommentListLength * (averageTotalCommentListCounter - 1) + newLength) / averageTotalCommentListCounter;
            averageTotalCommentListCounter++;
        }
    }

    public double averageLikeListLength = 40;
    public long averageTotalLikeListCounter = 1;

    public void calculateAverageLikeListLength(long newLength)
    {
        if (newLength > 15)
        {
            averageLikeListLength = (averageLikeListLength * (averageTotalLikeListCounter - 1) + newLength) / averageTotalLikeListCounter;
            averageTotalLikeListCounter++;
        }
    }


    long initialCounter = 30000;

    public double averageFollowerNo = 1000;
    public long averageTotalFollowerCounter = initialCounter;
    public long averageFollowerMax = 10000;

    public void calculateAverageFollowerNo(long newNumber)
    {
        if (newNumber == 0) {return;}

        newNumber = newNumber > averageFollowerMax ? averageFollowerMax : newNumber;

        averageFollowerNo = (averageFollowerNo*(averageTotalFollowerCounter -1) + newNumber) / averageTotalFollowerCounter;
        averageTotalFollowerCounter++;
    }

    public double averageFollowingNo = 500;
    public long averageTotalFollowingNoCounter = initialCounter;
    public long averageFollowingMax = 1000;

    public void calculateAverageFollowingNo(long newNumber)
    {
        if (newNumber == 0) {return;}

        newNumber = newNumber > averageFollowingMax ? averageFollowingMax : newNumber;

        averageFollowingNo = (averageFollowingNo*(averageTotalFollowingNoCounter -1) + newNumber) / averageTotalFollowingNoCounter;
        averageTotalFollowingNoCounter++;
    }

    public double averagePostNo = 100;
    public long averageTotalPostNoCounter = initialCounter;
    public long averagePostMax = 1000;

    public void calculateAveragePostNo(long newNumber)
    {
        if (newNumber == 0) {return;}

        newNumber = newNumber > averagePostMax ? averagePostMax : newNumber;

        averagePostNo = (averagePostNo*(averageTotalPostNoCounter -1) + newNumber) / averageTotalPostNoCounter;
        averageTotalPostNoCounter++;
    }


    public double averageCommentNo = 40;
    public long averageTotalCommentNoCounter = initialCounter;
    public long averageCommentMax = 100;

    public void calculateAverageCommentNo(long newNumber)
    {
        if (newNumber == 0) {return;}

        newNumber = newNumber > averageCommentMax ? averageCommentMax : newNumber;

        averageCommentNo = (averageCommentNo*(averageTotalCommentNoCounter -1) + newNumber) / averageTotalCommentNoCounter;
        averageTotalCommentNoCounter++;
    }


    public double averageLikeNo = 100;
    public long averageTotalLikeNoCounter = initialCounter;
    public long averageLikeMax = 1000;

    public void calculateAverageLikeNo(long newNumber)
    {
        if (newNumber == 0) {return;}

        newNumber = newNumber > averageLikeMax ? averageLikeMax : newNumber;

        averageLikeNo = (averageLikeNo*(averageTotalLikeNoCounter -1) + newNumber) / averageTotalLikeNoCounter;
        averageTotalLikeNoCounter++;
    }

    /*Database Transaction Times*/

    public long updateUserInfoCounter = 1;
    public double averageUpdateUserInfoTime = 0;

    public void calculateAverageUpdateUserInfo(double newValue)
    {
        averageUpdateUserInfoTime = ( averageUpdateUserInfoTime * (updateUserInfoCounter - 1) + newValue) / updateUserInfoCounter++;
    }

    public long addFollowerToDBCounter = 1;
    public double averageAddFollowersToDBTime = 0;

    public void calculateAverageAddFollowerToDB(double newValue)
    {
        averageAddFollowersToDBTime = ( averageAddFollowersToDBTime * (addFollowerToDBCounter - 1) + newValue) / addFollowerToDBCounter++;
    }

    public long addFollowingToDBCounter = 1;
    public double averageAddFollowingToDBTime = 0;

    public void calculateAverageAddFollowingToDB(double newValue)
    {
        averageAddFollowingToDBTime = ( averageAddFollowingToDBTime * (addFollowingToDBCounter - 1) + newValue) / addFollowingToDBCounter;
        addFollowingToDBCounter++;
    }

    public long addPostMediaToDBCounter = 1;
    public double averageAddPostMediaToDBTime = 0;

    public void calculateAverageAddPostMediaToDB(double newValue)
    {
        averageAddPostMediaToDBTime = ( averageAddPostMediaToDBTime * (addPostMediaToDBCounter - 1) + newValue) / addPostMediaToDBCounter++;
    }

    public long addLikeToDBCounter = 1;
    public double averageAddLikeToDBTime = 0;

    public void calculateAverageAddLikeToDB(double newValue)
    {
        averageAddLikeToDBTime = ( averageAddLikeToDBTime * (addLikeToDBCounter - 1) + newValue) / addLikeToDBCounter++;
    }

    public long addCommentToDBCounter = 1;
    public double averageAddCommentToDBTime = 0;

    public void calculateAverageAddCommentToDB(double newValue)
    {
        averageAddCommentToDBTime = ( averageAddCommentToDBTime * (addCommentToDBCounter - 1) + newValue) / addCommentToDBCounter++;
    }

    public long finalizationCounter = 1;
    public double averageFinalizationTime = 0;

    public void calculateAverageFinalization(double newValue)
    {
        averageFinalizationTime = ( averageFinalizationTime * (finalizationCounter - 1) + newValue) / finalizationCounter++;
    }
}
