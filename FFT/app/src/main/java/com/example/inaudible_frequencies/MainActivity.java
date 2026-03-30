package com.example.inaudible_frequencies;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.jtransforms.fft.DoubleFFT_1D;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_AUDIO_PERMISSION = 1;
    private static final String TAG = "MicTest";
    private TextView statusText;
    private WaveformView waveformView;

    private SpectrumView spectrumView;
    private static final int FFT_SIZE = 2048;
    //private FFT fft;
    private DoubleFFT_1D fft;

    private double[] fftData;
    private double[] magnitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        statusText.setText("앱 시작됨..\n 권한 확인 중...");

        waveformView = findViewById(R.id.waveformView);
        spectrumView = findViewById(R.id.spectrumView);
        //fft = new FFT(FFT_SIZE);
        fft = new DoubleFFT_1D(FFT_SIZE);
        fftData = new double[FFT_SIZE * 2];
        magnitude = new double[FFT_SIZE / 2];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSION
            );
        } else {
            startMicTest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMicTest();
            } else {
                Log.e(TAG, "마이크 권한이 거부됨");
            }
        }
    }

    private double[] convertToDouble(short[] buffer, int size) {
        double[] result = new double[size];

        int limit = Math.min(buffer.length, size);

        for(int i=0; i < limit; i++) {
            result[i] = buffer[i];
        }

        return result;
    }

    private double[] calculateMagnitude(double[] real, double[] imag) {
        double[] magnitude = new double[real.length / 2];

        for(int i=0; i<magnitude.length; i++) {
            magnitude[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }

        return magnitude;
    }

    private double findPeakFrequency(double[] magnitude) {
        int peakIndex = 1;
        double peakValue = magnitude[1];

        for(int i = 2; i < magnitude.length; i++) {
            if (magnitude[i] > peakValue) {
                peakValue = magnitude[i];
                peakIndex = i;
            }
        }

        return (double) peakIndex * 48000 / FFT_SIZE;
    }

    private void startMicTest() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            statusText.setText("마이크 입력 시작됨..");
            Log.e(TAG, "RECORD_AUDIO 권한 없음");
            return;
        }

        new Thread(() -> {
            int sampleRate = 48000;

            int bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "버퍼 크기 가져오기 실패");
                return;
            }

            AudioRecord audioRecord;

            try {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                );
            } catch (Exception e) {
                Log.e(TAG, "AudioRecord 생성 실패", e);
                return;
            }

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord 초기화 실패");
                return;
            }

            short[] buffer = new short[bufferSize];

            try {
                audioRecord.startRecording();
                Log.d(TAG, "녹음 시작");

                while (true) {
                    int read = audioRecord.read(buffer, 0, buffer.length);

                    if (read > 0) {
                        short[] drawBuffer = new short[read];
                        System.arraycopy(buffer, 0, drawBuffer, 0, read);

                        waveformView.updateWaveform(drawBuffer);
                        /*double[] real = convertToDouble(drawBuffer, FFT_SIZE);
                        double[] imag = new double[FFT_SIZE];

                        fft.fft(real, imag);*/

                        for(int i = 0; i < FFT_SIZE; i++) {
                            if(i<drawBuffer.length) {
                                fftData[2 * i] = drawBuffer[i];
                                fftData[2 * i + 1] = 0.0;
                            } else {
                                fftData[2 * i] = 0.0;
                                fftData[2 * i + 1] = 0.0;
                            }
                        }

                        fft.complexForward(fftData);

                        double[] magnitude = new double[FFT_SIZE / 2];
                        for(int i=0; i<magnitude.length; i++) {
                            double real = fftData[2 * i];
                            double imag = fftData[2 * i + 1];
                            magnitude[i] = Math.sqrt(real * real + imag * imag);
                        }

                        double[] spectrumCopy = magnitude.clone();
                        spectrumView.post(() -> spectrumView.updateSpectrum(spectrumCopy));
                        //spectrumView.updateSpectrum(magnitude);

                        int max = 0;
                        for (int i = 0; i < read; i++) {
                            int value = Math.abs(buffer[i]);
                            if (value > max) {
                                max = value;
                            }
                        }
                        int finalMax = max;
                        statusText.post(() -> statusText.setText("볼륨 최댓값: " + finalMax + ", 최고 주파수: "));
                        Log.d(TAG, "현재 볼륨 최대값: " + max);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "녹음 중 오류", e);
            } finally {
                try {
                    audioRecord.stop();
                } catch (Exception ignored) {
                }
                audioRecord.release();
            }
        }).start();
    }
}