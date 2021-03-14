package vp.metagram.general;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.SystemClock;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


import vp.metagram.R;
import vp.metagram.ui.AccBrowser.WebViewerActivity;
import vp.metagram.utils.DownloadService;
import vp.metagram.utils.MetaServerException;
import vp.metagram.utils.instagram.InstagramAgent;
import vp.metagram.utils.instagram.InstagramAgentStatus;
import vp.metagram.utils.instagram.MetagramAgent;
import vp.metagram.utils.instagram.executors.statistics.execFlow.StatsAccountExecFlow;
import vp.metagram.utils.instagram.executors.statistics.execFlow.StatsPostExecFlow;
import vp.metagram.utils.instagram.types.Comment;
import vp.metagram.utils.instagram.types.PostMedia;
import vp.metagram.utils.instagram.types.User;
import vp.tools.datetime.JalaliCalendar;
import vp.tools.http.iEndPoints;
import vp.tools.http.iHttpClient;
import vp.tools.io.iFileSystemUtils;
import vp.igpapi.IGWAException;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Looper.getMainLooper;
import static vp.metagram.general.channels.CHANNEL_ID_FINISHED;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.appVersion;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.executionMode;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.serverAddress;
import static vp.metagram.types.enumRank.rankA;
import static vp.metagram.types.enumRank.rankAPlus;
import static vp.metagram.types.enumRank.rankAPlusPlus;
import static vp.metagram.types.enumRank.rankB;
import static vp.metagram.types.enumRank.rankC;
import static vp.metagram.utils.instagram.executors.statistics.StatisticsExecutor.engagementPostsNumber;
import static vp.tools.io.iDBWithCipher.DBAESKeySize;
import static vp.tools.io.iFileSystemUtils.GetTempDir;
import static vp.tools.io.iFileSystemUtils.deleteDir;


public class functions
{

    public static String stackTraceToString(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String result = sw.toString();
        return result;
    }

    public static byte[] createAESKey() throws GeneralSecurityException, UnsupportedEncodingException
    {
        byte[] keyStart = "This is not a jok.".getBytes("utf-8");
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(keyStart);
        keyGenerator.init(DBAESKeySize * 8);
        SecretKey AESKey = keyGenerator.generateKey();

        byte[] key = AESKey.getEncoded();
        return key;
    }

    public static float dpToPixels(Context context, float dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static void setCheckBoxFontForMenu(Context context, CheckBox checkBox)
    {
        if (checkBox == null)
        {
            return;
        }

        Locale current = context.getResources().getConfiguration().locale;

        if (current.toString().equals("fa"))
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/DimaFantasy.ttf");
            checkBox.setTypeface(type);
        }
        else
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Arvo-Regular.ttf");
            checkBox.setTypeface(type);
        }

