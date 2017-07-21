快速开始
===

## 1) 预备步骤
 去 dev.xiaomi.com 中创建应用。步骤如下：登陆小米开放平台网页 -> ”管理控制台” -> ”手机及平板应用” -> ”创建应用” ->  填入应用名和包名 -> ”创建” -> 记下看到的AppID -> 页面下方找”帐号接入服务“ -> ”详情“ -> ”立即启用“ -> 填入“授权回调地址（URL）”(这里填入的url即是下文提到的*redirectUrl*) -> “启用“ -> 开启需要的开放接口。

## 2) 在应用的AndroidManifest.xml里添加以下配置：

``` xml
    <uses-permission android:name="com.xiaomi.permission.AUTH_SERVICE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <activity android:name="com.xiaomi.account.openauth.AuthorizeActivity" />
```

## 3) 授权并获取AccessToken/code

+ sdk会自行判断：在miui上，启动系统帐号进行授权；非miui上，使用webview登录然后授权
+ setCustomizedAuthorizeActivityClass()可以自定义非miui上的登录界面，设置actionbar、进度条等，可参照demo中的CustomizedAuthorizedActivity。

``` java
    XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
         //开发者预先申请好的 AppID
        .setAppId(appID)
         // 开发者预先申请时填好的 redirectUrl
        .setRedirectUrl(redirectUri)
        // int数组，可以用XiaomiOAuthConstants.SCOPE_*等常量
        .setScope(scope)
         // 设置自定义的非miui上登录界面(默认是AuthorizeActivity)
        .setCustomizedAuthorizeActivityClass(CustomizedAuthorizedActivity.class)
         // 如果是要获得Code的方式，则把startGetAccessToken改成startGetOAuthCode即可。其他相同
        .startGetAccessToken(activity);
```

#### fastOAuth - 在miui上以对话框方式授权 (可选)
效果：sdk检测miui上用户已经登录系统帐号时，弹出对话框

+ miui版本支持： 8.2以上。 8.2以下/非miui上 future.getResult()时抛出XMAuthericationException
+ 需已经登录系统账号，否则future.getResult()时抛出XMAuthericationException

``` java
    XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setAppId(getAppId())
        .setRedirectUrl(getRedirectUri())
        .setScope(getScopeFromUi())
        .fastOAuth(MainActivity.this, XiaomiOAuthorize.TYPE_TOKEN);

```

#### 获取授权结果AccessToken/code (在后台线程调用)

``` java
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
        // 用户取消
    } catch (XMAuthericationException e1) {
        // error
    }
```

## 4) 使用AccessToken获取用户信息

#### 获取用户名片

``` java
    // 这一句可以在UI线程调用
    XiaomiOAuthFuture<String> future = new XiaomiOAuthorize().callOpenApi(context,
        appId,
        XiaomiOAuthConstants.OPEN_API_PATH_PROFILE,
        results.getAccessToken(),
        results.getMacKey(),
        results.getMacAlgorithm());
```

``` java
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

---------------

# Tips

## SkipConfirm
+ 当用户已经授权过，不会再让用户确认
+ 用户此时无法切换帐号
+ 弹出授权页面会相对变慢

``` java
    XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setSkipConfirm(true)
        // .setOtherParams...
        // .startGetAccessToken(activity);
