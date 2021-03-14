package vp.metagram.ui.MainFragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;


import vp.metagram.R;
import vp.metagram.ui.LogInActivity;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.metagram.ui.Other.DebugActivity;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

import static vp.metagram.general.functions.isNetworkAvailable;
import static vp.metagram.general.functions.prepareCardViewForAPIBefore21;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.isReleaseMode;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.RegisterFragment_Help;
import static vp.metagram.ui.MainMenu.HelpFragments.HelpInteractiveItems.showInteractiveHelp;


public class LogInFragment extends Fragment
{
    public static final int LOGIN_REQUEST = 2;
    View rootLayout;
    TextView registerAccountTextViewPart1;
    TextView registerAccountAddButton;
    CardView logInButton;

    public LogInFragment()
    {
    }

    public static LogInFragment newInstance()
    {
        LogInFragment fragment = new LogInFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        {
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        showInteractiveHelp(RegisterFragment_Help, getActivity(),
                getResources().getString(R.string.i_RegisterFragment_Help_title),
                getResources().getString(R.string.i_RegisterFragment_content),
                logInButton,null, Gravity.auto);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_login, container, false);
        registerAccountTextViewPart1 = rootLayout.findViewById(R.id.account_fragment_titleTextView1);
        registerAccountAddButton = rootLayout.findViewById(R.id.registerAccountAddButton);
        logInButton = rootLayout.findViewById(R.id.logInButton);

        logInButton.setOnClickListener(v ->
        {
            if (!isNetworkAvailable(getContext()))
            {
                InformationDialog dialog = new InformationDialog();
                dialog.showDialog(getActivity(),
                        getActivity().getResources().getString(R.string.error_noNetworkTitle),
                        getActivity().getResources().getString(R.string.error_noNetwork),
                        getActivity().getResources().getString(R.string.button_ok), null);
                return;
            }
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            getActivity().startActivityForResult(intent, LOGIN_REQUEST);
        });

        logInButton.setOnLongClickListener((View v)->
        {
            if(!isReleaseMode)
            {
                Intent intent = new Intent(getActivity(), DebugActivity.class);
                startActivity(intent);
            }

            return false;
        });

        setTextViewFontForMessage(getActivity(), registerAccountTextViewPart1);
        setTextViewFontForMessage(getActivity(), registerAccountAddButton);

        prepareCardViewForAPIBefore21((ViewGroup) rootLayout,1);

        return rootLayout;
    }

}
