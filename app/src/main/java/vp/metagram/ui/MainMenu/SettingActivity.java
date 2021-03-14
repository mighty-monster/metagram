package vp.metagram.ui.MainMenu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSeekBar;

import java.util.Locale;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.Dialogs.SendErrorDialog;

import static vp.metagram.general.functions.prepareCardViewForAPIBefore21;
import static vp.metagram.general.functions.setTextViewFontArvoRegular;
import static vp.metagram.general.functions.setTextViewFontForMenu;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.deviceSettings;

public class SettingActivity extends BaseActivity
{

    int divider = 10;

    TextView title;

    TextView delayTitle;
    TextView delayDescription;
    TextView delaySeconds;
    AppCompatSeekBar delaySeekBar;

    TextView loggingTitle;
    TextView loggingDescription;
    AppCompatCheckBox loggingCheckBox;

    TextView wakeLockDescription;
    AppCompatCheckBox wakeLockCheckBox;

    Button defaultButton;
    Button sendError;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        prepareUIElements();
    }

    public void prepareUIElements()
    {
        title = findViewById(R.id.setting_title);

        setTextViewFontForMenu(this, title);

        delayTitle = findViewById(R.id.setting_delayTitle);
        delayDescription = findViewById(R.id.setting_delayDescription);
        delaySeconds = findViewById(R.id.setting_delaySeconds);
        delaySeekBar = findViewById(R.id.setting_delaySeekBar);

        setTextViewFontForMessage(this, delayTitle);
        setTextViewFontForMessage(this, delayDescription);
        setTextViewFontArvoRegular(this, delaySeconds);
        delaySeekBar.setMax(49);
        delaySeekBar.setProgress(4);
        delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                int progressValue = Math.abs(progress - seekBar.getMax());
                float value = (float) (progressValue + 1) / divider;
                delaySeconds.setText(String.format(Locale.ENGLISH, "Delay: %.1f s", value));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                try
                {
                    int progressValue = Math.abs(seekBar.getProgress() - seekBar.getMax());
                    float value = (float) (progressValue + 1) / divider;
                    deviceSettings.statisticsDelayTime = (int) (value * 1000);
                    deviceSettings.save();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
        int seekBarValue = Math.abs((int) (((float) deviceSettings.statisticsDelayTime / 1000) * divider - 1) - delaySeekBar.getMax());
        delaySeekBar.setProgress(seekBarValue);

        loggingTitle = findViewById(R.id.setting_loggingTitle);
        loggingDescription = findViewById(R.id.setting_loggingDescription);
        loggingCheckBox = findViewById(R.id.setting_loggingCheckBox);
        setTextViewFontForMessage(this, loggingTitle);
        setTextViewFontForMessage(this, loggingDescription);
        loggingCheckBox.setChecked(deviceSettings.logging);
        loggingCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            try
            {
                deviceSettings.logging = isChecked;
                deviceSettings.save();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        defaultButton = findViewById(R.id.setting_defaultButton);
        setTextViewFontForMessage(this, defaultButton);
        defaultButton.setOnClickListener((View v) ->
        {
            try
            {
                deviceSettings.stickyNotification = false;
                deviceSettings.wakeLock = true;
                deviceSettings.logging = true;
                deviceSettings.statisticsDelayTime = 500;
                deviceSettings.save();
                wakeLockCheckBox.setChecked(deviceSettings.wakeLock);
                loggingCheckBox.setChecked(deviceSettings.logging);
                int oldValue = Math.abs((int) (((float) deviceSettings.statisticsDelayTime / 1000) * divider - 1) - delaySeekBar.getMax());
                delaySeekBar.setProgress(oldValue);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        sendError = findViewById(R.id.setting_sendErrorButton);
        setTextViewFontForMessage(this, sendError);
        sendError.setOnClickListener((View v)->
        {
            SendErrorDialog sendErrorDialog = SendErrorDialog.newInstance();
            sendErrorDialog.show(getFragmentManager(), "");
        });


        wakeLockDescription = findViewById(R.id.setting_wakeLockDescription);
        setTextViewFontForMessage(this, wakeLockDescription);
        wakeLockCheckBox = findViewById(R.id.setting_wakeLockCheckBox);
        wakeLockCheckBox.setChecked(deviceSettings.wakeLock);
        wakeLockCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            try
            {
                deviceSettings.wakeLock = isChecked;
                deviceSettings.save();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        prepareCardViewForAPIBefore21(findViewById(R.id.setting_rootLayout));
    }

}
