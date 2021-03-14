package vp.metagram.ui.AccBrowser.CommentViewer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashMap;
import java.util.Map;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.AccBrowser.ListSource.ListSource;
import vp.metagram.ui.AccBrowser.ListSource.ListSource_Comments_Online;
import vp.metagram.ui.Dialogs.InformationDialog;

import vp.metagram.utils.instagram.types.Comment;

import static vp.metagram.general.functions.setHTMLForTextView;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.ScrollerDeltaMargin;
import static vp.metagram.general.variables.ScrollerPreFetchItems;
import static vp.metagram.general.variables.threadPoolExecutor;

public class CommentViewerActivity extends BaseActivity
{
    Map<Long, Comment> comments = new LinkedHashMap<>(10, 0.75F, true);

    ListSource listSource;

    boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private RecyclerView recycleView;
    public RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private SpinKitView loadingView;

    TextView captionTextView;
    TextView titleTextView;
    ImageButton backButton;

    String username;
    String Caption;
    String short_code;

    boolean isFirstTime = true;

    boolean isShowing = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment_viewer);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            Caption = extras.getString("Caption");
            short_code = extras.getString("short_code");
            username = extras.getString("username");
        }

        prepareUIElements();
    }


    public void prepareUIElements()
    {
        captionTextView = findViewById(R.id.CommentViewer_Caption);
        titleTextView = findViewById(R.id.CommentViewer_title);
        backButton = findViewById(R.id.CommentViewer_backButton);
        loadingView = findViewById(R.id.CommentViewer_loadingView);
        recycleView = findViewById(R.id.CommentViewer_recyclerView);

        setTextViewFontForMessage(this, captionTextView);

        String content = String.format("<b>%s:</b><br>%s",username,Caption);
        setHTMLForTextView(captionTextView,content);

        setTextViewFontForMessage(this, titleTextView);
        titleTextView.setText("Comments");

        backButton.setOnClickListener((View v)->onBackPressed());

        captionTextView.setMovementMethod(new ScrollingMovementMethod());
        captionTextView.setOnLongClickListener((View v)->
        {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("",  captionTextView.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this,getResources().getString(R.string.copyToClipboard_caption), Toast.LENGTH_SHORT).show();

            return true;
        });

        listSource = new ListSource_Comments_Online(short_code, comments);

        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycleView.getContext(),
                layoutManager.getOrientation());
        recycleView.addItemDecoration(dividerItemDecoration);
        recycleView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if ( dy > ScrollerDeltaMargin ) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ( loading )
                    {
                        if ( (visibleItemCount + pastVisibleItems) + ScrollerPreFetchItems >= totalItemCount )
                        {
                            loading = false;
                            loadData();
                        }
                    }
                }
            }
        });
        adapter = new CommentAdapter(comments,this);
        recycleView.setAdapter(adapter);

    }

    public void loadData()
    {

        loadingView.setVisibility(View.VISIBLE);
        threadPoolExecutor.execute(() ->
        {
            try
            {
                listSource.getNextList();
                new Handler(getMainLooper()).post(() ->
                        adapter.notifyDataSetChanged());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();

                Throwable e;

                if (ex instanceof UndeclaredThrowableException)
                {
                    e = ((InvocationTargetException)((UndeclaredThrowableException)ex).getUndeclaredThrowable()).getTargetException();
                }
                else
                {
                    e = ex;
                }

                if (e instanceof IOException)
                {
                    e.printStackTrace();

                    new Handler(getMainLooper()).post(()->
                    {
                        InformationDialog dialog = new InformationDialog();
                        dialog.showDialog(this,
                                getString(R.string.addOrder_WarningTitle),
                                getString(R.string.connectionInfo_noInternetContent),
                                getString(R.string.button_ok),()->
                                {
                                    if (!isShowing) {return;}
                                    finish();
                                });
                    });
                }
            }
            finally
            {
                new Handler(getMainLooper()).post(() ->
                {
                    loadingView.setVisibility(View.GONE);
                    loading = true;
                });
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (isFirstTime)
        {
            isFirstTime = false;
            loadData();
        }

        isShowing = true;
    }

    @Override
    public void onPause()
    {
        isShowing = false;
        super.onPause();
    }
}
