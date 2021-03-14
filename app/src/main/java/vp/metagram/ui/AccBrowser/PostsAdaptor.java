package vp.metagram.ui.AccBrowser;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import vp.metagram.R;
import vp.metagram.ui.AccBrowser.CommentViewer.CommentViewerActivity;
import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;
import vp.metagram.utils.DownloadService;
import vp.metagram.utils.instagram.types.PostMedia;

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


public class PostsAdaptor extends RecyclerView.Adapter<PostsAdaptor.ViewHolder>
{

    String username;

    Context context;
    Map<Long, PostMedia> itemsList;
    Runnable onClickRunnable;

    boolean isPrivate;
    long IPK;

    DownloadService downloadService;

    List<PostMedia> showList;

    public PostsAdaptor(Map<Long, PostMedia> itemsList, Context context, Runnable onClickRunnable, boolean isPrivate, long IPK, String username)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.onClickRunnable = onClickRunnable;
        this.isPrivate = isPrivate;
        this.IPK = IPK;
        this.username = username;

        downloadService = new DownloadService(context);

        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onChanged()
            {
                showList = new ArrayList<>(itemsList.values());
                super.onChanged();
            }
        });
    }

    public void setDataSet(Map<Long, PostMedia> itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instagram_post, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        PostMedia listItem = showList.get(position);

        String postImageLink = listItem.picURL;

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

        holder.likeTextView.setText(String.format(Locale.ENGLISH,"%,d Likes", listItem.likeCount));
        holder.likeTextView.setOnClickListener((View v)->
        {
            Intent intent = new Intent(context, AccntLikesListActivity.class);
            intent.putExtra("short_code",listItem.miniLink);
            context.startActivity(intent);
        });

        holder.commentTextView.setText(String.format(Locale.ENGLISH,"%,d Comments", listItem.commentCount));


        if (listItem.caption == null || listItem.caption.equals(""))
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

            downloadPost(context, listItem.urls, username, listItem.ID, downloadService, "Post");
        });

        holder.postImage.setOnClickListener((View v)->
        {
            Intent intent = new Intent(context, MediaViewerActivity.class);
            intent.putExtra("urls", arrayListToStringArray(listItem.urls));
            context.startActivity(intent);
        });

        holder.shareButton.setOnClickListener((View v)->
        {
            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_share_black);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            sharePost(context, listItem.urls);
        });

        holder.commentTextView.setOnClickListener((View v)->
        {
            Intent intent = new Intent(context,CommentViewerActivity.class);
            intent.putExtra("Caption",listItem.caption);
            intent.putExtra("short_code",listItem.miniLink);
            intent.putExtra("username",username);
            context.startActivity(intent);
        });

        holder.captionTextView.setOnLongClickListener((View v)->
        {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("",  holder.captionTextView.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context,context.getResources().getString(R.string.copyToClipboard_caption), Toast.LENGTH_SHORT).show();

            return true;
        });

        holder.postImage.setOnLongClickListener((View v)->
        {
            if (!isPrivate) {openPostPageOnInstagram(context, listItem.miniLink);}

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
        TextView likeTextView;
        TextView commentTextView;
        ImageButton saveButton;
        ImageButton shareButton;
        ImageView postType;

        public ViewHolder(View itemView)
        {
            super(itemView);

            postImage = itemView.findViewById(R.id.postImage);
            captionTextView = itemView.findViewById(R.id.captionTextView);
            likeTextView = itemView.findViewById(R.id.likeTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            shareButton = itemView.findViewById(R.id.shareButton);
            saveButton = itemView.findViewById(R.id.saveButton);
            postType = itemView.findViewById(R.id.postType);

        }
    }
}
