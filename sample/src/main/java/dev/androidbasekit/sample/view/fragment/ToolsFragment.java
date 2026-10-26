package dev.androidbasekit.sample.view.fragment;

import android.os.Bundle;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import dev.androidbasekit.common.ByteSizes;
import dev.androidbasekit.common.Durations;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.FragmentToolsBinding;
import dev.androidbasekit.storage.MemoryCache;
import dev.androidbasekit.system.ClipboardTools;
import dev.androidbasekit.system.permission.CameraPermissionManager;
import dev.androidbasekit.system.permission.PermissionOptions;
import dev.androidbasekit.ui.BaseFragment;
import dev.androidbasekit.ui.Clicks;
import dev.androidbasekit.ui.LoadingDialog;
import dev.androidbasekit.ui.UiFeedback;

/** 常用工具的可操作示例；缓存属于当前 Fragment，剪贴板仅在用户主动点击后写入。 */
public final class ToolsFragment extends BaseFragment<FragmentToolsBinding> {
    private static final String CACHE_KEY = "tools.demo";
    private static final String LOADING_TAG = "tools.loading";
    private final MemoryCache<String> cache = new MemoryCache<>();
    private androidx.appcompat.app.AlertDialog confirmDialog;

    public ToolsFragment() { super(FragmentToolsBinding::inflate); }

    @Override protected void onBindingCreated(Bundle state) {
        getBinding().toastButton.setOnClickListener(view -> UiFeedback.toast(view, R.string.tools_feedback_message));
        getBinding().snackbarButton.setOnClickListener(view -> UiFeedback.snackbar(view, R.string.tools_feedback_message));
        getBinding().dialogButton.setOnClickListener(view -> {
            if (confirmDialog != null && confirmDialog.isShowing()) return;
            confirmDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.tools_dialog_title).setMessage(R.string.tools_dialog_body)
                    .setNegativeButton(R.string.tools_cancel, null)
                    .setPositiveButton(R.string.tools_confirm, (dialog, which) -> UiFeedback.toast(view, R.string.tools_feedback_message)).show();
            confirmDialog.setOnDismissListener(dialog -> confirmDialog = null);
        });
        Clicks.setDebounced(getBinding().loadingButton, () -> {
            if (!getChildFragmentManager().isStateSaved()
                    && getChildFragmentManager().findFragmentByTag(LOADING_TAG) == null) {
                new LoadingDialog().show(getChildFragmentManager(), LOADING_TAG);
            }
        });
        getBinding().copyButton.setOnClickListener(view -> {
            String text = getBinding().clipboardInput.getText().toString();
            if (text.trim().isEmpty()) {
                getBinding().clipboardInput.setError(getString(R.string.tools_input_empty));
                return;
            }
            ClipboardTools.copyText(requireContext(), text, getString(R.string.tools_clipboard_label), false);
            UiFeedback.snackbar(view, R.string.tools_copied);
        });
        getBinding().formatButton.setOnClickListener(view -> formatValues());
        getBinding().permissionButton.setOnClickListener(view -> new CameraPermissionManager().request(this,
                new PermissionOptions().explainBeforeRequest()
                        .onGranted(permissions -> UiFeedback.toast(getBinding().getRoot(), R.string.tools_permission_granted))
                        .onDenied((granted, denied) -> UiFeedback.toast(getBinding().getRoot(), R.string.tools_permission_denied))));
        formatValues();
        getBinding().cacheWrite.setOnClickListener(view -> {
            cache.put(CACHE_KEY, getString(R.string.tools_cache_value), 10_000L);
            getBinding().cacheResult.setText(R.string.tools_cache_written);
        });
        getBinding().cacheRead.setOnClickListener(view -> {
            String value = cache.get(CACHE_KEY);
            getBinding().cacheResult.setText(value == null ? getString(R.string.tools_cache_empty)
                    : getString(R.string.tools_cache_result, value));
        });
        getBinding().cacheClear.setOnClickListener(view -> {
            cache.remove(CACHE_KEY);
            getBinding().cacheResult.setText(R.string.tools_cache_cleared);
        });
    }

    private void formatValues() {
        try {
            long seconds = Long.parseLong(getBinding().secondsInput.getText().toString().trim());
            long bytes = Long.parseLong(getBinding().bytesInput.getText().toString().trim());
            getBinding().formatResult.setText(getString(R.string.tools_format_result,
                    Durations.formatSeconds(seconds), ByteSizes.format(bytes)));
        } catch (IllegalArgumentException error) {
            getBinding().formatResult.setText(R.string.tools_number_error);
        }
    }

    @Override public void onDestroyView() {
        if (confirmDialog != null) { confirmDialog.dismiss(); confirmDialog = null; }
        super.onDestroyView();
    }
}