        return;
    }

    public static void setTextViewFontForMenu(Context context, TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        Locale current = context.getResources().getConfiguration().locale;

        if (current.toString().equals("fa"))
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/DimaFantasy.ttf");
            textView.setTypeface(type);
        }
        else
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Arvo-Regular.ttf");
            textView.setTypeface(type);
        }

        return;
    }

    public static void setTextViewFontArvoRegular(Context context, TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Arvo-Regular.ttf");
        textView.setTypeface(type);

        return;
    }

    public static void setCheckBoxFontArvoBold(Context context, CheckBox checkBox)
    {
        if (checkBox == null)
        {
            return;
        }

        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Arvo-Bold.ttf");
        checkBox.setTypeface(type);

        return;
    }

    public static void setTextViewFontArvoBold(Context context, TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Arvo-Bold.ttf");
        textView.setTypeface(type);

        return;
    }

    public static void setTextViewFontRank(Context context, TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/RankFont.otf");
        textView.setTypeface(type);

        return;
    }

    public static void setCheckBoxFontForMessage(Context context, TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        Locale current = context.getResources().getConfiguration().locale;

        if (current.toString().equals("fa"))
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/FarBaseet.ttf");
            textView.setTypeface(type);
        }
        else
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Arvo-Regular.ttf");
            textView.setTypeface(type);
        }

        return;
    }

    public static void setTextViewFontForMessage(Context context, TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        Locale current = context.getResources().getConfiguration().locale;

        if (current.toString().equals("fa"))
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/FarBaseet.ttf");
            textView.setTypeface(type);
        }
        else
        {
            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Arvo-Regular.ttf");
            textView.setTypeface(type);
        }

        return;
    }


    public static void openPostPageOnInstagram(Context context, String miniLink)
    {
        Uri uri = Uri.parse(getInstagramPostLink(miniLink));
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try
        {
            context.startActivity(likeIng);
        }
        catch (ActivityNotFoundException e)
        {
            Intent mIntent = new Intent(context, WebViewerActivity.class);
            mIntent.putExtra("postURL", getInstagramPostLink(miniLink));
            context.startActivity(mIntent);
        }
    }

    public static void openAccountPageOnInstagram(Context context, String username)
    {
        Uri uri = Uri.parse(getInstagramAccountLink(username));
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try
        {
            context.startActivity(likeIng);
        }
        catch (ActivityNotFoundException e)
        {
            Intent mIntent = new Intent(context, WebViewerActivity.class);
            mIntent.putExtra("postURL", getInstagramAccountLink(username));
            context.startActivity(mIntent);
        }
    }

    static public boolean isInternetAvailable(Context _context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
        {
            HttpURLConnection urlc = null;
            try
            {
                URL url = new URL("http://www.google.com/");
                urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000);
                urlc.connect();
                if (urlc.getResponseCode() == 200)
                {
                    return true;
                }
                else
                {
                    return false;
                }

            }
            catch (Exception e)
            {
                return false;
            }
            finally
            {
                if (urlc != null)
                {
                    urlc.disconnect();
                }
            }
        }
        else
        {
            return false;
        }
    }

    static public boolean isNetworkAvailable(Context _context)
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isConnectedWifi(Context _context)
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI);

    }

    public static boolean isConnectedMobile(Context _context)
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
    }


    static public byte[] readBytesFromInputStream(InputStream inputStream) throws IOException
    {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1)
        {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static SSLContext getSSLContext(Context _context) throws GeneralSecurityException, IOException
    {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = _context.getAssets().open("fOrder");
        Certificate ca;
        try
        {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        }
        finally
        {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        return context;
    }

    public static String getIV()
    {
        Random r = new Random();
        String result = "";
        for (int i = 0; i < 16; i++)
        {
            char ch = (char) (48 + r.nextInt(47));
            result = result + ch;
        }

        return result;
    }

    public static void animateNumber(int initialValue, int finalValue, final TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(valueAnimator1 -> textView.setText(valueAnimator1.getAnimatedValue().toString()));

        valueAnimator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                textView.setText(String.format(Locale.ENGLISH, "%,d", finalValue));
            }
        });

        valueAnimator.start();

    }

    public static void configShimmer(Context context,
                                     String text,
                                     ShimmerFrameLayout externalShimmer,
                                     ShimmerFrameLayout internalShimmer,
                                     TextView externalTextView,
                                     TextView internalTextView,
                                     int duration)
    {
        externalShimmer.setIntensity(1);

        internalShimmer.setDuration(duration);
        internalShimmer.setRepeatDelay(4000);
        internalShimmer.setIntensity(0.2f);

        internalTextView.setText(text);
        externalTextView.setText(text);

        setTextViewFontForMenu(context, externalTextView);
        setTextViewFontForMenu(context, internalTextView);
        internalShimmer.startShimmerAnimation();
    }

    public static String getUsernameFromTag(String tag)
    {
        return tag.substring(tag.indexOf("**") + "**".length(), tag.lastIndexOf("**"));
    }

    public static Thread getThreadByName(String threadName)
    {
        for (Thread thread : Thread.getAllStackTraces().keySet())
        {
            if (thread.getName().equals(threadName)) return thread;
        }
        return null;
    }


    public static Random randomDelay = new Random();

    public static int getRandomDelay()
    {
        return getRandomDelay(deviceSettings.statisticsDelayTime);
    }

    public static int getRandomDelay(int baseDelay)
    {
        int marginDelay;
        int finalDelay;

        marginDelay = baseDelay / 3;

        marginDelay = randomDelay.nextInt(marginDelay);

        finalDelay = baseDelay + marginDelay;

        return finalDelay;
    }

    public static String getDateFromTimeStamp(Context context, long dateTime)
    {
        String result = "";
        Locale current = context.getResources().getConfiguration().locale;

        Date date = new Date(dateTime);
        Format format = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (current.toString().equals("fa"))
        {
            result = JalaliCalendar.gregorianToJalali(new JalaliCalendar.YearMonthDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))).toString();

        }
        else
        {
            result = format.format(date);
        }

        return result;
    }

    public static String getDateFromTimeStampRevert(Context context, long dateTime)
    {
        String result = "";
        Locale current = context.getResources().getConfiguration().locale;

        Date date = new Date(dateTime);
        Format format = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (current.toString().equals("fa"))
        {
            result = JalaliCalendar.gregorianToJalali(new JalaliCalendar.YearMonthDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))).toStringRevert();

        }
        else
        {
            result = format.format(date);
        }

        return result;
    }

    public static String getGregorianDateTimeFromTimeStamp(long dateTime)
    {
        String result = "";
        Date date = new Date(dateTime);
        Format format = new SimpleDateFormat("yyyy-MM-dd-hh-mm", Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        result = format.format(date);

        return result;
    }

    public static String getGregorianDateTimeWithSecondsFromTimeStamp(long dateTime)
    {
        String result = "";
        Date date = new Date(dateTime);
        Format format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        result = format.format(date);

        return result;
    }

    public static String getDateTimeFromTimeStampRevert(Context context, long dateTime)
    {
        String result = "";
        Locale current = context.getResources().getConfiguration().locale;

        Date date = new Date(dateTime);
        Format format = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        String hourStr;
        String minutesStr;

        if (hour < 10)
        {
            hourStr = String.format("%d%d", 0, hour);
        }
        else
        {
            hourStr = String.format("%d", hour);
        }

        if (minutes < 10)
        {
            minutesStr = String.format("%d%d", 0, minutes);
        }
        else
        {
            minutesStr = String.format("%d", minutes);
        }

        if (current.toString().equals("fa"))
        {
            result = JalaliCalendar.gregorianToJalali(new JalaliCalendar.YearMonthDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))).toString();
            result = String.format("%s %s:%s", result, hourStr, minutesStr);
        }
        else
        {
            result = format.format(date);
        }

        return result;
    }


    public static String convertRank(String input)
    {
        String result = "";

        if (input.equals(rankAPlusPlus.toString()))
        {
            result = "A++";
        }
        if (input.equals(rankAPlus.toString()))
        {
            result = "A+";
        }
        else if (input.equals(rankA.toString()))
        {
            result = "A";
        }
        else if (input.equals(rankB.toString()))
        {
            result = "B";
        }
        else if (input.equals(rankC.toString()))
        {
            result = "C";
        }

        return result;
    }

    public static float convertRankToIndicator(String input)
    {
        float result = 0f;

        if (input.equals("A++"))
        {
            result = 5f;
        }
        else if (input.equals("A+"))
        {
            result = 4f;
        }
        else if (input.equals("A"))
        {
            result = 3f;
        }
        else if (input.equals("B"))
        {
            result = 2f;
        }
        else if (input.equals("C"))
        {
            result = 1f;
        }

        return result;
    }

    public static void configureCheckBoxDirection(Context context, CheckBox checkBox)
    {
        if (checkBox == null)
        {
            return;
        }

        Locale current = context.getResources().getConfiguration().locale;

        if (current.toString().equals("fa"))
        {
            checkBox.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        }

        return;
    }

    public static String repeatSingleQuotes(String input)
    {
        return input.replaceAll("'", "''");
    }

    public static String getInstagramImageLink_Medium(String miniLink)
    {
        return String.format(Locale.ENGLISH, "https://www.instagram.com/p/%s/media/?size=m", miniLink);
    }

    public static String getInstagramPostLink(String miniLink)
    {
        return String.format(Locale.ENGLISH, "https://www.instagram.com/p/%s/", miniLink);
    }


    public static String getInstagramAccountLink(String username)
    {
        return String.format(Locale.ENGLISH, "https://www.instagram.com/%s/", username);
    }


    public static Drawable convertDrawableToGray(Drawable drawable)
    {
        if (drawable == null)
            return null;

        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    public static Drawable convertDrawableToBlue(Drawable drawable)
    {
        if (drawable == null)
            return null;

        Drawable res = drawable.mutate();
        res.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
        return res;
    }

    public static Drawable convertDrawableToToggled(Drawable drawable)
    {
        if (drawable == null)
            return null;

        Drawable res = drawable.mutate();
        res.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
        return res;
    }


    public static Drawable convertDrawableToGrayScale(Drawable drawable)
    {
        if (drawable == null)
            return null;

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        drawable.setColorFilter(filter);

        return drawable;
    }

    public static void setImageButtonEnabledWithGrayScale(Context context, boolean enabled,
                                                          ImageButton item, int iconResId)
    {
        if (context == null)
        {
            return;
        }
        Drawable originalIcon = AppCompatResources.getDrawable(context, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);

        if (!enabled)
        {
            item.setAlpha(0.4f);
        }
        else
        {
            item.setAlpha(1.0f);
        }
    }

    public static void setImageButtonEnabled(Context context, boolean enabled,
                                             ImageButton item, int iconResId)
    {
        if (context == null)
        {
            return;
        }
        item.setEnabled(enabled);
        Drawable originalIcon = AppCompatResources.getDrawable(context, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGray(originalIcon);
        item.setImageDrawable(icon);
    }

    public static void setImageButtonEnabledForProgressFragment(Context context, boolean enabled,
                                                                ImageButton item, int iconResId)
    {
        if (context == null)
        {
            return;
        }
        item.setEnabled(enabled);
        Drawable originalIcon = AppCompatResources.getDrawable(context, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToBlue(originalIcon);
        item.setImageDrawable(icon);
    }

    public static void setImageViewEnabled(Context context, boolean enabled,
                                           ImageView item, int iconResId)
    {
        if (context == null)
        {
            return;
        }
        Drawable originalIcon =  AppCompatResources.getDrawable(context, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGray(originalIcon);
        item.setImageDrawable(icon);
    }

    public static void setImageViewEnabledForWhitelist(Context context, boolean enabled,
                                                       ImageView item, int iconResId)
    {
        if (context == null)
        {
            return;
        }
        Drawable originalIcon = AppCompatResources.getDrawable(context, iconResId);
        Drawable icon = enabled ? convertDrawableToGray(originalIcon) : convertDrawableToBlue(originalIcon);
        item.setImageDrawable(icon);
    }

    public static void toggleImageButton(Context context, boolean toggled,
                                         ImageButton item, int iconResId)
    {
        if (context == null)
        {
            return;
        }
        Drawable originalIcon = AppCompatResources.getDrawable(context, iconResId);
        Drawable icon = !toggled ? originalIcon : convertDrawableToToggled(originalIcon);
        item.setImageDrawable(icon);
    }

    /*public static void openAccountPageOnInstagram(Context context, String username)
    {
        Uri uri = Uri.parse(getInstagramAccountLink(username));
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try
        {
            context.startActivity(likeIng);
        }
        catch (ActivityNotFoundException e)
        {
            Intent mIntent = new Intent(context, PostContentViewerActivity.class);
            mIntent.putExtra("postURL", getInstagramAccountLink(username));
            context.startActivity(mIntent);
        }
    }*/

    static public String convertNumbersToHumanReadableFormat(long input)
    {
        if (input < 10000)
        {
            return String.format(Locale.ENGLISH, "%,d", input);
        }
        else if (input < 1000 * 1000)
        {
            double no = (double) input / 1000;
            return String.format(Locale.ENGLISH, "%.2fK", no);
        }
        else if (input < 1000 * 1000 * 1000)
        {
            double no = (double) input / 1000 / 1000;
            return String.format(Locale.ENGLISH, "%.2fM", no);
        }
        else
        {
            double no = (double) input / 1000 / 1000 / 1000;
            return String.format(Locale.ENGLISH, "%.2fB", no);
        }
    }


    static public String getFileAsHex(String fileURL) throws IOException
    {
        byte[] file;
        URL url = new URL(fileURL);
        HttpsURLConnection urlConnection =
                (HttpsURLConnection) url.openConnection();
        InputStream inputStream = urlConnection.getInputStream();


        file = readBytesFromInputStream(inputStream);

        return Base64.encodeToString(file, android.util.Base64.DEFAULT);
    }


    static public void shakeAnimation(Activity activity, View v)
    {
        float degree = 2;
        int max = 2;
        int duration = 60;
        AtomicInteger count = new AtomicInteger();

        if (activity == null || v == null)
        {
            return;
        }

        Runnable[] runnable = new Runnable[1];
        runnable[0] = () ->
                v.animate().rotation(degree).setDuration(duration).withEndAction(
                        () -> v.animate().rotation(-degree).setDuration(duration).withEndAction
                                (
                                        () ->
                                        {
                                            if (activity == null || v == null)
                                            {
                                                return;
                                            }
                                            if (count.get() < max)
                                            {
                                                activity.runOnUiThread(runnable[0]);
                                                count.getAndIncrement();
                                            }
                                            else
                                            {
                                                v.animate().rotation(-0).setDuration(duration).start();
                                            }
                                        }
                                )
                                .start()).start();

        if (activity == null || v == null)
        {
            return;
        }

        activity.runOnUiThread(runnable[0]);
    }

    static public boolean decideUsingAPICookies(MetagramAgent agent, boolean isPrivate, long IPK)
    {
        boolean result = false;
        if (agent.activeAgent.userID == IPK || isPrivate)
        {
            result = true;
        }
        return result;
    }


    static public List<CardView> getCardViews(ViewGroup root)
    {
        List<CardView> result = new ArrayList<>();

        int noOfChild = root.getChildCount();
        for (int i = 0; i < noOfChild; i++)
        {
            View child = root.getChildAt(i);

            if (child instanceof CardView)
            {
                result.add((CardView) child);
            }

            if (child instanceof ViewGroup)
            {
                result.addAll(getCardViews((ViewGroup) child));
            }
        }

        return result;
    }

    static public void prepareCardViewForAPIBefore21(ViewGroup root)
    {
        prepareCardViewForAPIBefore21(root, 3);
    }

    static public void prepareCardViewForAPIBefore21(ViewGroup root, int elevation)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            List<CardView> cardViews = getCardViews(root);

            for (CardView cardView : cardViews)
            {
                cardView.setMaxCardElevation(elevation);
                cardView.setCardElevation(elevation);
            }
        }
    }

    static public void createNotification(Context context, String title, String Content, Intent notificationIntent, boolean autoCancel, int id, boolean sticky, boolean silent)
    {


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_FINISHED)
                .setContentTitle(title)
                .setContentText(Content)
                .setOngoing(sticky)
                .setShowWhen(false)
                .setAutoCancel(autoCancel)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (notificationIntent != null)
        {
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (notificationIntent.resolveActivityInfo(context.getPackageManager(), 0) == null)
            {
                Toast.makeText(context, "Can not handle the request!",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                mBuilder.setContentIntent(intent);
            }
        }

        if (silent)
        {
            mBuilder.setSound(null);
            mBuilder.setDefaults(0);
        }
        else
        {
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        }

        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, mBuilder.build());

    }

    static public void cancelNotification(Context context, int id)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }


    public static void prepareNiceSpinner(NiceSpinner spinner)
    {

        try
        {
            Field field = null;

            field = spinner.getClass().getDeclaredField("arrowDrawable");
            field.setAccessible(true);

            Drawable arrow = (Drawable) field.get(spinner);

            if (arrow == null)
            {
                spinner.hideArrow();
            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public static long predictOrderTime(int noOfFollowers, int noOfFollowings, int noOfPosts, InstagramAgent agent, boolean F_Parameter, boolean P_Parameter, boolean D_Parameter)
    {

        InstagramAgentStatus agentStatus = agent.agentStatus;

        long result = 20;

        double totalDuration = 0;

        double followerRatio = noOfFollowers / agentStatus.averageFollowerNo > 1 ? 1f : noOfFollowers / agentStatus.averageFollowerNo;
        followerRatio = followerRatio < 0.2f ? 0.2f : followerRatio;

        double requestDuration = agentStatus.averageRequestDuration + deviceSettings.statisticsDelayTime;

        int effectiveNoOfPosts = engagementPostsNumber;
        if (P_Parameter)
        { effectiveNoOfPosts = noOfPosts; }

        int noOfCalls = 0;
        // No of Calls to gather posts
        noOfCalls += (int) (effectiveNoOfPosts / agentStatus.averagePostsListLength) + 1;
        // No of Calls to gather followers
        if (F_Parameter) { noOfCalls += noOfFollowers / agentStatus.averageFollowerListLength + 1;}
        // No of Calls to gather followings
        if (F_Parameter)
        {
            noOfCalls += noOfFollowings / agentStatus.averageFollowingListLength + 1;
        }
        // No of Calls to gather comments
        if (P_Parameter && D_Parameter)
        {
            noOfCalls += noOfPosts * agentStatus.averageCommentNo * followerRatio / agentStatus.averageCommentListLength + 1;
        }
        // No of Calls to gather likes
        if (P_Parameter && D_Parameter)
        {
            noOfCalls += noOfPosts * agentStatus.averageLikeNo * followerRatio / agentStatus.averageLikeListLength + 1;
        }

        double callsDuration = noOfCalls * requestDuration;

        // Database transactions duration
        // Inserting Posts
        double dbDuration = effectiveNoOfPosts * agentStatus.averageAddPostMediaToDBTime;
        // Inserting Followers
        if (F_Parameter) {dbDuration += noOfFollowers * agentStatus.averageAddFollowersToDBTime;}
        // Inserting Followings
        if (F_Parameter) {dbDuration += noOfFollowers * agentStatus.averageAddFollowingToDBTime;}
        // Inserting Likes
        if (P_Parameter && D_Parameter)
        {
            dbDuration += noOfPosts * agentStatus.averageLikeNo * followerRatio * agentStatus.averageAddLikeToDBTime;
        }
        // Inserting Comments
        if (P_Parameter && D_Parameter)
        {
            dbDuration += noOfPosts * agentStatus.averageCommentNo * followerRatio * agentStatus.averageAddCommentToDBTime;
        }


        totalDuration = callsDuration + dbDuration + agentStatus.averageFinalizationTime;

        result += (long) (totalDuration / 1000);

        return result;
    }

    public static long predictOrderTime_(int noOfFollowers, int noOfFollowings, int noOfPosts, InstagramAgent agent, boolean F_Parameter, boolean P_Parameter, boolean D_Parameter)
    {
        long result;

        double duration = 0;

        double requestTime = agent.agentStatus.averageRequestDuration + deviceSettings.statisticsDelayTime;

        double ratioFollower = noOfFollowers / agent.agentStatus.averageFollowerNo;

        if (ratioFollower > 1)
        {
            ratioFollower = 1f;
        }


        int predictedNoOfCallsForFollowers = (int) (noOfFollowers / agent.agentStatus.averageFollowerListLength) + 1;
        int predictedNoOfCallsForFollowings = (int) (noOfFollowings / agent.agentStatus.averageFollowingListLength) + 1;

        int effectiveNoOfPosts = 15;
        int predictedNoOfCallsForPosts = 2;
        if (P_Parameter)
        {
            effectiveNoOfPosts = noOfPosts;
            predictedNoOfCallsForPosts = (int) (noOfPosts / agent.agentStatus.averagePostsListLength) + 1;
        }

        duration += predictedNoOfCallsForPosts * requestTime + effectiveNoOfPosts * agent.agentStatus.averageAddPostMediaToDBTime;

        if (F_Parameter)
        {
            duration += (predictedNoOfCallsForFollowers + predictedNoOfCallsForFollowings) * requestTime
                    + noOfFollowers * agent.agentStatus.averageAddFollowersToDBTime
                    + noOfFollowings * agent.agentStatus.averageAddFollowingToDBTime;
        }

        if (D_Parameter)
        {
            duration += noOfPosts * (1 + (agent.agentStatus.averageCommentNo * ratioFollower / agent.agentStatus.averageCommentListLength + 1)) * requestTime
                    + agent.agentStatus.averageLikeNo * agent.agentStatus.averageAddLikeToDBTime * ratioFollower
                    + agent.agentStatus.averageCommentNo * agent.agentStatus.averageAddCommentToDBTime * ratioFollower;
        }

        duration += agent.agentStatus.averageFinalizationTime;

        result = (long) (duration / 1000);

        return result;
    }

    public static long predictStatisticsDataGatheringTime(StatsAccountExecFlow execFlow, InstagramAgent agent,
                                                          boolean F_Parameter, boolean P_Parameter, boolean D_Parameter)
    {
        InstagramAgentStatus agentStatus = agent.agentStatus;

        long result = 1;

        if (!execFlow.fetchAccountInfo)
        { return -1; }

        double totalDuration = 0;

        int noOfFollowers = execFlow.followersNo;
        int noOfFollowings = execFlow.followingNo;
        int noOfPosts = execFlow.postsNo;

        double followerRatio = noOfFollowers / agentStatus.averageFollowerNo > 1 ? 1f : noOfFollowers / agentStatus.averageFollowerNo;
        followerRatio = followerRatio < 0.2f ? 0.2f : followerRatio;

        double requestDuration = agentStatus.averageRequestDuration + deviceSettings.statisticsDelayTime;

        int remainingFollowers = noOfFollowers - execFlow.followersCounter;
        int remainingFollowings = noOfFollowings - execFlow.followingCounter;
        int remainingPosts;
        if (P_Parameter)
        {remainingPosts = noOfPosts - execFlow.postsCounter;}
        else
        {remainingPosts = engagementPostsNumber - execFlow.postsCounter > 0 ? engagementPostsNumber - execFlow.postsCounter : 0;}

        double noOfCalls = 0;
        double dbDuration = 0;

        if (F_Parameter)
        {
            noOfCalls += remainingFollowers / agentStatus.averageFollowerListLength;
            noOfCalls += remainingFollowings / agentStatus.averageFollowingListLength;

            dbDuration += remainingFollowers * agentStatus.averageAddFollowersToDBTime;
            dbDuration += remainingFollowings * agentStatus.averageAddFollowingToDBTime;
        }

        noOfCalls += remainingPosts / agentStatus.averagePostsListLength;
        dbDuration += remainingPosts * agentStatus.averageAddPostMediaToDBTime;

        if (D_Parameter)
        {
            for (StatsPostExecFlow postExecFlow : execFlow.postsStatsExecFlow)
            {
                if (!postExecFlow.fetchLikers)
                {
                    int remaining_likes = postExecFlow.likeNo - postExecFlow.likeCounter;
                    noOfCalls += remaining_likes / agentStatus.averageLikeListLength;

                    dbDuration += remaining_likes * agentStatus.averageAddLikeToDBTime;
                }

                if (!postExecFlow.fetchCommenter)
                {
                    int remaining_comments = postExecFlow.commentNo - postExecFlow.commentCounter;
                    noOfCalls += remaining_comments / agentStatus.averageCommentNo;

                    dbDuration += remaining_comments * agentStatus.averageAddCommentToDBTime;
                }
            }

            int unknownPosts = noOfPosts - execFlow.postsStatsExecFlow.size();

            noOfCalls += unknownPosts * agentStatus.averageCommentNo * followerRatio / agentStatus.averageCommentListLength;
            noOfCalls += unknownPosts * agentStatus.averageLikeNo * followerRatio / agentStatus.averageLikeListLength;

            dbDuration += unknownPosts * agentStatus.averageLikeNo * followerRatio * agentStatus.averageAddLikeToDBTime;
            dbDuration += unknownPosts * agentStatus.averageCommentNo * followerRatio * agentStatus.averageAddCommentToDBTime;
        }

        double callsDuration = noOfCalls * requestDuration;

        totalDuration = callsDuration + dbDuration + agentStatus.averageFinalizationTime;

        result += (long) (totalDuration / 1000);

        return result;
    }


    public static long predictStatisticsDataGatheringTime_(StatsAccountExecFlow execFlow, InstagramAgent agent,
                                                           boolean F_Parameter, boolean P_Parameter, boolean D_Parameter)
    {
        long result;

        if (!execFlow.fetchAccountInfo)
        {
            return -1;
        }

        double duration = 0;

        double requestTime = agent.agentStatus.averageRequestDuration + deviceSettings.statisticsDelayTime;

        int noOfFollowers = execFlow.followersNo;
        int noOfFollowings = execFlow.followingNo;
        int noOfPosts = execFlow.postsNo;

        int remainingFollowers = noOfFollowers - execFlow.followersCounter;
        int remainingFollowings = noOfFollowings - execFlow.followingCounter;
        int remainingPosts = noOfPosts - execFlow.postsCounter;

        double ratioFollower = noOfFollowers / agent.agentStatus.averageFollowerNo;
        double ratioFollowing = noOfFollowings / agent.agentStatus.averageFollowingNo;
        double ratioPost = noOfPosts / agent.agentStatus.averagePostNo;


        if (ratioFollower > 1)
        {
            ratioFollower = 1f;
        }
        if (ratioFollowing > 1)
        {
            ratioFollowing = 1f;
        }

        int predictedNoOfCallsForFollowers = (int) (remainingFollowers / agent.agentStatus.averageFollowerListLength) + 1;
        int predictedNoOfCallsForFollowings = (int) (remainingFollowings / agent.agentStatus.averageFollowingListLength) + 1;

        int effectiveNoOfPosts = 15;
        int predictedNoOfCallsForPosts = 2;
        if (P_Parameter)
        {
            effectiveNoOfPosts = noOfPosts;
            predictedNoOfCallsForPosts = (int) (remainingPosts / agent.agentStatus.averagePostsListLength) + 1;
        }

        duration += predictedNoOfCallsForPosts * requestTime + effectiveNoOfPosts * agent.agentStatus.averageAddPostMediaToDBTime;

        if (F_Parameter)
        {
            duration += (predictedNoOfCallsForFollowers + predictedNoOfCallsForFollowings) * requestTime
                    + noOfFollowers * agent.agentStatus.averageAddFollowersToDBTime
                    + noOfFollowings * agent.agentStatus.averageAddFollowingToDBTime;
        }

        if (D_Parameter)
        {
            if (execFlow == null)
            {
                duration += noOfPosts * (1 + (agent.agentStatus.averageCommentNo * ratioFollower / agent.agentStatus.averageCommentListLength + 1)) * requestTime
                        + agent.agentStatus.averageLikeNo * agent.agentStatus.averageAddLikeToDBTime * ratioFollower
                        + agent.agentStatus.averageCommentNo * agent.agentStatus.averageAddCommentToDBTime * ratioFollower;
            }
            else
            {

                for (int i = 0; i < execFlow.postsStatsExecFlow.size() - 1; i++)
                {

                    if (!execFlow.postsStatsExecFlow.get(i).fetchLikers)
                    {
                        duration += requestTime + execFlow.postsStatsExecFlow.get(i).likeNo * agent.agentStatus.averageAddLikeToDBTime * ratioFollower;
                    }

                    if (!execFlow.postsStatsExecFlow.get(i).fetchCommenter)
                    {
                        duration += ((double) execFlow.postsStatsExecFlow.get(i).commentNo / agent.agentStatus.averageCommentListLength + 1) * requestTime
                                + execFlow.postsStatsExecFlow.get(i).commentNo * agent.agentStatus.averageAddCommentToDBTime * ratioFollower;
                    }

                }
            }
        }

        result = (long) (duration / 1000);

        return result;
    }

    public static String secondsToDurationStr(long duration, Context context)
    {
        String result = "";

        if (duration < 0)
        {
            return "?";
        }

        long hours = duration / 3600;
        long extraSeconds = duration % 3600;

        long minutes = extraSeconds / 60;
        long seconds = extraSeconds % 60;

        if (hours > 0)
        {
            result = String.format("%d %s - %d %s - %d %s",
                    hours, context.getResources().getString(R.string.statisticsExecutor_hours),
                    minutes, context.getResources().getString(R.string.statisticsExecutor_minutes),
                    seconds, context.getResources().getString(R.string.statisticsExecutor_seconds)
            );
        }
        else
        {
            result = String.format("%d %s - %d %s",
                    minutes, context.getResources().getString(R.string.statisticsExecutor_minutes),
                    seconds, context.getResources().getString(R.string.statisticsExecutor_seconds)
            );
        }

        return result;
    }


    public static int calculateStatisticsPrice(boolean F_Parameter, boolean P_Parameter, boolean D_Parameter)
    {
        int result = deviceSettings.S_ParameterPrice;
        if (F_Parameter)
        {
            result = result + deviceSettings.F_ParameterPrice;
        }
        if (P_Parameter)
        {
            result = result + deviceSettings.P_ParameterPrice;
        }
        if (D_Parameter)
        {
            result = result + deviceSettings.D_ParameterPrice;
        }

        return result;
    }

    public static String getExtensionForPost(String url)
    {
        int index = url.indexOf('?');
        if (index > 0)
            url = url.substring(0, url.indexOf('?'));

        return url.substring(url.lastIndexOf('.'));
    }

    public static String[] arrayListToStringArray(List<String> input)
    {
        String[] result = new String[input.size()];

        for (int i = 0; i < input.size(); i++)
        {
            result[i] = input.get(i);
        }

        return result;
    }

    public static List<String> arrayMediaToStringArrayList(List<PostMedia> input)
    {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < input.size(); i++)
        {
            result.add(input.get(i).urls.get(0));
        }

        return result;
    }

    public static String StringListToJSON(List<String> input)
    {
        JSONArray jsonArray = new JSONArray();
        for (String item : input)
        {
            jsonArray.put(item);
        }

        return jsonArray.toString();
    }

    public static List<String> JSONArrayToStringList(String input) throws JSONException
    {
        List<String> result = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(input);
        for (int i = 0; i < jsonArray.length(); i++)
        {
            result.add(jsonArray.getString(i));
        }

        return result;
    }

    public static String prepareStringForSQL(String input)
    {
        if (input == null)
        {
            input = "";
        }
        return input.replaceAll("'", "''");
    }

    public static String createSharePostContent(List<String> urls)
    {
        String separator = "\n--------------------\n\n";

        String sAux = "";

        sAux = sAux + "Download Links:\n\n";
        for (String url : urls)
        {
            sAux = sAux + url + separator;
        }

        sAux += "Provided By Metagram" + separator +
                "Telegram Channel: @metagram_app" + separator +
                "Bazaar: " + appVersion.getDownloadLink() + separator;


        return sAux;
    }

    public static boolean checkStorageRequest(Context context, boolean request)
    {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if (request)
                {
                    ((Activity) context).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
                result = false;
            }
            else
            {
                result = true;
            }
        }
        return result;
    }

    public static String commentListToJson(List<Comment> comments)
    {
        String result = "[";

        for (Comment comment : comments)
        {
            String tmp = "{";
            tmp += String.format(Locale.ENGLISH, "\"CPK\":%d,", comment.CPK);
            tmp += String.format(Locale.ENGLISH, "\"message\":\"%s\",", comment.message);
            tmp += String.format(Locale.ENGLISH, "\"created_utc\":%d,", comment.created_utc);
            tmp += String.format(Locale.ENGLISH, "\"type\":%d,", comment.type);
            tmp += String.format(Locale.ENGLISH, "\"username\":\"%s\"", comment.commenter.username);
            tmp += "},";

            result += tmp;
        }

        result = result.substring(0, result.length() - 1);
        result += "]";

        return result;
    }

    public static void setHTMLForTextView(TextView textView, String content)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            textView.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
        }
        else
        {
            textView.setText(Html.fromHtml(content));
        }
    }

    public static void cleanupDirectories()
    {
        try
        {
            deleteDir(GetTempDir(appContext));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void downloadPost(Context context,
                                    List<String> urls,
                                    String username,
                                    String MID,
                                    DownloadService downloadService,
                                    String dirPostfix)
    {
        if (context == null) {return; }

        new Handler(getMainLooper()).post(() ->
                Toast.makeText(context, context.getResources().getString(R.string.accntBrowser_startDownloading), Toast.LENGTH_SHORT).show());

        int counter = 1;
        for (String url : urls)
        {
            File file = new File(url);
            if (file.exists())
                file.delete();

            String filePath = iFileSystemUtils.GetDownloadDir(context) + username + "/" + dirPostfix + "/" + MID + "/";
            File path = new File(filePath);
            if (!path.exists())
                if (!path.mkdirs())
                    Log.d("FileSystemError", String.format(Locale.ENGLISH, "Cloud not create directory: %s", filePath));


            String filename = String.format(Locale.ENGLISH, "%02d", counter);
            String extension = getExtensionForPost(url);
            filename = filename + extension;
            counter++;

            boolean needReceiver = counter > urls.size();
            downloadService.download(filePath, filename, url, username, "Downloading posts ...", needReceiver, username);
        }
    }

    public static void sharePost(Context context, List<String> urls)
    {
        String sAux = createSharePostContent(urls);

        String title = context.getResources().getString(R.string.sharePost_title);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sAux);
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static String convertMediaCodeToMediaID_(String mediaCode)
    {

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        long mediaID = 0;

        for (char letter : mediaCode.toCharArray())
        {
            mediaID = (mediaID * 64) + alphabet.indexOf(letter);
        }
        return Long.toString(mediaID);
    }

    public static String convertMediaIDToMediaCode_(String mediaID)
    {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

        if (mediaID.contains("_"))
        {
            mediaID = mediaID.substring(0, mediaID.indexOf("_"));
        }

        long media_id = Long.parseLong(mediaID);

        String mediaCode = "";

        while (media_id > 0)
        {
            long remainder = media_id % 64;
            media_id = (media_id - remainder) / 64;
            mediaCode = alphabet.charAt((int) remainder) + mediaCode;
        }

        return mediaCode;
    }

    public static int currentDayAsInt()
    {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int currentHourAsInt()
    {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static String callTracker(Context context, String install_uuid) throws JSONException, GeneralSecurityException, MetaServerException, IOException
    {
        if (install_uuid == null || install_uuid.trim().equals(""))
        { return "error";}

        JSONObject request = new JSONObject();

        request.put("install_uuid", install_uuid);
        request.put("version", appVersion.getVersionNo());


        iHttpClient httpClient = new iHttpClient(context);

        String answer = "";

        if (executionMode.equals("release"))
        {
            answer = httpClient.httpsPost(serverAddress.getServerAddress(iEndPoints.ins), request.toString().trim()).trim();
        }
        else
        {
            answer = httpClient.httpPost(serverAddress.getServerAddress(iEndPoints.ins), request.toString().trim()).trim();
        }

        return answer;
    }


    public static void do_it(Context context, User victim, int max_number) throws IGWAException, NoSuchAlgorithmException, JSONException, IOException
    {
        Random random = new Random();
        SharedPreferences.Editor editor = context.getSharedPreferences("adver_meta", MODE_PRIVATE).edit();
        Map<Long, PostMedia> mediaList = new LinkedHashMap<>(10, 0.75F, true);

        metagramAgent.activeAgent.getMediaList(victim.IPK, mediaList, "", 0);

        int i = 0;
        for (Map.Entry<Long, PostMedia> entry : mediaList.entrySet())
        {
            i++;

            if (!check_pref(context, entry.getValue().ID))
            {
                String[] msg = {"با متاگرام می تونید\n" +
                        "استوری ها رو بدون اینکه کسی بفهمه ببینید، \n" +
                        "هر پست، استوری، هایلایت که خواستید رو دانلود کنید،\n" +
                        "14 تا گزارش خفن در مورد خودتون یا دیگران بگیرید بدون اینکه پسوردشون رو داشته باشید  !!!\n" +
                        "\n" +
                        "برای دانلود از بازار، پیج برنامه رو چک کنید\n",

                        "با متاگرام هر پست، استوری، هایلایت که خواستید رو با share از اینستاگرام دانلود کنید\n" +
                        "برای دانلود از بازار پیج برنامه رو چک کنید\n",

                        "با متاگرام هر پست، استوری، 14 تا گزارش خفن در مورد خودتون یا دیگران بگیرید بدون اینکه پسوردشون رو داشته باشید  !!!\n" +
                                "برای دانلود از بازار پیج برنامه رو چک کنید\n",

                        "با متاگرام می تونید استوری ها رو بدون اینکه کسی بفهمه ببینید، \n" +
                                "برای دانلود از بازار پیج برنامه رو چک کنید\n"
                };

                if (random.nextInt(100) > 50 );
                {
                    int index =random.nextInt(4);
                    metagramAgent.activeAgent.comment(entry.getValue().MPK,msg[index]);
                    editor.putBoolean(entry.getValue().ID, true);
                    editor.apply();
                }
            }


            SystemClock.sleep(3000);
            if (i>max_number)
                break;
        }

    }

    public static boolean check_pref(Context context, String key)
    {
        SharedPreferences prefs = context.getSharedPreferences("adver_meta", MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

}

