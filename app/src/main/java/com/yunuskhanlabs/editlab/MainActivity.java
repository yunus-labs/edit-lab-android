package com.yunuskhanlabs.editlab;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public final class MainActivity extends Activity {
    private static final String APP_URL = "https://edit-lab.local/index.html";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(8192 | 16);
        WebView view = new WebView(this);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setAllowFileAccess(false);
        view.getSettings().setAllowContentAccess(false);
        view.getSettings().setBlockNetworkLoads(true);
        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String path = uri.getPath();
                if (request.isForMainFrame() && request.hasGesture()
                    && "https".equals(uri.getScheme()) && "github.com".equals(uri.getHost())
                    && path != null && path.matches("/yunus-labs/edit-lab-android/releases/download/v[0-9.]+/[a-z0-9-]+-(practice\\.zip|reference\\.mp4)")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "Download ke liye browser chahiye.", Toast.LENGTH_LONG).show();
                    }
                }
                return !APP_URL.equals(uri.toString());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
                return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
            }
        });
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);
        root.addView(view, new FrameLayout.LayoutParams(-1, -1));
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((content, insets) -> {
                android.graphics.Insets padding = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout()
                    | WindowInsets.Type.ime());
                content.setPadding(padding.left, padding.top, padding.right, padding.bottom);
                return insets;
            });
        }
        setContentView(root);
        root.requestApplyInsets();
        try (InputStream html = getAssets().open("index.html")) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int length;
            while ((length = html.read(chunk)) != -1) {
                buffer.write(chunk, 0, length);
            }
            view.loadDataWithBaseURL(APP_URL, buffer.toString("UTF-8"), "text/html", "UTF-8", APP_URL);
        } catch (IOException e) {
            throw new IllegalStateException("Missing bundled Edit Lab page", e);
        }
    }
}
