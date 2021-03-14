package vp.metagram.ui.AccBrowser.DownloadHistory.adaptors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

import vp.metagram.R;
import vp.metagram.ui.AccBrowser.DownloadHistory.DownloadViewerActivity;
import vp.metagram.ui.AccBrowser.DownloadHistory.types.AccountItem;
import vp.metagram.ui.Dialogs.ConfirmationDialog;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.tools.io.iFileSystemUtils.GetDownloadDir;
import static vp.tools.io.iFileSystemUtils.deleteDir;

public class DownloadAccountListAdaptor extends RecyclerView.Adapter<DownloadAccountListAdaptor.ViewHolder>
{

    Context context;
    List<AccountItem> items;

    public DownloadAccountListAdaptor(Context context, List<AccountItem> items)
    {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download_list, parent, false);
        return new DownloadAccountListAdaptor.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        AccountItem item = items.get(position);

        View.OnClickListener openDownloadsActivity = (View v) ->
        {
            Intent intent = new Intent(context, DownloadViewerActivity.class);
            intent.putExtra("username", item.name);
            context.startActivity(intent);
        };

        holder.root.setOnClickListener(openDownloadsActivity);
        holder.title.setOnClickListener(openDownloadsActivity);

        holder.title.setText(item.name);
        setTextViewFontForMessage(context, holder.title);

        String description = android.text.format.Formatter.formatFileSize(context, item.folderSize);
        holder.description.setText(description);
        setTextViewFontForMessage(context, holder.title);

        holder.delete.setOnClickListener((View v)->
        {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog();

            confirmationDialog.showDialog((Activity) context,
                    context.getString(R.string.DownloadItem_DeleteTitle),
                    context.getString(R.string.DownloadItem_DeleteAccount),
                    context.getString(R.string.button_confirmCaption), () ->
                    {
                        threadPoolExecutor.execute(() ->
                        {
                            try
                            {
                                deleteDir(GetDownloadDir(context) + item.name + "/");
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                items.remove(position);
                                new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                            }
                        });
                    });
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public void setDataSet(List<AccountItem> itemsList)
    {
        this.items = itemsList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ConstraintLayout root;
        public ImageButton delete;
        public TextView title;
        public TextView description;

        public ViewHolder(View itemView)
        {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            delete = itemView.findViewById(R.id.delete);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.info);
        }
    }
}
