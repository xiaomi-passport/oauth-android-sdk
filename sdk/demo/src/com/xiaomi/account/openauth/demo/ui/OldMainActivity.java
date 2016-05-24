
package com.xiaomi.account.openauth.demo.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaomi.account.openauth.AuthorizeActivity;
import com.xiaomi.account.openauth.XMAuthericationException;
import com.xiaomi.account.openauth.XiaomiOAuthConstants;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;
import com.xiaomi.account.openauth.demo.R;
import com.xiaomi.auth.AuthConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class OldMainActivity extends Activity {

    public static final Long appId = MainActivity.appId;
    public static final String redirectUri = MainActivity.redirectUri;
    private static final String TAG = "OAuthDemoActivity";

    XiaomiOAuthResults results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);

        findViewById(R.id.get_token_old).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle options = buildOptions();
                XiaomiOAuthorize.startGetAccessToken(OldMainActivity.this, appId, redirectUri, options, 2);
            }
        });

        findViewById(R.id.get_code_old).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle options = buildOptions();
                XiaomiOAuthorize.startGetOAuthCode(OldMainActivity.this, appId, redirectUri, options, 3);
            }
        });
    }

    private Bundle buildOptions() {
        Bundle options = new Bundle();
        int[] scopes = getScopeFromUi();
        if (scopes != null && scopes.length > 0) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (int scope : scopes) {
                if (i++ > 0) {
                    sb.append(" ");
                }
                sb.append(scope);
            }
            options.putString(AuthConstants.EXTRA_SCOPE, sb.toString());
        }
        return options;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        results = XiaomiOAuthResults.parseBundle(bundle);
        if (2 == requestCode) {
            if (AuthorizeActivity.RESULT_SUCCESS == resultCode) {
                String accessToken = bundle.getString("access_token");
                String expiresIn = bundle.getString("expires_in");
                String scope = bundle.getString("scope");
                String state = bundle.getString("state");
                String tokenType = bundle.getString("token_type");
                String macKey = bundle.getString("mac_key");
                String macAlgorithm = bundle.getString("mac_algorithm");
                showResult("accessToken=" + accessToken + ",expiresIn=" + expiresIn
                        + ",scope=" + scope
                        + ",state=" + state + ",tokenType=" + tokenType + ",macKey=" + macKey
                        + ",macAlogorithm="
                        + macAlgorithm);
            } else if (AuthorizeActivity.RESULT_FAIL == resultCode) {
                String error = bundle.getString("error");
                String errorDescription = bundle.getString("error_description");
                showResult("error=" + error + ",errorDescription=" + errorDescription);
            } else if (AuthorizeActivity.RESULT_CANCEL == resultCode) {
                showResult("canceled");
            }
        } else if (3 == requestCode) {
            if (AuthorizeActivity.RESULT_SUCCESS == resultCode) {
                String code = bundle.getString("code");
                String state = bundle.getString("state");
                showResult("code=" + code + ",state=" + state);
            } else if (AuthorizeActivity.RESULT_FAIL == resultCode) {
                String error = bundle.getString("error");
                String errorDescription = bundle.getString("error_description");
                showResult("error=" + error + ",errorDescription=" + errorDescription);
            } else if (AuthorizeActivity.RESULT_CANCEL == resultCode) {
                showResult("canceled");
            }
        }
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

    private void showResult(String text) {
        String timeFormatted = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(System.currentTimeMillis()));
        ((TextView) findViewById(R.id.content)).setText(timeFormatted + "\n" + text);
        Log.v(TAG, "result:" + text);
    }
}
