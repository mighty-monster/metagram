package vp.metagram.ui.MainMenu.HelpFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import vp.metagram.R;

import static vp.metagram.general.functions.prepareNiceSpinner;
import static vp.metagram.general.functions.setTextViewFontForMessage;

/**
 * Created by arash on 1/31/18.
 */

public class HelpTrainingFragment extends Fragment
{
    int itemsNo = 6;

    NiceSpinner trainingSpinner;
    View root;
    ImageView img;
    String[] helpContent = new String[itemsNo];
    String[] helpSiteLinks = new String[itemsNo];
    String[] helpVideoLinks = new String[itemsNo];

    TextView training_content;
    ScrollView training_scroll;
    int animationDuration = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root =  inflater.inflate(R.layout.fragment_help_training, container, false);

        if ( Locale.getDefault().getLanguage().trim().equals("fa"))
        {
            root.setRotationY(180);
        }

        loadContent();

        training_content = root.findViewById(R.id.training_content);
        setTextViewFontForMessage(getActivity(), training_content);


        training_scroll = root.findViewById(R.id.training_scroll);

        trainingSpinner = root.findViewById(R.id.trainingSpinner);
        List<String> trainingDataSet = new LinkedList<>(Arrays.asList(
                " ", // No 0
                getResources().getString(R.string.l_engagement_title), // No 1
                getResources().getString(R.string.l_ranking_title), // No 2
                getResources().getString(R.string.l_reports_title), // No 3
                getResources().getString(R.string.l_downloadPosts_title), // No 4
                getResources().getString(R.string.l_addStatisticsOrder_title), // No 5
                getResources().getString(R.string.l_buyingRubies_title) // No 6
        ));

        trainingSpinner.attachDataSource(trainingDataSet);

        setTextViewFontForMessage(getActivity(), trainingSpinner);


        trainingSpinner.addOnItemClickListener((parent, view, position, id) -> {

            showContent(position);
        });

        img = root.findViewById(R.id.training_img);

        return root;
    }



    @Override
    public void onResume()
    {
        super.onResume();
        trainingSpinner.setText(getResources().getString(R.string.pleaseChoose_title));
        prepareNiceSpinner(trainingSpinner);
    }


    public void loadContent()
    {
        helpContent[0] = getResources().getString(R.string.l_engagement_content);
        helpContent[1] = getResources().getString(R.string.l_ranking_content);
        helpContent[2] = getResources().getString(R.string.l_reports_content);
        helpContent[3] = getResources().getString(R.string.l_downloadPosts_content);
        helpContent[4] = getResources().getString(R.string.l_addStatisticsOrder_content);
        helpContent[5] = "";

    }

    public void showContent(int id)
    {
        if (id == 0)
        {

            if (training_scroll.getVisibility() == View.VISIBLE)
            {

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(animationDuration);


                fadeOut.setAnimationListener(new Animation.AnimationListener()
                {
                    public void onAnimationEnd(Animation animation)
                    {
                        training_scroll.setVisibility(View.GONE);

                        trainingSpinner.setText(getResources().getString(R.string.pleaseChoose_title));
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new AccelerateInterpolator());
                        fadeIn.setDuration(animationDuration);

                        fadeIn.setAnimationListener(new Animation.AnimationListener()
                        {
                            public void onAnimationEnd(Animation animation)
                            {}
                            public void onAnimationRepeat(Animation animation) {}
                            public void onAnimationStart(Animation animation)
                            {
                                img.setVisibility(View.VISIBLE);
                            }
                        });

                        img.startAnimation(fadeIn);
                    }
                    public void onAnimationRepeat(Animation animation) {}
                    public void onAnimationStart(Animation animation) {}
                });

                training_scroll.startAnimation(fadeOut);
            }
            return;
        }


        if (training_scroll.getVisibility() == View.VISIBLE)
        {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(animationDuration);

            fadeOut.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    training_scroll.setVisibility(View.GONE);

                    training_content.setText(helpContent[id-1]);

                    Animation fadeIn = new AlphaAnimation(0, 1);
                    fadeIn.setInterpolator(new AccelerateInterpolator());
                    fadeIn.setDuration(animationDuration);

                    fadeIn.setAnimationListener(new Animation.AnimationListener()
                    {
                        public void onAnimationEnd(Animation animation)
                        {}
                        public void onAnimationRepeat(Animation animation) {}
                        public void onAnimationStart(Animation animation)
                        {
                            training_scroll.setVisibility(View.VISIBLE);
                        }
                    });

                    training_scroll.startAnimation(fadeIn);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            training_scroll.startAnimation(fadeOut);
        }
        else if (img.getVisibility() == View.VISIBLE)
        {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(animationDuration);

            fadeOut.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    img.setVisibility(View.GONE);

                    training_content.setText(helpContent[id-1]);

                    Animation fadeIn = new AlphaAnimation(0, 1);
                    fadeIn.setInterpolator(new AccelerateInterpolator());
                    fadeIn.setDuration(animationDuration);

                    fadeIn.setAnimationListener(new Animation.AnimationListener()
                    {
                        public void onAnimationEnd(Animation animation)
                        {}
                        public void onAnimationRepeat(Animation animation) {}
                        public void onAnimationStart(Animation animation)
                        {
                            training_scroll.setVisibility(View.VISIBLE);
                        }
                    });

                    training_scroll.startAnimation(fadeIn);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            img.startAnimation(fadeOut);
        }
    }

}
