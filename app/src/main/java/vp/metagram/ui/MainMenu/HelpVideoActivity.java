package vp.metagram.ui.MainMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import vp.igwa.IGTVList;
import vp.igwa.IGTVSummery;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.AccBrowser.MediaViewer.MediaViewerActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.User;
import vp.igpapi.IGWAException;

import static vp.metagram.general.functions.arrayListToStringArray;
import static vp.metagram.general.functions.configShimmer;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appVersion;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;

public class HelpVideoActivity extends BaseActivity
{

    RecyclerView videoRecyclerView;
    HelpVideoListAdaptor helpAdaptor;
    private LinearLayoutManager layoutManager;

    ShimmerFrameLayout menuExternalShimmer;
    TextView menuExternalShimmerTextView;
    ShimmerFrameLayout menuInternalShimmer;
    TextView menuInternalShimmerTextView;

    boolean isFirstTime = true;

    ConnectingDialog connectingDialog;

    IGTVList igtvList = new IGTVList();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_video);

        prepareUIElements();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isFirstTime)
        {
            isFirstTime = false;
            connectingDialog.show(getFragmentManager(), "");
            threadPoolExecutor.execute(()->
            {
                try
                {
                    getIGTVs();
                }
                catch (IGWAException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (NoSuchAlgorithmException | JSONException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    runOnUiThread(()->
                    {
                        Collections.reverse(igtvList.IGTVs);
                        connectingDialog.close();
                        videoRecyclerView.setAdapter(helpAdaptor);
                    });
                }
            });
        }
    }

    //TODO Add Lazy Loading when number of highlights are large
    public void getIGTVs() throws IGWAException, IOException, NoSuchAlgorithmException, JSONException
    {
        User Metagram = metagramAgent.activeAgent.proxy.getUserInfo("Metagram_app");
        metagramAgent.activeAgent.proxy.getIGTVList(Metagram.IPK, igtvList);
    }

    private void prepareUIElements()
    {
        connectingDialog = ConnectingDialog.newInstance(getResources().getString(R.string.login_connectMessage));

        menuExternalShimmer = findViewById(R.id.shimmer_view_container_external);
        menuInternalShimmer =  findViewById(R.id.shimmer_view_container_internal);
        menuExternalShimmerTextView = findViewById(R.id.shimmer_text_exteral);
        menuInternalShimmerTextView = findViewById(R.id.shimmer_text_internal);

        String shimmerText = "";
        if (appVersion.languagePartNo == 1)
            shimmerText = appVersion.get_appName_en();
        else if(appVersion.languagePartNo == 2)
            shimmerText = appVersion.get_appName_fa();

        configShimmer(this,
                shimmerText ,
                menuExternalShimmer,
                menuInternalShimmer,
                menuExternalShimmerTextView,
                menuInternalShimmerTextView,
                2000);

        videoRecyclerView = findViewById(R.id.helpVideo_RecyclerView);
        videoRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        videoRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(videoRecyclerView.getContext(),
                layoutManager.getOrientation());
        videoRecyclerView.addItemDecoration(dividerItemDecoration);

        helpAdaptor = new HelpVideoListAdaptor(this, igtvList);

    }


    public class HelpVideoListAdaptor extends RecyclerView.Adapter<HelpVideoListAdaptor.ViewHolder>
    {

        Context context;
        IGTVList items;

        public HelpVideoListAdaptor(Context context, IGTVList items)
        {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public HelpVideoListAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_help_video_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HelpVideoListAdaptor.ViewHolder holder, int position)
        {
            IGTVSummery item = items.IGTVs.get(position);

            View.OnClickListener openMediaViewer = (View v) ->
            {
                connectingDialog.show(getFragmentManager(), "");
                threadPoolExecutor.execute(()->
                {
                    try
                    {
                        PostMedia igtv = metagramAgent.activeAgent.api.media_info(item.short_code, new PostMedia());
                        new Handler(getMainLooper()).post(() ->
                        {
                            Intent intent = new Intent(context, MediaViewerActivity.class);
                            intent.putExtra("urls",arrayListToStringArray(igtv.urls));
                            context.startActivity(intent);
                        });
                    }
                    catch (JSONException | NoSuchAlgorithmException | IOException | IGWAException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        new Handler(getMainLooper()).post(connectingDialog::close);
                    }
                });
            };

            holder.root.setOnClickListener(openMediaViewer);

            holder.title.setText(item.title);
            setTextViewFontForMessage(context, holder.title);


        }

        @Override
        public int getItemCount()
        {
            return items.IGTVs.size();
        }

        public void setDataSet(IGTVList itemsList)
        {
            this.items = itemsList;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public ConstraintLayout root;
            public TextView title;

            public ViewHolder(View itemView)
            {
                super(itemView);

                root = itemView.findViewById(R.id.helpItem_root);
                title = itemView.findViewById(R.id.helpItem_title);
            }
        }
    }
}
