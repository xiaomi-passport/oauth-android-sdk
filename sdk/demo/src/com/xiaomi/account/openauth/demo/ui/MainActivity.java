
package com.xiaomi.account.openauth.demo.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaomi.account.openauth.XMAuthericationException;
import com.xiaomi.account.openauth.XiaomiOAuthConstants;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;
import com.xiaomi.account.openauth.demo.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends Activity {

    public static final Long appId = 179887661252608L;
    public static final String redirectUri = "http://xiaomi.com";
    private static final String TAG = "OAuthDemoActivity";

    XiaomiOAuthResults results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((EditText) findViewById(R.id.appId)).setText(String.valueOf(appId));
        ((EditText) findViewById(R.id.redirectUrl)).setText(redirectUri);

        findViewById(R.id.get_token).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean skipConfirm = ((CheckBox) findViewById(R.id.skipConfirm)).isChecked();
                XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                        .setAppId(getAppId())
                        .setRedirectUrl(getRedirectUri())
                        .setScope(getScopeFromUi())
                        .setKeepCookies(getTryKeepCookies()) // 不调的话默认是false
                        .setNoMiui(getNoMiui()) // 不调的话默认是false
                        .setSkipConfirm(skipConfirm) // 不调的话默认是false
                        .startGetAccessToken(MainActivity.this);
                waitAndShowFutureResult(future);
            }
        });

        findViewById(R.id.get_code).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int[] scopes = getScopeFromUi();
                boolean noMiui = getNoMiui();
                boolean skipConfirm = ((CheckBox) findViewById(R.id.skipConfirm)).isChecked();
                XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                        .setAppId(getAppId())
                        .setRedirectUrl(getRedirectUri())
                        .setScope(scopes)
                        .setKeepCookies(getTryKeepCookies()) // 不调的话默认是false
                        .setNoMiui(getNoMiui()) // 不调的话默认是false
                        .setSkipConfirm(skipConfirm) // 不调的话默认是false
                        .startGetOAuthCode(MainActivity.this);
                waitAndShowFutureResult(future);
            }

        });

        findViewById(R.id.profile_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_PROFILE,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }

        });

        findViewById(R.id.relation_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_RELATION,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }
        });

        findViewById(R.id.openid_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_OPEN_ID,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }
        });

        findViewById(R.id.phone_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_PHONE,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }
        });
    }

    private boolean getTryKeepCookies() {
        return ((CheckBox) findViewById(R.id.tryKeepCookies)).isChecked();
    }

    private boolean getNoMiui() {
        return ((CheckBox) findViewById(R.id.nonMiui)).isChecked();
    }

    private String getRedirectUri() {
        return redirectUri;
    }

    private Long getAppId() {
        return appId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.oldApiDemo) {
            startActivity(new Intent(this, OldMainActivity.class));
        } else if (item.getItemId() == R.id.customizeAuthorizeActivity) {
            XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                    .setAppId(getAppId())
                    .setRedirectUrl(getRedirectUri())
                    .setNoMiui(true) // set to true only because we want to simulate behavior on Non-MIUI ROM.
                    .setScope(getScopeFromUi())
                    .setCustomizedAuthorizeActivityClass(CustomizedAuthorizedActivity.class)
                    .startGetAccessToken(this);
            waitAndShowFutureResult(future);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private int[] getScopeFromUi() {
        HashMap<Integer, Integer> scopeMap = new HashMap<Integer, Integer>();
        scopeMap.put(R.id.scopeProfile, XiaomiOAuthConstants.SCOPE_PROFILE);
        scopeMap.put(R.id.scopeRelation, XiaomiOAuthConstants.SCOPE_RELATION);
        scopeMap.put(R.id.scopeOpenId, XiaomiOAuthConstants.SCOPE_OPEN_ID);
        scopeMap.put(R.id.scopePhone, XiaomiOAuthConstants.SCOPE_PHONE);

        int[] scopes = new int[scopeMap.size()];
        int checkedCount = 0;

        for (Integer id : scopeMap.keySet()) {
            CheckBox cb = (CheckBox) findViewById(id);
            if (cb.isChecked()) {
                scopes[checkedCount] = scopeMap.get(id);
                checkedCount++;
            }
        }

        return Arrays.copyOf(scopes, checkedCount);
    }

    private <V> void waitAndShowFutureResult(final XiaomiOAuthFuture<V> future) {
        new AsyncTask<Void, Void, V>() {
            Exception e;

            @Override
            protected void onPreExecute() {
                showResult("waiting for Future result...");
            }

            @Override
            protected V doInBackground(Void... params) {
                V v = null;
                try {
                    v = future.getResult();
                } catch (IOException e1) {
                    this.e = e1;
                } catch (OperationCanceledException e1) {
                    this.e = e1;
                } catch (XMAuthericationException e1) {
                    this.e = e1;
                }
                return v;
            }

            @Override
            protected void onPostExecute(V v) {
                if (v != null) {
                    if (v instanceof XiaomiOAuthResults) {
                        results = (XiaomiOAuthResults) v;
                    }
                    showResult(v.toString());
                } else if (e != null) {
                    showResult(e.toString());
                } else {
                    showResult("done and ... get no result :(");
                }
            }
        }.execute();
    }

    private void showResult(String text) {
        String timeFormatted = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(System.currentTimeMillis()));
        ((TextView) findViewById(R.id.content)).setText(timeFormatted + "\n" + text);
        Log.v(TAG, "result:" + text);
    }
}
