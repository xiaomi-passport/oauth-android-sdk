快速开始
===

1) 预备步骤 : 去 dev.xiaomi.com 中创建应用。步骤如下：登陆小米开放平台网页 -> ”管理控制台” -> ”手机及平板应用” -> ”创建应用” ->  填入应用名和包名 -> ”创建” -> 记下看到的AppID -> 页面下方找”帐号接入服务“ -> ”详情“ -> ”立即启用“ -> 填入“授权回调地址（URL）”(这里填入的url即是下文提到的*redirectUrl*) -> “启用“ -> 开启需要的开放接口。

2) 在应用的AndroidManifest.xml里添加以下配置：

```
    <uses-permission android:name="com.xiaomi.permission.AUTH_SERVICE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <activity android:name="com.xiaomi.account.openauth.AuthorizeActivity" />
```

3) 获取AccessToken

```
    // 这一句可以在UI线程调用
    XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setAppId(appID)
        .setRedirectUrl(redirectUri)
        .setScope(scope)
        .startGetAccessToken(activity);
    // 如果是要获得Code的方式，则把startGetAccessToken改成startGetOAuthCode即可。其他相同
    // 参数解释：
    //     appID : 开发者预先申请好的 AppID。
    //     redirectUrl : 开发者预先申请时填好的 redirectUrl。
    //     scope : int数组，可以用XiaomiOAuthConstants.SCOPE_*等常量
    //     activity : 用于启动用户登陆/授权的Activity。

    // 接下来这一段必须在后台线程调用
    try {
        XiaomiOAuthResults result = future.getResult();

        if (results.hasError()) {
            int errorCode = results.getErrorCode();
            String errorMessage = results.getErrorMessage();
        } else {
            String accessToken = results.getAccessToken();
            String macKey = results.getMacKey();
            String macAlgorithm = results.getMacAlgorithm();
        }
    } catch (IOException e1) {
        // error
    } catch (OperationCanceledException e1) {
        // user cancel
    } catch (XMAuthericationException e1) {
        // error
    }
```

4) 获取用户名片

```
    // 这一句可以在UI线程调用
    XiaomiOAuthFuture<String> future = new XiaomiOAuthorize().callOpenApi(context,
        appId,
        XiaomiOAuthConstants.OPEN_API_PATH_PROFILE,
        results.successResult.accessToken,
        results.successResult.macKey,
        results.successResult.macAlgorithm);

    // 接下来这一段必须在后台线程调用
    try {
        String result = future.getResult();
    } catch (IOException e1) {
        // error
    } catch (OperationCanceledException e1) {
        // error
    } catch (XMAuthericationException e1) {
        // error
    }
```

5) fastOAuth (可选)

调用这个接口可以在最新MIUI系统中用户登陆小米账号的情况下快速授权 - 通过miui的对话框样式。（在系
统不支持时返回XMAuthericationException，未登录小米账号时简单返回错误码）

```

    XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setAppId(getAppId())
        .setRedirectUrl(getRedirectUri())
        .setScope(getScopeFromUi())
        .fastOAuth(MainActivity.this, XiaomiOAuthorize.TYPE_TOKEN);

    // 接下来这一段必须在后台线程调用
    try {
        XiaomiOAuthResults result = future.getResult();

        if (results.hasError()) {
            int errorCode = results.getErrorCode();
            String errorMessage = results.getErrorMessage();
        } else {
            String accessToken = results.getAccessToken();
            String macKey = results.getMacKey();
            String macAlgorithm = results.getMacAlgorithm();
        }
    } catch (IOException e1) {
        // error
    } catch (OperationCanceledException e1) {
        // user cancel
    } catch (XMAuthericationException e1) {
        // error
    }

```



如果希望用户无法切换帐号？
===
```
    XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setSkipConfirm(true)
        // .setOtherParams...
        // .startGetAccessToken(activity);
```

Scope是什么？应该怎么取值？
===

简单来说，Scope代表了一个AccessToken的权限。当使用一个AccessToken去访问OpenApi时，只有该AccessToken的Scope和该OpenApi需要的权限对得上的时候，服务器才会返回正确的结果，否则会报错。
代码中只有一个地方要用到Scope，那就是去拿AccessToken的时候。
代码中Scope的值应该是多少？请参照 http://dev.xiaomi.com/docs/passport/way/ 中“scope设置说明”一节，然后根据APP需要访问到的API去决定用哪些scope。比如，我将用AccessToken去活取用户的个人资料和好友信息，那么我的scope就应该是1和3。也可以用SDK中预定义好的常量XiaomiOAuthConstants.SCOPE_***。当然前提是，APP已经在预备步骤中，在dev.xiaomi.com上为该应用开启了相应的接口权限。


更多OAuth资料？
===
http://dev.xiaomi.com/docs/passport/user_guide
