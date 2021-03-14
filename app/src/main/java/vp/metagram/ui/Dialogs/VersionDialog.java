package vp.metagram.ui.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import vp.metagram.R;

import static android.content.Context.CLIPBOARD_SERVICE;
import static vp.metagram.general.functions.setTextViewFontArvoBold;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.appVersion;

@SuppressLint("StringFormatInvalid")
public class VersionDialog extends DialogFragment
{
    private View rootView;

    TextView title;
    TextView version;
    TextView language;

    Button ok;

    public static VersionDialog newInstance()
    {
        return new VersionDialog();
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
        View view = inflater.inflate(R.layout.dialog_version_info, container, false);

        rootView = view;

        prepareUIElements();

        return view;
    }

    private void prepareUIElements()
    {
        title = rootView.findViewById(R.id.version_Title);
        version = rootView.findViewById(R.id.version_version);
        language = rootView.findViewById(R.id.version_language);
        ok = rootView.findViewById(R.id.version_okButton);

        setTextViewFontArvoBold(getActivity(), title);
        setTextViewFontForMessage(getActivity(), version);
        setTextViewFontForMessage(getActivity(), language);

        setTextViewFontForMessage(getActivity(), ok);

        String versionStr = String.format(Locale.ENGLISH, getActivity().getString(R.string.VersionDialog_Version), appVersion.getMinimisedVersionString());
        String languageStr = String.format(Locale.ENGLISH, getActivity().getString(R.string.VersionDialog_Language), appVersion.supportedLanguages);

        version.setText(versionStr);
        language.setText(languageStr);


        ok.setOnClickListener((View v)->close());


        version.setOnClickListener((View v)->
        {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("",  appVersion.getMinimisedVersionString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.VersionDialog_Clipboard), Toast.LENGTH_SHORT).show();
        });

        language.setOnClickListener((View v)->
        {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("",  appVersion.supportedLanguages.toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.VersionDialog_Clipboard), Toast.LENGTH_SHORT).show();
        });

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

