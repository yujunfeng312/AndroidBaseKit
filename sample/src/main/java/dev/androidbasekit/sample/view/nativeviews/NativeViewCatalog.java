package dev.androidbasekit.sample.view.nativeviews;

import dev.androidbasekit.sample.R;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/** Framework View 目录；类名同时作为稳定的页面路由，不依赖列表位置。 */
public final class NativeViewCatalog {
    private NativeViewCatalog() {}

    public static final class Entry {
        @androidx.annotation.StringRes public final int categoryRes;
        public final String name;
        @androidx.annotation.StringRes public final int titleRes;
        @androidx.annotation.StringRes public final int propertiesRes;

        private Entry(int categoryRes, String name, int titleRes, int propertiesRes) {
            this.categoryRes = categoryRes; this.name = name;
            this.titleRes = titleRes; this.propertiesRes = propertiesRes;
        }

        public String display(android.content.Context context) {
            return context.getString(R.string.native_catalog_row, context.getString(categoryRes),
                    context.getString(titleRes), context.getString(propertiesRes));
        }
    }

    private static Entry entry(int category, String name, int title, int properties) {
        return new Entry(category, name, title, properties);
    }

    public static final List<Entry> ALL = Collections.unmodifiableList(Arrays.asList(
            entry(R.string.native_category_1, "TextView", R.string.native_title_text_view, R.string.native_properties_text_view),
            entry(R.string.native_category_1, "EditText", R.string.native_title_edit_text, R.string.native_properties_edit_text),
            entry(R.string.native_category_1, "AutoCompleteTextView", R.string.native_title_auto_complete_text_view, R.string.native_properties_auto_complete_text_view),
            entry(R.string.native_category_1, "MultiAutoCompleteTextView", R.string.native_title_multi_auto_complete_text_view, R.string.native_properties_multi_auto_complete_text_view),
            entry(R.string.native_category_1, "SearchView", R.string.native_title_search_view, R.string.native_properties_search_view),
            entry(R.string.native_category_1, "CheckedTextView", R.string.native_title_checked_text_view, R.string.native_properties_checked_text_view),
            entry(R.string.native_category_2, "Button", R.string.native_title_button, R.string.native_properties_button),
            entry(R.string.native_category_2, "ImageButton", R.string.native_title_image_button, R.string.native_properties_image_button),
            entry(R.string.native_category_2, "CheckBox", R.string.native_title_check_box, R.string.native_properties_check_box),
            entry(R.string.native_category_2, "RadioButton", R.string.native_title_radio_button, R.string.native_properties_radio_button),
            entry(R.string.native_category_2, "RadioGroup", R.string.native_title_radio_group, R.string.native_properties_radio_group),
            entry(R.string.native_category_2, "Switch", R.string.native_title_switch, R.string.native_properties_switch),
            entry(R.string.native_category_2, "ToggleButton", R.string.native_title_toggle_button, R.string.native_properties_toggle_button),
            entry(R.string.native_category_2, "Spinner", R.string.native_title_spinner, R.string.native_properties_spinner),
            entry(R.string.native_category_2, "NumberPicker", R.string.native_title_number_picker, R.string.native_properties_number_picker),
            entry(R.string.native_category_3, "ProgressBar", R.string.native_title_progress_bar, R.string.native_properties_progress_bar),
            entry(R.string.native_category_3, "SeekBar", R.string.native_title_seek_bar, R.string.native_properties_seek_bar),
            entry(R.string.native_category_3, "RatingBar", R.string.native_title_rating_bar, R.string.native_properties_rating_bar),
            entry(R.string.native_category_4, "ImageView", R.string.native_title_image_view, R.string.native_properties_image_view),
            entry(R.string.native_category_4, "ImageSwitcher", R.string.native_title_image_switcher, R.string.native_properties_image_switcher),
            entry(R.string.native_category_4, "QuickContactBadge", R.string.native_title_quick_contact_badge, R.string.native_properties_quick_contact_badge),
            entry(R.string.native_category_4, "VideoView", R.string.native_title_video_view, R.string.native_properties_video_view),
            entry(R.string.native_category_4, "MediaController", R.string.native_title_media_controller, R.string.native_properties_media_controller),
            entry(R.string.native_category_4, "SurfaceView", R.string.native_title_surface_view, R.string.native_properties_surface_view),
            entry(R.string.native_category_4, "TextureView", R.string.native_title_texture_view, R.string.native_properties_texture_view),
            entry(R.string.native_category_5, "DatePicker", R.string.native_title_date_picker, R.string.native_properties_date_picker),
            entry(R.string.native_category_5, "TimePicker", R.string.native_title_time_picker, R.string.native_properties_time_picker),
            entry(R.string.native_category_5, "CalendarView", R.string.native_title_calendar_view, R.string.native_properties_calendar_view),
            entry(R.string.native_category_5, "Chronometer", R.string.native_title_chronometer, R.string.native_properties_chronometer),
            entry(R.string.native_category_5, "TextClock", R.string.native_title_text_clock, R.string.native_properties_text_clock),
            entry(R.string.native_category_6, "View", R.string.native_title_view, R.string.native_properties_view),
            entry(R.string.native_category_6, "LinearLayout", R.string.native_title_linear_layout, R.string.native_properties_linear_layout),
            entry(R.string.native_category_6, "FrameLayout", R.string.native_title_frame_layout, R.string.native_properties_frame_layout),
            entry(R.string.native_category_6, "RelativeLayout", R.string.native_title_relative_layout, R.string.native_properties_relative_layout),
            entry(R.string.native_category_6, "TableLayout", R.string.native_title_table_layout, R.string.native_properties_table_layout),
            entry(R.string.native_category_6, "TableRow", R.string.native_title_table_row, R.string.native_properties_table_row),
            entry(R.string.native_category_6, "GridLayout", R.string.native_title_grid_layout, R.string.native_properties_grid_layout),
            entry(R.string.native_category_6, "Space", R.string.native_title_space, R.string.native_properties_space),
            entry(R.string.native_category_6, "ViewStub", R.string.native_title_view_stub, R.string.native_properties_view_stub),
            entry(R.string.native_category_7, "ScrollView", R.string.native_title_scroll_view, R.string.native_properties_scroll_view),
            entry(R.string.native_category_7, "HorizontalScrollView", R.string.native_title_horizontal_scroll_view, R.string.native_properties_horizontal_scroll_view),
            entry(R.string.native_category_7, "ListView", R.string.native_title_list_view, R.string.native_properties_list_view),
            entry(R.string.native_category_7, "GridView", R.string.native_title_grid_view, R.string.native_properties_grid_view),
            entry(R.string.native_category_7, "ExpandableListView", R.string.native_title_expandable_list_view, R.string.native_properties_expandable_list_view),
            entry(R.string.native_category_7, "StackView", R.string.native_title_stack_view, R.string.native_properties_stack_view),
            entry(R.string.native_category_8, "AdapterViewFlipper", R.string.native_title_adapter_view_flipper, R.string.native_properties_adapter_view_flipper),
            entry(R.string.native_category_8, "ViewAnimator", R.string.native_title_view_animator, R.string.native_properties_view_animator),
            entry(R.string.native_category_8, "ViewFlipper", R.string.native_title_view_flipper, R.string.native_properties_view_flipper),
            entry(R.string.native_category_8, "ViewSwitcher", R.string.native_title_view_switcher, R.string.native_properties_view_switcher),
            entry(R.string.native_category_8, "TextSwitcher", R.string.native_title_text_switcher, R.string.native_properties_text_switcher),
            entry(R.string.native_category_8, "Toolbar", R.string.native_title_toolbar, R.string.native_properties_toolbar),
            entry(R.string.native_category_8, "ActionMenuView", R.string.native_title_action_menu_view, R.string.native_properties_action_menu_view),
            entry(R.string.native_category_9, "WebView", R.string.native_title_web_view, R.string.native_properties_web_view),
            entry(R.string.native_category_10, "TabHost", R.string.native_title_tab_host, R.string.native_properties_tab_host),
            entry(R.string.native_category_10, "TabWidget", R.string.native_title_tab_widget, R.string.native_properties_tab_widget),
            entry(R.string.native_category_10, "Gallery", R.string.native_title_gallery, R.string.native_properties_gallery),
            entry(R.string.native_category_10, "SlidingDrawer", R.string.native_title_sliding_drawer, R.string.native_properties_sliding_drawer),
            entry(R.string.native_category_10, "AbsoluteLayout", R.string.native_title_absolute_layout, R.string.native_properties_absolute_layout),
            entry(R.string.native_category_10, "AnalogClock", R.string.native_title_analog_clock, R.string.native_properties_analog_clock),
            entry(R.string.native_category_10, "DigitalClock", R.string.native_title_digital_clock, R.string.native_properties_digital_clock),
            entry(R.string.native_category_10, "ZoomButton", R.string.native_title_zoom_button, R.string.native_properties_zoom_button),
            entry(R.string.native_category_10, "ZoomControls", R.string.native_title_zoom_controls, R.string.native_properties_zoom_controls),
            entry(R.string.native_category_10, "TwoLineListItem", R.string.native_title_two_line_list_item, R.string.native_properties_two_line_list_item),
            entry(R.string.native_category_10, "DialerFilter", R.string.native_title_dialer_filter, R.string.native_properties_dialer_filter)
    ));

    public static Entry find(String name) {
        for (Entry entry : ALL) if (entry.name.equals(name)) return entry;
        return null;
    }
}
