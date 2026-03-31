package com.example.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context; //앱의 환경과 정보 접근에 사용함
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager; //wifi 스캔, 현재 연결 상태 확인, 스캔 결과 확인
import android.net.wifi.ScanResult; //wifi 하나의 정보 객체를 저장

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List; //리스트

import android.os.Handler;
import android.util.Log; //android studio Log
import android.Manifest;
import android.content.pm.PackageManager; //pm은 permission을 뜻함. 권한 확인 결과를 비교할 때 쓰는 상수를 제공

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat; //호환성 처리
import android.os.Bundle; //Activity 시작할 때 전달되는 데이터
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity; //Activity의 추가 기능 지원

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ZJZTEST";
    private TextView rssiText;
    private TextView wifiScanText;
    private WifiManager wifiManager;
    private static int callCount = 0;
    private static Handler handler = new Handler();
    private static final int WIFI_PERMISSION_REQUEST_CODE = 100;
    private RssiView rssiView;
    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            boolean success = wifiManager.startScan(); //Android 10 이상일 경우 위치 ACCESS_FINE_LOCATION, CHANGE_WIFI_STATE 권한 필요, 기기의 위치 설정 on
            if(success) {
                scanSuccess();
                callCount += 1;
            }
            else {
                scanFailure();
            }
            handler.postDelayed(this, 10000); // Android 9 이상 포그라운드 앱은 2분 간격으로 4회 스캔 가능.
        }
    };

    /*private void checkWifiPermission() {
        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "permission denied : ACCESS_FINE_LOCATION");
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "permission denied : CHANGE_WIFI_STATE");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "permission denied : ACCESS_WIFI_STATE");
        }
        if(!permissionList.isEmpty()) {
            permissionLauncher.launch(permissionList.toArray(new String[0]));
        } else {
            Log.d(LOG_TAG, "권한 모두 허용됨");
        }
    }*/

    @Override
    protected void onCreate(Bundle saveInstanceState) { //saveInstanceState는 화면의 상태를 저장하는 객체
        super.onCreate(saveInstanceState);

        setContentView(R.layout.main_activity);

        rssiText = findViewById(R.id.rssiText);
        rssiText.setText("앱 실행됨. RSSI 강도 측정 중...");

        wifiScanText = findViewById(R.id.wifiScanResult);
        wifiScanText.setText("onCreate() 호출. wifiScan() 대기중..");

        //rssiView = findViewById(R.id.rssiView);

        //checkWifiPermission();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
        registerReceiver(rssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        handler.post(scanRunnable);
    }
    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            Log.d(LOG_TAG, "broadcast 수신, 성공 여부 : " + success);

            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }
    };
    private BroadcastReceiver rssiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "Time rssiReceiver");

            WifiManager wman = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            WifiInfo info = wman.getConnectionInfo();

            int _rssi = info.getRssi();

            rssiText.setText("현재 연결 중인 Wifi의 RSSI 신호 강도 : " + _rssi);
            Log.e(LOG_TAG, "_rssi ==> " + _rssi);
        }
    };

    /*private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false
                        );

                        if (Boolean.TRUE.equals(fineLocationGranted)) {
                            Log.d(LOG_TAG, "권한 허용됨");
                        } else {
                            Log.d(LOG_TAG, "권한 거부됨");
                        }
                    }
            );*/

    private void scanSuccess() {
        if (wifiManager == null) {
            Log.d(LOG_TAG, "wifiManager null");
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "권한 없음");
        }

        List<ScanResult> results = wifiManager.getScanResults(); //getScanResult()를 호출하려면 ACCESS_FINE_STATE 권한 필요.

        String str = new String();

        for (ScanResult result : results) {
            Log.d(LOG_TAG, "Call count = " + callCount + ", SSID = " + result.SSID + "BSSID" + result.BSSID + ", level : " + result.level);
            str += "SSID : " + result.SSID + ", RSSI : " + result.level + "\n";
        }

        /*results.sort(Comparator.comparingInt((ScanResult r) -> r.level).reversed());

        List<ScanResult> top5 = results.subList(0, Math.min(5, results.size()));

        rssiView.post(() -> rssiView.setScanResult(top5));*/

        wifiScanText.setText(str);
    }

    private void scanFailure() {
        Log.d(LOG_TAG, "scan Failure");
    }
}
