package vp.metagram.utils.instagram.executors.statistics.types;

import org.json.JSONArray;
import org.json.JSONException;


import vp.metagram.types.enumRank;

import static vp.metagram.types.enumRank.rankA;
import static vp.metagram.types.enumRank.rankAPlus;
import static vp.metagram.types.enumRank.rankAPlusPlus;
import static vp.metagram.types.enumRank.rankB;
import static vp.metagram.types.enumRank.rankC;


public class RankCalculator
{
    int limitsLength = 7;

    final int lessThan1K = 0;
    final int lessThan10KMoreThan1k = 1;
    final int lessThan100KMoreThan10k = 2;
    final int lessThan1MMoreThan100k = 3;
    final int lessThan5MMoreThan1M = 4;
    final int lessThan15MMoreThan5M = 5;
    final int moreThan15M = 6;

    RankDecisionLimit[] limits = new RankDecisionLimit[limitsLength];

    public RankCalculator()
    {
        try
        {
            JSONArray jsonArray = new JSONArray(defaultValues);
            for (int i=0 ; i <limitsLength; i++)
            {
                limits[i] = new RankDecisionLimit(jsonArray.getString(i));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public enumRank calculateRank(int followersCount, double alpha, int postsCount, double meanEngagement)
    {

        int index = getIndexBasedOnFollowerCount(followersCount);
        RankDecisionLimit rankDecisionLimit = limits[index];

        enumRank rank;

        // Big Pages Exceptions
        if (followersCount > 100*1000*1000)
        {
            if(meanEngagement > 0.0015)
            {
                rank = rankAPlusPlus;
            }
            else
            {
                rank = rankAPlus;
            }

            return rank;
        }


        if (followersCount > 50*1000*1000)
        {
            if (meanEngagement > 0.005)
            {
                rank = rankAPlusPlus;
            }
            else
            {
                rank = rankAPlus;
            }


            return rank;
        }
        // Big Pages Exceptions


        rank = rankAPlusPlus;
        enumRank tempRank;
        // measure no of posts
        if ( postsCount > rankDecisionLimit.postsNo_rankAPlusPlusLimit )
        {
            tempRank = rankAPlusPlus;
        }
        else if ( postsCount > rankDecisionLimit.postsNo_rankAPlusLimit )
        {
            tempRank = rankAPlus;
        }
        else if ( postsCount > rankDecisionLimit.postsNo_rankALimit )
        {
            tempRank = rankA;
        }
        else if ( postsCount > rankDecisionLimit.postsNo_rankBLimit )
        {
            tempRank = rankB;
        }
        else
        {
            tempRank = rankC;
        }

        if ( tempRank.ordinal() < rank.ordinal() )
        {
            rank = tempRank;
        }
        // measure no of posts

        // measure alpha

        if ( alpha < rankDecisionLimit.alpha_rankAPlusPlusLimit )
        {
            tempRank = rankAPlusPlus;
        }
        else if ( alpha < rankDecisionLimit.alpha_rankAPlusLimit )
        {
            tempRank = rankAPlus;
        }
        else if ( alpha < rankDecisionLimit.alpha_rankALimit )
        {
            tempRank = rankA;
        }
        else if ( alpha < rankDecisionLimit.alpha_rankBLimit )
        {
            tempRank = rankB;
        }
        else
        {
            tempRank = rankC;
        }

        if ( tempRank.ordinal() < rank.ordinal() )
        {
            rank = tempRank;
        }
        // measure alpha

        // measure mean engagement
        if ( meanEngagement > rankDecisionLimit.meanEngagement_rankAPlusPlusLimit )
        {
            tempRank = rankAPlusPlus;
        }
        else if ( meanEngagement > rankDecisionLimit.meanEngagement_rankAPlusLimit )
        {
            tempRank = rankAPlus;
        }
        else if ( meanEngagement > rankDecisionLimit.meanEngagement_rankALimit )
        {
            tempRank = rankA;
        }
        else if ( meanEngagement > rankDecisionLimit.meanEngagement_rankBLimit )
        {
            tempRank = rankB;
        }
        else
        {
            tempRank = rankC;
        }

        if ( tempRank.ordinal() < rank.ordinal() )
        {
            rank = tempRank;
        }
        // measure mean engagement


        switch ( index )
        {
            case lessThan1K:
                if (rank.ordinal() > rankB.ordinal())
                {
                    rank = rankB;
                }
                break;
            case lessThan10KMoreThan1k:
                if (rank.ordinal() > rankA.ordinal())
                {
                    rank = rankA;
                }
                break;
            case lessThan100KMoreThan10k:
                if (rank.ordinal() > rankAPlus.ordinal())
                {
                    rank = rankAPlus;
                }
                break;
        }

        return rank;
    }

    public void setLimitValues(String values)
    {
        try
        {
            JSONArray jsonArray = new JSONArray(values);
            for (int i=0; i < limitsLength; i++)
            { limits[i].setValues(jsonArray.getString(i)); }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getIndexBasedOnFollowerCount(int followerCount)
    {
        int result = 0;

        if (followerCount >= 0 && followerCount < 1000)
        {
            result = lessThan1K;
        }
        else if (followerCount >= 1000 && followerCount < 10 * 1000)
        {
            result = lessThan10KMoreThan1k;
        }
        else if (followerCount >= 10 * 1000 && followerCount < 100 * 1000)
        {
            result = lessThan100KMoreThan10k;
        }
        else if (followerCount >= 100 * 1000 && followerCount < 1000 * 1000)
        {
            result = lessThan1MMoreThan100k;
        }
        else if (followerCount >= 1000 * 1000 && followerCount < 5* 1000 * 1000)
        {
            result = lessThan5MMoreThan1M;
        }
        else if (followerCount >= 5* 1000 * 1000 && followerCount < 15* 1000 * 1000)
        {
            result = lessThan15MMoreThan5M;
        }
        else if (followerCount >= 15* 1000 * 1000)
        {
            result = moreThan15M;
        }

        return result;
    }


    final static String defaultValues = "[\n" +
            "  {\n" +
            "    \"name\": \"lessThan1K\",\n" +
            "    \"postsNo_rankAPlusPlusLimit\": 2147483647,\n" +
            "    \"postsNo_rankAPlusLimit\": 2147483647,\n" +
            "    \"postsNo_rankALimit\": 2147483647,\n" +
            "    \"postsNo_rankBLimit\": 5,\n" +
            "    \"alpha_rankAPlusPlusLimit\": -1,\n" +
            "    \"alpha_rankAPlusLimit\": -1,\n" +
            "    \"alpha_rankALimit\": -1,\n" +
            "    \"alpha_rankBLimit\": 3,\n" +
            "    \"meanEngagement_rankAPlusPlusLimit\": 2147483647,\n" +
            "    \"meanEngagement_rankAPlusLimit\": 2147483647,\n" +
            "    \"meanEngagement_rankALimit\": 2147483647,\n" +
            "    \"meanEngagement_rankBLimit\": 0.1\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"lessThan10KMoreThan1k\",\n" +
            "    \"postsNo_rankAPlusPlusLimit\": 2147483647,\n" +
            "    \"postsNo_rankAPlusLimit\": 2147483647,\n" +
            "    \"postsNo_rankALimit\": 10,\n" +
            "    \"postsNo_rankBLimit\": 5,\n" +
            "    \"alpha_rankAPlusPlusLimit\": -1,\n" +
            "    \"alpha_rankAPlusLimit\": -1,\n" +
            "    \"alpha_rankALimit\": 1.5,\n" +
            "    \"alpha_rankBLimit\": 2,\n" +
            "    \"meanEngagement_rankAPlusPlusLimit\": 2147483647,\n" +
            "    \"meanEngagement_rankAPlusLimit\": 2147483647,\n" +
            "    \"meanEngagement_rankALimit\": 0.0550,\n" +
            "    \"meanEngagement_rankBLimit\": 0.0250\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"lessThan100KMoreThan10k\",\n" +
            "    \"postsNo_rankAPlusPlusLimit\": 2147483647,\n" +
            "    \"postsNo_rankAPlusLimit\": 20,\n" +
            "    \"postsNo_rankALimit\": 10,\n" +
            "    \"postsNo_rankBLimit\": 5,\n" +
            "    \"alpha_rankAPlusPlusLimit\": -1,\n" +
            "    \"alpha_rankAPlusLimit\": 1,\n" +
            "    \"alpha_rankALimit\": 1.5,\n" +
            "    \"alpha_rankBLimit\": 1.7,\n" +
            "    \"meanEngagement_rankAPlusPlusLimit\": 2147483647,\n" +
            "    \"meanEngagement_rankAPlusLimit\": 0.0310,\n" +
            "    \"meanEngagement_rankALimit\": 0.0210,\n" +
            "    \"meanEngagement_rankBLimit\": 0.0110\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"lessThan1MMoreThan100k\",\n" +
            "    \"postsNo_rankAPlusPlusLimit\": 20,\n" +
            "    \"postsNo_rankAPlusLimit\": 20,\n" +
            "    \"postsNo_rankALimit\": 10,\n" +
            "    \"postsNo_rankBLimit\": 5,\n" +
            "    \"alpha_rankAPlusPlusLimit\": 0.3,\n" +
            "    \"alpha_rankAPlusLimit\": 0.4,\n" +
            "    \"alpha_rankALimit\": 0.5,\n" +
            "    \"alpha_rankBLimit\": 0.7,\n" +
            "    \"meanEngagement_rankAPlusPlusLimit\": 0.0380,\n" +
            "    \"meanEngagement_rankAPlusLimit\": 0.0270,\n" +
            "    \"meanEngagement_rankALimit\": 0.0150,\n" +
            "    \"meanEngagement_rankBLimit\": 0.0080\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"lessThan5MMoreThan1M\",\n" +
            "    \"postsNo_rankAPlusPlusLimit\": 20,\n" +
            "    \"postsNo_rankAPlusLimit\": 20,\n" +
            "    \"postsNo_rankALimit\": 10,\n" +
            "    \"postsNo_rankBLimit\": 5,\n" +
            "    \"alpha_rankAPlusPlusLimit\": 0.01,\n" +
            "    \"alpha_rankAPlusLimit\": 0.03,\n" +
            "    \"alpha_rankALimit\": 0.05,\n" +
            "    \"alpha_rankBLimit\": 0.1,\n" +
            "    \"meanEngagement_rankAPlusPlusLimit\": 0.0180,\n" +
            "    \"meanEngagement_rankAPlusLimit\": 0.0150,\n" +
            "    \"meanEngagement_rankALimit\": 0.0075,\n" +
            "    \"meanEngagement_rankBLimit\": 0.0050\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"lessThan15MMoreThan5M\",\n" +
            "    \"postsNo_rankAPlusPlusLimit\": 20,\n" +
            "    \"postsNo_rankAPlusLimit\": 20,\n" +
            "    \"postsNo_rankALimit\": 10,\n" +
            "    \"postsNo_rankBLimit\": 5,\n" +
            "    \"alpha_rankAPlusPlusLimit\": 0.01,\n" +
            "    \"alpha_rankAPlusLimit\": 0.03,\n" +
            "    \"alpha_rankALimit\": 0.05,\n" +
            "    \"alpha_rankBLimit\": 0.1,\n" +
            "    \"meanEngagement_rankAPlusPlusLimit\": 0.0100,\n" +
            "    \"meanEngagement_rankAPlusLimit\": 0.0080,\n" +
            "    \"meanEngagement_rankALimit\": 0.0060,\n" +
            "    \"meanEngagement_rankBLimit\": 0.0030\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"moreThan15M\",\n" +
            "    \"postsNo_rankAPlusPlusLimit\": 20,\n" +
            "    \"postsNo_rankAPlusLimit\": 20,\n" +
            "    \"postsNo_rankALimit\": 10,\n" +
            "    \"postsNo_rankBLimit\": 5,\n" +
            "    \"alpha_rankAPlusPlusLimit\": 0.01,\n" +
            "    \"alpha_rankAPlusLimit\": 0.03,\n" +
            "    \"alpha_rankALimit\": 0.05,\n" +
            "    \"alpha_rankBLimit\": 0.1,\n" +
            "    \"meanEngagement_rankAPlusPlusLimit\": 0.0090,\n" +
            "    \"meanEngagement_rankAPlusLimit\": 0.0075,\n" +
            "    \"meanEngagement_rankALimit\": 0.0055,\n" +
            "    \"meanEngagement_rankBLimit\": 0.0020\n" +
            "  }\n" +
            "]";
}
