package vp.metagram.ui.AccBrowser.MediaViewer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.File;
import java.util.ArrayList;

import vp.metagram.R;

public class MediaViewerVideoFragment extends Fragment
{

    View rootLayout;
    VideoView videoView;
    SpinKitView loading;
    MediaController mediaController;
    MediaPlayer mediaPlayer;

    ImageView shareButton;

    String url;
    boolean isLocal;

    boolean isFirstTime = true;

    int position;

    static public MediaViewerVideoFragment newInstance(String url, int position, boolean isLocal)
    {
        MediaViewerVideoFragment videoFragment = new MediaViewerVideoFragment();
        videoFragment.setUrl(url, position);
        videoFragment.isLocal = isLocal;

        return videoFragment;
    }

    public void setUrl(String url, int position)
    {
        this.url = url;
        this.position = position;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        if (isVisibleToUser)
        {
            if (videoView != null && !videoView.isPlaying())
            { videoView.start(); }

            showController();
        }
        else
        {
            if (videoView != null && videoView.isPlaying())
            { videoView.pause(); }

            hideController();
        }
    }

    public void hideController()
    {
        if (mediaController != null)
        {
            try
            {
                mediaController.hide();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    public void showController()
    {
        if (mediaController != null)
        {
            try
            {
                mediaController.show();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            url = savedInstanceState.getString("url");
            position = savedInstanceState.getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.layout_video_viewer, container, false);
        prepareUIElements();
        return rootLayout;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isFirstTime)
        {
            Uri uri = Uri.parse(url);
            videoView.setOnPreparedListener(mediaPlayer ->
            {
                loading.setVisibility(View.GONE);
                videoView.setAlpha(1f);

                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mediaPlayer.setLooping(true);
                mediaPlayer.setScreenOnWhilePlaying(true);
            });

            mediaPlayer = new MediaPlayer();


            mediaController = new MediaController(getActivity())
            {
                public boolean dispatchKeyEvent(KeyEvent event)
                {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                        getActivity().finish();

                    return super.dispatchKeyEvent(event);
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                mediaController.addOnUnhandledKeyEventListener((v, event) ->
                {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                    {
                        getActivity().finish();
                    }
                    return true;
                });
            }
            mediaController.setAnchorView(videoView);

            videoView.setMediaController(mediaController);

            videoView.setVideoURI(uri);

            mediaController.hide();

            if (position == 0)
            {
                videoView.start();
            }
            isFirstTime = false;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString("url", url);
        outState.putInt("position", position);
    }

    public void prepareUIElements()
    {
        videoView = rootLayout.findViewById(R.id.videoView);
        loading = rootLayout.findViewById(R.id.loading);
        shareButton = rootLayout.findViewById(R.id.shareButton);

        if (!isLocal) {shareButton.setVisibility(View.GONE);}

        shareButton.bringToFront();
        shareButton.setOnClickListener((View v)->
        {
            ArrayList<Uri> filesToShare = new ArrayList<>();
            filesToShare.add(FileProvider.getUriForFile(getContext(), "nava.metagram", new File(url)));

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getActivity().startActivity(Intent.createChooser(shareIntent, ""));
        });
    }


}
