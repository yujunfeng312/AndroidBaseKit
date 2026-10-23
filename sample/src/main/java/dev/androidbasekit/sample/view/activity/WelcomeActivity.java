package dev.androidbasekit.sample.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.ActivityWelcomeBinding;
import dev.androidbasekit.sample.model.repository.LaunchPreferences;
import dev.androidbasekit.sample.view.adapter.WelcomePagerAdapter;

/** 四张 PNG 产品介绍；完成欢迎后直接进入主页。 */
public final class WelcomeActivity extends TemplateActivity<ActivityWelcomeBinding> {
    private boolean openingNext;
    private final ViewPager2.OnPageChangeCallback pageCallback = new ViewPager2.OnPageChangeCallback() {
        @Override public void onPageSelected(int position) { updateControls(position); }
    };

    public WelcomeActivity() { super(ActivityWelcomeBinding::inflate); }

    @Override protected void onPageCreated(Bundle state) {
        getBinding().welcomePager.setAdapter(new WelcomePagerAdapter());
        getBinding().welcomePager.registerOnPageChangeCallback(pageCallback);
        updateControls(getBinding().welcomePager.getCurrentItem());
        getBinding().startButton.setOnClickListener(view -> {
            if (openingNext) return;
            int page = getBinding().welcomePager.getCurrentItem();
            if (page < WelcomePagerAdapter.PAGE_COUNT - 1) {
                getBinding().welcomePager.setCurrentItem(page + 1, false);
                return;
            }
            openingNext = true;
            LaunchPreferences preferences = new LaunchPreferences(this);
            preferences.setWelcomeCompleted(true);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void updateControls(int position) {
        getBinding().pageIndicator.setText(getString(R.string.template_page_indicator,
                position + 1, WelcomePagerAdapter.PAGE_COUNT));
        getBinding().startButton.setText(position == WelcomePagerAdapter.PAGE_COUNT - 1
                ? R.string.template_welcome_continue : R.string.template_next);
    }

    @Override protected void onDestroy() {
        getBinding().welcomePager.unregisterOnPageChangeCallback(pageCallback);
        getBinding().welcomePager.setAdapter(null);
        super.onDestroy();
    }
}
