package vp.metagram.ui.MainFragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsConfig;
import com.jawnnypoo.physicslayout.PhysicsFrameLayout;

import java.util.Random;
import java.util.concurrent.TimeUnit;


import vp.metagram.R;

import static vp.metagram.general.functions.prepareCardViewForAPIBefore21;
import static vp.metagram.general.variables.deviceInfoJS;
import static vp.metagram.general.variables.logger;
import static vp.metagram.general.variables.threadPoolExecutor;


public class IdleFragment extends Fragment
{
    View rootLayout;

    boolean isAttached = false;
    boolean isFirstTime = true;

    int no = 0;
    int topMarginAppearance = 40;

    int iconSize;
    int iconFadeInAnimationTime = 400;
    int iconFadeInAnimationTimeMargin = 600;
    int numberOfElements = 10;
    int iconAppearanceDelayMaxMargin = 3000;
    int iconAppearanceDelayMin = 1500;
    int physicsStopDelay = 10000;
    int physicsLayoutWith = -1;

    PhysicsFrameLayout physicsFrameLayout;

    Activity ownerActivity;

    Random random = new Random();

    int drawableType;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null )
        {
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_idle, container, false);
        physicsFrameLayout = rootLayout.findViewById(R.id.physics_layout);
        prepareCardViewForAPIBefore21((ViewGroup) rootLayout);
        return rootLayout;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        isAttached = true;
    }

    @Override
    public void onDetach()
    {
        isAttached = false;

        super.onDetach();
    }


    public void reset()
    {
        if ( !isAdded() ) {return;}
        if ( physicsFrameLayout != null )
        {

            new Handler(Looper.getMainLooper()).post(()->physicsFrameLayout.getPhysics().enablePhysics());

        }

        if (no >= numberOfElements)
        {
            isFirstTime = true;
        }
        no = 0;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if ( !isAdded() ) {return;}
        reset();

        if (isFirstTime)
        {
            runPhysicsAnimation();
            isFirstTime = false;
        }

    }

    @Override
    public void onPause()
    {
        super.onPause();

        new Handler(Looper.getMainLooper()).post(() ->
        {
            physicsFrameLayout.removeAllViews();
            physicsFrameLayout.getPhysics().disablePhysics();

        });


    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    private Drawable getNextDrawable()
    {
        drawableType = 1 + random.nextInt(4);
        switch ( drawableType )
        {
            case 1:
            case 2:
                return getResources().getDrawable(R.mipmap.ic_launcher_round);
            case 3:
                return getResources().getDrawable(R.drawable.ic_instagram);
            case 4:
                return getResources().getDrawable(R.drawable.ic_instagram_multi);
        }
        return getResources().getDrawable(R.mipmap.ic_launcher_round);
    }

    public void runPhysicsAnimation()
    {
        new Handler(Looper.getMainLooper()).post(() ->
        {
            if ( !isAdded() ) {return;}
            threadPoolExecutor.execute(physicsRunnable);
        });
    }


    Runnable physicsRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            ownerActivity = getActivity();
            iconSize = getResources().getDimensionPixelSize(R.dimen.falling_icon_size);
            final Handler mainHandler = new Handler(ownerActivity.getMainLooper());

            try
            {

                try
                {
                    if ( !isAttached )
                    {return;}

                    Thread.sleep(iconAppearanceDelayMin / 3);

                    new Handler(Looper.getMainLooper()).post(() ->
                    {
                        try
                        {
                            physicsFrameLayout.getPhysics().enablePhysics();

                            ImageView imageView = new ImageView(ownerActivity);
                            imageView.setImageDrawable(getNextDrawable());

                            if ( physicsLayoutWith <= 0 )
                            {
                                physicsLayoutWith = physicsFrameLayout.getWidth();
                            }
                            if ( physicsLayoutWith <= 0 )
                            {
                                physicsLayoutWith = deviceInfoJS.getJSONObject("Software").getInt("widthPixels") - 50;
                            }
                            if ( physicsLayoutWith <= 0 )
                            {
                                physicsLayoutWith = 350;
                            }

                            int randomTop = random.nextInt(topMarginAppearance);
                            int randomSide = random.nextInt(physicsLayoutWith);

                            imageView.setPadding(0, 0, 0, 0);
                            Animation fadeIn = new AlphaAnimation(0, 1);
                            fadeIn.setInterpolator(new DecelerateInterpolator());
                            fadeIn.setDuration(iconFadeInAnimationTime + random.nextInt(iconFadeInAnimationTimeMargin));


                            int hSize = iconSize;
                            int wSize = iconSize;


                            PhysicsConfig config = PhysicsConfig.create();
                            config.shapeType = PhysicsConfig.SHAPE_TYPE_CIRCLE;

                            switch ( drawableType )
                            {
                                case 1:
                                    hSize = iconSize * 1;
                                    wSize = hSize;

                                    Physics.setPhysicsConfig(imageView, config);
                                    break;
                                case 2:
                                    hSize = (int) (iconSize * 1.2);
                                    wSize = hSize;

                                    Physics.setPhysicsConfig(imageView, config);
                                    break;
                                case 3:
                                    hSize = (int) (iconSize * 1.3);
                                    wSize = hSize;

                                    Physics.setPhysicsConfig(imageView, config);
                                    break;
                                case 4:
                                    wSize = (int) ((iconSize * 2.67) * 0.75);
                                    hSize = (int) (iconSize * 0.85);
                                    break;
                            }

                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(wSize, hSize);
                            params.leftMargin = randomSide;
                            params.rightMargin = randomSide;
                            params.topMargin = randomTop;
                            params.bottomMargin = 0;


                            physicsFrameLayout.addView(imageView, params);
                            AnimationSet animation = new AnimationSet(true);
                            animation.addAnimation(fadeIn);
                            imageView.setAnimation(animation);
                        }
                        catch (Exception e)
                        {
                            logger.logError(this.getClass().getName(),
                                    "Reading screen size for Idle fragment animation.\n", e);
                        }
                    });

                }

                finally
                {
                    if ( isAttached )
                    {
                        int iconAppearanceDelay = iconAppearanceDelayMin + random.nextInt(iconAppearanceDelayMaxMargin);
                        if ( no < numberOfElements )
                        {
                            no++;
                            threadPoolExecutor.schedule(physicsRunnable,iconAppearanceDelay, TimeUnit.MILLISECONDS);
                        }
                        else
                        {
                            mainHandler.postDelayed(() ->
                            {
                                if ( isAttached )
                                { physicsFrameLayout.getPhysics().disablePhysics(); }

                            }, physicsStopDelay);

                            return;
                        }
                    }
                    else
                    {
                        return;
                    }
                }

            }

            catch (Exception e)
            {
                logger.logError(this.getClass().getName(),
                        "error in Idle fragment animation main thread function.\n", e);
            }
        }

    };

}
