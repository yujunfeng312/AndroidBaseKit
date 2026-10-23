package dev.androidbasekit.sample.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.ActivityNativeViewsBinding;
import dev.androidbasekit.sample.view.adapter.NativeViewCatalogAdapter;
import dev.androidbasekit.sample.view.nativeviews.NativeViewCatalog;
import java.util.ArrayList;
import java.util.Locale;

/** 按类别排列的原生控件入口，搜索类名、分类或属性均可打开独立页面。 */
public final class NativeViewsActivity extends TemplateActivity<ActivityNativeViewsBinding> {
    private final ArrayList<NativeViewCatalog.Entry> visible = new ArrayList<>();
    private NativeViewCatalogAdapter adapter;

    public NativeViewsActivity() { super(ActivityNativeViewsBinding::inflate); }

    @Override protected void onPageCreated(Bundle state) {
        getBinding().toolbar.setNavigationOnClickListener(view -> finish());
        adapter = new NativeViewCatalogAdapter(this);
        getBinding().catalog.setAdapter(adapter);
        getBinding().catalog.setEmptyView(getBinding().empty);
        getBinding().catalog.setOnItemClickListener((parent, view, position, id) -> {
            NativeViewCatalog.Entry entry = adapter.entryAt(position);
            if (entry != null) startActivity(new Intent(this, NativeViewDemoActivity.class)
                    .putExtra(NativeViewDemoActivity.EXTRA_VIEW, entry.name));
        });
        getBinding().search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }
            @Override public boolean onQueryTextChange(String query) { filter(query); return true; }
        });
        filter("");
    }

    private void filter(String query) {
        String key = query.trim().toLowerCase(Locale.ROOT);
        visible.clear();
        for (NativeViewCatalog.Entry entry : NativeViewCatalog.ALL) {
            String label = entry.display(this);
            if (label.toLowerCase(Locale.ROOT).contains(key)) {
                visible.add(entry);
            }
        }
        int groups = adapter.submitEntries(visible);
        getBinding().summary.setText(getString(R.string.native_catalog_summary, groups, visible.size()));
    }
}
