package vp.metagram.utils.instagram;

import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import vp.igwa.CommentList;
import vp.igwa.FollowerList;
import vp.igwa.FollowingList;
import vp.igwa.GraphReel;
import vp.igwa.Highlight;
import vp.igwa.HighlightList;
import vp.igwa.HighlightSummery;
import vp.igwa.IGTVList;
import vp.igwa.IGWAExtractor;
import vp.igwa.LikeList;
import vp.igwa.MediaList;
import vp.igwa.SearchList;
import vp.igwa.Story;
import vp.igwa.UserSaviour;
import vp.metagram.ui.AccBrowser.DownloadStory.highlight.HighlightItemType;
import vp.metagram.utils.instagram.types.AccountInfo;
import vp.metagram.utils.instagram.types.Comment;
import vp.metagram.utils.instagram.types.HighlightItem;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.User;
import vp.metagram.utils.instagram.types.UserFull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import vp.igpapi.IGWAException;


import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.logger;
import static vp.metagram.general.variables.threadPoolExecutor;

/**
 * Created by arash on 2/26/18.
 */


public class InstagramAgent implements InstagramInterface
{
    public IGWAExtractor api;

    public String username;
    public String pictureURL;

    public long userID = 0;

    public boolean isEnabled = true;
    public boolean isReady = false;
    public boolean isRegistered = false;

    public InstagramInterface proxy;
    public InstagramProxy dynamicProxy;

    public InstagramAgentStatus agentStatus;

    public Picasso picasso;

    private UserSaviour userSaviour;


    public InstagramAgent(IGWAExtractor api)
    {
        this.api = api;
        this.username = api.get_username();

        ClassLoader instagramClassLoader = getClass().getClassLoader();

        agentStatus = InstagramAgentStatus.load(this.userID);

        dynamicProxy = new InstagramProxy(this);
        proxy = (InstagramInterface) Proxy.newProxyInstance(instagramClassLoader, new Class[]{InstagramInterface.class}, dynamicProxy);

        userSaviour = new UserSaviour(this);

        api.set_saviour(userSaviour);

        buildPicasso();


        try
        {
            if ( api.isLogedin )
            {
                UserFull user = api.user_info(username, new UserFull());

                this.username = user.username;
                this.userID = user.IPK;
                this.pictureURL = user.picURL;

                agentStatus = InstagramAgentStatus.load(userID);

                saveAPIInternalsToDB();

            }
        }
        catch (Exception e)
        {
            logger.logError(this.getClass().getName(),
                    "Error while trying to save login account to db.\n", e);
        }
    }


    public InstagramAgent(String username) throws IOException, JSONException, IGWAException, GeneralSecurityException
    {
        api = new IGWAExtractor(username, new UserSaviour(this));

        userID = api.user_id;
        this.username = username;

        ClassLoader instagramClassLoader = getClass().getClassLoader();

        dynamicProxy = new InstagramProxy(this);
        proxy = (InstagramInterface) Proxy.newProxyInstance(instagramClassLoader, new Class[]{InstagramInterface.class}, dynamicProxy);

        agentStatus = InstagramAgentStatus.load(userID);
        try
        {
            loadAPIInternalsFromDB();
        }
        catch (JSONException | IGWAException | IOException e)
        {
            logger.logError(this.getClass().getName(),
                    "Instagram Agent loadAPIInternalsFromDB failed.\n", e);

            throw e;
        }

        buildPicasso();
    }

