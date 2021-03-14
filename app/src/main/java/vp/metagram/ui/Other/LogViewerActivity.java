package vp.metagram.ui.Other;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.utils.MemoryLogString;
import vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor;

import static vp.metagram.general.functions.setCheckBoxFontForMessage;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.metagramAgent;


public class LogViewerActivity extends BaseActivity
{
    String Type = "";

    StatisticsExecutor executor;
    int JobID;

    TextView titleTextView;
    ImageButton backButton;
    TextView logTextView;
    ScrollView logScrollView;

    CheckBox autoScrollCheckBox;
    CheckBox advancedLoggingCheckBox;

    boolean isFirstTime = true;

    boolean autoScroll = true;

    boolean isActive;

    MemoryLogString memoryLog;
    String Username;
    String ThreadName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            Type = extras.getString("Type");

            if (Type.equals("Report"))
            {
                JobID = extras.getInt("JobID");
                executor = metagramAgent.getStatisticsExecutorByJobID(JobID);

                if(executor != null)
                {
                    memoryLog = executor.memoryLog;
                    Username = executor.username;
                    ThreadName = executor.threadName;
                }
            }

        }



        prepareUIElements();
    }

    @Override
    public void onResume()
    {
        super.onResume();


        if (memoryLog != null)
        {
            isActive = true;

            if (isFirstTime)
            {
                updateLog();
                isFirstTime = false;
            }
        }

        if (executor != null)
        {
            advancedLoggingCheckBox.setChecked(true);
        }

    }


    @Override
    public void onPause()
    {
        isActive = false;
        if (executor != null)
        {
            executor.advanced_logging = false;
        }
        super.onPause();
    }

    public void prepareUIElements()
    {
        titleTextView = findViewById(R.id.LogActivity_title);
        backButton = findViewById(R.id.LogActivity_backButton);
        logTextView = findViewById(R.id.LogActivity_LogTextView);
        logScrollView = findViewById(R.id.LogActivity_LogScrollView);
        autoScrollCheckBox = findViewById(R.id.LogActivity_autoScroll);
        advancedLoggingCheckBox = findViewById(R.id.LogActivity_advancedLogging);

        backButton.setOnClickListener((View v)->onBackPressed());

        setTextViewFontForMessage(this,titleTextView);
        setTextViewFontForMessage(this,logTextView);

        titleTextView.setText(String.format(Locale.ENGLISH,"%s - %s", Username, ThreadName));

        logTextView.setOnClickListener((View v)->
                autoScroll = false);

        logTextView.setOnLongClickListener((View v)->
        {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", logTextView.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this,"Log copied to clipboard.",
                    Toast.LENGTH_SHORT).show();

            return true;
        });

        autoScrollCheckBox.setChecked(true);
        autoScrollCheckBox.setOnCheckedChangeListener((compoundButton, b) -> autoScroll = b);
        setCheckBoxFontForMessage(this,autoScrollCheckBox);


        advancedLoggingCheckBox.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (executor != null)
            {executor.advanced_logging = b;}
        });
        setCheckBoxFontForMessage(this,advancedLoggingCheckBox);
    }


    public void updateLog()
    {
        if (!isActive) {return;}

        if (memoryLog != null)
        {
            logTextView.setText(memoryLog.read());

            if (autoScroll)
            {logScrollView.post(() -> logScrollView.fullScroll(ScrollView.FOCUS_DOWN));}
        }

        new Handler(getMainLooper()).postDelayed(()->updateLog(),1000);
    }
}
