package dev.androidbasekit.sample.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import dev.androidbasekit.common.IntentKeys;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.ActivityResultBinding;

/** 结果返回示例：提交按钮返回消息，工具栏返回仅关闭页面。 */
public final class ResultActivity extends TemplateActivity<ActivityResultBinding> {
    public ResultActivity() { super(ActivityResultBinding::inflate); }
    @Override protected void onPageCreated(Bundle state) {
        getBinding().toolbar.setNavigationOnClickListener(view -> finish());
        getBinding().doneButton.setOnClickListener(view -> {
            setResult(Activity.RESULT_OK, new Intent().putExtra(IntentKeys.RESULT_TEXT, getString(R.string.result_sample)));
            finish();
        });
    }
}
