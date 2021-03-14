package vp.metagram.ui.AccBrowser.MediaViewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


import me.relex.circleindicator.CircleIndicator;
import vp.igwa.IGWAExtractor;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.utils.DownloadService;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.User;

import static vp.metagram.general.functions.downloadPost;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;


public class MediaViewerActivity extends BaseActivity
{

    boolean isLocal = false;
    String localPath;
    List<String> urls;

    ViewPager viewPager;

    MediaViewerPageAdapter adapter;

    String mediaCode;

    DownloadService downloadService;
    PostMediaWithUserInfo media;
    boolean isShared = false;

    ImageView downloadButton;

    Locale oldLocale;

    String dirPostfix;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_media_viewer);

        downloadButton = findViewById(R.id.MediaViewer_downloadButton);

        oldLocale = Locale.getDefault();
        Locale locale = new Locale("en");
        Locale.setDefault(locale);

        Intent intent = getIntent();

        String action = intent.getAction();
        String type = intent.getType();

        downloadService = new DownloadService(this);

        if (Intent.ACTION_SEND.equals(action) && type != null)
        {
            if ("text/plain".equals(type))
            {
                String postLink = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (postLink != null)
                {
                    checkPostURL(postLink);

                    isShared = true;

                    ConnectingDialog connectingDialog = ConnectingDialog.newInstance(getResources().getString(R.string.login_connectMessage));
                    connectingDialog.show(getFragmentManager(), "");

                    threadPoolExecutor.execute(() ->
                    {
                        try
                        {
                            IGWAExtractor ig;
                            if (metagramAgent.activeAgent != null)
                            {
                                ig = metagramAgent.activeAgent.api;
                            }
                            else
                            {
                                ig = new IGWAExtractor();
                            }

                            media = ig.media_info(mediaCode, new PostMediaWithUserInfo());

                            urls = media.urls;

                            runOnUiThread(() ->
                                    configureUI());

                        }
                        catch (Exception e)
                        {
                            runOnUiThread(() ->
                                    errorHappened());
                        }
                        finally
                        {
                            runOnUiThread(() ->
                                    connectingDialog.close());
                        }
                    });
                }
            }
        }
        else
        {
            Bundle extras = intent.getExtras();

            if (extras != null)
            {
                String[] urlsArray = extras.getStringArray("urls");
                if (urlsArray != null)
                    urls = Arrays.asList(urlsArray);
                else
                    urls = new ArrayList<>();

                isLocal = extras.getBoolean("isLocal");
                localPath = extras.getString("path");
            }

            configureUI();
        }

    }

    @Override
    public void onDestroy()
    {
        Locale.setDefault(oldLocale);
        super.onDestroy();
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    public void configureUI()
    {
        if (isShared)
        {
            downloadButton.setVisibility(View.VISIBLE);
        }

        downloadButton.setOnClickListener((View v)->
        {
            downloadPost(this, urls, media.userInfo.username, media.ID, downloadService,dirPostfix );
            finish();
        });

        if (isLocal)
        {
            File directory = new File(localPath);
            File[] fList = directory.listFiles();
            if (fList != null)
                for (File file : fList)
                {
                    if (file.isFile())
                    { urls.add(file.getAbsolutePath()); }
                }
        }

        viewPager = findViewById(R.id.MediaViewer_viewPager);

        adapter = new MediaViewerPageAdapter(getSupportFragmentManager(), urls, this, isLocal);

        viewPager.setAdapter(adapter);

        CircleIndicator indicator = findViewById(R.id.MediaViewer_indicator);
        indicator.bringToFront();
        indicator.setViewPager(viewPager);
    }

    public void checkPostURL(String url)
    {
        boolean wrongFormat = false;

        try
        {
            if (!url.contains("www.instagram.com/p") &&
                    !url.contains("www.instagram.com/tv") &&
                    !url.contains("www.instagram.com/reel/"))
            {
                wrongFormat = true;
            }

            if (url.contains("www.instagram.com/p"))
                dirPostfix = "Post";

            if (url.contains("www.instagram.com/tv"))
                dirPostfix = "IGTV";

            url = url.substring(1, url.indexOf("?"));
            url = url.substring(1, url.lastIndexOf("/"));
            mediaCode = url.substring(url.lastIndexOf("/") + 1);

            if (mediaCode == null || mediaCode.equals(""))
            {
                wrongFormat = true;
            }
        }
        catch (Exception e)
        {
            wrongFormat = true;
        }
        if (wrongFormat)
        {
            errorHappened();
        }
    }

    public void errorHappened()
    {
        InformationDialog dialog = new InformationDialog();
        dialog.showDialog(this,
                getString(R.string.accntBrowser_downloadTitle),
                getString(R.string.mediaViewer_error),
                getString(R.string.button_ok), () -> finish());

    }

    class PostMediaWithUserInfo extends PostMedia
    {
        User userInfo = new User();

        @Override
        public PostMedia digest(JSONObject jsonObject) throws JSONException
        {
            super.digest(jsonObject);

            jsonObject = jsonObject.getJSONObject("graphql");

            JSONObject mediaJSON = jsonObject.getJSONObject("shortcode_media");

            MPK = mediaJSON.getLong("id");
            miniLink = mediaJSON.getString("shortcode");

            jsonObject = mediaJSON.getJSONObject("owner");

            userInfo.IPK = jsonObject.getLong("id");
            userInfo.username = jsonObject.getString("username");
            userInfo.fullname = jsonObject.getString("full_name");
            userInfo.picURL = jsonObject.getString("profile_pic_url");
            userInfo.isPrivate = jsonObject.getBoolean("is_private");

            return this;
        }
    }

}
