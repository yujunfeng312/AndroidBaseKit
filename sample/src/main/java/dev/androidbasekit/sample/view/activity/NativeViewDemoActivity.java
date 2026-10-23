package dev.androidbasekit.sample.view.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.ActivityNativeViewDemoBinding;
import dev.androidbasekit.sample.view.nativeviews.NativeViewCatalog;
import dev.androidbasekit.sample.view.nativeviews.NativeViewExamples;
import java.util.ArrayList;
import java.util.List;

/** 每种 View 的独立属性页；通用属性实时作用于示例，控件专属属性以多组示例对比。 */
public final class NativeViewDemoActivity extends TemplateActivity<ActivityNativeViewDemoBinding> {
    public static final String EXTRA_VIEW = "native_view";
    private final List<View> targets = new ArrayList<>();
    private NativeViewExamples examples;
    private int opacity = 100;
    private int padding = 0;
    private boolean enabled = true;
    private static final class OriginalProperties {
        final int left, top, right, bottom;
        final float alpha;
        OriginalProperties(View view) {
            left = view.getPaddingLeft(); top = view.getPaddingTop();
            right = view.getPaddingRight(); bottom = view.getPaddingBottom(); alpha = view.getAlpha();
        }
    }

    public NativeViewDemoActivity() { super(ActivityNativeViewDemoBinding::inflate); }

    @Override protected void onPageCreated(Bundle state) {
        NativeViewCatalog.Entry entry = NativeViewCatalog.find(getIntent().getStringExtra(EXTRA_VIEW));
        if (entry == null) { finish(); return; }
        if (state != null) {
            opacity = state.getInt("opacity", 100);
            padding = state.getInt("padding", 0);
            enabled = state.getBoolean("enabled", true);
        }
        getBinding().back.setOnClickListener(view -> finish());
        getBinding().title.setText(entry.titleRes);
        render(entry);
    }

    private void render(NativeViewCatalog.Entry entry) {
        if (examples != null) examples.close();
        targets.clear();
        getBinding().examples.removeAllViews();
        note(getString(R.string.native_entry_description, getString(entry.categoryRes), getString(entry.propertiesRes)));
        note(getString(R.string.native_common_panel));
        Switch available = new Switch(this);
        available.setText(R.string.native_enabled_label); available.setChecked(enabled);
        available.setOnCheckedChangeListener((button, checked) -> {
            enabled = checked;
            for (View target : targets) setEnabledTree(target, checked);
        });
        getBinding().examples.addView(available);
        slider(getString(R.string.native_alpha_label), opacity, 100, value -> {
            opacity = value;
            for (View target : targets) target.setAlpha(((OriginalProperties) target.getTag()).alpha * value / 100f);
        });
        slider(getString(R.string.native_padding_label), padding, 32, value -> {
            padding = value;
            for (View target : targets) applyPadding(target);
        });
        Button reset = new Button(this); reset.setText(R.string.native_reset);
        reset.setOnClickListener(view -> {
            opacity = 100; padding = 0; enabled = true; render(entry);
        });
        getBinding().examples.addView(reset);
        examples = new NativeViewExamples(this, this::showExample, this::note);
        examples.build(entry.name);
        // 稳定的 ID 让文本、勾选、选中项等参与系统 View 状态恢复。
        assignIds(getBinding().examples, new int[]{0x00aa0000});
    }

    private void showExample(String attributes, View view, int heightDp) {
        note(attributes);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightDp == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : dp(heightDp));
        params.bottomMargin = dp(16);
        getBinding().examples.addView(view, params);
        targets.add(view);
        view.setTag(new OriginalProperties(view));
        view.setAlpha(view.getAlpha() * opacity / 100f);
        applyPadding(view);
        if (!enabled) setEnabledTree(view, false);
    }

    private void note(String text) {
        TextView label = new TextView(this);
        label.setText(text); label.setTextSize(14);
        label.setPadding(0, dp(12), 0, dp(8));
        getBinding().examples.addView(label);
    }

    private void slider(String property, int initial, int max, java.util.function.IntConsumer update) {
        TextView label = new TextView(this); label.setText(getString(R.string.native_property_value, property, initial));
        getBinding().examples.addView(label);
        SeekBar slider = new SeekBar(this); slider.setMax(max); slider.setProgress(initial);
        slider.setContentDescription(property);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                label.setText(getString(R.string.native_property_value, property, value)); update.accept(value);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        getBinding().examples.addView(slider);
    }

    private void applyPadding(View view) {
        OriginalProperties original = (OriginalProperties) view.getTag();
        int extra = dp(padding);
        view.setPadding(original.left + extra, original.top + extra,
                original.right + extra, original.bottom + extra);
    }

    private void setEnabledTree(View view, boolean value) {
        view.setEnabled(value);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) setEnabledTree(group.getChildAt(i), value);
        }
    }

    private void assignIds(View view, int[] next) {
        if (view.getId() == View.NO_ID) view.setId(next[0]++);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) assignIds(group.getChildAt(i), next);
        }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("opacity", opacity); state.putInt("padding", padding);
        state.putBoolean("enabled", enabled);
        super.onSaveInstanceState(state);
    }

    @Override protected void onStop() {
        if (examples != null) examples.pause();
        super.onStop();
    }

    @Override protected void onStart() {
        super.onStart();
        if (examples != null) examples.resume();
    }

    @Override protected void onDestroy() {
        if (examples != null) { examples.close(); examples = null; }
        super.onDestroy();
    }
}
