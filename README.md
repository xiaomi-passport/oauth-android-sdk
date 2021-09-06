快速开始
===

# 此README已不再更新，点击[查看最新文档](https://mzd5tljlyv.feishu.cn/docs/doccnCKqdBhEUX5Lq5izm4Apswe#IQr3Hr)
===

## 1) 预备步骤
 在[小米开放平台](https://dev.mi.com/console/doc/detail?pId=897) 中创建应用，查看更多文档。
 
 有如何关于接入 Android oauth sdk 的问题或建议，可以在[issus](https://github.com/xiaomi-passport/oauth-android-sdk/issues)中提出。

## 2) 在应用的里添加以下配置：

添加依赖, 总是依赖最新版本 或者 [查看release版本](https://github.com/xiaomi-passport/oauth-android-sdk/releases)

```groovy
repositories {
    
    // maven { url 'https://raw.githubusercontent.com/xiaomi-passport/maven-repository/master/releases' }
    // github如果被X，需要换成下面这一行
    maven { url 'https://gitlab.com/xiaomi-passport/maven-repository/raw/master/releases' }    
}

dependencies {
    compile 'com.xiaomi.account:oauth-android:latest.release' // 总是依赖最新版本
}
```
### 注意:

'com.xiaomi.account:oauth-android:3.1.1' 版本迁移至 https://packages.aliyun.com/repos/2028284-release-awMPKn/packages
若使用需要进行如下配置:

(a) 在build.gradle中设置仓库的访问凭证
``` java
allprojects {
  repositories {
    maven {
      url 'https://maven.aliyun.com/repository/public'
    }
    maven {
      credentials {
        username '************************'
        password '************'
      }
      url 'https://packages.aliyun.com/maven/repository/2028284-release-awMPKn/'
    }
    maven {
      credentials {
        username '************************'
        password '************'
      }
      url 'https://packages.aliyun.com/maven/repository/2028284-snapshot-gw4bH1/'
    }
  }
}

```

(b)配置包信息,在你的build.gradle文件中加入你要引用的文件信息。
```java
dependencies {
  compile 'com.xiaomi.account:oauth-android:3.1.1'
}
```

AndroidManifest.xml
``` xml
<uses-permission android:name="com.xiaomi.permission.AUTH_SERVICE" />
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<activity android:name="com.xiaomi.account.openauth.AuthorizeActivity" />
```

## 3) 授权并获取AccessToken/code

### 3.1 授权
sdk会自行判断：在miui上，启动系统帐号进行授权；非miui上，使用webview登录然后授权

``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
     //开发者预先申请好的 AppID
    .setAppId(appID)
     // 开发者预先申请时填好的 redirectUrl
     // .setUseSystemAccountLogin(false) // 默认是true,,如果小米系统账号没有登录,则调起系统登录ui进行登录;如果设置为false ,如果小米系统账号没有登录,则返回错误吗code=-1004 代表系统账号未登录.
    .setRedirectUrl(redirectUri)
     // 设置登录方式,例如"qr"为扫码登录
    .setLoginType("qr")
     // (3.1.1版本支持)业务使用了帐号的sdk可以在h5页面定制ui, 值是业务提供的
    .setBannerBiz("test")
     // (3.1.1版本支持)隐藏二维码登录中下方的3个app图标
     .setHideQrApps(true)
     // 如果是要获得Code的方式，则把startGetAccessToken改成startGetOAuthCode即可。其他相同
    .startGetAccessToken(activity);
    
```

获取授权结果AccessToken/code (在后台线程调用)

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

### 3.2 设置 scope (可选)
设置授权的权限列表，不设置默认是全部权限。
  
``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
    // ...
    // int数组，可以用XiaomiOAuthConstants.SCOPE_*等常量, 也可自己添加
    .setScope(scope)
    // ...
```

简单来说，Scope代表了一个AccessToken的权限。当使用一个AccessToken去访问OpenApi时，
只有该AccessToken的Scope和该OpenApi需要的权限对得上的时候，服务器才会返回正确的结果，否则会报错。
代码中只有一个地方要用到Scope，那就是去拿AccessToken的时候。

代码中Scope的值应该是多少？请参照 dev.mi.com/console/doc/detail?pId=762，
然后根据APP需要访问到的API去决定用哪些scope。比如，我将用AccessToken去获取用户的个人资料和好友信息，
那么我的scope就应该是1和3。也可以用SDK中预定义好的常量XiaomiOAuthConstants.SCOPE_***

### 3.3 使用webview登录授权时，自定义页面的activity (可选)
可以自定义设置actionbar、进度条等，可参照demo中的CustomizedAuthorizedActivity。

``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
    // ...
    // 设置自定义的非miui上登录界面(默认是AuthorizeActivity)
    .setCustomizedAuthorizeActivityClass(CustomizedAuthorizedActivity.class)
    // ...
```

### 3.4 当用户已经授权过，不会再让用户确认 (可选)
当用户已经授权过，不会再让用户确认，用户此时无法切换帐号。
如果用户没有授权过，会再次弹起授权页面

``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
    // ...
    .setSkipConfirm(true)
    // ...
```

### 3.5 在miui上以对话框方式授权 (可选)
效果：sdk检测miui上用户已经登录系统帐号时，弹出对话框

+ miui版本支持： 8.2以上。 8.2以下/非miui上 future.getResult()时抛出XMAuthericationException
+ 需已经登录系统账号，否则授权结果xiaomiOAuthResults.getErrorCode()返回错误码 XiaomiOAuthConstants.ERROR_LOGIN_FAILED(-1002)

``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
    // ...
    .fastOAuth(MainActivity.this, XiaomiOAuthorize.TYPE_TOKEN);

```

### 3.6 使用高安全级别（系统浏览器登录）

``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
    // ...
   .setUseSystemBrowserLogin(true);

```

## 4) 使用AccessToken获取用户信息

获取用户名片

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

更多接口请在 https://dev.mi.com/console/doc/detail?pId=713 查看


## 5) 更多OAuth资料？

https://dev.mi.com/console/doc/detail?pId=897

## 6) 业务方接入log文档
``` java
//app初始化
public class DiagnosisDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化并设置日志类型
        DiagnosisController.init(this, "oauth");
    }
}

上层app打开诊断开关和信息页面

import com.xiaomi.accountsdk.diagnosis.ui.PassportDiagnosisActivity;

PassportDiagnosisActivity.start(MainActivity.this);

```
---------------

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
``` xml
    <uses-permission android:name="com.xiaomi.permission.AUTH_SERVICE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <activity android:name="com.xiaomi.account.openauth.AuthorizeActivity" />
``` 

### 3. Authorizing and getting AccessToken/code

+ sdk is able to detect: if it’s on miui, taking system account to authorize; if it’s on other OEMs, taking webview to log in and then authorize  
+ setCustomizedAuthorizeActivityClass(): it’s able to customize login page UI on non-miui roms, like actionbar, loading bar etc., please refer to CustomizedAuthorizedActivity in the demo.

``` java
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

``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setAppId(getAppId())
        .setRedirectUrl(getRedirectUri())
        .setScope(getScopeFromUi())
        .fastOAuth(MainActivity.this, XiaomiOAuthorize.TYPE_TOKEN);
```

+ Getting authorized AccessToken/Code (call on a background thread)

``` java
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

``` java
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

``` java
XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
        .setSkipConfirm(true)
        // .setOtherParams...
        // .startGetAccessToken(activity);
```

### 2. Scope

In simple terms, Scope is representing a permission of AccessToken. When using an ssAccessToken to access OpenApi, only if the Scope of the AccessToken is the permission that this OpenApi needed, the server will return a correct result, otherwise will produce an error. Scope will be used only once when  getting the AccessToken.  
For the value of Scope, please refer to https://dev.mi.com/docs/passport/en/scopes/ , and please choose the scope based on the API you needed. For example, if getting user’s personal data and friends’ list on Mi Talk, then value of scope would be 1 and 2. Another defined constant in SDK XiaomiOAuthConstants.SCOPE_*** works as well, as long as relevant permissions were enabled on dev.xiaomi.com in preliminary steps.  

#### Please visit https://dev.mi.com/docs/passport/en/user-guide/ for more details
