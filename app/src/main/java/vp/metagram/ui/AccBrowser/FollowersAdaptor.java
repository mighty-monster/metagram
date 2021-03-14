package vp.metagram.ui.AccBrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import vp.metagram.R;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.utils.instagram.types.User;
import vp.metagram.utils.instagram.types.UserFull;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.threadPoolExecutor;


public class FollowersAdaptor extends RecyclerView.Adapter<FollowersAdaptor.ViewHolder>
{

    Context context;
    Map<Long,User> itemsList;
    Runnable onClickRunnable;

    List<User> showList;


    public FollowersAdaptor(Map<Long,User> itemsList, Context context, Runnable onClickRunnable)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.onClickRunnable = onClickRunnable;

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

    public void setDataSet(Map<Long,User> itemsList)
    {
        this.itemsList = itemsList;
        showList = new ArrayList<>(itemsList.values());
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instagram_account, parent, false);
        return new ViewHolder(v);
    }




    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        User listItem = showList.get(position);

        holder.username.setText(listItem.username);
        setTextViewFontForMessage(context,holder.username);

        holder.fullname.setText(listItem.fullname);
        setTextViewFontForMessage(context,holder.username);

        Picasso.with(context).load(listItem.picURL)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_long_touch)).into(holder.profileImage);

        View.OnClickListener onClickListener = (View v)->
        {
            Intent intent = new Intent(context, AccntBrowserActivity.class);
            intent.putExtra("username",listItem.username);
            intent.putExtra("picURL", listItem.picURL);
            intent.putExtra("ipk",listItem.IPK);
            intent.putExtra("isPrivate",listItem.isPrivate);

            context.startActivity(intent);
        };

        holder.rootLayout.setOnClickListener(onClickListener);

        holder.profileImage.setOnClickListener(onClickListener);

        if (listItem.isPrivate)
        {
            holder.privateIcon.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.privateIcon.setVisibility(View.INVISIBLE);
        }

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

                            listItem.picURL = user.picURL;
                            listItem.username = user.username;
                            listItem.fullname = user.fullname;


                            Picasso.with(context).load(user.picURL)
                                    .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                                    .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.profileImage);

                            holder.fullname.setText(user.username);
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
        public TextView fullname;
        public ImageView privateIcon;
        public ImageButton orderIcon;

        public AccImageView profileImage;

        public ViewHolder(View itemView)
        {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);
            fullname = itemView.findViewById(R.id.fullname);
            privateIcon = itemView.findViewById(R.id.privateImageView);
            orderIcon = itemView.findViewById(R.id.orderImageButton);
        }
    }
}
