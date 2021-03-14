package vp.metagram.ui.MainMenu.HelpFragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;


import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

import static vp.metagram.general.variables.dbMetagram;


public class HelpInteractiveItems
{
    public static String isShowingHelp = "isShowingHelp";

    public static String RegisterFragment_Help = "RegisterFragment_Help";

    public static String ProfileFragment_Help = "ProfileFragment_Help";
    public static String ProfileFragment_delete = "ProfileFragment_delete";
    public static String ProfileFragment_edit = "ProfileFragment_edit";
    public static String ProfileFragment_status = "ProfileFragment_status";
    public static String ProfileFragment_downloads = "ProfileFragment_downloads";
    public static String ProfileFragment_downloadHighlight = "ProfileFragment_downloadHighlight";
    public static String ProfileFragment_downloadHistory = "ProfileFragment_downloadHistory";

    public static String ProcessFragment_Help = "ProcessFragment_Help";
    public static String ProcessFragment_progress = "ProcessFragment_progress";
    public static String ProcessFragment_time = "ProcessFragment_time";
    public static String ProcessFragment_log = "ProcessFragment_log";

    public static String ResultFragment_Help = "ResultFragment_Help";
    public static String ResultFragment_rank = "ResultFragment_rank";
    public static String ResultFragment_engagement = "ResultFragment_engagement";
    public static String ResultFragment_report = "ResultFragment_report";
    public static String ResultFragment_chart = "ResultFragment_chart";
    public static String ResultFragment_setting = "ResultFragment_setting";
    public static String ResultFragment_refresh = "ResultFragment_refresh";

    public static String ReportFragment_Help = "ReportFragment_Help";
    public static String ReportFragment_filter = "ReportFragment_filter";

    public static String AccntBrowser_Post = "AccntBrowser_Post";
    public static String AccntBrowser_order = "AccntBrowser_order";
    public static String AccntBrowser_story = "AccntBrowser_story";

    public static String AddOrder_Help = "AddOrder_Help";
    public static String AddOrder_Ruby = "AddOrder_Ruby";

    public static String DownloadStory_Search = "DownloadStory_Search";


    public static boolean decideToShowInteractiveHelp(String key)
    {
        return dbMetagram.getItemStatus(key) == 0;
    }


    public static void showInteractiveHelp(String key, Activity activity, String title, String content, View view, GuideListener guideListener, smartdevelop.ir.eram.showcaseviewlib.config.Gravity gravity)
    {

        if (decideToShowInteractiveHelp(key))
        {

            Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/FarBaseet.ttf");

            GuideView guideView = new GuideView.Builder(activity)
                    .setTitle(title)
                    .setContentText(content)
                    .setContentTypeFace(typeface)
                    .setTitleTypeFace(typeface)
                    .setTitleTextSize(12)
                    .setContentTextSize(10)
                    .setTargetView(view)
                    .setGuideListener(guideListener)
                    .setGravity(gravity)
                    .setDismissType(DismissType.outside)
                    .build();

            guideView.show();

            dbMetagram.setItemStatus(key,1);
        }
        else
        {
            dbMetagram.setItemStatus(isShowingHelp, 0);
        }
    }


}
