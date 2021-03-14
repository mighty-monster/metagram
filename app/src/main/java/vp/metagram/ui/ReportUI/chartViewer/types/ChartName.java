package vp.metagram.ui.ReportUI.chartViewer.types;

import android.graphics.drawable.Drawable;


import vp.metagram.R;


public enum ChartName
{
    followersInTime(101),
    followingsInTime(102),
    totalLikesInTime(103),
    totalCommentsInTime(104),
    totalViewsInTime(105),
    engagementInTime(106),
    addedLikes(107),
    addedComments(108),
    addedViews(109);


    int ordinal;

    ChartName(int ordinal)
    {this.ordinal = ordinal;}

    Drawable getIcon()
    {
        return null;
    }

    public int getChartTitle()
    {
        int result = 0;

        switch ( ordinal )
        {
            case 101:
                result = R.string.chartTitle_changeOfFollowers;
                break;
            case 102:
                result = R.string.chartTitle_changeOfFollowings;
                break;
            case 103:
                result = R.string.chartTitle_totalLikesInTime;
                break;
            case 104:
                result = R.string.chartTitle_totalCommentsInTime;
                break;
            case 105:
                result = R.string.chartTitle_totalViewsInTime;
                break;
            case 106:
                result = R.string.chartTitle_engagementInTime;
                break;
            case 107:
                result = R.string.chartTitle_addedLikes;
                break;
            case 108:
                result = R.string.chartTitle_addedComments;
                break;
            case 109:
                result = R.string.chartTitle_addedViews;
                break;



        }


        return result;
    }



}
