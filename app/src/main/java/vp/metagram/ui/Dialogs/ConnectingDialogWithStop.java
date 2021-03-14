package vp.metagram.ui.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


import vp.metagram.R;

import static vp.metagram.general.functions.dpToPixels;
import static vp.metagram.general.functions.setTextViewFontForMenu;


public class ConnectingDialogWithStop extends DialogFragment
{
    private View rootView;
    public static Boolean initStarted = false;
    public boolean result = false;
    private String message;

    List<Boolean> canceled;

    public ConnectingDialogWithStop() {}

    public static ConnectingDialogWithStop newInstance(String _message, List<Boolean> canceled)
    {
        ConnectingDialogWithStop dialogWithStop = new ConnectingDialogWithStop();
        dialogWithStop.message = _message;
        dialogWithStop.canceled = canceled;
        return dialogWithStop ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialog_connecting_stop, container, false);
        rootView = view;

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int width = (int)dpToPixels(rootView.getContext(),200);
        int height = (int)dpToPixels(rootView.getContext(),350);

        getDialog().getWindow().setLayout(width, height);

        ((TextView)rootView.findViewById(R.id.serverConnectionLabel_TextView)).setText(message);
        setTextViewFontForMenu(rootView.getContext(),rootView.findViewById(R.id.serverConnectionLabel_TextView));

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
