package vp.metagram.ui.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import vp.metagram.R;

import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;

public class ConnectionInfoDialog extends DialogFragment
{
    private View rootView;

    String username;

    TextView titleTextView;
    TextView usernameTextView;
    TextView totalRequests;
    TextView averageTime;
    TextView databaseSize;
    TextView noOfOthers;

    Button confirmButton;

    public static ConnectionInfoDialog newInstance(String username)
    {
        ConnectionInfoDialog result = new ConnectionInfoDialog();

        result.username = username;

        return result;
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
        View view = inflater.inflate(R.layout.dialog_connection_info, container, false);
        rootView = view;

        titleTextView = rootView.findViewById(R.id.connectionInfo_title);
        usernameTextView = rootView.findViewById(R.id.connectionInfo_username);
        totalRequests = rootView.findViewById(R.id.connectionInfo_totalRequests);
        averageTime = rootView.findViewById(R.id.connectionInfo_meanTime);
        databaseSize = rootView.findViewById(R.id.connectionInfo_databaseSize);
        confirmButton = rootView.findViewById(R.id.connectionInfo_confirmButton);
        noOfOthers = rootView.findViewById(R.id.connectionInfo_noOfOthers);

        usernameTextView.setText(username);
        titleTextView.setText(getActivity().getResources().getString(R.string.connectionInfo_healthy));

        totalRequests.setText(String.format(Locale.ENGLISH,getActivity().getResources().getString(R.string.connectionInfo_noOfRequest),metagramAgent.activeAgent.agentStatus.requestsTotal));
        averageTime.setText(String.format(Locale.ENGLISH,getActivity().getResources().getString(R.string.connectionInfo_requestAverageTime),metagramAgent.activeAgent.agentStatus.averageRequestDuration/1000));

        databaseSize.setText(String.format(Locale.ENGLISH,getActivity().getResources().getString(R.string.connectionInfo_databaseSize),(float)dbMetagram.getDatabaseSize()/1000000 ));

        threadPoolExecutor.execute(()->
        {
            String sqlText = "Select Count(*) as No From Others";
            try
            {
                MatrixCursor result = dbMetagram.selectQuery(sqlText);
                result.moveToFirst();
                new Handler(Looper.getMainLooper()).post(()->
                        noOfOthers.setText(String.format(Locale.ENGLISH,getActivity().getResources().getString(R.string.connectionInfo_downloadedAccounts),result.getInt(result.getColumnIndex("No")))));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (GeneralSecurityException e)
            {
                e.printStackTrace();
            }

        });

        setTextViewFontForMessage(getActivity(),titleTextView);
        setTextViewFontForMessage(getActivity(),usernameTextView);
        setTextViewFontForMessage(getActivity(),totalRequests);
        setTextViewFontForMessage(getActivity(),averageTime);
        setTextViewFontForMessage(getActivity(),databaseSize);
        setTextViewFontForMessage(getActivity(),noOfOthers);
        setTextViewFontForMessage(getActivity(),confirmButton);

        confirmButton.setOnClickListener((View v)->close());

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager wm = (WindowManager) appContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        int width = (int)(metrics.widthPixels * (float)4/5);

        getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT );
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
