package vp.metagram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vp.igwa.IGWAExtractor;
import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.Dialogs.ConnectingDialog;
import vp.metagram.ui.Dialogs.InformationDialog;
import vp.igpapi.IGWAStorage;

import static vp.metagram.general.functions.isNetworkAvailable;
import static vp.metagram.general.functions.setTextViewFontForMenu;
import static vp.metagram.general.variables.metagramAgent;
import static vp.metagram.general.variables.threadPoolExecutor;

public class LogInActivity extends BaseActivity
{
    WebView logInWebView;
    TextView loginTitle;
    View parentLayout;
    TextView logInProblem;

    String message;

    long editIPK = -1;

    boolean isForRobot;
    int robotType = -1;

    boolean isEditRobot;
    String RobotUUID;

    private String cookies;


    public static LogInActivity self = null;

    LoginSaviour saviour = new LoginSaviour();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        self = this;

        setContentView(R.layout.activity_log_in);
        setTheme(R.style.AppTheme_FullScreen);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            editIPK = bundle.getLong("editIPK");
        }

        parentLayout = findViewById(R.id.login_rootLayout);

        logInWebView = findViewById(R.id.login_webview);
        logInWebView.getSettings().setJavaScriptEnabled(true);
        logInWebView.getSettings().setDomStorageEnabled(true);

        loginTitle = findViewById(R.id.login_title);
        loginTitle.setVisibility(View.GONE);

        logInProblem = findViewById(R.id.login_problem);
        logInProblem.setVisibility(View.GONE);

        setTextViewFontForMenu(this, logInProblem);
        setTextViewFontForMenu(this, loginTitle);

        saviour.user_agent = logInWebView.getSettings().getUserAgentString();
        saviour.mobile_user_agent = logInWebView.getSettings().getUserAgentString();

        new Handler(Looper.getMainLooper()).postDelayed(() -> loadWebView(), 200);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        self = null;
        super.onDestroy();
    }

    public void showSnackBar(String message)
    {
        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_LONG);
        snackbar.getView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        snackbar.show();
    }

    boolean cookiesExtracted = false;

    private void getLoginPage()
    {
        final String mURL = "https://www.instagram.com/accounts/login/";
        final String[] cookie_names = new String[]{"ds_user_id", "ig_did", "mid", "csrftoken", "sessionid"};
        logInWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onLoadResource(WebView view, String url)
            {
                super.onLoadResource(view, url);

                cookies = CookieManager.getInstance().getCookie(url);

                String[] tmp;
                if (cookies != null && !cookies.equals(""))
                {
                    tmp = cookies.split(";");

                    if (cookies.contains("sessionid") && !cookiesExtracted)
                    {
                        ConnectingDialog connectingDialog = ConnectingDialog.newInstance(getResources().getString(R.string.login_connectMessage));
                        connectingDialog.show(getFragmentManager(), "");

                        cookiesExtracted = true;

                        for (String item: tmp)
                        {
                            for (String cName : cookie_names)
                            {
                                if (item.contains(cName))
                                {
                                    saviour.cookieString += item + ";";

                                    String value = item.substring(item.indexOf(cName) + cName.length() +1);

                                    if (cName.equals("ds_user_id"))
                                    {
                                        saviour.user_id = Long.parseLong(value);

                                        if (editIPK > 0 && saviour.user_id != editIPK)
                                        {
                                            InformationDialog.showDialog(LogInActivity.self,
                                                    LogInActivity.self.getString(R.string.error_cannotConnectToInstagram),
                                                    LogInActivity.self.getString(R.string.editAccount_message),
                                                    LogInActivity.self.getString(R.string.button_ok),
                                                    ()-> LogInActivity.self.finish());

                                            connectingDialog.close();
                                            clearWebViewCookies();
                                            return;
                                        }
                                    }
                                    else if (cName.equals("csrftoken"))
                                    {
                                        saviour.init_csrftoken = value;
                                        saviour.csrftoken = value;
                                    }
                                }
                            }
                        }

                        saviour.cookieString = saviour.cookieString.substring(0, saviour.cookieString.length()-1);

                        saviour.cookies = Arrays.asList(saviour.cookieString.split(";"));

                        threadPoolExecutor.execute(()->
                        {
                            try
                            {
                                IGWAExtractor ig = new IGWAExtractor("", saviour);
                                ig.init();
                                JSONObject timeline_feed = ig.timeline_feed("");

                                if(timeline_feed != null || !timeline_feed.equals(""))
                                {
                                    timeline_feed = timeline_feed.getJSONObject("data");
                                    timeline_feed = timeline_feed.getJSONObject("user");
                                    String username = timeline_feed.getString("username");

                                    if (username != null || !username.equals(""))
                                    {
                                        ig.set_username(username);
                                        ig.user_id = saviour.user_id;

                                        //Every thing is fine -> Register the api


                                        message = metagramAgent.addInstagramAccount(ig);

                                        metagramAgent.setAccountRegisteredByUsername(username);
                                        metagramAgent.reloadInstagramAgents();
                                        metagramAgent.setActiveAgentByUsername(username);

                                        ig.save();

                                        runOnUiThread(()->
                                        {
                                            connectingDialog.close();

                                            clearWebViewCookies();

                                            Intent intent = getIntent();
                                            intent.putExtra("message", message);
                                            setResult(RESULT_OK, intent);
                                            finish();

                                        });
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                else
                {
                    cookies = "";
                }
            }

        });

        logInWebView.loadUrl(mURL);

    }

    public void clearWebViewCookies()
    {
        CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
        cookieSyncMngr.startSync();
        CookieManager cookieManager=CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.removeSessionCookie();
        cookieSyncMngr.stopSync();
        cookieSyncMngr.sync();
    }


    public void loadWebView()
    {
        if (!isNetworkAvailable(this))
        {
            InformationDialog dialog = new InformationDialog();
            dialog.showDialog(this, getResources().getString(R.string.error_cannotConnectToInstagram),
                    getResources().getString(R.string.error_noInternetMessage),
                    getResources().getString(R.string.button_ok), () -> finish());
        } else
        {
            getLoginPage();
        }
    }
}

class LoginSaviour implements IGWAStorage
{
    long user_id;

    String username = "";
    String user_agent = "";
    String mobile_user_agent = "";
    String init_csrftoken = "";
    String rhx_gis = "";
    String csrftoken = "";
    String rollout_hash = "";
    boolean isLogedin = true;
    boolean initialized = true;
    String cookieString = "";
    List<String> cookies = new ArrayList<>();

    String PicURL = "";

    @Override
    public void save(String name, String value) throws IOException
    {

    }

    @Override
    public String load(String name) throws IOException
    {
        JSONObject result = new JSONObject();
        try
        {
            result.put("username", "");
            result.put("user_agent", user_agent);
            result.put("mobile_user_agent", mobile_user_agent);
            result.put("init_csrftoken", init_csrftoken);
            result.put("rhx_gis", rhx_gis);
            result.put("csrftoken", csrftoken);
            result.put("rollout_hash", rollout_hash);
            result.put("isLogedin", isLogedin);
            result.put("initialized", initialized);
            result.put("cookieString", cookieString);

            cookies = Arrays.asList(cookieString.split(";"));

            JSONArray cookiesArray = new JSONArray();
            for (String cookie : cookies)
            {
                cookiesArray.put(cookie);
            }

            result.put("cookies", cookiesArray);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result.toString();
    }
}

