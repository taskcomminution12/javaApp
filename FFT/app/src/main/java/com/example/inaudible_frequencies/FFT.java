package com.example.inaudible_frequencies;

public class FFT {

    private final int n;
    private final int m;

    public FFT(int n) {
        if (Integer.bitCount(n) != 1) {
            throw new IllegalArgumentException("FFT 크기는 2의 제곱수여야 합니다.");
        }
        this.n = n;
        this.m = (int) (Math.log(n) / Math.log(2));
    }

    public void fft(double[] real, double[] imag) {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;

        // Bit-reversal
        j = 0;
        for (i = 1; i < n - 1; i++) {
            n1 = n / 2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = real[i];
                real[i] = real[j];
                real[j] = t1;

                t1 = imag[i];
                imag[i] = imag[j];
                imag[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;
            n2 = n2 * 2;
            a = 0;

            for (j = 0; j < n1; j++) {
                c = Math.cos(-2 * Math.PI * j / n2);
                s = Math.sin(-2 * Math.PI * j / n2);

                for (k = j; k < n; k = k + n2) {
                    t1 = c * real[k + n1] - s * imag[k + n1];
                    t2 = s * real[k + n1] + c * imag[k + n1];

                    real[k + n1] = real[k] - t1;
                    imag[k + n1] = imag[k] - t2;
                    real[k] = real[k] + t1;
                    imag[k] = imag[k] + t2;
                }
                a += 1;
            }
        }
    }
}