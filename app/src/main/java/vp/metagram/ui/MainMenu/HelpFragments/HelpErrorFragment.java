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

public class HelpErrorFragment extends Fragment
{
    int itemsNo = 3;

    NiceSpinner errorSpinner;
    View root;
    ImageView img;
    String[] errorContent = new String[itemsNo];
    String[] errorSiteLinks = new String[itemsNo];
    String[] errorVideoLinks = new String[itemsNo];
    TextView error_content;
    ScrollView error_scroll;
    int animationDuration = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_help_error, container, false);

        if ( Locale.getDefault().getLanguage().trim().equals("fa"))
        {
            root.setRotationY(180);
        }

        loadContent();

        error_content = root.findViewById(R.id.error_content);
        setTextViewFontForMessage(getActivity(), error_content);

        error_scroll = root.findViewById(R.id.error_scroll);

        errorSpinner = root.findViewById(R.id.errorSpinner);
        List<String> errorDataSet = new LinkedList<>(Arrays.asList(
                " ", // No 0
                getResources().getString(R.string.e_login_title), //  No 1
                getResources().getString(R.string.e_automaticReport_title), // No 2
                getResources().getString(R.string.e_reportErrors_title)
        ));


        errorSpinner.attachDataSource(errorDataSet);

        setTextViewFontForMessage(getActivity(), errorSpinner);


        errorSpinner.addOnItemClickListener((parent, view, position, id) -> {

            showContent(position);
        });

        img = root.findViewById(R.id.error_img);

        return root;
    }

    public void selectLoginError()
    {
        errorSpinner.setSelectedIndex(1);
        showContent(1);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        errorSpinner.setText(getResources().getString(R.string.pleaseChoose_title));
        prepareNiceSpinner(errorSpinner);
    }

    public void loadContent()
    {
        errorContent[0] = getResources().getString(R.string.e_login_content);
        errorContent[1] = getResources().getString(R.string.e_automaticReport_content);
        errorContent[2] = getResources().getString(R.string.e_reportErrors_content);
    }

    public void showContent(int id)
    {
        if (id == 0)
        {
            if (error_scroll.getVisibility() == View.VISIBLE)
            {
                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(animationDuration);

                fadeOut.setAnimationListener(new Animation.AnimationListener()
                {
                    public void onAnimationEnd(Animation animation)
                    {
                        error_scroll.setVisibility(View.GONE);

                        errorSpinner.setText(getResources().getString(R.string.pleaseChoose_title));
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

                error_scroll.startAnimation(fadeOut);
            }
            return;
        }


        if (error_scroll.getVisibility() == View.VISIBLE)
        {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(animationDuration);

            fadeOut.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    error_scroll.setVisibility(View.GONE);

                    error_content.setText(errorContent[id-1]);

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
                            error_scroll.setVisibility(View.VISIBLE);
                        }
                    });

                    error_scroll.startAnimation(fadeIn);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            error_scroll.startAnimation(fadeOut);
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

                    error_content.setText(errorContent[id-1]);

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
                            error_scroll.setVisibility(View.VISIBLE);
                        }
                    });

                    error_scroll.startAnimation(fadeIn);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            img.startAnimation(fadeOut);
        }
    }

}
