package vp.metagram.ui.AccBrowser;

import android.os.Bundle;
import android.webkit.WebView;


import vp.metagram.base.BaseActivity;


public class WebViewerActivity extends BaseActivity
{
    WebView postViewer;
    String postUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        postViewer = new WebView(this);
        postViewer.getSettings().setJavaScriptEnabled(true);
        postUrl = getIntent().getExtras().getString("postURL");
        postViewer.loadUrl(postUrl);
        setContentView(postViewer);

    }

}
