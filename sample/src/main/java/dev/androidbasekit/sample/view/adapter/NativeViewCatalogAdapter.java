package dev.androidbasekit.sample.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.core.view.ViewCompat;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.ItemNativeViewCatalogBinding;
import dev.androidbasekit.sample.databinding.ItemNativeViewSectionBinding;
import dev.androidbasekit.sample.view.nativeviews.NativeViewCatalog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 目录分组与控件卡片。分类标题不可点击，搜索后重新计算每组数量。 */
public final class NativeViewCatalogAdapter extends BaseAdapter {
    private static final class Row {
        final int category;
        final int count;
        final NativeViewCatalog.Entry entry;

        Row(int category, int count, NativeViewCatalog.Entry entry) {
            this.category = category; this.count = count; this.entry = entry;
        }
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final List<Row> rows = new ArrayList<>();

    public NativeViewCatalogAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public int submitEntries(List<NativeViewCatalog.Entry> entries) {
        Map<Integer, List<NativeViewCatalog.Entry>> groups = new LinkedHashMap<>();
        for (NativeViewCatalog.Entry entry : entries) {
            List<NativeViewCatalog.Entry> group = groups.get(entry.categoryRes);
            if (group == null) { group = new ArrayList<>(); groups.put(entry.categoryRes, group); }
            group.add(entry);
        }
        rows.clear();
        for (Map.Entry<Integer, List<NativeViewCatalog.Entry>> group : groups.entrySet()) {
            rows.add(new Row(group.getKey(), group.getValue().size(), null));
            for (NativeViewCatalog.Entry entry : group.getValue()) rows.add(new Row(group.getKey(), 0, entry));
        }
        notifyDataSetChanged();
        return groups.size();
    }

    public NativeViewCatalog.Entry entryAt(int position) { return rows.get(position).entry; }

    @Override public int getCount() { return rows.size(); }
    @Override public String getItem(int position) {
        Row row = rows.get(position);
        return row.entry == null ? context.getString(row.category) : row.entry.display(context);
    }
    @Override public long getItemId(int position) { return position; }
    @Override public int getViewTypeCount() { return 2; }
    @Override public int getItemViewType(int position) { return rows.get(position).entry == null ? 0 : 1; }
    @Override public boolean areAllItemsEnabled() { return false; }
    @Override public boolean isEnabled(int position) { return rows.get(position).entry != null; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        Row row = rows.get(position);
        if (row.entry == null) {
            ItemNativeViewSectionBinding binding;
            if (convertView == null) {
                binding = ItemNativeViewSectionBinding.inflate(inflater, parent, false);
                binding.getRoot().setTag(binding);
                ViewCompat.setAccessibilityHeading(binding.category, true);
            } else binding = (ItemNativeViewSectionBinding) convertView.getTag();
            binding.category.setText(row.category);
            binding.count.setText(context.getString(R.string.native_catalog_section_count, row.count));
            return binding.getRoot();
        }
        ItemNativeViewCatalogBinding binding;
        if (convertView == null) {
            binding = ItemNativeViewCatalogBinding.inflate(inflater, parent, false);
            binding.getRoot().setTag(binding);
        } else binding = (ItemNativeViewCatalogBinding) convertView.getTag();
        String title = context.getString(row.entry.titleRes);
        String suffix = context.getString(R.string.native_catalog_class_suffix, row.entry.name);
        if (title.endsWith(suffix)) title = title.substring(0, title.length() - suffix.length());
        binding.name.setText(title);
        binding.className.setText(row.entry.name);
        // 列表仅保留中文概览，详情页继续显示完整 API 名称与取值。
        binding.properties.setText(context.getString(row.entry.propertiesRes).replaceAll("（[^）]*）", ""));
        return binding.getRoot();
    }
}
