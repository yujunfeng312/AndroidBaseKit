package dev.androidbasekit.sample.view.fragment;

import android.os.Bundle;
import androidx.navigation.fragment.NavHostFragment;
import dev.androidbasekit.common.BundleKeys;
import dev.androidbasekit.ui.BaseFragment;
import dev.androidbasekit.sample.databinding.FragmentDetailsBinding;

/**
 * Sample Java 详情页。读取 Navigation 参数并显示内容，演示 BaseFragment/ViewBinding 的轻量接入。
 */
public final class DetailsFragment extends BaseFragment<FragmentDetailsBinding> {
    public DetailsFragment() { super(FragmentDetailsBinding::inflate); }
    @Override protected void onBindingCreated(Bundle state) {
        getBinding().title.setText(requireArguments().getString(BundleKeys.PAGE_ID));
        getBinding().toolbar.setNavigationOnClickListener(view -> NavHostFragment.findNavController(this).popBackStack());
    }
}
