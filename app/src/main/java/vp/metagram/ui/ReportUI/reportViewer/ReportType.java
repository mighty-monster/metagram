package vp.metagram.ui.ReportUI.reportViewer;


import vp.metagram.R;


public enum ReportType
{
    notFollowedBack(101),
    notFollowingBack(102),
    postsWithMostLikes(103),
    postsWithMostComments(104),
    postsWithMostViews(105),
    peopleLikedMost(106),
    peopleCommentedMost(107),
    peopleEngagedMost(108),
    lazyFollowers(109),
    likedButDidNotFollow(110),
    commentedButDidNotFollow(111),
    peopleUnfollowedYou(112),
    removedLikes(113),
    removedComments(114),
    someonesComments(115),
    someonesLikes(116),
    peopleYouUnfollowed(117);


    public int ordinal;

    ReportType(int ordinal)
    {this.ordinal = ordinal;}

    public int getIconID()
    {
        int result = 0;

        switch (ordinal)
        {
            case 101:
                result = R.drawable.ic_not_followed_back;
                break;
            case 102:
                result = R.drawable.ic_not_following_back;
                break;
            case 103:
                result = R.drawable.ic_most_liked_posts;
                break;
            case 104:
                result = R.drawable.ic_most_commented_posts;
                break;
            case 105:
                result = R.drawable.ic_most_viewed_posts;
                break;
            case 106:
                result = R.drawable.ic_people_most_liked;
                break;
            case 107:
                result = R.drawable.ic_people_most_commented;
                break;
            case 108:
                result = R.drawable.ic_people_most_engaged;
                break;
            case 109:
                result = R.drawable.ic_lazy_followers;
                break;
            case 110:
                result = R.drawable.ic_liked_but_did_not_follow;
                break;
            case 111:
                result = R.drawable.ic_commented_but_did_not_follow;
                break;
            case 112:
                result = R.drawable.ic_unfollowed_you;
                break;
            case 113:
                result = R.drawable.ic_removed_likes;
                break;
            case 114:
                result = R.drawable.ic_removed_comments;
                break;
            case 115:
                result = R.drawable.ic_comment;
                break;
            case 116:
                result = R.drawable.ic_like;
                break;
            case 117:
                result = R.drawable.ic_you_unfollowed;
                break;
        }


        return result;
    }

    int getTitleID()
    {
        int result = 0;

        switch (ordinal)
        {
            case 101:
                result = R.string.reportType_notFollowedBack;
                break;
            case 102:
                result = R.string.reportType_notFollowingBack;
                break;
            case 103:
                result = R.string.reportType_mostLikedPosts;
                break;
            case 104:
                result = R.string.reportType_mostCommentedPosts;
                break;
            case 105:
                result = R.string.reportType_mostViewedPosts;
                break;
            case 106:
                result = R.string.reportType_peopleMostLiked;
                break;
            case 107:
                result = R.string.reportType_peopleMostCommented;
                break;
            case 108:
                result = R.string.reportType_peopleMostEngaged;
                break;
            case 109:
                result = R.string.reportType_lazyFollowers;
                break;
            case 110:
                result = R.string.reportType_likedButDidNotFollow;
                break;
            case 111:
                result = R.string.reportType_commentedButDidNotFollow;
                break;
            case 112:
                result = R.string.reportType_unfollowedYou;
                break;
            case 113:
                result = R.string.reportType_removedLikes;
                break;
            case 114:
                result = R.string.reportType_removedComments;
                break;
            case 115:
                result = R.string.reportType_someonesComments;
                break;
            case 116:
                result = R.string.reportType_someonesLikes;
                break;
            case 117:
                result = R.string.reportType_youunfollowed;
                break;
        }

        return result;
    }

    static ReportType newInstance(int ordinal)
    {
        ReportType result = null;
        for ( ReportType reportType : ReportType.values() )
        {
            if ( reportType.ordinal == ordinal )
            {
                result = reportType;
                break;
            }
        }
        return result;
    }

}
