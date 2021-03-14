package vp.metagram.ui.AccBrowser.CommentViewer;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vp.metagram.R;
import vp.metagram.ui.AccBrowser.AccImageView;
import vp.metagram.ui.AccBrowser.AccntBrowserActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.utils.instagram.types.Comment;
import vp.metagram.utils.instagram.types.UserFull;

import static android.content.Context.CLIPBOARD_SERVICE;
import static vp.metagram.general.functions.setHTMLForTextView;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.threadPoolExecutor;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>
{
    Context context;
    Map<Long, Comment> itemsList;

    List<Comment> showList;

    public CommentAdapter(Map<Long, Comment> itemsList, Context context)
    {
        this.itemsList = itemsList;
        this.context = context;

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

    public void setDataSet(Map<Long, Comment> itemsList)
    {
        this.itemsList = itemsList;
        showList = new ArrayList<>(itemsList.values());
        notifyDataSetChanged();
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instagram_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentAdapter.ViewHolder holder, int position)
    {
        Comment listItem = showList.get(position);

        Picasso.with(context).load(listItem.commenter.picURL)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.profileImage);

        setTextViewFontForMessage(context, holder.message);

        String content = String.format("<b>%s:</b><br>%s",listItem.commenter.username,listItem.message);
        setHTMLForTextView(holder.message,content);

        setTextViewFontForMessage(context, holder.noOfLikes);
        content = String.format("<b>Likes:</b> %s",listItem.noOfLikes);
        setHTMLForTextView(holder.noOfLikes,content);

        holder.profileImage.setOnClickListener((View v)->
        {
            Intent intent = new Intent(context, AccntBrowserActivity.class);
            intent.putExtra("username",listItem.commenter.username);
            intent.putExtra("picURL", listItem.commenter.picURL);
            intent.putExtra("ipk",listItem.commenter.IPK);
            intent.putExtra("isPrivate",listItem.commenter.isPrivate);

            context.startActivity(intent);
        });

        holder.message.setOnLongClickListener((View v)->
        {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("",  holder.message.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context,context.getResources().getString(R.string.copyToClipboard_comment), Toast.LENGTH_SHORT).show();

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
                    UserFull user = holder.profileImage.Refresh(listItem.commenter.username);
                    if (user!= null )
                    {
                        activity.runOnUiThread(() ->
                        {

                            listItem.commenter.picURL = user.picURL;
                            listItem.commenter.username = user.username;
                            listItem.commenter.fullname = user.fullname;


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


            //openAccountPageOnInstagram(context, listItem.commenter.username);
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

        AccImageView profileImage;
        TextView message;
        TextView noOfLikes;

        public ViewHolder(View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            message = itemView.findViewById(R.id.message);
            noOfLikes = itemView.findViewById(R.id.noOfLikes);
        }
    }
}
