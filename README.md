# TBS腾讯浏览服务
## 概述
移动互联网已经从生活到工作渗透到现实生活中每个点，伴随着链接的建立各种数字化的文件充满了我们的身边，在各种应用中出现：从小说中的epub、到股票中的pdf、再到工作群里的压缩包，更不用说，在日常微信、邮箱中的表格、ppt、doc信息。文件的浏览与管理的诉求已经变成了常态。TBS服务除了在网页、视频领域外，也在文件维度提供了稳定、全面的文件浏览服务。


**TBS已提供9种主流文件格式的本地打开，如果您需要使用更高级的能力请使用QQ浏览器打开文件：**
- 接入TBS可支持打开文件格式：doc、docx、ppt、pptx、xls、xlsx、pdf、txt、epub
- 调用QQ浏览器可打开：rar（包含加密格式）、zip（包含加密格式）、tar、bz2、gz、7z（包含加密格式）、doc、docx、ppt、pptx、xls、xlsx、txt、pdf、epub、chm、html/htm、xml、mht、url、ini、log、bat、php、js、lrc、jpg、jpeg、png、gif、bmp、tiff 、webp、mp3、m4a、aac、amr、wav、ogg、mid、ra、wma、mpga、ape、flac


### 1. 基础配置
#### 1.1 SDK接入
* jar包方式集成 您可将官网下载的jar包复制到您的App的libs目录，并且通过Add As Library的方式集成TBS SDK
* Gradle方式集成 您可以在使用SDK的模块的dependencies中添加引用进行集成：

```
api 'com.tencent.tbs.tbssdk:sdk:43939'
```
#### 1.2 权限配置
为了保障内核的动态下发和正常使用，您需要在您的AndroidManifest.xml增加如下权限：

```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```
#### 1.3 混淆配置
为了保障X5功能的正常使用，您需要在您的proguard.cfg文件中添加如下配置：

```
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**

-keep class com.tencent.smtt.** {
    *;
}

-keep class com.tencent.tbs.** {
    *;
}
```
#### 1.4 异常上报配置
为了提高合作方的webview场景稳定性，及时发现并解决x5相关问题，当客户端发生crash等异常情况并上报给服务器时请务必带上x5内核相关信息。x5内核异常信息获取接口为：com.tencent.smtt.sdk.WebView.getCrashExtraMessage(context)。以bugly日志上报为例：

```
UserStrategy strategy = new UserStrategy(appContext);
　　strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
　　　　public Map<String, String> onCrashHandleStart(
            int crashType,
            String errorType,
            String errorMessage,
            String errorStack) {

            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            String x5CrashInfo = com.tencent.smtt.sdk.WebView.getCrashExtraMessage(appContext);
            map.put("x5crashInfo", x5CrashInfo);
            return map;
　　　　}
　　　　@Override
　　　　public byte[] onCrashHandleStart2GetExtraDatas(
            int crashType,
            String errorType,
            String errorMessage,
            String errorStack) {
            try {
                return "Extra data.".getBytes("UTF-8");
            } catch (Exception e) {
                return null;
            }
　　　　}
　　});

　　CrashReport.initCrashReport(appContext, APPID, true, strategy);
```
#### 1.5 首次初始化冷启动优化
TBS内核首次使用和加载时，ART虚拟机会将Dex文件转为Oat，该过程由系统底层触发且耗时较长，很容易引起anr问题，解决方法是使用TBS的 ”dex2oat优化方案“。

（1）. 设置开启优化方案

```
// 在调用TBS初始化、创建WebView之前进行如下配置
HashMap map = new HashMap();
map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
QbSdk.initTbsSettings(map);
```
（2）增加Service声明

1. 在AndroidManifest.xml中增加内核首次加载时优化Service声明。
2. 该Service仅在TBS内核首次Dex加载时触发并执行dex2oat任务，任务完成后自动结束。

```
<service
android:name="com.tencent.smtt.export.external.DexClassLoaderProviderService"
android:label="dexopt"
android:process=":dexopt" >
</service>
```
#### 1.6 在APPlication中初始化腾讯的X5内核

