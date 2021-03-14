package vp.metagram.ui.AccBrowser.DownloadStory.highlight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import vp.metagram.R;
import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.utils.DownloadService;
import vp.metagram.utils.instagram.types.HighlightItem;

import static android.os.Looper.getMainLooper;
import static vp.metagram.general.functions.arrayListToStringArray;
import static vp.metagram.general.functions.arrayMediaToStringArrayList;
import static vp.metagram.general.functions.convertDrawableToBlue;
import static vp.metagram.general.functions.downloadPost;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.functions.sharePost;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.ViewHolder>
{
    Context context;
    List<HighlightItem> itemsList;

    String username;

    DownloadService downloadService;

    public HighlightAdapter(List<HighlightItem> itemsList, Context context, String username)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.username = username;

        downloadService = new DownloadService(context);

    }

    public void setDataSet(List<HighlightItem> itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    @Override
    public HighlightAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_instagram_highlight, viewGroup, false);
        return new HighlightAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i)
    {
        HighlightItem item = itemsList.get(i);

        Picasso.with(context).load(item.CoverURL)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.image);

        holder.title.setText(item.title);
        setTextViewFontForMessage(context, holder.title);

        holder.mediaCount.setText(String.format("Items: %d", item.mediaCount));
        setTextViewFontForMessage(context, holder.mediaCount);
        holder.mediaCount.setVisibility(View.GONE);

        View.OnClickListener openHighlight = (View v)->
        {
            if (item.itemType == HighlightItemType.Story)
            {
                Intent intent = new Intent(context, MediaViewerActivity.class);
                intent.putExtra("urls",arrayListToStringArray(arrayMediaToStringArrayList(item.mediaList)));
                context.startActivity(intent);
            }
            else
            {
                getHighlightDetails(item.id,"open");
            }
        };

        holder.image.setOnClickListener(openHighlight);
        holder.rootLayout.setOnClickListener(openHighlight);

        holder.saveButton.setImageResource(R.drawable.ic_download);
        holder.saveButton.setOnClickListener((View v)->
        {
            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_download);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            if (item.itemType == HighlightItemType.Story)
            {
                downloadPost(context, arrayMediaToStringArrayList(item.mediaList),username, String.format(Locale.ENGLISH,"%d",System.currentTimeMillis()), downloadService, "Story" );
            }
            else
            {
                getHighlightDetails(item.id,"save");
            }
        });



        holder.shareButton.setOnClickListener((View v)->
        {
            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_share_black);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            if (item.itemType == HighlightItemType.Story)
            {
                sharePost(context, arrayMediaToStringArrayList(item.mediaList));
            }
            else
            {
                getHighlightDetails(item.id,"share");
            }
        });
    }

    boolean successes;
    public void getHighlightDetails(String ID, String action)
    {
        Activity activity = (Activity) context;

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {return;}

        successes = true;

        ConnectingDialog connectingDialog = ConnectingDialog.newInstance(activity.getResources().getString(R.string.login_connectMessage));
        connectingDialog.show(activity.getFragmentManager(), "");
        threadPoolExecutor.execute(()->
        {
            HighlightItem highlightItem = null;
            try
            {
                highlightItem = metagramAgent.activeAgent.getHighlightsDetail(ID);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                successes = false;
            }
            finally
            {
                activity.runOnUiThread(()->connectingDialog.close());

                if (highlightItem != null)
                {
                    HighlightItem finalHighlightItem = highlightItem;
                    activity.runOnUiThread(()->
                    {

                        if (action.equals("open"))
                        {
                            Intent intent = new Intent(context, MediaViewerActivity.class);
                            intent.putExtra("urls",arrayListToStringArray(arrayMediaToStringArrayList(finalHighlightItem.mediaList)));
                            intent.putExtra("showDownload",true);
                            context.startActivity(intent);
                        }
                        else if (action.equals("share"))
                        {
                            sharePost(context, arrayMediaToStringArrayList(finalHighlightItem.mediaList));
                        }
                        else if (action.equals("save"))
                        {
                            downloadPost(context, arrayMediaToStringArrayList(finalHighlightItem.mediaList),username, finalHighlightItem.id, downloadService, "Highlight" );
                        }
                    });
                }
                else
                {
                    new Handler(getMainLooper()).post(() ->
                    {
                        InformationDialog dialog = new InformationDialog();
                        dialog.showDialog(activity,
                                activity.getString(R.string.accntBrowser_downloadTitle),
                                activity.getString(R.string.storyDownloader_downloadHighlightError),
                                activity.getString(R.string.button_ok), null);
                    });
                }

            }
        });
    }




    @Override
    public int getItemCount()
    {
        return itemsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ConstraintLayout rootLayout;
        public CircleImageView image;
        public TextView title;
        public TextView mediaCount;
        public ImageButton saveButton;
        public ImageButton shareButton;

        public ViewHolder(View itemView)
        {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            image = itemView.findViewById(R.id.highlightImage);
            title = itemView.findViewById(R.id.captionTextView);
            mediaCount = itemView.findViewById(R.id.mediaCountTextView);
            saveButton = itemView.findViewById(R.id.saveButton);
            shareButton = itemView.findViewById(R.id.shareButton);

        }
    }
}
