package vp.metagram.ui.ReportUI.reportViewer.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;


import vp.metagram.R;
import vp.metagram.ui.AccBrowser.AccntLikesListActivity;
import vp.metagram.ui.AccBrowser.CommentViewer.CommentViewerActivity;
import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;
import vp.metagram.ui.ReportUI.reportViewer.types.ReportMedia;
import vp.metagram.utils.DownloadService;

import static android.content.Context.CLIPBOARD_SERVICE;
import static vp.metagram.general.functions.arrayListToStringArray;
import static vp.metagram.general.functions.convertDrawableToBlue;
import static vp.metagram.general.functions.decideUsingAPICookies;
import static vp.metagram.general.functions.downloadPost;
import static vp.metagram.general.functions.getInstagramImageLink_Medium;
import static vp.metagram.general.functions.openPostPageOnInstagram;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.functions.sharePost;
import static vp.metagram.general.variables.metagramAgent;


public class ReportPostsAdapter extends RecyclerView.Adapter<ReportPostsAdapter.ViewHolder>
{

    Context context;
    List<ReportMedia> itemsList;
    Runnable onClickRunnable;

    String infoTitle;
    String username;

    boolean isPrivate;

    long IPK;

    DownloadService downloadService;

    public ReportPostsAdapter(List<ReportMedia> itemsList, Context context, Runnable onClickRunnable, String infoTitle, boolean isPrivate, long IPK, String username)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.onClickRunnable = onClickRunnable;
        this.infoTitle = infoTitle;
        this.isPrivate = isPrivate;
        this.IPK = IPK;
        this.username = username;

        downloadService = new DownloadService(context);
    }


    public void setDataSet(List<ReportMedia> itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instagram_post_report, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        ReportMedia listItem = itemsList.get(position);
        String postImageLink = listItem.PicURL;

        if (decideUsingAPICookies(metagramAgent,isPrivate,IPK))
        {
            metagramAgent.activeAgent.picasso.load(postImageLink)
                    .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                    .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.postImage);
        }
        else
        {
            Picasso.with(context).load(postImageLink)
                    .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                    .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.postImage);
        }

        if (infoTitle.equals("Likes"))
        {
            holder.infoTextView.setText(String.format(Locale.ENGLISH,"%,d Likes", listItem.likeNo));
            holder.infoTextView.setOnClickListener((View v)->
            {
                Intent intent = new Intent(context, AccntLikesListActivity.class);
                intent.putExtra("short_code",listItem.miniLink);
                context.startActivity(intent);
            });

            holder.infoImageView.setImageResource(R.drawable.ic_like);
        }
        else if (infoTitle.equals("Comments"))
        {
            holder.infoTextView.setText(String.format(Locale.ENGLISH,"%,d Comments", listItem.commentNo));

            holder.infoTextView.setOnClickListener((View v)->
            {
                Intent intent = new Intent(context,CommentViewerActivity.class);
                intent.putExtra("Caption",listItem.caption);
                intent.putExtra("short_code",listItem.miniLink);
                intent.putExtra("username",username);
                context.startActivity(intent);
            });

            holder.infoImageView.setImageResource(R.drawable.ic_comment);
        }
        else if (infoTitle.equals("Views"))
        {
            holder.infoTextView.setText(String.format(Locale.ENGLISH,"%,d Views", listItem.viewNo));


            holder.infoImageView.setImageResource(R.drawable.ic_view);
        }


        if (listItem.caption == null || listItem.caption.equals("") || listItem.caption.equals("null"))
        {
            holder.captionTextView.setText("Caption");
            setTextViewFontForMessage(context, holder.captionTextView);
            holder.captionTextView.setTextColor(context.getResources().getColor(R.color.sCoolGrayC3));
        }
        else
        {
            holder.captionTextView.setText(listItem.caption);
            setTextViewFontForMessage(context, holder.captionTextView);
            holder.captionTextView.setTextColor(context.getResources().getColor(R.color.sBlack));
        }

        holder.captionTextView.setOnLongClickListener((View v)->
        {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("",  holder.captionTextView.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context,context.getResources().getString(R.string.copyToClipboard_caption), Toast.LENGTH_SHORT).show();

            return true;
        });


        holder.postType.bringToFront();
        holder.postType.setVisibility(View.GONE);
        if (listItem.type == 1)
        {
            holder.postType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_post_type_image));
            holder.postType.setVisibility(View.VISIBLE);
        }
        else if (listItem.type == 8)
        {
            holder.postType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_post_type_slide));
            holder.postType.setVisibility(View.VISIBLE);
        }
        else if (listItem.type == 2)
        {
            holder.postType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_post_type_movie));
            holder.postType.setVisibility(View.VISIBLE);
        }

        holder.saveButton.setImageResource(R.drawable.ic_download);
        holder.saveButton.setOnClickListener((View v)->
        {

            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_download);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            downloadPost(context, listItem.urls, username, listItem.MID, downloadService, "Post");
        });

        holder.shareButton.setOnClickListener((View v)->
        {
            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_share_black);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            sharePost(context, listItem.urls);
        });

        holder.postImage.setOnClickListener((View v)->
        {
            Intent intent = new Intent(context, MediaViewerActivity.class);
            intent.putExtra("urls",arrayListToStringArray(listItem.urls));
            intent.putExtra("showDownload",true);
            context.startActivity(intent);
        });

        holder.postImage.setOnLongClickListener((View v)->
        {
            openPostPageOnInstagram(context, listItem.miniLink);
            return true;
        });

    }

    @Override
    public int getItemCount()
    {
        return itemsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        ImageView postImage;
        TextView captionTextView;
        TextView infoTextView;
        ImageButton saveButton;
        ImageButton shareButton;
        ImageView postType;
        ImageView infoImageView;

        public ViewHolder(View itemView)
        {
            super(itemView);

            postImage = itemView.findViewById(R.id.postImage);
            captionTextView = itemView.findViewById(R.id.captionTextView);
            infoTextView = itemView.findViewById(R.id.infoTextView);
            infoImageView = itemView.findViewById(R.id.infoImageView);
            shareButton = itemView.findViewById(R.id.shareButton);
            saveButton = itemView.findViewById(R.id.saveButton);
            postType = itemView.findViewById(R.id.postType);

        }

    }
}
