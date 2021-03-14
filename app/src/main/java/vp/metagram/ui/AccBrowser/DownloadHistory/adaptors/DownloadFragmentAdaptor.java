package vp.metagram.ui.AccBrowser.DownloadHistory.adaptors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vp.metagram.R;
import vp.metagram.ui.AccBrowser.DownloadHistory.types.DownloadItem;
import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;
import vp.metagram.ui.Dialogs.ConfirmationDialog;

import static vp.metagram.general.functions.convertDrawableToBlue;
import static vp.metagram.general.functions.getDateFromTimeStampRevert;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.tools.io.iFileSystemUtils.deleteDir;

public class DownloadFragmentAdaptor extends RecyclerView.Adapter<DownloadFragmentAdaptor.ViewHolder>
{

    Context context;
    List<DownloadItem> items;

    int holderWidth;

    public DownloadFragmentAdaptor(Context context, List<DownloadItem> items)
    {
        this.context = context;
        this.items = items;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        holderWidth = displayMetrics.widthPixels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        if (items.size() == 0 || items.get(position).filesAddress.size() == 0)
            return;

        DownloadItem item = items.get(position);

        String fileAddress = item.filesAddress.get(0);

        File file = new File(fileAddress);

        if (fileAddress.contains(".mp"))
        {
            threadPoolExecutor.execute(() ->
            {
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(fileAddress);
                String heightStr = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                String widthStr = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

                int height = Integer.parseInt(heightStr);
                int width = Integer.parseInt(widthStr);

                float ratio = (float) holderWidth / width;

                width = (int) (ratio * width);
                height = (int) (ratio * height);

                Bitmap bmThumbnail = ThumbnailUtils.extractThumbnail(ThumbnailUtils.createVideoThumbnail(fileAddress,
                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND), width, height);


                new android.os.Handler(Looper.getMainLooper()).post(() ->
                        holder.image.setImageBitmap(bmThumbnail));
            });
        }
        else
        {
            File imageFile = new File(item.filesAddress.get(0));

            Picasso.with(context).load(imageFile)
                    .placeholder(context.getResources().getDrawable(R.drawable.ic_download_from_net))
                    .error(context.getResources().getDrawable(R.drawable.ic_sync_problem_black)).into(holder.image);

        }

        String titleStr = String.format(
                context.getResources().getString(R.string.DownloadItem_Title),
                getDateFromTimeStampRevert(context, file.lastModified()),
                item.noOfItems);
        holder.title.setText(titleStr);
        setTextViewFontForMessage(context, holder.title);

        holder.shareButton.setOnClickListener((View v)->
        {
            ArrayList<Uri> filesToShare = new ArrayList<>();
            for (String address: item.filesAddress)
                filesToShare.add(FileProvider.getUriForFile(context, "nava.metagram", new File(address)));

            Drawable originalIcon = context.getResources().getDrawable(R.drawable.ic_share_black);
            Drawable icon =  convertDrawableToBlue(originalIcon);
            ((ImageButton)v).setImageDrawable(icon);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, ""));
        });

        holder.deleteButton.setOnClickListener((View v)->
        {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog();

            confirmationDialog.showDialog((Activity) context,
                    context.getString(R.string.DownloadItem_DeleteTitle),
                    context.getString(R.string.DownloadItem_DeletePost),
                    context.getString(R.string.button_confirmCaption), () ->
                    {
                        threadPoolExecutor.execute(() ->
                        {
                            try
                            {
                                deleteDir(file.getParent());
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

        holder.image.setOnClickListener((View v)->
        {
            Intent intent = new Intent(context, MediaViewerActivity.class);
            intent.putExtra("path",file.getParent());
            intent.putExtra("isLocal",true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public void setDataSet(List<DownloadItem> itemsList)
    {
        this.items = itemsList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        public ConstraintLayout root;
        public ImageView image;
        public TextView title;
        public ImageButton shareButton;
        public ImageButton deleteButton;


        public ViewHolder(View itemView)
        {
            super(itemView);

            root = itemView.findViewById(R.id.downloadItem_root);
            image = itemView.findViewById(R.id.downloadItem_image);
            title = itemView.findViewById(R.id.downloadItem_title);
            shareButton = itemView.findViewById(R.id.downloadItem_share);
            deleteButton = itemView.findViewById(R.id.downloadItem_delete);
        }
    }
}
