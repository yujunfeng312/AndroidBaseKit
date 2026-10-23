package dev.androidbasekit.sample.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.graphics.Insets;

/** 在透明系统栏下方绘制底色；兼容新版边到边窗口，支持横屏导航栏位于左右侧。 */
public final class SystemBarBackdropView extends View {
    private final Paint paint = new Paint();
    private Insets status = Insets.NONE;
    private Insets navigation = Insets.NONE;
    private int statusColor;
    private int navigationColor;

    public SystemBarBackdropView(Context context, AttributeSet attributes) { super(context, attributes); }

    public void setBarInsets(Insets status, Insets navigation) {
        this.status = status;
        this.navigation = navigation;
        invalidate();
    }

    public void setBarColors(int statusColor, int navigationColor) {
        this.statusColor = statusColor;
        this.navigationColor = navigationColor;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawInsets(canvas, status, statusColor);
        drawInsets(canvas, navigation, navigationColor);
    }

    private void drawInsets(Canvas canvas, Insets insets, int color) {
        paint.setColor(color);
        int width = getWidth(), height = getHeight();
        canvas.drawRect(0, 0, width, insets.top, paint);
        canvas.drawRect(0, height - insets.bottom, width, height, paint);
        canvas.drawRect(0, insets.top, insets.left, height - insets.bottom, paint);
        canvas.drawRect(width - insets.right, insets.top, width, height - insets.bottom, paint);
    }
}
