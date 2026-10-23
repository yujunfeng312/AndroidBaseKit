package dev.androidbasekit.sample.view.activity;

import android.os.Bundle;
import dev.androidbasekit.sample.databinding.ActivityGuideBinding;
import dev.androidbasekit.sample.model.repository.LaunchPreferences;

/** 独立功能引导页：用户主动点击开始后才记录完成，返回退出不会跳过下次引导。 */
public final class GuideActivity extends TemplateActivity<ActivityGuideBinding> {
    private boolean openingMain;

    public GuideActivity() {
        super(ActivityGuideBinding::inflate);
    }

    @Override protected void onPageCreated(Bundle state) {
        getBinding().startButton.setOnClickListener(view -> {
            if (openingMain) return;
            openingMain = true;
            new LaunchPreferences(this).setGuideCompleted(true);
            finish();
        });
    }
}
