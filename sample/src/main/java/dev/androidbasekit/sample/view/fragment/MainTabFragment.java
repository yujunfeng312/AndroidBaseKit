package dev.androidbasekit.sample.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.FragmentMainTabBinding;
import dev.androidbasekit.sample.databinding.ItemHubEntryBinding;
import dev.androidbasekit.sample.view.activity.DemoActivity;
import dev.androidbasekit.sample.view.activity.NativeViewsActivity;
import dev.androidbasekit.sample.view.activity.SystemUiDemoActivity;
import dev.androidbasekit.ui.BaseFragment;

/** 示例目录：每种演示只有一个入口，工具和项目说明由独立 Fragment 承载。 */
public final class MainTabFragment extends BaseFragment<FragmentMainTabBinding> {
    public MainTabFragment() { super(FragmentMainTabBinding::inflate); }

    @Override protected void onBindingCreated(Bundle state) {
        getBinding().title.setText(R.string.hub_catalog_title);
        getBinding().description.setText(R.string.hub_catalog_body);
        ViewCompat.setAccessibilityHeading(getBinding().title, true);
        addEntry(R.string.hub_modules_tag, R.string.hub_modules_title,
                R.string.hub_modules_body, DemoActivity.class);
        addEntry(R.string.hub_native_tag, R.string.native_views_title,
                R.string.hub_native_body, NativeViewsActivity.class);
        addEntry(R.string.hub_system_tag, R.string.system_ui_title,
                R.string.hub_system_body, SystemUiDemoActivity.class);
    }

    private void addEntry(@StringRes int tag, @StringRes int title,
                          @StringRes int description, Class<?> destination) {
        ItemHubEntryBinding card = ItemHubEntryBinding.inflate(getLayoutInflater(), getBinding().cards, false);
        card.tag.setText(tag);
        card.title.setText(title);
        card.description.setText(description);
        card.getRoot().setClickable(true);
        card.getRoot().setFocusable(true);
        card.getRoot().setOnClickListener(view -> startActivity(new Intent(requireContext(), destination)));
        getBinding().cards.addView(card.getRoot());
    }
}
