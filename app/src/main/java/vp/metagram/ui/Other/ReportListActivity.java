package vp.metagram.ui.Other;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.Dialogs.InformationDialog;

import static vp.metagram.general.functions.setTextViewFontForMessage;

public class ReportListActivity extends BaseActivity
{
    ImageButton F_Section_F_Parameter_ImageButton;
    TextView F_Section_notFollowedBack;
    TextView F_Section_notFollowingBack;
    TextView F_Section_title;


    ImageButton P_Section_P_Parameter_ImageButton;
    TextView P_Section_mostLikedPosts;
    TextView P_Section_mostCommentedPosts;
    TextView P_Section_mostViewedPosts;
    TextView P_Section_title;


    ImageButton PD_Section_P_Parameter_ImageButton;
    ImageButton PD_Section_D_Parameter_ImageButton;
    TextView PD_Section_peopleLikedMost;
    TextView PD_Section_peopleCommentedMost;
    TextView PD_Section_peopleEngagedMost;
    TextView PD_Section_title;


    ImageButton FPD_Section_P_Parameter_ImageButton;
    ImageButton FPD_Section_D_Parameter_ImageButton;
    ImageButton FPD_Section_F_Parameter_ImageButton;
    TextView FPD_Section_lazyFollowers;
    TextView FPD_Section_likedButDidNotFollow;
    TextView FPD_Section_commentedButDidNotFollow;
    TextView FPD_Section_title;


    ImageButton FR_Section_F_Parameter_ImageButton;
    ImageButton FR_Section_R_Parameter_ImageButton;
    TextView FR_Section_unfollowedYou;
    TextView FR_Section_youUnfollowed;
    TextView FR_Section_title;
    TextView FR_Section_Description;


