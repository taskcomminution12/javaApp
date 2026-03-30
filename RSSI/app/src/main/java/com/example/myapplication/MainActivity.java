package com.example.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context; //앱의 환경과 정보 접근에 사용함
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager; //wifi 스캔, 현재 연결 상태 확인, 스캔 결과 확인
import android.net.wifi.ScanResult; //wifi 하나의 정보 객체를 저장
import java.util.List; //리스트
import android.util.Log; //android studio Log
import android.Manifest;
import android.content.pm.PackageManager; //pm은 permission을 뜻함. 권한 확인 결과를 비교할 때 쓰는 상수를 제공

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat; //호환성 처리
import android.os.Bundle; //Activity 시작할 때 전달되는 데이터
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity; //Activity의 추가 기능 지원

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ZJZTEST";
    private TextView rssiText;

    @Override
    protected void onCreate(Bundle saveInstanceState) { //saveInstanceState는 화면의 상태를 저장하는 객체
        super.onCreate(saveInstanceState);

        setContentView(R.layout.main_activity);

        rssiText = findViewById(R.id.rssiText);
        rssiText.setText("앱 실행됨. RSSI 강도 측정 중...");

        registerReceiver(rssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
    }

    private BroadcastReceiver rssiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "Time rssiReceiver");

            WifiManager wman = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            WifiInfo info = wman.getConnectionInfo();

            int _rssi = info.getRssi();

            rssiText.setText("RSSI 신호 강도 : " + _rssi);
            Log.e(LOG_TAG, "_rssi ==> " + _rssi);
        }
    };
}
