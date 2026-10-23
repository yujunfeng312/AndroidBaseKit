package dev.androidbasekit.sample.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.ItemWelcomePageBinding;

/** 本地欢迎介绍素材；替换四张同名 PNG 即可更换插图。 */
public final class WelcomePagerAdapter extends RecyclerView.Adapter<WelcomePagerAdapter.PageHolder> {
    public static final int PAGE_COUNT = 4;
    private static final int[] IMAGES = { R.drawable.welcome_intro_1, R.drawable.welcome_intro_2,
            R.drawable.welcome_intro_3, R.drawable.welcome_intro_4 };
    private static final int[] TITLES = { R.string.template_intro_title_1, R.string.template_intro_title_2,
            R.string.template_intro_title_3, R.string.template_intro_title_4 };
    private static final int[] BODIES = { R.string.template_intro_body_1, R.string.template_intro_body_2,
            R.string.template_intro_body_3, R.string.template_intro_body_4 };

    @NonNull @Override public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new PageHolder(ItemWelcomePageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        holder.binding.illustration.setImageResource(IMAGES[position]);
        holder.binding.title.setText(TITLES[position]);
        holder.binding.body.setText(BODIES[position]);
    }
    @Override public int getItemCount() { return PAGE_COUNT; }

    static final class PageHolder extends RecyclerView.ViewHolder {
        final ItemWelcomePageBinding binding;
        PageHolder(ItemWelcomePageBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
