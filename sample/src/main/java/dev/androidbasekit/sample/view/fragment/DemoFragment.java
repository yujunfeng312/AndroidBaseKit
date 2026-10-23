package dev.androidbasekit.sample.view.fragment;

import dev.androidbasekit.sample.view.activity.ResultActivity;
import dev.androidbasekit.sample.view.adapter.DemoAdapter;
import dev.androidbasekit.sample.viewmodel.DemoViewModel;
import dev.androidbasekit.sample.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.navigation.fragment.NavHostFragment;
import dev.androidbasekit.common.IntentKeys;
import dev.androidbasekit.common.BundleKeys;
import dev.androidbasekit.ui.*;
import dev.androidbasekit.sample.databinding.FragmentDemoBinding;

/**
 * Sample Java 功能页。观察 ViewModel，演示列表、Loading/Empty/Error、刷新和 Activity Result；观察绑定视图生命周期。
 */
public final class DemoFragment extends BaseFragment<FragmentDemoBinding> {
    public DemoFragment() { super(FragmentDemoBinding::inflate); }
    private DemoAdapter adapter;
    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (getView() != null && result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                getBinding().resultText.setText(getString(R.string.result_value, result.getData().getStringExtra(IntentKeys.RESULT_TEXT)));
            }
        });
    @Override protected void onBindingCreated(Bundle state) {
        DemoViewModel vm = new ViewModelProvider(this, new DemoViewModel.Factory(requireActivity().getApplication())).get(DemoViewModel.class);
        getBinding().toolbar.setNavigationOnClickListener(view -> requireActivity().finish());
        adapter = new DemoAdapter(item -> {
            Bundle args = new Bundle(); args.putString(BundleKeys.PAGE_ID, item.getTitle());
            NavHostFragment.findNavController(this).navigate(R.id.action_demo_to_details, args);
        });
        getBinding().list.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().list.setAdapter(adapter);
        getBinding().pageState.attachContent(getBinding().list);
        getBinding().pageState.setRetryAction(vm::loadLocal);
        vm.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            getBinding().dataCount.setText(getString(R.string.demo_list_count, items.size()));
        });
        vm.getPageState().observe(getViewLifecycleOwner(), status -> {
            getBinding().pageState.render(status);
            int label;
            switch (status.getStatus()) {
                case CONTENT: label = R.string.demo_status_content; break;
                case EMPTY: label = R.string.demo_status_empty; break;
                case ERROR: label = R.string.demo_status_error; break;
                default: label = R.string.demo_status_loading;
            }
            getBinding().pageStatus.setText(label);
            renderBusy(vm);
        });
        vm.getRequestBusy().observe(getViewLifecycleOwner(), busy -> renderBusy(vm));
        vm.getNetworkRequest().observe(getViewLifecycleOwner(), network -> renderBusy(vm));
        vm.getRefreshError().observe(getViewLifecycleOwner(), message -> UiFeedback.snackbar(getBinding().getRoot(), message));
        Clicks.setDebounced(getBinding().localButton, vm::loadLocal);
        Clicks.setDebounced(getBinding().networkButton, vm::refreshNetwork);
        Clicks.setDebounced(getBinding().emptyButton, () -> { vm.showEmpty(); showPageState(); });
        Clicks.setDebounced(getBinding().errorButton, () -> { vm.showError(); showPageState(); });
        Clicks.setDebounced(getBinding().resultButton, () -> resultLauncher.launch(new Intent(requireContext(), ResultActivity.class)));
    }

    private void showPageState() {
        FragmentDemoBinding binding = getBinding();
        binding.demoScroll.post(() -> binding.demoScroll.smoothScrollTo(0, binding.pageStatus.getTop()));
    }

    private void renderBusy(DemoViewModel vm) {
        boolean busy = Boolean.TRUE.equals(vm.getRequestBusy().getValue());
        boolean network = Boolean.TRUE.equals(vm.getNetworkRequest().getValue());
        getBinding().localButton.setLoading(busy && !network);
        getBinding().networkButton.setLoading(busy && network);
        PageState state = vm.getPageState().getValue();
        boolean refreshingContent = busy && state != null && state.getStatus() == PageStatus.CONTENT;
        getBinding().refreshProgress.setVisibility(refreshingContent ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    @Override public void onDestroyView() {
        getBinding().list.setAdapter(null); adapter = null;
        super.onDestroyView();
    }
}
