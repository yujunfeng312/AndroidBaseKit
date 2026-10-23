package dev.androidbasekit.sample.view.adapter;

import dev.androidbasekit.sample.R;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import dev.androidbasekit.storage.DemoRecord;
import dev.androidbasekit.image.ImageLoader;
import dev.androidbasekit.sample.databinding.ItemDemoBinding;

/**
 * Java ListAdapter 示例。通过 DiffUtil 更新 DemoRecord 列表，绑定图片及点击，回收时清理图片请求。
 */
public final class DemoAdapter extends ListAdapter<DemoRecord, DemoAdapter.Holder> {
    /**
     * 示例列表点击接口。将被点击的数据交回页面，不在 Adapter 中直接导航。
     */
    public interface ItemClick { void onClick(DemoRecord item); }
    private final ItemClick click;
    public DemoAdapter(ItemClick click) {
        super(new DiffUtil.ItemCallback<DemoRecord>() {
            @Override public boolean areItemsTheSame(@NonNull DemoRecord a, @NonNull DemoRecord b) { return a.getId() == b.getId() && a.getUserId().equals(b.getUserId()); }
            @Override public boolean areContentsTheSame(@NonNull DemoRecord a, @NonNull DemoRecord b) { return a.equals(b); }
        });
        this.click = click;
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemDemoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        DemoRecord item = getItem(position);
        holder.binding.title.setText(item.getTitle());
        holder.binding.subtitle.setText(holder.itemView.getContext().getString(R.string.demo_item_subtitle, item.getId()));
        Object image = item.getImageUrl().isEmpty() ? R.drawable.demo_placeholder : item.getImageUrl();
        ImageLoader.loadRounded(holder.binding.image, image,
                holder.itemView.getResources().getDimensionPixelSize(R.dimen.hub_image_radius));
        holder.binding.getRoot().setOnClickListener(view -> click.onClick(item));
    }
    @Override public void onViewRecycled(@NonNull Holder holder) { ImageLoader.clear(holder.binding.image); holder.binding.getRoot().setOnClickListener(null); super.onViewRecycled(holder); }
    /**
     * 列表行 ViewHolder。保存本行视图或 Binding，配合所属 Adapter 完成复用与绑定。
     */
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemDemoBinding binding;
        Holder(ItemDemoBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
