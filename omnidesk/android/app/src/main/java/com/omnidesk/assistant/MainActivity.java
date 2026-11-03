package com.omnidesk.assistant;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class MainActivity extends Activity {
    private WebView workspace;

    private void openExternal(Uri uri) {
        try {
            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        } catch (ActivityNotFoundException ignored) {
        }
    }

    private boolean route(Uri uri) {
        if (uri == null) return true;
        String scheme = uri.getScheme();
        if ("file".equals(scheme) || "about".equals(scheme)) return false;
        openExternal(uri);
        return true;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(16, 27, 60));
        getWindow().setNavigationBarColor(Color.rgb(16, 27, 60));

        workspace = new WebView(this);
        WebSettings settings = workspace.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        workspace.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return route(request.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return route(Uri.parse(url));
            }
        });

        workspace.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog,
                                          boolean userGesture, Message resultMsg) {
                WebView popup = new WebView(MainActivity.this);
                popup.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                        openExternal(r.getUrl());
                        v.destroy();
                        return true;
                    }
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, String url) {
                        openExternal(Uri.parse(url));
                        v.destroy();
                        return true;
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popup);
                resultMsg.sendToTarget();
                return true;
            }
        });
        workspace.setOverScrollMode(View.OVER_SCROLL_NEVER);
        setContentView(workspace);
        workspace.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (workspace != null && workspace.canGoBack()) {
            workspace.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (workspace != null) {
            workspace.destroy();
            workspace = null;
        }
        super.onDestroy();
    }
}
