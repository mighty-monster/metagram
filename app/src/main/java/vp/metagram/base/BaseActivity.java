package vp.metagram.base;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import vp.metagram.utils.LocaleManager;

import static android.content.pm.PackageManager.GET_META_DATA;
import static vp.metagram.general.variables.logger;

/**
 * Created by arash on 2/12/18.
 */

public class BaseActivity extends AppCompatActivity
{

    LocaleManager localeManager = new LocaleManager();

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(localeManager.setLocale(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        resetTitle();

    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1)
        {
            localeManager.setLocale(this);
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    private void resetTitle()
    {
        try
        {
            int label = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA).labelRes;
            if ( label != 0 )
            {
                setTitle(label);
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            logger.logError(this.getClass().getName(),
                    "Error in resetting title for locale change.\n",e);
        }

    }



}