```
public class TbsApplication extends Application {

    // tbs是否初始化成功
    public static boolean mIsInitTBSSuccess = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initTBS();
    }

    public void initTBS() {
        // 设置开启优化方案
        HashMap<String, Object> map = new HashMap<>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);

        // 下载x5内核，可以不需要，因为会共用其他软件的x5内核，比如微信、QQ等
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.setTbsListener(
                new TbsListener() {
                    @Override
                    public void onDownloadFinish(int i) {
                        //下载结束时的状态，下载成功时errorCode为100,其他均为失败，外部不需要关注具体的失败原因
                        Log.d("QbSdk", "onDownloadFinish -->下载X5内核完成：" + i);
                    }

                    @Override
                    public void onInstallFinish(int i) {
                        //安装结束时的状态，安装成功时errorCode为200,其他均为失败，外部不需要关注具体的失败原因
                        Log.d("QbSdk", "onInstallFinish -->安装X5内核进度：" + i);
                    }

                    @Override
                    public void onDownloadProgress(int i) {
                        //下载过程的通知，提供当前下载进度[0-100]
                        Log.d("QbSdk", "onDownloadProgress -->下载X5内核进度：" + i);
                    }
                });

        QbSdk.PreInitCallback cb =
                new QbSdk.PreInitCallback() {
                    @Override
                    public void onViewInitFinished(boolean arg0) {
                        // x5內核初始化完成的回调，true表x5内核加载成功，否则表加载失败，会自动切换到系统内核。
                        Log.d("QbSdk", " 内核加载 " + arg0);
                        mIsInitTBSSuccess = arg0;
                        Toast.makeText(TbsApplication.this, " 内核加载 " + arg0, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCoreInitFinished() {
                        //内核初始化完毕
                        Log.d("QbSdk", "内核初始化完毕");
                    }
                };

        // x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
        //Log.i("QbSdk", "是否可以加载X5内核: " + QbSdk.canLoadX5(this));
        Log.i("QbSdk", "app是否主动禁用了X5内核: " + QbSdk.getIsSysWebViewForcedByOuter());

    }
}
```

### 2.接入文件能力：
1. 加载文件核心类是TbsReaderView，TbsReaderView建议通过动态创建，不要使用xml中引用；
1. TBS目前只支持加载本地文件。所以远程文件需要先下载后用TBS加载文件显示；
1. 加载文件的界面，离开本界面之后务必销毁TbsReaderView，否则再次加载文件无法加载成功，会一直显示加载文件进度条；代码如下：tbsReaderView.onStop();
#### 2.1 实例化TbsReaderView

```
//实例化TbsReaderView，然后将它装入我们准备的容器
TbsReaderView tbsReaderView = new TbsReaderView(activity,readerCallback);
rlContent.addView(tbsReaderView,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
```
#### 2.2 预览文件的方法

```
public void displayFile(File mFile) {

        if (mFile != null && !TextUtils.isEmpty(mFile.toString())) {
            //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
            String bsReaderTemp = "/storage/emulated/0/TbsReaderTemp";
            File bsReaderTempFile = new File(bsReaderTemp);

            if (!bsReaderTempFile.exists()) {
                Log.d(TAG, "准备创建/storage/emulated/0/TbsReaderTemp！！");
                boolean mkdir = bsReaderTempFile.mkdir();
                if (!mkdir) {
                    Log.d(TAG, "创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
                }
            }

            //加载文件
            Bundle localBundle = new Bundle();
            Log.d(TAG, mFile.toString());
            localBundle.putString("filePath", mFile.toString());

            localBundle.putString("tempPath", Environment.getExternalStorageDirectory() + "/" + "TbsReaderTemp");

            if (this.mTbsReaderView == null) {
                this.mTbsReaderView = getTbsReaderView(context);
            }
            boolean bool = this.mTbsReaderView.preOpen(getFileType(mFile.toString()), false);
            if (bool) {
                this.mTbsReaderView.openFile(localBundle);
            } else {
                // todo 这里可以做个容错，比如用第三方软件打开。
                Toast.makeText(getContext(), "打开" + getFileType(mFile.toString()) + "文件失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "文件路径无效！");
        }
    }


    /***
     * 获取文件类型
     *
     * @param paramString
     * @return
     */
    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
            Log.d(TAG, "paramString---->null");
            return str;
        }
        Log.d(TAG, "paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            Log.d(TAG, "i <= -1");
            return str;
        }


        str = paramString.substring(i + 1);
        Log.d(TAG, "paramString.substring(i + 1)------>" + str);
        return str;
    }
```

```
public void onStopDisplay() {
        if (mTbsReaderView != null) {
            mTbsReaderView.onStop();
        }
    }
```
