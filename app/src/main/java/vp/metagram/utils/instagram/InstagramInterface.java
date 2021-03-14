package vp.metagram.utils.instagram;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import vp.igwa.IGTVList;
import vp.metagram.utils.instagram.types.Comment;
import vp.metagram.utils.instagram.types.HighlightItem;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.User;
import vp.metagram.utils.instagram.types.UserFull;
import vp.igpapi.IGWAException;

public interface InstagramInterface
{
    UserFull getUserInfo(String username) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    void follow(long IPK) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    void unfollow(long IPK) throws  IOException, JSONException, IGWAException, NoSuchAlgorithmException;

    void like(long MPK) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    void comment(long MPK, String commentContent) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    void search(String username, List<User> result) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    String getFollowerList(long IPK, Map<Long, User> result, String hash) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    String getFollowingList(long IPK, Map<Long, User> result, String hash) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    String getMediaList(long IPK, Map<Long, PostMedia> result, String hash, long minTimeStamp) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    String getCommentList(String MID, Map<Long, Comment> result, String hash) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    String getLikeList(String short_code, Map<Long, User> result, String hash) throws  IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    void getStories(long IPK, List<PostMedia> result) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    void getHighlights(long IPK, List<HighlightItem> highlightItems)  throws IOException, JSONException, NoSuchAlgorithmException, IGWAException;

    void getIGTVList(long IPK, IGTVList igtvList) throws IOException, JSONException, NoSuchAlgorithmException, IGWAException;
}
