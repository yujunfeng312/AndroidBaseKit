package dev.androidbasekit.sample.view.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.view.fragment.MainTabFragment;
import dev.androidbasekit.sample.view.fragment.ToolsFragment;
import dev.androidbasekit.sample.view.fragment.AboutFragment;

/** 固定三个 Tab，每次按位置创建 Fragment；不缓存 Fragment 或 View 实例。 */
public final class MainPagerAdapter extends FragmentStateAdapter {
    // 新主页使用独立 ID，避免更新后恢复旧版三个占位 Fragment。
    private static final long PAGE_ID_BASE = 100L;
    private static final int[] TITLES = {
            R.string.template_tab_home, R.string.template_tab_discover, R.string.template_tab_profile
    };

    public MainPagerAdapter(FragmentActivity activity) {
        super(activity);
    }

    @StringRes public static int titleAt(int position) {
        return TITLES[position];
    }

    @NonNull @Override public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new MainTabFragment();
            case 1: return new ToolsFragment();
            case 2: return new AboutFragment();
            default: throw new IndexOutOfBoundsException(String.valueOf(position));
        }
    }

    @Override public int getItemCount() {
        return TITLES.length;
    }

    @Override public long getItemId(int position) { return PAGE_ID_BASE + position; }

    @Override public boolean containsItem(long itemId) {
        return itemId >= PAGE_ID_BASE && itemId < PAGE_ID_BASE + TITLES.length;
    }
}
