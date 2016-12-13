package com.xiaomi.account.openauth.demo.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;

import com.xiaomi.account.openauth.AuthorizeActivityBase;

public class CustomizedAuthorizedActivity extends AuthorizeActivityBase{

    private ProgressBar mProgressBar;
    private Button mRefreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        mProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        layout.addView(mProgressBar, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        mRefreshButton = new Button(this);
        mRefreshButton.setText("click to refresh");
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomizedAuthorizedActivity.this.refreshWebView();
            }
        });
        layout.addView(mRefreshButton);

        final WebView mWebView = super.getWebView();
        layout.addView(mWebView, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        setContentView(layout);
    }

    @Override
    protected void onShowProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onUpdateProgress(int newProgress) {
        mProgressBar.setProgress(newProgress);
    }

    @Override
    protected void onHideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onShowErrorUI() {
        mRefreshButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onHideErrorUI() {
        mRefreshButton.setVisibility(View.GONE);
    }
}
