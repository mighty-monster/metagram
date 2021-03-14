package vp.metagram.ui.AccBrowser.MediaViewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import vp.metagram.R;

import static vp.metagram.general.variables.metagramAgent;

public class MediaViewerImageFragment extends Fragment
{
    View rootLayout;
    ImageView imageView;

    ImageView shareButton;

    String url;
    boolean isLocal;

    static public MediaViewerImageFragment newInstance(String url, boolean isLocal)
    {
        MediaViewerImageFragment imageFragment = new MediaViewerImageFragment();
        imageFragment.url = url;
        imageFragment.isLocal = isLocal;

        return imageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            url = savedInstanceState.getString("url");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.layout_image_viewer, container, false);
        prepareUIElements();
        return rootLayout;
    }

    @Override
    public void  onResume()
    {
        super.onResume();

        if (isLocal)
        {
            File imageFile = new File(url);

            Picasso.with(getContext()).load(imageFile)
                    .placeholder(getContext().getResources().getDrawable(R.drawable.ic_download_from_net))
                    .error(getContext().getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(imageView);
        }
        else
        {
            if (metagramAgent.activeAgent != null)
            {
                metagramAgent.activeAgent.picasso.load(url)
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.ic_download_from_net))
                        .error(getActivity().getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(imageView);
            }
            else
            {
                Picasso.with(getContext()).load(url)
                        .placeholder(getActivity().getResources().getDrawable(R.drawable.ic_download_from_net))
                        .error(getActivity().getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(imageView);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString("url",url);
    }

    public void prepareUIElements()
    {
        imageView = rootLayout.findViewById(R.id.imageView);
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
