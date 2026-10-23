package dev.androidbasekit.sample.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import dev.androidbasekit.sample.BuildConfig;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.FragmentAboutBinding;
import dev.androidbasekit.sample.model.repository.LaunchPreferences;
import dev.androidbasekit.sample.view.activity.SplashActivity;
import dev.androidbasekit.sample.view.activity.GuideActivity;
import dev.androidbasekit.ui.BaseFragment;

/** 项目与使用说明，集中放置版本信息和启动引导入口。 */
public final class AboutFragment extends BaseFragment<FragmentAboutBinding> {
    public AboutFragment() { super(FragmentAboutBinding::inflate); }

    @Override protected void onBindingCreated(Bundle state) {
        getBinding().versionInfo.setText(getString(R.string.hub_about_version,
                BuildConfig.VERSION_NAME, (long) BuildConfig.VERSION_CODE, BuildConfig.APPLICATION_ID));
        getBinding().openGuideButton.setOnClickListener(view ->
                startActivity(new Intent(requireContext(), GuideActivity.class)));
        getBinding().resetWelcomeButton.setOnClickListener(view -> {
            LaunchPreferences preferences = new LaunchPreferences(requireContext());
            preferences.setWelcomeCompleted(false);
            Intent intent = new Intent(requireContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