    ImageButton RPD_Section_P_Parameter_ImageButton;
    ImageButton RPD_Section_D_Parameter_ImageButton;
    ImageButton RPD_Section_R_Parameter_ImageButton;
    TextView RPD_Section_removedLikes;
    TextView RPD_Section_removedComments;
    TextView RPD_Section_title;
    TextView RPD_Section_Description2;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        findUIElements();
    }

    public void findUIElements()
    {

        View.OnClickListener F_Parameter_OnClick = (View v)->
        {
            InformationDialog dialog = new InformationDialog();
            dialog.showDialog(this,
                    getString(R.string.informationDialog_Title),
                    getString(R.string.addOrder_fParameterTitle),
                    getString(R.string.button_ok), null);
        };

        View.OnClickListener P_Parameter_OnClick = (View v)->
        {
            InformationDialog dialog = new InformationDialog();
            dialog.showDialog(this,
                    getString(R.string.informationDialog_Title),
                    getString(R.string.addOrder_pParameterTitle),
                    getString(R.string.button_ok), null);
        };

        View.OnClickListener D_Parameter_OnClick = (View v)->
        {
            InformationDialog dialog = new InformationDialog();
            dialog.showDialog(this,
                    getString(R.string.informationDialog_Title),
                    getString(R.string.addOrder_dParameterTitle),
                    getString(R.string.button_ok), null);
        };

        View.OnClickListener R_Parameter_OnClick = (View v)->
        {
            InformationDialog dialog = new InformationDialog();
            dialog.showDialog(this,
                    getString(R.string.informationDialog_Title),
                    getString(R.string.addOrder_rParameterTitle),
                    getString(R.string.button_ok), null);
        };


        //////////////////////////////////////////////////////////
        F_Section_F_Parameter_ImageButton = findViewById(R.id.ReportList_F_Section_F_Parameter_ImageView);
        F_Section_F_Parameter_ImageButton.setOnClickListener(F_Parameter_OnClick);

        F_Section_notFollowedBack = findViewById(R.id.ReportList_notFollowedBack_TextView);
        setTextViewFontForMessage(this,F_Section_notFollowedBack);

        F_Section_notFollowingBack = findViewById(R.id.ReportList_notFollowingBack_TextView);
        setTextViewFontForMessage(this,F_Section_notFollowingBack);

        F_Section_title = findViewById(R.id.ReportList_F_Section_Title);
        setTextViewFontForMessage(this,F_Section_title);

        //////////////////////////////////////////////////////////
        P_Section_P_Parameter_ImageButton = findViewById(R.id.ReportList_P_Section_P_Parameter_ImageView);
        P_Section_P_Parameter_ImageButton.setOnClickListener(P_Parameter_OnClick);

        P_Section_mostLikedPosts = findViewById(R.id.ReportList_postsWithMostLikes_TextView);
        setTextViewFontForMessage(this,P_Section_mostLikedPosts);

        P_Section_mostCommentedPosts  = findViewById(R.id.ReportList_postsWithMostComments_TextView);
        setTextViewFontForMessage(this,P_Section_mostCommentedPosts);

        P_Section_mostViewedPosts = findViewById(R.id.ReportList_postsWithMostViews_TextView);
        setTextViewFontForMessage(this,P_Section_mostViewedPosts);

        P_Section_title = findViewById(R.id.ReportList_P_Section_Title);
        setTextViewFontForMessage(this,P_Section_title);


        //////////////////////////////////////////////////////////
        PD_Section_P_Parameter_ImageButton = findViewById(R.id.ReportList_PD_Section_P_Parameter_ImageView);
        PD_Section_P_Parameter_ImageButton.setOnClickListener(P_Parameter_OnClick);

        PD_Section_D_Parameter_ImageButton = findViewById(R.id.ReportList_PD_Section_D_Parameter_ImageView);
        PD_Section_D_Parameter_ImageButton.setOnClickListener(D_Parameter_OnClick);

        PD_Section_peopleLikedMost = findViewById(R.id.ReportList_peopleLikedMost_TextView);
        setTextViewFontForMessage(this,PD_Section_peopleLikedMost);
        PD_Section_peopleLikedMost.setText(PD_Section_peopleLikedMost.getText().toString());

        PD_Section_peopleEngagedMost = findViewById(R.id.ReportList_peopleEngagedMost_TextView);
        setTextViewFontForMessage(this,PD_Section_peopleEngagedMost);
        PD_Section_peopleEngagedMost.setText(PD_Section_peopleEngagedMost.getText().toString());

        PD_Section_peopleCommentedMost = findViewById(R.id.ReportList_peopleCommentedMost_TextView);
        setTextViewFontForMessage(this,PD_Section_peopleCommentedMost);

        PD_Section_title = findViewById(R.id.ReportList_PD_Section_Title);
        setTextViewFontForMessage(this,PD_Section_title);



        //////////////////////////////////////////////////////////
        FPD_Section_P_Parameter_ImageButton = findViewById(R.id.ReportList_FPD_Section_P_Parameter_ImageView);
        FPD_Section_P_Parameter_ImageButton.setOnClickListener(P_Parameter_OnClick);

        FPD_Section_D_Parameter_ImageButton = findViewById(R.id.ReportList_FPD_Section_D_Parameter_ImageView);
        FPD_Section_D_Parameter_ImageButton.setOnClickListener(D_Parameter_OnClick);

        FPD_Section_F_Parameter_ImageButton = findViewById(R.id.ReportList_FPD_Section_F_Parameter_ImageView);
        FPD_Section_F_Parameter_ImageButton.setOnClickListener(F_Parameter_OnClick);

        FPD_Section_lazyFollowers = findViewById(R.id.ReportList_lazyFollowers_TextView);
        setTextViewFontForMessage(this,FPD_Section_lazyFollowers);
        FPD_Section_lazyFollowers.setText(FPD_Section_lazyFollowers.getText().toString());

        FPD_Section_likedButDidNotFollow  = findViewById(R.id.ReportList_likedButDidNotFollow_TextView);
        setTextViewFontForMessage(this,FPD_Section_likedButDidNotFollow);
        FPD_Section_likedButDidNotFollow.setText(FPD_Section_likedButDidNotFollow.getText().toString());

        FPD_Section_commentedButDidNotFollow = findViewById(R.id.ReportList_commentedButDidNotFollow_TextView);
        setTextViewFontForMessage(this,FPD_Section_commentedButDidNotFollow);

        FPD_Section_title= findViewById(R.id.ReportList_FPD_Section_Title);
        setTextViewFontForMessage(this,FPD_Section_title);



        //////////////////////////////////////////////////////////
        FR_Section_F_Parameter_ImageButton = findViewById(R.id.ReportList_FR_Section_F_Parameter_ImageView);
        FR_Section_F_Parameter_ImageButton.setOnClickListener(F_Parameter_OnClick);

        FR_Section_R_Parameter_ImageButton = findViewById(R.id.ReportList_FR_Section_R_Parameter_ImageView);
        FR_Section_R_Parameter_ImageButton.setOnClickListener(R_Parameter_OnClick);

        FR_Section_unfollowedYou = findViewById(R.id.ReportList_peopleUnfollowedYou_TextView);
        setTextViewFontForMessage(this,FR_Section_unfollowedYou);

        FR_Section_youUnfollowed = findViewById(R.id.ReportList_peopleYouUnfollowed_TextView);
        setTextViewFontForMessage(this,FR_Section_youUnfollowed);

        FR_Section_title= findViewById(R.id.ReportList_FR_Section_Title);
        setTextViewFontForMessage(this,FR_Section_title);

        FR_Section_Description = findViewById(R.id.ReportList_FR_Section_Description);
        setTextViewFontForMessage(this,FR_Section_Description);



        //////////////////////////////////////////////////////////
        RPD_Section_P_Parameter_ImageButton = findViewById(R.id.ReportList_RPD_Section_P_Parameter_ImageView);
        RPD_Section_P_Parameter_ImageButton.setOnClickListener(P_Parameter_OnClick);

        RPD_Section_D_Parameter_ImageButton = findViewById(R.id.ReportList_RPD_Section_D_Parameter_ImageView);
        RPD_Section_D_Parameter_ImageButton.setOnClickListener(D_Parameter_OnClick);

        RPD_Section_R_Parameter_ImageButton = findViewById(R.id.ReportList_RPD_Section_R_Parameter_ImageView);
        RPD_Section_R_Parameter_ImageButton.setOnClickListener(R_Parameter_OnClick);

        RPD_Section_removedLikes = findViewById(R.id.ReportList_removedLikes_TextView);
        setTextViewFontForMessage(this,RPD_Section_removedLikes);
        RPD_Section_removedLikes.setText(RPD_Section_removedLikes.getText().toString());

        RPD_Section_removedComments = findViewById(R.id.ReportList_removedComments_TextView);
        setTextViewFontForMessage(this,RPD_Section_removedComments);

        RPD_Section_title = findViewById(R.id.ReportList_RPD_Section_Title);
        setTextViewFontForMessage(this,RPD_Section_title);

        RPD_Section_Description2 = findViewById(R.id.ReportList_RPD_Section_Description2);
        setTextViewFontForMessage(this,RPD_Section_Description2);
    }
}
