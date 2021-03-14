package vp.metagram.ui.AccBrowser.DownloadStory.igtv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import vp.igwa.IGTVList;
import vp.igwa.IGTVSummery;
import vp.metagram.R;
import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.utils.DownloadService;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.igpapi.IGWAException;

import static android.os.Looper.getMainLooper;
import static vp.metagram.general.functions.arrayListToStringArray;
import static vp.metagram.general.functions.convertDrawableToBlue;
import static vp.metagram.general.functions.downloadPost;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.functions.sharePost;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;

public class IGTVAdapter extends RecyclerView.Adapter<IGTVAdapter.ViewHolder>
{
    Context context;
    IGTVList itemsList;

    String username;

    DownloadService downloadService;

    public IGTVAdapter(IGTVList itemsList, Context context, String username)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.username = username;

        downloadService = new DownloadService(context);

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_instagram_igtv, viewGroup, false);
        return new IGTVAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i)
    {
        IGTVSummery igtv = itemsList.IGTVs.get(i);

        Picasso.with(context).load(igtv.display_url)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.image);

        holder.title.setText(igtv.caption);
        setTextViewFontForMessage(context, holder.title);

        holder.image.setOnClickListener((View v)->
        {
            ConnectingDialog connectingDialog = ConnectingDialog.newInstance(context.getResources().getString(R.string.login_connectMessage));
            connectingDialog.show(((Activity)context).getFragmentManager(), "");

            threadPoolExecutor.execute(()->
            {
                try
                {
                    PostMedia media = getIGTV(igtv.short_code);
                    new Handler(getMainLooper()).post(() ->
                    {
                        Intent intent = new Intent(context, MediaViewerActivity.class);
                        intent.putExtra("urls",arrayListToStringArray(media.urls));
                        context.startActivity(intent);
                    });
                }
                catch (JSONException | NoSuchAlgorithmException | IOException | IGWAException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    new Handler(getMainLooper()).post(connectingDialog::close);
                }
            });


        });

        holder.saveButton.setImageResource(R.drawable.ic_download);
        holder.saveButton.setOnClickListener((View v)->
        {
            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_download);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            ConnectingDialog connectingDialog = ConnectingDialog.newInstance(context.getResources().getString(R.string.login_connectMessage));
            connectingDialog.show(((Activity)context).getFragmentManager(), "");

            threadPoolExecutor.execute(()->
            {
                try
                {
                    PostMedia media = getIGTV(igtv.short_code);
                    new Handler(getMainLooper()).post(() ->
                            downloadPost(context, media.urls,username, media.ID, downloadService, "IGTV" ));
                }
                catch (JSONException | NoSuchAlgorithmException | IOException | IGWAException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    new Handler(getMainLooper()).post(connectingDialog::close);
                }
            });

        });

        holder.shareButton.setOnClickListener((View v)->
        {
            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_share_black);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            ConnectingDialog connectingDialog = ConnectingDialog.newInstance(context.getResources().getString(R.string.login_connectMessage));
            connectingDialog.show(((Activity)context).getFragmentManager(), "");

            threadPoolExecutor.execute(()->
            {
                try
                {
                    PostMedia media = getIGTV(igtv.short_code);
                    new Handler(getMainLooper()).post(() ->
                            sharePost(context, media.urls));
                }
                catch (JSONException | NoSuchAlgorithmException | IOException | IGWAException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    new Handler(getMainLooper()).post(connectingDialog::close);
                }
            });

        });


    }


    public PostMedia getIGTV(String short_code) throws JSONException, NoSuchAlgorithmException, IGWAException, IOException
    {
        PostMedia igtv = metagramAgent.activeAgent.api.media_info(short_code, new PostMedia());

        return igtv;
    }

    @Override
    public int getItemCount()
    {
        return itemsList.IGTVs.size();
    }

    public void setDataSet(IGTVList itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ConstraintLayout rootLayout;
        public ImageView image;
        public TextView title;
        public ImageButton saveButton;
        public ImageButton shareButton;


        public ViewHolder(View itemView)
        {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            image = itemView.findViewById(R.id.highlightImage);
            title = itemView.findViewById(R.id.captionTextView);
            saveButton = itemView.findViewById(R.id.saveButton);
            shareButton = itemView.findViewById(R.id.shareButton);
        }
    }
}
