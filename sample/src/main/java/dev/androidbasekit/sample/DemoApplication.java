package dev.androidbasekit.sample;

import android.app.Application;
import dev.androidbasekit.storage.AppPreferences;
import timber.log.Timber;
/**
 * Sample Application。初始化 MMKV 与调试日志，是框架初始化接入示例。
 */
public final class DemoApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        AppPreferences.initialize(this);
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    }
}
