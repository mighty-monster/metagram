package vp.metagram.ui.ReportUI;

import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import me.grantland.widget.AutofitTextView;
import vp.metagram.R;
import vp.metagram.utils.instagram.types.User;

import static vp.metagram.general.functions.setTextViewFontArvoBold;


public class AddOrderAdaptor extends RecyclerView.Adapter<AddOrderAdaptor.ViewHolder>
{

    Context context;
    List<User> itemsList;
    Runnable onClickRunnable;
    AddOrderActivity addOrderActivity;

    public AddOrderAdaptor(List<User> itemsList, Context context, Runnable onClickRunnable, AddOrderActivity addOrderActivity)
    {
        this.itemsList = itemsList;
        this.context = context;
        this.onClickRunnable = onClickRunnable;
        this.addOrderActivity = addOrderActivity;
    }

    public void setDataSet(List<User> itemsList)
    {
        this.itemsList = itemsList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_add_order_search, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        User listItem = itemsList.get(position);

        holder.accountTitle.setText(listItem.username);
        setTextViewFontArvoBold(context, holder.accountTitle);

        setTextViewFontArvoBold(context, holder.followersTitle);
        //holder.followersCount.setText(String.format(Locale.ENGLISH,"%,d",listItem.followerCount));
        setTextViewFontArvoBold(context, holder.followersCount);

        holder.followersLayout.setVisibility(View.GONE);

        Picasso.with(context).load(listItem.picURL)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.imageView);

        if (onClickRunnable != null)
        {
            View.OnClickListener onClickListener = (View V)->
            {
                addOrderActivity.IPK = listItem.IPK;
                addOrderActivity.username = listItem.username;
                new android.os.Handler(Looper.getMainLooper()).post(onClickRunnable);
            };


            holder.rootView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount()
    {
        return itemsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public LinearLayout rootView;
        public CircleImageView imageView;
        public AutofitTextView accountTitle;
        public TextView followersTitle;
        public AutofitTextView followersCount;
        public LinearLayout followersLayout;



        public ViewHolder(View itemView)
        {
            super(itemView);

            rootView = itemView.findViewById(R.id.accountViewer_root);
            imageView = itemView.findViewById(R.id.accountViewer_Image);
            accountTitle = itemView.findViewById(R.id.accountViewer_accountTitle);
            followersTitle = itemView.findViewById(R.id.accountViewer_followersCountTitle);
            followersCount = itemView.findViewById(R.id.accountViewer_followersCountNumber);
            followersLayout = itemView.findViewById(R.id.accountViewer_followersInfoLayout);


        }
    }
}
