package com.example.inaudible_frequencies;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SpectrumView extends View {

    private double[] spectrum;
    private Paint paint;

    // 기본 생성자 (코드에서 생성할 때)
    public SpectrumView(Context context) {
        super(context);
        init();
    }

    // XML에서 사용할 생성자 (필수)
    public SpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // 공통 초기화
    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true);
    }

    public void updateSpectrum(double[] data) {
        this.spectrum = data;
        postInvalidate(); // UI 스레드 안전 호출
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (spectrum == null) return;

        int width = getWidth();
        int height = getHeight();

        int size = spectrum.length;

        // ❗ 0 나누기 방지
        if (size == 0) return;

        float barWidth = (float) width / size;

        for (int i = 0; i < size; i++) {
            float x = i * barWidth;

            float value = (float) spectrum[i];

            //  더 좋은 스케일 (로그 추천)
            float scaled = (float) Math.log10(value + 1) * 50;

            if (scaled > height) scaled = height;

            canvas.drawLine(
                    x,
                    height,
                    x,
                    height - scaled,
                    paint
            );
        }
    }
}