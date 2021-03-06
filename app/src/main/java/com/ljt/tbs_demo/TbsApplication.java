package com.ljt.tbs_demo;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.util.HashMap;

/**
 * @author Administrator
 */
public class TbsApplication extends Application {

    // tbs是否初始化成功
    public static boolean mIsInitTBSSuccess = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initTBS();
    }

    public void initTBS() {
        //  设置开启优化方案
        HashMap<String, Object> map = new HashMap<>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);

        //下载x5内核，可以不需要，因为会共用其他软件的x5内核，比如微信、QQ等
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
