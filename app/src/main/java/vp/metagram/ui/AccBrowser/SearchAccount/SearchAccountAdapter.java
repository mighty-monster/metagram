package vp.metagram.ui.AccBrowser.SearchAccount;

import android.content.Context;

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

import de.hdodenhof.circleimageview.CircleImageView;

import vp.metagram.R;
import vp.metagram.utils.instagram.types.User;

import static vp.metagram.general.functions.setTextViewFontForMessage;


public class SearchAccountAdapter extends RecyclerView.Adapter<SearchAccountAdapter.ViewHolder>
{

    Context context;
    List<User> itemsList;
    Runnable confirmRunnable;

    public SearchAccountAdapter(List<User> itemsList, Context context, Runnable confirmRunnable)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.confirmRunnable = confirmRunnable;

    }

    public void setDataSet(List<User> itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instagram_account_robot, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        User listItem = itemsList.get(position);


        holder.username.setText(listItem.username);
        setTextViewFontForMessage(context,holder.username);


        holder.fullname.setText(listItem.fullname);
        setTextViewFontForMessage(context,holder.fullname);

        Picasso.with(context).load(listItem.picURL)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.profileImage);

        View.OnClickListener onClickListener = (View v)->
        {

        };

        holder.privateIcon.setVisibility(View.INVISIBLE);

        holder.rootLayout.setOnClickListener(onClickListener);

        holder.profileImage.setOnClickListener(onClickListener);

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
        public CircleImageView profileImage;
        public TextView fullname;

        public ViewHolder(View itemView)
        {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);
            privateIcon = itemView.findViewById(R.id.privateImageView);
            orderIcon = itemView.findViewById(R.id.orderImageButton);
            infoImage = itemView.findViewById(R.id.infoImageView);
            fullname = itemView.findViewById(R.id.fullname);
        }
    }
}
