package vp.metagram.ui.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


import vp.metagram.R;

import static vp.metagram.general.functions.setTextViewFontForMessage;


public class ConfirmationDialog
{
    public void showDialog(Activity activity, String title , String msg, String buttonText, Runnable confirmClick)
    {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_confirmation);

        TextView dialog_title = dialog.findViewById(R.id.dialog_title);
        dialog_title.setText(title);
        setTextViewFontForMessage(activity,dialog_title);

        TextView dialog_content = dialog.findViewById(R.id.dialog_content);
        dialog_content.setText(msg);
        setTextViewFontForMessage(activity,dialog_content);

        Button dialogButton = dialog.findViewById(R.id.btn_dialog_confirm);
        dialogButton.setText(buttonText);
        setTextViewFontForMessage(activity,dialogButton);
        dialogButton.setOnClickListener(v ->
        {
            dialog.dismiss();
            if(confirmClick != null)
            {new Handler(Looper.getMainLooper()).post(confirmClick); }
        });

        Button cancelButton = dialog.findViewById(R.id.btn_dialog_cancel);
        cancelButton.setText(activity.getResources().getString(R.string.button_cancelCaption));
        setTextViewFontForMessage(activity,cancelButton);
        cancelButton.setOnClickListener(v ->
        {
            dialog.dismiss();
        });

        dialog.show();

    }
}
