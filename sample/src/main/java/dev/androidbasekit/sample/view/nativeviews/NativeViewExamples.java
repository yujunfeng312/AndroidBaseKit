package dev.androidbasekit.sample.view.nativeviews;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.net.Uri;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import dev.androidbasekit.sample.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

/** 使用 framework 实例演示属性，不经 XML inflater 替换为 AppCompat/Material 控件。 */
@SuppressWarnings("deprecation")
public final class NativeViewExamples implements AutoCloseable {
    public interface Presenter { void show(String attributes, View view, int heightDp); }
    private final Activity context;
    private final Presenter presenter;
    private final Consumer<String> note;
    private final List<Chronometer> clocks = new ArrayList<>();
    private final List<ViewFlipper> flippers = new ArrayList<>();
    private final List<Chronometer> runningClocks = new ArrayList<>();
    private final List<ViewFlipper> runningFlippers = new ArrayList<>();
    private WebView web;
    private VideoView video;
    private MediaController mediaController;
    private final String[] items;

    public NativeViewExamples(Activity context, Presenter presenter, Consumer<String> note) {
        this.context = context; this.presenter = presenter; this.note = note;
        items = context.getResources().getStringArray(R.array.native_completion_items);
    }

    public void build(String name) {
        switch (name) {
            case "TextView":
                TextView title = text(s(R.string.native_text_view_text_1));
                title.setTextSize(24); title.setTypeface(null, Typeface.BOLD);
                title.setTextColor(color(dev.androidbasekit.ui.R.color.color_primary));
                show(s(R.string.native_text_view_text_2), title);
                TextView ellipsis = text(s(R.string.native_text_view_text_3));
                ellipsis.setMaxLines(1); ellipsis.setEllipsize(TextUtils.TruncateAt.END);
                show(s(R.string.native_text_view_text_4), ellipsis);
                TextView center = text(s(R.string.native_text_view_text_5));
                center.setGravity(Gravity.CENTER); center.setTextIsSelectable(true);
                show(s(R.string.native_text_view_text_6), center); break;
            case "EditText":
                EditText plain = edit(s(R.string.native_edit_text_text_1), InputType.TYPE_CLASS_TEXT);
                plain.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                show(s(R.string.native_edit_text_text_2), plain);
                show(s(R.string.native_edit_text_text_3), edit(s(R.string.native_edit_text_text_4), InputType.TYPE_CLASS_NUMBER));
                show(s(R.string.native_edit_text_text_5), edit(s(R.string.native_edit_text_text_6), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                EditText multi = edit(s(R.string.native_edit_text_text_7), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                multi.setSingleLine(false); multi.setMinLines(3); multi.setGravity(Gravity.TOP);
                show(s(R.string.native_edit_text_text_8), multi); break;
            case "AutoCompleteTextView":
                AutoCompleteTextView complete = new AutoCompleteTextView(context);
                complete.setHint(s(R.string.native_auto_complete_text_view_text_1)); complete.setThreshold(1);
                complete.setAdapter(adapter(items)); complete.setCompletionHint(s(R.string.native_auto_complete_text_view_text_2));
                show(s(R.string.native_auto_complete_text_view_text_3), complete); break;
            case "MultiAutoCompleteTextView":
                MultiAutoCompleteTextView tokens = new MultiAutoCompleteTextView(context);
                tokens.setHint(s(R.string.native_multi_auto_complete_text_view_text_1)); tokens.setThreshold(1);
                tokens.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer()); tokens.setAdapter(adapter(items));
                show(s(R.string.native_multi_auto_complete_text_view_text_2), tokens); break;
            case "SearchView":
                SearchView search = new SearchView(context);
                search.setIconifiedByDefault(false); search.setQueryHint(s(R.string.native_search_view_text_1)); search.setSubmitButtonEnabled(true);
                search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override public boolean onQueryTextSubmit(String query) { toast(s(R.string.native_search_result, query)); search.clearFocus(); return true; }
                    @Override public boolean onQueryTextChange(String query) { return false; }
                });
                show(s(R.string.native_search_view_text_2), search);
                SearchView collapsed = new SearchView(context); collapsed.setQueryHint(s(R.string.native_search_view_text_3));
                show(s(R.string.native_search_view_text_4), collapsed); break;
            case "CheckedTextView":
                for (boolean checked : new boolean[]{false, true}) {
                    CheckedTextView check = new CheckedTextView(context); check.setText(s(R.string.native_checked_text_view_text_1));
                    check.setCheckMarkDrawable(android.R.drawable.checkbox_on_background); check.setChecked(checked);
                    check.setOnClickListener(v -> check.toggle());
                    show(s(R.string.native_checked_attributes, s(checked ? R.string.native_state_on : R.string.native_state_off)), check);
                } break;
            case "Button":
                Button caps = button(s(R.string.native_button_text_1), () -> toast(s(R.string.native_button_text_2))); caps.setAllCaps(true);
                show(s(R.string.native_button_text_3), caps);
                Button icon = button(s(R.string.native_button_text_4), () -> toast(s(R.string.native_button_text_5)));
                icon.setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_input_add, 0, 0, 0);
                icon.setAllCaps(false); show(s(R.string.native_button_text_6), icon);
                Button disabled = button(s(R.string.native_button_text_7), () -> {}); disabled.setEnabled(false);
                show(s(R.string.native_button_text_8), disabled); break;
            case "ImageButton":
                ImageButton imageButton = new ImageButton(context);
                imageButton.setImageResource(android.R.drawable.ic_menu_camera);
                imageButton.setContentDescription(s(R.string.native_image_button_text_1)); imageButton.setScaleType(ImageView.ScaleType.CENTER);
                imageButton.setBackgroundTintList(ColorStateList.valueOf(color(dev.androidbasekit.ui.R.color.color_info_background)));
                imageButton.setOnClickListener(v -> toast(s(R.string.native_image_button_text_2)));
                presenter.show(s(R.string.native_image_button_text_3), imageButton, 80); break;
            case "CheckBox":
                CheckBox checkbox = new CheckBox(context); checkbox.setText(s(R.string.native_check_box_text_1)); checkbox.setChecked(true);
                show(s(R.string.native_check_box_text_2), checkbox);
                CheckBox tinted = new CheckBox(context); tinted.setText(s(R.string.native_check_box_text_3));
                tinted.setButtonTintList(ColorStateList.valueOf(color(dev.androidbasekit.ui.R.color.color_secondary)));
                show(s(R.string.native_check_box_text_4), tinted); break;
            case "RadioButton":
                RadioButton radio = new RadioButton(context); radio.setText(s(R.string.native_radio_button_text_1)); show(s(R.string.native_radio_button_text_2), radio);
                RadioButton selected = new RadioButton(context); selected.setText(s(R.string.native_radio_button_text_3)); selected.setChecked(true);
                selected.setButtonTintList(ColorStateList.valueOf(color(dev.androidbasekit.ui.R.color.color_secondary)));
                show(s(R.string.native_radio_button_text_4), selected); break;
            case "RadioGroup":
                for (int orientation : new int[]{LinearLayout.VERTICAL, LinearLayout.HORIZONTAL}) {
                    RadioGroup group = new RadioGroup(context); group.setOrientation(orientation);
                    for (String item : new String[]{s(R.string.native_radio_group_text_1), s(R.string.native_radio_group_text_2), s(R.string.native_radio_group_text_3)}) {
                        RadioButton child = new RadioButton(context); child.setId(0x00bb0000 + orientation * 10 + group.getChildCount()); child.setText(item); group.addView(child);
                    }
                    group.check(group.getChildAt(1).getId());
                    show(s(R.string.native_orientation_attributes, s(orientation == 1 ? R.string.native_vertical : R.string.native_horizontal)), group);
                } break;
            case "Switch":
                Switch toggle = new Switch(context); toggle.setText(s(R.string.native_switch_text_1)); toggle.setShowText(true);
                toggle.setTextOn(s(R.string.native_switch_text_2)); toggle.setTextOff(s(R.string.native_switch_text_3)); toggle.setChecked(true);
                show(s(R.string.native_switch_text_4), toggle);
                Switch compact = new Switch(context); compact.setText(s(R.string.native_switch_text_5)); compact.setShowText(false);
                show(s(R.string.native_switch_text_6), compact); break;
            case "ToggleButton":
                ToggleButton toggleButton = new ToggleButton(context);
                toggleButton.setTextOn(s(R.string.native_toggle_button_text_1)); toggleButton.setTextOff(s(R.string.native_toggle_button_text_2)); toggleButton.setChecked(false);
                show(s(R.string.native_toggle_button_text_3), toggleButton); break;
            case "Spinner":
                for (int mode : new int[]{Spinner.MODE_DROPDOWN, Spinner.MODE_DIALOG}) {
                    Spinner spinner = new Spinner(context, mode); spinner.setAdapter(adapter(items)); spinner.setSelection(2);
                    show(s(R.string.native_spinner_attributes, s(mode == 1 ? R.string.native_dropdown : R.string.native_dialog)), spinner);
                } break;
            case "NumberPicker":
                NumberPicker number = new NumberPicker(context); number.setMinValue(1); number.setMaxValue(10);
                number.setValue(5); number.setWrapSelectorWheel(true); show(s(R.string.native_number_picker_text_1), number);
                NumberPicker sizes = new NumberPicker(context); sizes.setMinValue(0); sizes.setMaxValue(2);
                sizes.setDisplayedValues(new String[]{s(R.string.native_number_picker_text_2), s(R.string.native_number_picker_text_3), s(R.string.native_number_picker_text_4)}); sizes.setWrapSelectorWheel(false);
                show(s(R.string.native_number_picker_text_5), sizes); break;
            case "ProgressBar":
                show(s(R.string.native_progress_bar_text_1), new ProgressBar(context));
                ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
                progress.setIndeterminate(false); progress.setMax(100); progress.setProgress(45); progress.setSecondaryProgress(75);
                show(s(R.string.native_progress_bar_text_2), progress);
                show(s(R.string.native_progress_bar_text_3), button(s(R.string.native_progress_bar_text_4), () -> progress.setProgress((progress.getProgress() + 10) % 101))); break;
            case "SeekBar":
                SeekBar seek = new SeekBar(context); seek.setMax(100); seek.setProgress(35); seek.setKeyProgressIncrement(5);
                TextView seekValue = text(s(R.string.native_progress_value, 35));
                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override public void onProgressChanged(SeekBar bar, int value, boolean user) { seekValue.setText(s(R.string.native_progress_value, value)); }
                    @Override public void onStartTrackingTouch(SeekBar bar) {}
                    @Override public void onStopTrackingTouch(SeekBar bar) {}
                });
                show(s(R.string.native_seek_bar_text_1), column(seek, seekValue)); break;
            case "RatingBar":
                RatingBar rating = new RatingBar(context); rating.setNumStars(5); rating.setStepSize(.5f); rating.setRating(3.5f);
                show(s(R.string.native_rating_bar_text_1), wrap(rating));
                RatingBar indicator = new RatingBar(context, null, android.R.attr.ratingBarStyleSmall);
                indicator.setNumStars(5); indicator.setRating(4); indicator.setIsIndicator(true);
                show(s(R.string.native_rating_bar_text_2), wrap(indicator)); break;
            default: buildVisuals(name);
        }
    }

    private void buildVisuals(String name) {
        switch (name) {
            case "ImageView":
                for (ImageView.ScaleType scale : new ImageView.ScaleType[]{ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_INSIDE}) {
                    ImageView image = new ImageView(context); image.setImageResource(R.drawable.demo_placeholder);
                    image.setContentDescription(s(R.string.native_image_description, imageScale(scale))); image.setScaleType(scale); image.setAdjustViewBounds(true);
                    image.setBackgroundColor(color(dev.androidbasekit.ui.R.color.color_info_background));
                    presenter.show(s(R.string.native_image_attributes, imageScale(scale)), image, 120);
                }
                ImageView tint = new ImageView(context); tint.setImageResource(android.R.drawable.ic_menu_gallery);
                tint.setImageTintList(ColorStateList.valueOf(color(dev.androidbasekit.ui.R.color.color_secondary))); tint.setContentDescription(s(R.string.native_image_view_text_1));
                presenter.show(s(R.string.native_image_view_text_2), tint, 64); break;
            case "ImageSwitcher":
                ImageSwitcher images = new ImageSwitcher(context); images.setFactory(() -> {
                    ImageView image = new ImageView(context); image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    image.setContentDescription(s(R.string.native_image_switcher_text_1)); return image;
                });
                images.setInAnimation(context, android.R.anim.fade_in); images.setOutAnimation(context, android.R.anim.fade_out);
                int[] index = {0}; int[] icons = {android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_camera};
                images.setImageResource(icons[0]); presenter.show(s(R.string.native_image_switcher_text_2), images, 120);
                show(s(R.string.native_image_switcher_text_3), button(s(R.string.native_image_switcher_text_4), () -> images.setImageResource(icons[++index[0] % 2]))); break;
            case "QuickContactBadge":
                QuickContactBadge badge = new QuickContactBadge(context);
                badge.setImageResource(android.R.drawable.ic_menu_myplaces);
                badge.setContentDescription(s(R.string.native_quick_contact_badge_text_1)); badge.setMode(android.provider.ContactsContract.QuickContact.MODE_LARGE);
                presenter.show(s(R.string.native_quick_contact_badge_text_2), badge, 96);
                note.accept(s(R.string.native_quick_contact_badge_text_3)); break;
            case "VideoView": case "MediaController":
                video = new VideoView(context);
                mediaController = new MediaController(context);
                mediaController.setMediaPlayer(video);
                FrameLayout videoFrame = new FrameLayout(context);
                videoFrame.addView(video, new FrameLayout.LayoutParams(-1, -1));
                mediaController.setAnchorView(videoFrame); video.setMediaController(mediaController);
                presenter.show(s(R.string.native_media_controller_text_1), videoFrame, 180);
                video.setOnPreparedListener(player -> { player.setLooping(true); video.seekTo(1); });
                video.setVideoURI(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.native_demo));
                show(s(R.string.native_media_controller_text_2), column(button(s(R.string.native_play), () -> { video.start(); mediaController.show(0); }),
                        button(s(R.string.native_pause), () -> video.pause()), button(s(R.string.native_restart_video), () -> video.seekTo(0))));
                note.accept(s(R.string.native_media_controller_text_6)); break;
            case "SurfaceView":
                SurfaceView surface = new SurfaceView(context);
                presenter.show(s(R.string.native_surface_view_text_1), surface, 160);
                note.accept(s(R.string.native_surface_view_text_2)); break;
            case "TextureView":
                TextureView texture = new TextureView(context); texture.setOpaque(false); texture.setAlpha(.7f);
                presenter.show(s(R.string.native_texture_view_text_1), texture, 160);
                note.accept(s(R.string.native_texture_view_text_2)); break;
            case "DatePicker":
                DatePicker calendar = new DatePicker(context, null, 0, android.R.style.Widget_Material_DatePicker);
                limitDates(calendar); show(s(R.string.native_date_picker_text_1), calendar);
                DatePicker spinner = new DatePicker(context, null, 0, R.style.NativeGallery_DatePickerSpinner);
                spinner.setCalendarViewShown(false); limitDates(spinner);
                show(s(R.string.native_date_picker_text_2), spinner); break;
            case "TimePicker":
                TimePicker clock = new TimePicker(context, null, 0, android.R.style.Widget_Material_TimePicker);
                clock.setIs24HourView(true); clock.setHour(14); clock.setMinute(30);
                show(s(R.string.native_time_picker_text_1), clock);
                TimePicker time = new TimePicker(context, null, 0, R.style.NativeGallery_TimePickerSpinner);
                time.setIs24HourView(false); time.setHour(9); time.setMinute(15);
                show(s(R.string.native_time_picker_text_2), time); break;
            case "CalendarView":
                CalendarView dates = new CalendarView(context); dates.setFirstDayOfWeek(Calendar.MONDAY);
                dates.setMinDate(date(2020, 0, 1)); dates.setMaxDate(date(2030, 11, 31)); dates.setDate(date(2026, 9, 23));
                show(s(R.string.native_calendar_view_text_1), dates); break;
            case "Chronometer":
                Chronometer elapsed = new Chronometer(context); elapsed.setBase(SystemClock.elapsedRealtime());
                elapsed.setFormat(s(R.string.native_elapsed_format)); clocks.add(elapsed);
                show(s(R.string.native_chronometer_text_2), elapsed);
                show(s(R.string.native_chronometer_text_3), column(button(s(R.string.native_chronometer_text_4), () -> {
                    if (!runningClocks.contains(elapsed)) runningClocks.add(elapsed);
                    elapsed.start();
                }), button(s(R.string.native_chronometer_text_5), () -> { runningClocks.remove(elapsed); elapsed.stop(); }),
                        button(s(R.string.native_chronometer_text_6), () -> elapsed.setBase(SystemClock.elapsedRealtime())))); break;
            case "TextClock":
                TextClock local = new TextClock(context); local.setFormat12Hour(s(R.string.native_text_clock_text_1)); local.setFormat24Hour(s(R.string.native_text_clock_text_2));
                show(s(R.string.native_text_clock_text_3), local);
                TextClock utc = new TextClock(context); utc.setTimeZone("UTC"); utc.setFormat12Hour(s(R.string.native_text_clock_text_4)); utc.setFormat24Hour(s(R.string.native_text_clock_text_4));
                show(s(R.string.native_text_clock_text_5), utc); break;
            default: buildContainers(name);
        }
    }

    private void buildContainers(String name) {
        switch (name) {
            case "View":
                View basic = new View(context); basic.setBackgroundColor(color(dev.androidbasekit.ui.R.color.color_primary));
                presenter.show(s(R.string.native_view_text_1), basic, 64);
                View rotated = new View(context); rotated.setBackgroundColor(color(dev.androidbasekit.ui.R.color.color_secondary)); rotated.setRotation(4);
                presenter.show(s(R.string.native_view_text_2), rotated, 64); break;
            case "LinearLayout":
                show(s(R.string.native_linear_layout_text_1), column(tile(s(R.string.native_linear_layout_text_2)), tile(s(R.string.native_linear_layout_text_3))));
                LinearLayout row = new LinearLayout(context); row.setOrientation(LinearLayout.HORIZONTAL);
                row.addView(tile(s(R.string.native_linear_layout_text_4)), new LinearLayout.LayoutParams(0, dp(72), 1));
                row.addView(tile(s(R.string.native_linear_layout_text_5)), new LinearLayout.LayoutParams(0, dp(72), 2));
                show(s(R.string.native_linear_layout_text_6), row); break;
            case "FrameLayout":
                FrameLayout frame = new FrameLayout(context); frame.setBackgroundColor(color(dev.androidbasekit.ui.R.color.color_info_background));
                frame.addView(tile(s(R.string.native_frame_layout_text_1)), new FrameLayout.LayoutParams(dp(130), dp(60), Gravity.CENTER));
                frame.addView(tile(s(R.string.native_frame_layout_text_2)), new FrameLayout.LayoutParams(dp(140), dp(50), Gravity.END | Gravity.BOTTOM));
                frame.setForeground(context.getDrawable(android.R.drawable.picture_frame));
                presenter.show(s(R.string.native_frame_layout_text_3), frame, 160); break;
            case "RelativeLayout":
                RelativeLayout relative = new RelativeLayout(context);
                RelativeLayout.LayoutParams center = new RelativeLayout.LayoutParams(dp(140), dp(60)); center.addRule(RelativeLayout.CENTER_IN_PARENT);
                relative.addView(tile(s(R.string.native_relative_layout_text_1)), center);
                RelativeLayout.LayoutParams end = new RelativeLayout.LayoutParams(dp(150), dp(48)); end.addRule(RelativeLayout.ALIGN_PARENT_END);
                relative.addView(tile(s(R.string.native_relative_layout_text_2)), end);
                presenter.show(s(R.string.native_relative_layout_text_3), relative, 160); break;
            case "TableLayout": case "TableRow":
                TableLayout table = new TableLayout(context); table.setStretchAllColumns(true); table.setShrinkAllColumns(true);
                for (String[] values : new String[][]{{s(R.string.native_table_row_text_1), s(R.string.native_table_row_text_2), s(R.string.native_table_row_text_3)}, {s(R.string.native_table_row_text_4), s(R.string.native_table_row_text_5), s(R.string.native_table_row_text_6)}, {s(R.string.native_table_row_text_7), s(R.string.native_table_row_text_8), s(R.string.native_table_row_text_9)}}) {
                    TableRow tableRow = new TableRow(context);
                    for (String value : values) tableRow.addView(tile(value));
                    table.addView(tableRow);
                }
                TableRow spanRow = new TableRow(context); TableRow.LayoutParams span = new TableRow.LayoutParams(); span.span = 3;
                spanRow.addView(tile(s(R.string.native_table_row_text_10)), span); table.addView(spanRow);
                show(s(R.string.native_table_row_text_11), table); break;
            case "GridLayout":
                GridLayout grid = new GridLayout(context); grid.setColumnCount(2); grid.setRowCount(3); grid.setUseDefaultMargins(true);
                grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
                for (int i = 0; i < 6; i++) {
                    GridLayout.LayoutParams cell = new GridLayout.LayoutParams(GridLayout.spec(i / 2), GridLayout.spec(i % 2, 1f));
                    cell.width = 0; cell.height = dp(56); grid.addView(tile(s(R.string.native_grid_cell, i + 1)), cell);
                }
                show(s(R.string.native_grid_layout_text_1), grid); break;
            case "Space":
                LinearLayout spaced = column(tile(s(R.string.native_space_text_1))); spaced.setBackgroundColor(color(dev.androidbasekit.ui.R.color.color_warning_background));
                spaced.addView(new Space(context), new LinearLayout.LayoutParams(1, dp(48))); spaced.addView(tile(s(R.string.native_space_text_2)));
                show(s(R.string.native_space_text_3), spaced); break;
            case "ViewStub":
                LinearLayout lazy = column(); ViewStub stub = new ViewStub(context); stub.setLayoutResource(R.layout.native_stub_content); lazy.addView(stub);
                Button inflate = button(s(R.string.native_view_stub_text_1), () -> { stub.inflate(); });
                inflate.setOnClickListener(v -> {
                    if (stub.getParent() != null) stub.inflate();
                    inflate.setEnabled(false); inflate.setText(s(R.string.native_view_stub_text_2));
                });
                lazy.addView(inflate); show(s(R.string.native_view_stub_text_3), lazy); break;
            case "ScrollView":
                ScrollView vertical = new ScrollView(context); vertical.setFillViewport(true); vertical.setScrollbarFadingEnabled(false);
                LinearLayout longContent = column(); for (int i = 1; i <= 12; i++) longContent.addView(tile(s(R.string.native_scroll_item, i)));
                vertical.addView(longContent); nested(vertical);
                presenter.show(s(R.string.native_scroll_view_text_1), vertical, 220); break;
            case "HorizontalScrollView":
                HorizontalScrollView horizontal = new HorizontalScrollView(context); horizontal.setFillViewport(true); horizontal.setHorizontalScrollBarEnabled(true);
                LinearLayout wide = column(); wide.setOrientation(LinearLayout.HORIZONTAL);
                for (int i = 1; i <= 8; i++) wide.addView(tile(s(R.string.native_card_number, i)), new LinearLayout.LayoutParams(dp(120), dp(100)));
                horizontal.addView(wide); presenter.show(s(R.string.native_horizontal_scroll_view_text_1), horizontal, 120); break;
            case "ListView":
                ListView list = new ListView(context); list.setChoiceMode(ListView.CHOICE_MODE_SINGLE); list.setDividerHeight(dp(2));
                list.addHeaderView(text(s(R.string.native_list_view_text_1)), null, false);
                list.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice, items));
                list.setItemChecked(2, true); nested(list);
                presenter.show(s(R.string.native_list_view_text_2), list, 260); break;
            case "GridView":
                for (int columns : new int[]{2, 3}) {
                    GridView gridView = new GridView(context); gridView.setNumColumns(columns);
                    gridView.setHorizontalSpacing(dp(8)); gridView.setVerticalSpacing(dp(8)); gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                    gridView.setAdapter(adapter(items)); gridView.setOnItemClickListener((p, v, position, id) -> toast(items[position])); nested(gridView);
                    presenter.show(s(R.string.native_grid_attributes, columns), gridView, 180);
                } break;
            case "ExpandableListView":
                ExpandableListView expandable = new ExpandableListView(context);
                expandable.setAdapter(new BaseExpandableListAdapter() {
                    final String[] groups = {s(R.string.native_expandable_list_view_text_1), s(R.string.native_expandable_list_view_text_2)};
                    final String[][] children = {{s(R.string.native_table_row_text_4), s(R.string.native_expandable_list_view_text_3)}, {s(R.string.native_table_row_text_7), s(R.string.native_expandable_list_view_text_4), s(R.string.native_expandable_list_view_text_5)}};
                    @Override public int getGroupCount() { return groups.length; }
                    @Override public int getChildrenCount(int g) { return children[g].length; }
                    @Override public Object getGroup(int g) { return groups[g]; }
                    @Override public Object getChild(int g, int c) { return children[g][c]; }
                    @Override public long getGroupId(int g) { return g; }
                    @Override public long getChildId(int g, int c) { return g * 10L + c; }
                    @Override public boolean hasStableIds() { return true; }
                    @Override public View getGroupView(int g, boolean open, View old, ViewGroup parent) {
                        TextView label = tile(s(R.string.native_group_label, s(open ? R.string.native_collapse_symbol : R.string.native_expand_symbol), groups[g])); label.setPadding(dp(48), dp(12), dp(8), dp(12)); return label;
                    }
                    @Override public View getChildView(int g, int c, boolean last, View old, ViewGroup parent) {
                        TextView label = text(s(R.string.native_child_label, children[g][c])); label.setPadding(dp(8), dp(12), dp(8), dp(12)); return label;
                    }
                    @Override public boolean isChildSelectable(int g, int c) { return true; }
                });
                expandable.expandGroup(0); nested(expandable);
                presenter.show(s(R.string.native_expandable_list_view_text_6), expandable, 280); break;
            default: buildSwitchers(name);
        }
    }

    private void buildSwitchers(String name) {
        switch (name) {
            case "ViewAnimator": case "ViewFlipper": case "ViewSwitcher":
                ViewAnimator animator;
                if (name.equals("ViewFlipper")) {
                    ViewFlipper flipper = new ViewFlipper(context); flipper.setFlipInterval(1800); flippers.add(flipper); animator = flipper;
                } else animator = name.equals("ViewSwitcher") ? new ViewSwitcher(context) : new ViewAnimator(context);
                animator.addView(tile(s(R.string.native_view_switcher_text_1))); animator.addView(tile(s(R.string.native_view_switcher_text_2)));
                animator.setInAnimation(context, android.R.anim.slide_in_left); animator.setOutAnimation(context, android.R.anim.slide_out_right);
                animator.setAnimateFirstView(false);
                presenter.show(s(R.string.native_view_switcher_text_3), animator, 100);
                show(s(R.string.native_view_switcher_text_4), column(button(s(R.string.native_view_switcher_text_5), animator::showNext), button(s(R.string.native_view_switcher_text_6), animator::showPrevious)));
                if (animator instanceof ViewFlipper) {
                    ViewFlipper flipper = (ViewFlipper) animator;
                    show(s(R.string.native_view_switcher_text_7), column(button(s(R.string.native_view_switcher_text_8), flipper::startFlipping), button(s(R.string.native_view_switcher_text_9), flipper::stopFlipping)));
                } break;
            case "TextSwitcher":
                TextSwitcher texts = new TextSwitcher(context); texts.setFactory(() -> { TextView t = text(""); t.setTextSize(24); return t; });
                texts.setInAnimation(context, android.R.anim.fade_in); texts.setOutAnimation(context, android.R.anim.fade_out); texts.setCurrentText(s(R.string.native_text_switcher_text_1));
                presenter.show(s(R.string.native_text_switcher_text_2), texts, 80);
                int[] next = {1}; show(s(R.string.native_text_switcher_text_3), button(s(R.string.native_text_switcher_text_4), () -> texts.setText(s(R.string.native_message_number, ++next[0])))); break;
            case "Toolbar":
                Toolbar toolbar = new Toolbar(context); toolbar.setTitle(s(R.string.native_toolbar_text_1)); toolbar.setSubtitle(s(R.string.native_toolbar_text_2));
                toolbar.setNavigationIcon(android.R.drawable.ic_media_previous); toolbar.setNavigationContentDescription(s(R.string.native_toolbar_text_3));
                toolbar.setNavigationOnClickListener(v -> context.finish());
                toolbar.getMenu().add(s(R.string.native_toolbar_text_4)).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                toolbar.getMenu().add(s(R.string.native_toolbar_text_5)); toolbar.setOnMenuItemClickListener(item -> { toast(item.getTitle().toString()); return true; });
                show(s(R.string.native_toolbar_text_6), toolbar); break;
            case "ActionMenuView":
                ActionMenuView menu = new ActionMenuView(context);
                menu.getMenu().add(s(R.string.native_action_menu_view_text_1)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.getMenu().add(s(R.string.native_action_menu_view_text_2)); menu.setOnMenuItemClickListener(item -> { toast(item.getTitle().toString()); return true; });
                show(s(R.string.native_action_menu_view_text_3), menu); break;
            case "StackView": case "AdapterViewFlipper":
                AdapterViewAnimator cards = name.equals("StackView") ? new StackView(context) : new AdapterViewFlipper(context);
                cards.setAdapter(new BaseAdapter() {
                    @Override public int getCount() { return items.length; }
                    @Override public Object getItem(int position) { return items[position]; }
                    @Override public long getItemId(int position) { return position; }
                    @Override public View getView(int position, View old, ViewGroup parent) { return tile(s(R.string.native_card_title, position + 1, items[position])); }
                });
                cards.setDisplayedChild(1);
                presenter.show(s(R.string.native_adapter_view_flipper_text_1), cards, 180);
                show(s(R.string.native_view_switcher_text_4), column(button(s(R.string.native_adapter_view_flipper_text_2), cards::showNext), button(s(R.string.native_adapter_view_flipper_text_3), cards::showPrevious)));
                if (cards instanceof AdapterViewFlipper) {
                    AdapterViewFlipper adapterFlipper = (AdapterViewFlipper) cards;
                    adapterFlipper.setFlipInterval(2000); adapterFlipper.setAutoStart(false);
                    // 手动切换避免后台自动推进；ViewFlipper 页面演示自动轮播及暂停。
                    note.accept(s(R.string.native_adapter_view_flipper_text_4));
                } break;
            case "WebView":
                web = new WebView(context); web.getSettings().setJavaScriptEnabled(false); web.getSettings().setAllowFileAccess(false);
                web.getSettings().setAllowContentAccess(false); web.getSettings().setBlockNetworkLoads(true); web.getSettings().setTextZoom(110);
                web.loadDataWithBaseURL(null, s(R.string.native_web_view_text_1), "text/html", "UTF-8", null);
                presenter.show(s(R.string.native_web_view_text_2), web, 260);
                show(s(R.string.native_web_view_text_3), column(button(s(R.string.native_web_view_text_4), () -> web.getSettings().setTextZoom(100)), button(s(R.string.native_web_view_text_5), () -> web.getSettings().setTextZoom(150)))); break;
            default: buildLegacy(name);
        }
    }

    private void buildLegacy(String name) {
        note.accept(s(R.string.native_legacy_notice));
        switch (name) {
            case "TabHost": case "TabWidget":
                TabHost host = new TabHost(context); LinearLayout structure = column();
                TabWidget tabs = new TabWidget(context); tabs.setId(android.R.id.tabs); tabs.setStripEnabled(true);
                FrameLayout content = new FrameLayout(context); content.setId(android.R.id.tabcontent);
                structure.addView(tabs); structure.addView(content, new LinearLayout.LayoutParams(-1, dp(100)));
                host.addView(structure); host.setup();
                for (int i = 1; i <= 3; i++) {
                    final String label = s(R.string.native_tab_number, i);
                    host.addTab(host.newTabSpec(label).setIndicator(tile(label)).setContent(tag -> tile(s(R.string.native_tab_body, label))));
                }
                host.setCurrentTab(1); show(s(R.string.native_tab_widget_text_1), host); break;
            case "Gallery":
                Gallery gallery = new Gallery(context); gallery.setSpacing(dp(16));
                gallery.setAdapter(new BaseAdapter() {
                    @Override public int getCount() { return items.length; }
                    @Override public Object getItem(int position) { return items[position]; }
                    @Override public long getItemId(int position) { return position; }
                    @Override public View getView(int position, View old, ViewGroup parent) {
                        TextView label = tile(items[position]); label.setLayoutParams(new Gallery.LayoutParams(dp(140), dp(90))); return label;
                    }
                });
                gallery.setSelection(2); presenter.show(s(R.string.native_gallery_text_1), gallery, 140); break;
            case "SlidingDrawer":
                // 该旧控件只有 AttributeSet 构造方式，XML 提供它必需的 handle/content 引用。
                SlidingDrawer drawer = (SlidingDrawer) context.getLayoutInflater().inflate(R.layout.native_sliding_drawer, null);
                presenter.show(s(R.string.native_sliding_drawer_text_1), drawer, 260); break;
            case "AbsoluteLayout":
                AbsoluteLayout absolute = new AbsoluteLayout(context);
                absolute.addView(tile(s(R.string.native_absolute_layout_text_1)), new AbsoluteLayout.LayoutParams(dp(150), dp(55), dp(16), dp(8)));
                absolute.addView(tile(s(R.string.native_absolute_layout_text_2)), new AbsoluteLayout.LayoutParams(dp(160), dp(55), dp(80), dp(80)));
                presenter.show(s(R.string.native_absolute_layout_text_3), absolute, 160); break;
            case "AnalogClock": presenter.show(s(R.string.native_analog_clock_text_1), new AnalogClock(context), 180); break;
            case "DigitalClock":
                DigitalClock digital = new DigitalClock(context); digital.setTextSize(28); digital.setTextColor(color(dev.androidbasekit.ui.R.color.color_secondary));
                show(s(R.string.native_digital_clock_text_1), digital); break;
            case "ZoomButton":
                ZoomButton zoom = new ZoomButton(context); zoom.setImageResource(android.R.drawable.ic_menu_zoom);
                zoom.setContentDescription(s(R.string.native_zoom_button_text_1)); zoom.setZoomSpeed(300);
                int[] zoomCount = {0}; TextView count = text(s(R.string.native_click_count, 0));
                zoom.setOnClickListener(v -> count.setText(s(R.string.native_click_count, ++zoomCount[0])));
                show(s(R.string.native_zoom_button_text_2), column(zoom, count)); break;
            case "ZoomControls":
                ZoomControls controls = new ZoomControls(context); controls.setZoomSpeed(300);
                TextView scale = text(s(R.string.native_zoom_percent, 100)); int[] percent = {100};
                controls.setOnZoomInClickListener(v -> { percent[0] += 10; scale.setText(s(R.string.native_zoom_percent, percent[0])); });
                controls.setOnZoomOutClickListener(v -> { percent[0] = Math.max(10, percent[0] - 10); scale.setText(s(R.string.native_zoom_percent, percent[0])); });
                show(s(R.string.native_zoom_controls_text_1), column(controls, scale)); break;
            case "TwoLineListItem":
                TwoLineListItem twoLines = (TwoLineListItem) context.getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                twoLines.getText1().setText(s(R.string.native_two_line_list_item_text_1)); twoLines.getText2().setText(s(R.string.native_two_line_list_item_text_2));
                show(s(R.string.native_two_line_list_item_text_3), twoLines); break;
            case "DialerFilter":
                DialerFilter dialer = (DialerFilter) context.getLayoutInflater().inflate(R.layout.native_dialer_filter, null);
                dialer.setMode(DialerFilter.DIGITS_AND_LETTERS);
                show(s(R.string.native_dialer_filter_text_1), dialer); break;
            default: throw new IllegalArgumentException("Missing native View example: " + name);
        }
    }

    private String s(@androidx.annotation.StringRes int resource, Object... arguments) {
        return arguments.length == 0 ? context.getString(resource) : context.getString(resource, arguments);
    }
    private String imageScale(ImageView.ScaleType scale) {
        switch (scale) {
            case CENTER_CROP: return s(R.string.native_scale_crop);
            case CENTER_INSIDE: return s(R.string.native_scale_inside);
            default: return s(R.string.native_scale_fit);
        }
    }

    private void show(String attributes, View view) { presenter.show(attributes, view, 0); }
    private TextView text(String value) { TextView text = new TextView(context); text.setText(value); text.setTextSize(16); return text; }
    private TextView tile(String value) {
        TextView tile = text(value); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(8), dp(14), dp(8), dp(14));
        tile.setBackgroundColor(color(dev.androidbasekit.ui.R.color.color_info_background)); tile.setTextColor(color(dev.androidbasekit.ui.R.color.color_info)); return tile;
    }
    private EditText edit(String hint, int inputType) {
        EditText edit = new EditText(context); edit.setHint(hint); edit.setInputType(inputType); edit.setSingleLine(true); return edit;
    }
    private Button button(String title, Runnable action) {
        Button button = new Button(context); button.setText(title); button.setAllCaps(false); button.setOnClickListener(v -> action.run()); return button;
    }
    private LinearLayout column(View... children) {
        LinearLayout column = new LinearLayout(context); column.setOrientation(LinearLayout.VERTICAL);
        for (View child : children) column.addView(child, new LinearLayout.LayoutParams(-1, -2)); return column;
    }
    private LinearLayout wrap(View child) {
        LinearLayout wrap = new LinearLayout(context); wrap.addView(child, new LinearLayout.LayoutParams(-2, -2)); return wrap;
    }
    private ArrayAdapter<String> adapter(String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); return adapter;
    }
    private void nested(View child) {
        child.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) view.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) view.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });
    }
    private void limitDates(DatePicker picker) {
        picker.setMinDate(date(2020, 0, 1)); picker.setMaxDate(date(2030, 11, 31)); picker.updateDate(2026, 9, 23);
    }
    private long date(int year, int month, int day) {
        Calendar date = Calendar.getInstance(); date.clear(); date.set(year, month, day); return date.getTimeInMillis();
    }
    private int color(int resource) { return context.getColor(resource); }
    private int dp(int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    private void toast(String value) { Toast.makeText(context, value, Toast.LENGTH_SHORT).show(); }

    public void pause() {
        for (Chronometer clock : clocks) clock.stop();
        runningFlippers.clear();
        for (ViewFlipper flipper : flippers) { if (flipper.isFlipping()) runningFlippers.add(flipper); flipper.stopFlipping(); }
        if (video != null) video.pause();
        if (mediaController != null) mediaController.hide();
        if (web != null) web.onPause();
    }
    public void resume() {
        for (Chronometer clock : runningClocks) clock.start();
        for (ViewFlipper flipper : runningFlippers) flipper.startFlipping();
        runningFlippers.clear();
        if (web != null) web.onResume();
    }
    @Override public void close() {
        pause(); runningClocks.clear(); runningFlippers.clear();
        if (video != null) video.stopPlayback();
        if (web != null) {
            if (web.getParent() instanceof ViewGroup) ((ViewGroup) web.getParent()).removeView(web);
            web.destroy(); web = null;
        }
    }
}