```

## Scope
简单来说，Scope代表了一个AccessToken的权限。当使用一个AccessToken去访问OpenApi时，只有该AccessToken的Scope和该OpenApi需要的权限对得上的时候，服务器才会返回正确的结果，否则会报错。
代码中只有一个地方要用到Scope，那就是去拿AccessToken的时候。

代码中Scope的值应该是多少？请参照 http://dev.xiaomi.com/docs/passport/way/ 中“scope设置说明”一节，然后根据APP需要访问到的API去决定用哪些scope。比如，我将用AccessToken去活取用户的个人资料和好友信息，那么我的scope就应该是1和3。也可以用SDK中预定义好的常量XiaomiOAuthConstants.SCOPE_***。当然前提是，APP已经在预备步骤中，在dev.xiaomi.com上为该应用开启了相应的接口权限。


更多OAuth资料？
===
http://dev.xiaomi.com/docs/passport/user_guide



# Oauth-Android-sdk
## Quick Start

### 1. Preliminary steps

To create an account for your app on http://dev.xiaomi.com
   
+ Go to the website and log in
+ Click on “管理控制台/Administrative Console”
+ Click on “手机及平板应用/Mobile and Tablets Apps”
+ Click on “创建应用/Create an app”
+ Fill in the app name and package name
+ Click on “创建/Create”
+ Write down the AppID shows up
+ Look for “账号接入服务/Account Access Services”
+ Click on “Details”
+ Click on “Enable now”
+ Fill in “授权回调地址/Authorized Redirect URL”
+ Click on “启用/Enable”
+ Enable needed Open API

### 2. Integrating the following code in “AndroidManifest.xml”
```xml
    <uses-permission android:name="com.xiaomi.permission.AUTH_SERVICE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <activity android:name="com.xiaomi.account.openauth.AuthorizeActivity" />
``` 

### 3. Authorizing and getting AccessToken/code

+ sdk is able to detect: if it’s on miui, taking system account to authorize; if it’s on other OEMs, taking webview to log in and then authorize  
+ setCustomizedAuthorizeActivityClass(): it’s able to customize login page UI on non-miui roms, like actionbar, loading bar etc., please refer to CustomizedAuthorizedActivity in the demo.

```java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
         //The AppID you got from xiaomi.com
        .setAppId(appID)
         // The redirectUrl you filled in in application
        .setRedirectUrl(redirectUri)
        // int array, you can use constant like XiaomiOAuthConstants.SCOPE_*
        .setScope(scope)
         // set login page for non-miui roms(AuthorizeActivity is default)
        .setCustomizedAuthorizeActivityClass(CustomizedAuthorizedActivity.class)
         // If you want Code instead of AccessToken，please replace startGetAccessToken with startGetOAuthCode
        .startGetAccessToken(activity);
```

#### fastOAuth – authorize on pop-up window on miui

Effect:  if sdk detect user has signed in with system account, the window will pop up

+ Support: miui v8.2+. On miui older than v8.2 and non-miui roms, future.getResult() will throw XMAuthericationException  
+ System account/Mi account has to be logged in, otherwise future.getResult() will throw XMAuthericationException

```java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setAppId(getAppId())
        .setRedirectUrl(getRedirectUri())
        .setScope(getScopeFromUi())
        .fastOAuth(MainActivity.this, XiaomiOAuthorize.TYPE_TOKEN);
```

+ Getting authorized AccessToken/Code (call on a background thread)

```java
// Must call on the background thread
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
        // User cancel
    } catch (XMAuthericationException e1) {
        // error
    }
```

### 4. Getting user info with AccessToken

Getting user card

```java
// Could call on the UI thread
    XiaomiOAuthFuture<String> future = new XiaomiOAuthorize().callOpenApi(context,
        appId,
        XiaomiOAuthConstants.OPEN_API_PATH_PROFILE,
        results.getAccessToken(),
        results.getMacKey(),
        results.getMacAlgorithm());
 

//Must call on the background thread
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

## Tips

### 1. SkipConfirm

+ When user has already authorized,  will not ask again for authorization
+ It will be impossible for user to change an account
+ The process of popping up authorizing page will be slow

```java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setSkipConfirm(true)
        // .setOtherParams...
        // .startGetAccessToken(activity);
```

### 2. Scope

In simple terms, Scope is representing a permission of AccessToken. When using an ssAccessToken to access OpenApi, only if the Scope of the AccessToken is the permission that this OpenApi needed, the server will return a correct result, otherwise will produce an error. Scope will be used only once when  getting the AccessToken.  
For the value of Scope, please refer to https://dev.mi.com/docs/passport/en/scopes/ , and please choose the scope based on the API you needed. For example, if getting user’s personal data and friends’ list on Mi Talk, then value of scope would be 1 and 2. Another defined constant in SDK XiaomiOAuthConstants.SCOPE_*** works as well, as long as relevant permissions were enabled on dev.xiaomi.com in preliminary steps.  

#### Please visit https://dev.mi.com/docs/passport/en/user-guide/ for more details