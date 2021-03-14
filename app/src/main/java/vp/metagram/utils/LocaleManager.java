package vp.metagram.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

import static vp.metagram.general.variables.appVersion;
import static vp.metagram.general.variables.logger;

/**
 * Created by arash on 2/12/18.
 */

public class LocaleManager
{

    public static Context setLocale(Context context)
    {
        return setNewLocale(context, getLanguage(context));
    }

    public static Context setNewLocale(Context c, String language)
    {
        persistLanguage(c, language);
        return updateResources(c, language);
    }

    public static String getLanguage(Context context)
    {
        if (appVersion == null)
        {
            try
            {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                appVersion = VersionUtils.createFromVersionString(pInfo.versionName);
            }
            catch (Exception e)
            {
                logger.logWTF("Local Manager",
                        "Version creation failed.\n", e);
            }
        }

        if (appVersion.supportedLanguages.get(0) == VersionUtils.LanguageType.persian)
        {return "fa";}
        else
        {return "en";}
    }

    private static void persistLanguage(Context context, String language) { }

    private static Context updateResources(Context context, String language)
    {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        configuration.setLocale(locale);
        context = context.createConfigurationContext(configuration);
        return context;
    }
}
