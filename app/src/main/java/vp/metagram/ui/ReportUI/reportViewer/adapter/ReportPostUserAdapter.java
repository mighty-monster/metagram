package vp.metagram.ui.ReportUI.reportViewer.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;


import vp.metagram.R;
import vp.metagram.ui.AccBrowser.AccImageView;
import vp.metagram.ui.AccBrowser.AccntBrowserActivity;
import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.ui.ReportUI.reportViewer.ReportType;
import vp.metagram.ui.ReportUI.reportViewer.types.ReportPostUser;
import vp.metagram.utils.DownloadService;
import vp.metagram.utils.instagram.types.UserFull;

import static android.content.Context.CLIPBOARD_SERVICE;
import static vp.metagram.general.functions.arrayListToStringArray;
import static vp.metagram.general.functions.decideUsingAPICookies;
import static vp.metagram.general.functions.getInstagramImageLink_Medium;
import static vp.metagram.general.functions.openPostPageOnInstagram;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;


public class ReportPostUserAdapter extends RecyclerView.Adapter<ReportPostUserAdapter.ViewHolder>
{
    Context context;
    List<ReportPostUser> itemsList;
    Runnable onClickRunnable;

    String infoTitle;
    String username;
    ReportType reportType;

    boolean isPrivate;

    long IPK;

    DownloadService downloadService;

    public ReportPostUserAdapter(List<ReportPostUser> itemsList, Context context, Runnable onClickRunnable, boolean isPrivate, long IPK, String username, ReportType reportType)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.onClickRunnable = onClickRunnable;
        this.isPrivate = isPrivate;
        this.IPK = IPK;
        this.username = username;
        this.reportType = reportType;

        downloadService = new DownloadService(context);
    }


    public void setDataSet(List<ReportPostUser> itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    @Override
    public ReportPostUserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instagram_post_account, parent, false);
        return new ReportPostUserAdapter.ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ReportPostUserAdapter.ViewHolder holder, int position)
    {

        ReportPostUser listItem = itemsList.get(position);

        String postImageLink = getInstagramImageLink_Medium(listItem.miniLink);

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


        holder.postImage.setOnClickListener((View v)->
        {
            Intent intent = new Intent(context, MediaViewerActivity.class);
            intent.putExtra("urls",arrayListToStringArray(listItem.urls));
            intent.putExtra("showDownload",true);
            context.startActivity(intent);
        });

        holder.username.setText(listItem.username);
        setTextViewFontForMessage(context,holder.username);

        Picasso.with(context).load(listItem.accountPicUrl)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_long_touch)).into(holder.profileImage);

        View.OnClickListener onClickListener = (View v)->
        {
            Intent intent = new Intent(context, AccntBrowserActivity.class);
            intent.putExtra("username",listItem.username);
            intent.putExtra("picURL", listItem.accountPicUrl);
            intent.putExtra("ipk",listItem.accountIPK);
            intent.putExtra("isPrivate",true);

            context.startActivity(intent);
        };

        holder.profileImage.setOnClickListener(onClickListener);

        setTextViewFontForMessage(context,holder.comment);

        if (reportType == ReportType.removedComments || reportType == ReportType.commentedButDidNotFollow || reportType == ReportType.someonesComments)
        {
            if (listItem.comment == null || listItem.comment.equals(""))
            {
                listItem.comment = "Comment";
                holder.comment.setTextColor(context.getResources().getColor(R.color.sCoolGrayC5));
            }
            else
            {
                holder.comment.setTextColor(context.getResources().getColor(R.color.sBlack));
            }

            holder.comment.setText(listItem.comment);

            holder.comment.setOnLongClickListener((View v)->
            {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("",  holder.comment.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context,context.getResources().getString(R.string.copyToClipboard_comment), Toast.LENGTH_SHORT).show();

                return true;
            });
        }
        else if (reportType == ReportType.removedLikes || reportType == ReportType.likedButDidNotFollow || reportType == ReportType.someonesLikes)
        {
            if (listItem.caption == null || listItem.caption.equals(""))
            {
                listItem.caption = "Caption";
                holder.comment.setTextColor(context.getResources().getColor(R.color.sCoolGrayC5));

            }

            holder.comment.setText(listItem.caption);
            holder.comment.setOnLongClickListener((View v)->
            {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("",  holder.comment.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context,context.getResources().getString(R.string.copyToClipboard_caption), Toast.LENGTH_SHORT).show();

                return true;
            });
        }

        holder.postImage.setOnLongClickListener((View v)->
        {
            openPostPageOnInstagram(context, listItem.miniLink);
            return true;
        });

        holder.profileImage.setOnLongClickListener((View v)->
        {
            Activity activity = (Activity)context;

            ConnectingDialog connectingDialog = ConnectingDialog.newInstance(activity.getResources().getString(R.string.accntBrowser_refreshingInfo));
            connectingDialog.show(activity.getFragmentManager(), "");

            threadPoolExecutor.execute(()->
            {
                try
                {
                    UserFull user = holder.profileImage.Refresh(listItem.username);
                    if (user!= null )
                    {
                        activity.runOnUiThread(() ->
                        {

                            listItem.accountPicUrl = user.picURL;
                            listItem.username = user.username;


                            Picasso.with(context).load(user.picURL)
                                    .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                                    .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.profileImage);

                        });
                    }
                }
                finally
                {
                    connectingDialog.dismiss();
                }

            });

            //openAccountPageOnInstagram(context, listItem.username);
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
        public ImageView postImage;
        public AccImageView profileImage;
        public TextView username;
        public TextView comment;

        public ViewHolder(View itemView)
        {
            super(itemView);

            postImage = itemView.findViewById(R.id.postImage);
            profileImage = itemView.findViewById(R.id.profileImage);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }

    }
}