    private void buildPicasso()
    {
        Picasso.Builder builder = new Picasso.Builder(appContext);
        builder.listener((picasso, uri, exception) -> exception.printStackTrace());

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain ->
                {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Cookie", api.get_cookie_string())
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();

        builder.downloader(new OkHttp3Downloader(client));
        builder.executor(threadPoolExecutor);
        picasso = builder.build();
    }


    public void saveAPIInternalsToDB() throws IOException, JSONException
    {
        api.save();
    }


    public void loadAPIInternalsFromDB() throws JSONException, IGWAException, IOException
    {
        api.load();
    }

    public void GetAccountInfoAndSaveToDB(int StatisticsJobID) throws  IOException, NoSuchAlgorithmException, JSONException, IGWAException
    {
        UserFull accountInfo = proxy.getUserInfo(username);

        pictureURL = accountInfo.picURL;

        String sqlText = String.format(Locale.ENGLISH, " Insert Into Account_Info(StatisticsJobID,FIPK,FollowersNo,FollowingNo,PostsNo) " +
                " Values(%d,%d,%d,%d,%d)", StatisticsJobID, userID, accountInfo.followerCount, accountInfo.followingCount, accountInfo.postsCount);
        dbMetagram.execQuery(sqlText);
    }

    public void GetAccountInfoAndSaveToDB() throws  IOException, NoSuchAlgorithmException, JSONException, IGWAException
    {
        GetAccountInfoAndSaveToDB(-1);
    }

    public void refreshAgentInfo() throws  IOException, JSONException, GeneralSecurityException, IGWAException
    {
        GetAccountInfoAndSaveToDB();

        saveAPIInternalsToDB();
    }


    public AccountInfo getAccountStatisticsFromDB()
    {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.IPK = userID;

        String sqlText = String.format(Locale.ENGLISH, " Select  FollowersNo, FollowingNo, PostsNo From Account_Info " +
                "     Where FIPK = %d Order By Account_Info.ID desc limit 1", userID);

        MatrixCursor result = null;
        try
        {
            result = dbMetagram.selectQuery(sqlText);
        }
        catch (Exception e)
        {
            logger.logError(this.getClass().getName(),
                    "Instagram Agent getAccountStatisticsFromDB failed.\n", e);
        }

        if ( result.moveToFirst() )
        {
            try
            {
                accountInfo.followerCount = result.getInt(result.getColumnIndex("FollowersNo"));
            }
            catch (Exception e) {}
            try
            {
                accountInfo.followingCount = result.getInt(result.getColumnIndex("FollowingNo"));
            }
            catch (Exception e) {}
            try
            {
                accountInfo.postsCount = result.getInt(result.getColumnIndex("PostsNo"));
            }
            catch (Exception e) {}
        }

        return accountInfo;
    }

    public Drawable getProfilePicture()
    {
        RoundedBitmapDrawable picture = null;
        String sqlText = String.format(Locale.ENGLISH, "Select Picture From Accounts Where IPK = %d ", userID);

        MatrixCursor result = null;
        try
        {
            result = dbMetagram.selectQuery(sqlText);
            result.moveToFirst();
            String picB64 = result.getString(result.getColumnIndex("Picture"));
            byte[] picBytes = Base64.decode(picB64, Base64.DEFAULT);

            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bMap = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length, options);
            picture = RoundedBitmapDrawableFactory.create(appContext.getResources(), bMap);
            picture.setCornerRadius(100f);

        }
        catch (Exception e)
        {
            logger.logError(this.getClass().getName(),
                    "Instagram Agent getProfilePicture failed.\n", e);
        }


        return picture;
    }



    public UserFull getUserInfo(String username) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        UserFull user = api.user_info(username, new UserFull());

        agentStatus.count_getUserInfo();

        agentStatus.calculateAverageFollowerNo(user.followerCount);
        agentStatus.calculateAverageFollowingNo(user.followingCount);
        agentStatus.calculateAveragePostNo(user.postsCount);

