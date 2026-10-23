package dev.androidbasekit.sample.view.activity;

import android.os.Bundle;
import dev.androidbasekit.sample.databinding.ActivityDemoBinding;

/** 基础模块演示容器，统一复用模板的中文环境与系统栏适配。 */
public final class DemoActivity extends TemplateActivity<ActivityDemoBinding> {
    public DemoActivity() { super(ActivityDemoBinding::inflate); }
    @Override protected void onPageCreated(Bundle state) { }
}
