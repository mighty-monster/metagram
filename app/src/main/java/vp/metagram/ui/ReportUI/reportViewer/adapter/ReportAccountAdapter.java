package vp.metagram.ui.ReportUI.reportViewer.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;


import vp.metagram.R;
import vp.metagram.ui.AccBrowser.AccImageView;
import vp.metagram.ui.AccBrowser.AccntBrowserActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.ui.ReportUI.reportViewer.ReportType;
import vp.metagram.ui.ReportUI.reportViewer.ReportViewerActivity;
import vp.metagram.ui.ReportUI.reportViewer.types.ReportUser;
import vp.metagram.utils.instagram.types.UserFull;

import static android.os.Looper.getMainLooper;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.someonesComments;
import static vp.metagram.ui.ReportUI.reportViewer.ReportType.someonesLikes;


public class ReportAccountAdapter extends RecyclerView.Adapter<ReportAccountAdapter.ViewHolder>
{

    Context context;
    List<ReportUser> itemsList;
    ReportType reportType;

    int noOfOthers;

    long IPK;

    public ReportAccountAdapter(List<ReportUser> itemsList, Context context, ReportType reportType, long IPK)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.reportType = reportType;
        this.IPK = IPK;
    }

    public void setDataSet(List<ReportUser> itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instagram_account_report, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        ReportUser listItem = itemsList.get(position);


        holder.username.setText(listItem.username);
        setTextViewFontForMessage(context,holder.username);


        Picasso.with(context).load(listItem.picUrl)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_long_touch)).into(holder.profileImage);

        View.OnClickListener onClickListener = (View v)->
        {
            Intent intent = new Intent(context, AccntBrowserActivity.class);
            intent.putExtra("username",listItem.username);
            intent.putExtra("picURL", listItem.picUrl);
            intent.putExtra("ipk",listItem.IPK);
            intent.putExtra("isPrivate",true);

            context.startActivity(intent);
        };

        setTextViewFontForMessage(context,holder.infoTextView);

        switch (reportType)
        {
            case peopleLikedMost:
                View.OnClickListener likesOnClickListener = (View v) ->
                {
                    Intent intent = new Intent(context, ReportViewerActivity.class);
                    intent.putExtra("reportID", someonesLikes.ordinal);
                    intent.putExtra("IPK", IPK);
                    intent.putExtra("username", listItem.username);
                    intent.putExtra("isPrivate", true);
                    intent.putExtra("OIPK", listItem.IPK);

                    context.startActivity(intent);
                };
                holder.infoImage.setImageResource(R.drawable.ic_like);
                holder.infoImage.setVisibility(View.VISIBLE);
                holder.infoTextView.setText(String.format(Locale.ENGLISH, "%,d", listItem.likeNo));
                holder.infoImage.setOnClickListener(likesOnClickListener);
                holder.infoTextView.setOnClickListener(likesOnClickListener);
                break;
            case peopleCommentedMost:
                View.OnClickListener commentsOnClickListener = (View v) ->
                {
                    Intent intent = new Intent(context, ReportViewerActivity.class);
                    intent.putExtra("reportID", someonesComments.ordinal);
                    intent.putExtra("IPK", IPK);
                    intent.putExtra("username", listItem.username);
                    intent.putExtra("isPrivate", true);
                    intent.putExtra("OIPK", listItem.IPK);

                    context.startActivity(intent);
                };
                holder.infoImage.setImageResource(R.drawable.ic_comment);
                holder.infoImage.setVisibility(View.VISIBLE);
                holder.infoTextView.setText(String.format(Locale.ENGLISH, "%,d", listItem.commentNo));
                holder.infoImage.setOnClickListener(commentsOnClickListener);
                holder.infoTextView.setOnClickListener(commentsOnClickListener);
                break;
            case peopleEngagedMost:
                holder.infoImage.setImageResource(R.drawable.ic_engage);
                holder.infoImage.setVisibility(View.VISIBLE);
                holder.infoTextView.setText(String.format(Locale.ENGLISH, "%,d", listItem.engageNo));
                break;
            case notFollowedBack:
                //holder.infoImage.setImageResource(R.drawable.ic_not_followed_back);
                //holder.infoImage.setVisibility(View.VISIBLE);
                break;
            case notFollowingBack:
                //holder.infoImage.setImageResource(R.drawable.ic_not_following_back);
                //holder.infoImage.setVisibility(View.VISIBLE);
                break;
            case lazyFollowers:
                holder.infoImage.setImageResource(R.drawable.ic_lazy_followers);
                holder.infoImage.setVisibility(View.VISIBLE);

                threadPoolExecutor.execute(()->
                {
                    String sqlText = String.format(Locale.ENGLISH, "Select FollowingIPK From Rel_Following Where FollowingIPK = %d and FIPK = %d" ,listItem.IPK,IPK);
                    try
                    {
                        MatrixCursor result = dbMetagram.selectQuery(sqlText);
                        if (result.moveToFirst())
                        {
                            new android.os.Handler(getMainLooper()).post(()->
                                    holder.infoImage.setImageResource(R.drawable.ic_lazy_followers_red));
                        }
                        else
                        {
                            new android.os.Handler(getMainLooper()).post(()->
                                    holder.infoImage.setOnClickListener(null));
                        }
                    }
                    catch (Exception e)
                    { e.printStackTrace(); }

                });

                break;
        }


        holder.rootLayout.setOnClickListener(onClickListener);

        holder.profileImage.setOnClickListener(onClickListener);

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

                            listItem.picUrl = user.picURL;
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

        public ConstraintLayout rootLayout;
        public TextView username;
        public ImageView privateIcon;
        public ImageButton orderIcon;
        public ImageView infoImage;
        public AccImageView profileImage;
        public TextView infoTextView;

        public ViewHolder(View itemView)
        {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);
            privateIcon = itemView.findViewById(R.id.privateImageView);
            orderIcon = itemView.findViewById(R.id.orderImageButton);
            infoImage = itemView.findViewById(R.id.infoImageView);
            infoTextView = itemView.findViewById(R.id.infoTextView);
        }
    }
}