        return user;
    }

    public void follow(long IPK) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {

        JSONObject result = api.follow(IPK);

        agentStatus.count_follow();

    }

    public void unfollow(long IPK) throws  IOException, JSONException, IGWAException, NoSuchAlgorithmException
    {
        JSONObject result = api.unfollow(IPK);

        agentStatus.count_unfollow();

    }

    public void like(long MPK) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        JSONObject result = api.post_like(MPK);

        agentStatus.count_like();


    }

    public void comment(long MPK, String commentContent) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        JSONObject result = api.post_comment(MPK, commentContent);

        agentStatus.count_comment();

    }

    public void search(String username, List<User> result) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        SearchList searchList = api.search(username, new SearchList());

        result.addAll(searchList.users);

        agentStatus.count_search();


    }

    public String getFollowerList(long IPK, Map<Long, User> result, String hash) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        String next_hash = null;

        FollowerList followerList = api.user_followers(IPK, hash, new FollowerList());

        result.putAll(followerList.followers);

        next_hash = followerList.next_hash;

        agentStatus.count_getFollowerList();

        agentStatus.calculateAverageFollowerListLength(followerList.followers.size());

        return next_hash;
    }

    public String getFollowingList(long IPK, Map<Long, User> result, String hash) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        String next_hash = null;

        FollowingList followingList = api.user_followings(IPK, hash, new FollowingList());

        result.putAll(followingList.followings);

        next_hash = followingList.next_hash;

        agentStatus.count_getFollowingList();

        agentStatus.calculateAverageFollowingListLength(followingList.followings.size());

        return next_hash;
    }

    public PostMedia getMediaInfoByMediaCode(String short_code) throws IOException,  JSONException, NoSuchAlgorithmException, IGWAException
    {
        PostMedia media  = api.media_info(short_code, new PostMedia());

        return media;
    }


    public String getMediaList(long IPK, Map<Long, PostMedia> result, String hash, long minTimeStamp) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        String next_hash = null;

        MediaList mediaList = api.user_feed(IPK, hash, new MediaList());

        result.putAll(mediaList.medias);

        next_hash = mediaList.next_hash;

        if (mediaList.count != 0 && mediaList.medias.size() == 0)
        {
            throw new IGWAException(IGWAException.not_authorized, "Not authorized to get media list");
        }

        agentStatus.count_getMediaList();

        agentStatus.calculateAveragePostsListLength(mediaList.medias.size());

        return next_hash;
    }

    public String getCommentList(String short_code, Map<Long, Comment> result, String hash) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        String next_hash = null;

        CommentList commentList = api.media_comments(short_code, hash, new CommentList());

        result.putAll(commentList.comments);

        next_hash = commentList.next_hash;

        agentStatus.count_getCommentList();

        agentStatus.calculateAverageCommentListLength(commentList.comments.size());

        return next_hash;
    }

    public String getLikeList(String short_code, Map<Long, User> result, String hash) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {

        String next_hash = null;

        LikeList likeList = api.media_likers(short_code, hash, new LikeList());

        result.putAll(likeList.likers);

        next_hash = likeList.next_hash;

        agentStatus.count_getLikeList();

        agentStatus.calculateAverageLikeListLength(likeList.likers.size());

        return next_hash;
    }

    @Override
    public void getStories(long IPK, List<PostMedia> result) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        Story story = api.reels_feed(new Long[]{IPK}, new Story());

        for (GraphReel item: story.items)
        {
            PostMedia newMedia = new PostMedia();

            newMedia.MPK = item.id;
            newMedia.picURL = item.display_url;

            if (item.isVideo)
            {
                newMedia.urls.add(item.video_url);
            }
            else
            {
                newMedia.urls.add(item.resource_url);
            }
            result.add(newMedia);
        }

    }

    public void getHighlights(long IPK, List<HighlightItem> highlightItems) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        HighlightList highlightList = api.highlight_reels(IPK, new HighlightList());

        for (HighlightSummery summery : highlightList.items)
        {
            HighlightItem newItem = new HighlightItem();

            newItem.id = Long.toString(summery.id);
            newItem.CoverURL = summery.cover_media;
            newItem.title = summery.title;
            newItem.itemType = HighlightItemType.Highlight;

            highlightItems.add(newItem);
        }

    }

    public HighlightItem getHighlightsDetail(String id) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        HighlightItem item = new HighlightItem();
        item.mediaList = new ArrayList<>();

        Highlight highlight = api.highlight_reel_media(new Long[] {Long.parseLong(id)} ,new Highlight());

        item.itemType = HighlightItemType.Highlight;
        item.id = id;
        item.mediaCount = highlight.items.size();

        for (GraphReel reelItem : highlight.items)
        {
            PostMedia newMedia = new PostMedia();

            newMedia.MPK = reelItem.id;
            newMedia.picURL = reelItem.display_url;

            if (reelItem.isVideo)
            {
                newMedia.urls.add(reelItem.video_url);
            }
            else
            {
                newMedia.urls.add(reelItem.resource_url);
            }

            item.mediaList.add(newMedia);

        }

        return item;
    }

    public void getIGTVList(long IPK, IGTVList igtvList) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        api.IGTV_feed(IPK, igtvList.next_hash, igtvList);
    }



    public boolean checkFriendship(String accountName) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException
    {
        boolean result;

        UserFull account = api.user_info(accountName, new UserFull());

        result = account.followedByViewer;

        return result;
    }


}



