package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.util.AttributeSet;
import android.view.View;
import java.util.List; //리스트

/* Xml
<com.example.myapplication.RssiView
android:id="@+id/rssiView"
android:layout_width="match_parent"
android:layout_height="500dp"/>
*/

public class RssiView extends View {

    private List<ScanResult> results;
    private Paint paint;

    public RssiView(Context context) {
        super(context);
        init();
    }

    public RssiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true);
    }

    public void setScanResult(List<ScanResult> results) {
        this.results = results;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(results == null) return;

        int width = getWidth();
        int height = getHeight();
        int size = results.size();

        if (size == 0) return;

        float barWidth = (float) width / size;
        float centerOffset = barWidth / 2f;

        paint.setStrokeWidth((float) width / (size * 2));

        int i = 0;

        for (ScanResult result : results) {
            int rssi = result.level;

            float normalized = (rssi + 100) / 70f;

            if (normalized < 0f) normalized = 0f;
            if (normalized > 1f) normalized = 1f;

            float barHeight = normalized * height;
            float x = i * barWidth + (float) (width / (size * 2));
            float yEnd = height - barHeight;

            int value =result.level;

            canvas.drawLine(x, height, x, yEnd, paint);

            i += 1;
        }
    }
}
