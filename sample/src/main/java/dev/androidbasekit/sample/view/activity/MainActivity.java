package dev.androidbasekit.sample.view.activity;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayoutMediator;
import dev.androidbasekit.sample.databinding.ActivityMainBinding;
import dev.androidbasekit.sample.view.adapter.MainPagerAdapter;

/** Java 主页：TabLayout 与 ViewPager2 双向联动，选中页交给 ViewPager2 自动保存恢复。 */
public final class MainActivity extends TemplateActivity<ActivityMainBinding> {
    private TabLayoutMediator tabMediator;

    public MainActivity() {
        super(ActivityMainBinding::inflate);
    }

    @Override protected void onPageCreated(Bundle state) {
        getBinding().viewPager.setAdapter(new MainPagerAdapter(this));
        tabMediator = new TabLayoutMediator(getBinding().tabs, getBinding().viewPager,
                (tab, position) -> tab.setText(MainPagerAdapter.titleAt(position)));
        tabMediator.attach();
        // 不在重建时调用 setCurrentItem(0)，以免覆盖 ViewPager2 恢复的选中页。
    }

    @Override protected void onDestroy() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        getBinding().viewPager.setAdapter(null);
        super.onDestroy();
    }
}
