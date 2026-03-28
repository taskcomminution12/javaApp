package com.example.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context; //앱의 환경과 정보 접근에 사용함
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.appcompat.app.AppCompatActivity; //Activity의 추가 기능 지원

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            Log.d("TEST", "broadcast 수신,  success = " + success);

            if(success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) { //saveInstanceState는 화면의 상태를 저장하는 객체
        super.onCreate(saveInstanceState);

        Log.d("TEST", "1. onCreate 실행");

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Log.d("TEST", "wifiManager = " + wifiManager);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { //ContextCompat으로 MainActivity의 권한 확인. PERMISSION_GRANTED(권한 있음) 상수랑 비교
            Log.d("TEST", "2.권한 없음");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //권한 요청, requestCode는 어떤 request인지 식별을 위해 설정
            return;
        } else {
            Log.d("TEST", "권한 있음");
            startWifiScan();
        }
    }

    void startWifiScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { //ContextCompat으로 MainActivity의 권한 확인. PERMISSION_GRANTED(권한 있음) 상수랑 비교
            Log.d("TEST", "권한이 없으므로 스캔을 중단함.");
            return;
        }

        /*Log.d("TEST", "wifiManager = " + wifiManager);
        Log.d("TEST", "wifi enabled = " + wifiManager.isWifiEnabled());
        Log.d("TEST", "permission = " +
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED));*/

        Log.d("TEST", "3. startWifiscan 실행");

        if (wifiManager == null) {
            Log.d("TEST", "wifiManager가 null");
            return;
        }

        boolean success = wifiManager.startScan();
        Log.d("TEST", "4. startScan 결과: " + success);

        if (!success) {
            scanFailure();
        }
    }

    private void scanSuccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TEST", "scanSuccess에서 권한 없음");
            return;
        }

        List<ScanResult> results = wifiManager.getScanResults();
        Log.d("TEST", "scanSuccess results size = " + results.size());

        for (ScanResult result : results) {
            Log.d("TEST", "SSID = " + result.SSID + ", BSSID = " + result.BSSID + ", level = " + result.level);
        }
    }

    private void scanFailure() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TEST", "scanFailure에서 권한 없음");
            return;
        }

        List<ScanResult> results = wifiManager.getScanResults();
        Log.d("TEST", "scanFailure results size = " + results.size());

        for (ScanResult result : results) {
            Log.d("TEST", "[OLD] SSID = " + result.SSID + ", BSSID = " + result.BSSID + ", level = " + result.level);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TEST", "권한 허용됨");
                startWifiScan();
            } else {
                Log.d("TEST", "권한 거부됨");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();;
        unregisterReceiver(wifiScanReceiver);
    }
}
