package vp.metagram.ui.Dialogs;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;


import me.grantland.widget.AutofitTextView;
import vp.metagram.R;

import static vp.metagram.general.functions.getGregorianDateTimeFromTimeStamp;
import static vp.metagram.general.functions.prepareCardViewForAPIBefore21;
import static vp.metagram.general.functions.setTextViewFontForMenu;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.logger;
import static vp.tools.io.iFileSystemUtils.GetTempDir;



public class SendErrorDialog extends DialogFragment
{
    private View rootView;

    TextView title;
    TextView deviceIDTitle;
    AutofitTextView deviceID;
    TextView descriptionTitle;
    EditText description;

    CardView sendToServerButton;
    TextView sentToServerTitle;

    CardView sendToTelegramButton;
    TextView sentToTelegramTitle;

    CardView cancelButton;
    TextView cancelTitle;

    boolean isFirstTime = true;

    public static SendErrorDialog newInstance()
    {
        SendErrorDialog self = new SendErrorDialog();
        return self ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.dialog_send_error, container, false);
        prepareUIElements();

        return rootView;
    }

    public void prepareUIElements()
    {
        title = rootView.findViewById(R.id.sendErrorTitle);
        setTextViewFontForMessage(getActivity(),title);

        deviceIDTitle = rootView.findViewById(R.id.sendErrorDeviceIDTitle);
        setTextViewFontForMessage(getActivity(),deviceIDTitle);

        deviceID = rootView.findViewById(R.id.sendErrorDeviceID);
        setTextViewFontForMessage(getActivity(),deviceID);
        String deviceUUID;
        if (deviceSettings.DeviceUUI.equals(""))
        {
            deviceUUID = getActivity().getResources().getString(R.string.sendError_deviceID_notRegistered);
        }
        else
        {
            deviceUUID = deviceSettings.DeviceUUI;
        }
        deviceID.setText(deviceUUID);

        descriptionTitle = rootView.findViewById(R.id.sendErrorDescriptionTitle);
        setTextViewFontForMessage(getActivity(),descriptionTitle);

        description = rootView.findViewById(R.id.sendErrorDescription);

        sendToServerButton = rootView.findViewById(R.id.sendError_sendToServerButton);
        sentToServerTitle = rootView.findViewById(R.id.sendError_sendToServerTitle);
        setTextViewFontForMenu(getActivity(),sentToServerTitle);
        sendToServerButton.setEnabled(false);
        sendToServerButton.setBackgroundColor(getResources().getColor(R.color.lDisabledButton));
        sentToServerTitle.setTextColor(getResources().getColor(R.color.lDisabledText));


        sendToTelegramButton = rootView.findViewById(R.id.sendError_sendToTelegramButton);
        sentToTelegramTitle = rootView.findViewById(R.id.sendError_sendToTelegramTitle);
        setTextViewFontForMenu(getActivity(),sentToTelegramTitle);
        sendToTelegramButton.setOnClickListener((View v)->
        {
            File SDTemp = new File(GetTempDir(appContext));
            if (!SDTemp.exists()) {SDTemp.mkdir();}

            logger.logError("User message",description.getText().toString());
            logger.logError("Device UUID",deviceSettings.DeviceUUI);


            String reportPath = GetTempDir(getActivity()) +
                    String.format(Locale.ENGLISH,"MetagramReport-%s.rpt", getGregorianDateTimeFromTimeStamp( System.currentTimeMillis()));

            File oldReport = new File(reportPath);
            if (oldReport.exists())
            {
                oldReport.delete();
            }

            try
            {
                logger.logDatabase.copyDatabase(reportPath);

                Uri uri = Uri.parse("file://"+reportPath);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.setType("file/*");
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                getActivity().startActivity(Intent.createChooser(share, getActivity().getResources().getString(R.string.sendError_sendToTelegram)));
                close();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


        });


        cancelButton = rootView.findViewById(R.id.sendError_cancelButton);
        cancelTitle = rootView.findViewById(R.id.sendError_cancelTitle);
        setTextViewFontForMenu(getActivity(),cancelTitle);
        cancelButton.setOnClickListener((View v)->
        {
            close();
        });

        prepareCardViewForAPIBefore21((ViewGroup) rootView);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager wm = (WindowManager) appContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels * 2 / 3;
        int width = metrics.widthPixels * 3 / 4;

        getDialog().getWindow().setLayout(width,height );

        if (isFirstTime)
        {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
            {
                if ( getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
                {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);


                }
            }
            isFirstTime = false;
        }


    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        View view = View.inflate(getActivity(), R.layout.dialog_connecting, null);
        dialog.setContentView(view);
        return dialog;
    }

    public void close()
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
    }

}
