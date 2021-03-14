package vp.metagram.ui.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import vp.metagram.R;
import vp.metagram.ui.Other.ReportListActivity;

import static vp.metagram.general.functions.setImageViewEnabled;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appContext;

public class ReportInfoDialog extends DialogFragment
{
    private View rootView;

    ImageView fParameter;
    ImageView pParameter;
    ImageView dParameter;
    ImageView rParameter;

    TextView usernameTextView;
    TextView description;

    Button actionButton;

    String username;
    boolean F_Parameter;
    boolean P_Parameter;
    boolean D_Parameter;
    boolean R_Parameter;
    View.OnClickListener onClick;
    boolean isFinished;


    public static ReportInfoDialog newInstance(String username, boolean F_Parameter, boolean P_Parameter, boolean D_Parameter, boolean R_Parameter, View.OnClickListener onClick, boolean isFinished)
    {
        ReportInfoDialog result = new ReportInfoDialog();

        result.username = username;
        result.F_Parameter = F_Parameter;
        result.P_Parameter = P_Parameter;
        result.D_Parameter = D_Parameter;
        result.R_Parameter = R_Parameter;
        result.onClick = onClick;
        result.isFinished = isFinished;

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
        View view = inflater.inflate(R.layout.dialog_report_info, container, false);
        rootView = view;

        fParameter = rootView.findViewById(R.id.reportInfo_F_ImageView);
        pParameter = rootView.findViewById(R.id.reportInfo_P_ImageView);
        dParameter = rootView.findViewById(R.id.reportInfo_D_ImageView);
        rParameter = rootView.findViewById(R.id.reportInfo_R_ImageView);

        usernameTextView = rootView.findViewById(R.id.reportInfo_username);
        description = rootView.findViewById(R.id.reportInfo_Description);

        actionButton = rootView.findViewById(R.id.reportInfo_button);


        setImageViewEnabled(getActivity(), F_Parameter, fParameter, R.drawable.ic_f_parameter);
        setImageViewEnabled(getActivity(), P_Parameter, pParameter, R.drawable.ic_p_parameter);
        setImageViewEnabled(getActivity(), D_Parameter, dParameter, R.drawable.ic_d_parameter);
        setImageViewEnabled(getActivity(), R_Parameter, rParameter, R.drawable.ic_r_parameter);

        usernameTextView.setText(username);
        setTextViewFontForMessage(getActivity(),usernameTextView);

        setTextViewFontForMessage(getActivity(),description);

        actionButton.setOnClickListener((View v)->
        {
            close();
            onClick.onClick(actionButton);
        });

        if (isFinished)
        { actionButton.setText(getResources().getString(R.string.reportInfo_Button_Setting)); }
        else
        { actionButton.setText(getResources().getString(R.string.reportInfo_Button_Log)); }

        setTextViewFontForMessage(getActivity(),actionButton);

        View.OnClickListener onClickListener = (View v) ->
        {
            Intent intent = new Intent(getActivity(), ReportListActivity.class);
            startActivity(intent);
        };

        fParameter.setOnClickListener(onClickListener);
        pParameter.setOnClickListener(onClickListener);
        dParameter.setOnClickListener(onClickListener);
        rParameter.setOnClickListener(onClickListener);


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
