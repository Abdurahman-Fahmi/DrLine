package com.wecareapp.android.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wecareapp.android.R;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebViewActivity extends BaseActivity {

    //    Toolbar toolbar;
    String link = "";
    ProgressDialog pd;
    private WebView webView;
    private float m_downX;
    private View progressBar;
    private String content;
    private String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

//        getWindow().setBackgroundDrawableResource(R.drawable.login_signup);

//        toolbar = findViewById(R.id.toolbar);
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptFileSchemeCookies(true);
        cookieManager.setAcceptCookie(true);
        cookieManager.acceptCookie();

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            link = intent.getExtras().getString("link", "");
            content = intent.getExtras().getString("content", "");
            paymentUrl = intent.getExtras().getString("payment_url", "");
        }

        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().setAcceptCookie(true);
        initWebView();

        if (TextUtils.isEmpty(content))
            webView.loadUrl(link);
        else if (!TextUtils.isEmpty(paymentUrl))
            webView.loadDataWithBaseURL(paymentUrl, content, "text/html; charset=utf-8", "UTF-8", null);
        else
            webView.loadData(content, "text/html", "UTF-8");
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initWebView() {
        webView.setWebChromeClient(new MyWebChromeClient(this));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                Log.e("Link ", url);
                if (url.contains("status=200")) {
                    finish();
                    Intent intent = new Intent(WebViewActivity.this, PaymentSuccessActivity.class);
                    intent.putExtra("type", "payment");
                    intent.putExtra("status", "success");
                    startActivity(intent);
                } else if (url.contains("status=400")) {
                    finish();
                    Intent intent = new Intent(WebViewActivity.this, PaymentSuccessActivity.class);
                    intent.putExtra("type", "payment");
                    intent.putExtra("status", "failed");
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                invalidateOptionsMenu();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                invalidateOptionsMenu();
            }

            //Intercept WebView Requests for Android API >= 21
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http://")) {
                    try {
                        //change protocol of url string
                        url = url.replace("http://", "https://");

                        //return modified response
                        URL httpsUrl = new URL(url);
                        URLConnection connection = httpsUrl.openConnection();
                        return new WebResourceResponse(connection.getContentType(), connection.getContentEncoding(), connection.getInputStream());
                    } catch (Exception e) {
                        //an error occurred
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

        });

        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().setAcceptCookie(true);
        webView.clearCache(true);
        webView.clearHistory();
        // Configure related browser settings
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);// Enable responsive layout
        webView.getSettings().setUseWideViewPort(true);
// Zoom out if the content width is greater than the width of the viewport
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true); // allow pinch to zooom
        webView.getSettings().setDisplayZoomControls(false);// disable the default zoom controls on the page
        webView.setOnTouchListener((v, event) -> {

            if (event.getPointerCount() > 1) {
                //Multi touch detected
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    // save the x
                    m_downX = event.getX();
                }
                break;

                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    // set x so that it doesn't move
                    event.setLocation(m_downX, event.getY());
                }
                break;
            }

            return false;
        });
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
    }

    private static class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
