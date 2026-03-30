package com.example.inaudible_frequencies;   // <- 네 MainActivity package랑 똑같이 바꿔

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {

    private short[] audioData = new short[0];

    private final Paint waveformPaint = new Paint();
    private final Paint centerLinePaint = new Paint();

    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        waveformPaint.setColor(Color.GREEN);
        waveformPaint.setStrokeWidth(2f);
        waveformPaint.setAntiAlias(true);

        centerLinePaint.setColor(Color.GRAY);
        centerLinePaint.setStrokeWidth(1f);
    }

    public void updateWaveform(short[] data) {
        if (data == null) return;

        audioData = new short[data.length];
        System.arraycopy(data, 0, audioData, 0, data.length);

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float centerY = height / 2f;
        canvas.drawLine(0, centerY, width, centerY, centerLinePaint);

        if (audioData == null || audioData.length < 2) return;

        float xStep = (float) width / (audioData.length - 1);

        float prevX = 0;
        float prevY = centerY - ((audioData[0] / 32768f) * centerY);

        for (int i = 1; i < audioData.length; i++) {
            float x = i * xStep;
            float y = centerY - ((audioData[i] / 32768f) * centerY);

            canvas.drawLine(prevX, prevY, x, y, waveformPaint);

            prevX = x;
            prevY = y;
        }
    }
}