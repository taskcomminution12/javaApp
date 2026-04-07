package com.example.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context; //앱의 환경과 정보 접근에 사용함
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager; //wifi 스캔, 현재 연결 상태 확인, 스캔 결과 확인
import android.net.wifi.ScanResult; //wifi 하나의 정보 객체를 저장

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List; //리스트
import java.util.Map;

import android.os.Handler;
import android.util.Log; //android studio Log
import android.Manifest;
import android.content.pm.PackageManager; //pm은 permission을 뜻함. 권한 확인 결과를 비교할 때 쓰는 상수를 제공
import android.os.Build;

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
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
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

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            Log.d(LOG_TAG, "BluetoothManager null");
            return;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Log.d(LOG_TAG, "블루투스 지원 안함");
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothLeScanner == null) {
            Log.d(LOG_TAG, "BluetoothLeScanner null");
            return;
        }

        bluetoothStateCheck();

        checkAndRequestBlePermissions();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
        registerReceiver(rssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        //handler.post(scanRunnable);
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

    private void bluetoothStateCheck() {
        if (bluetoothManager == null) {
            Log.d(LOG_TAG, "bluetooth  권한 없음");
            return;
        }

        if (bluetoothAdapter == null) {
            Log.d(LOG_TAG, "bluetooth  어댑터 연결 안됨.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Log.d(LOG_TAG, "bluetooh  꺼져있음");
            return;
        }
    }

    private boolean scanning;
    private void scanLeDevice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)  {
            Log.d(LOG_TAG, "permission denied : BLUETOOTH_SCAN");
            return;
        }
        if (!scanning) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(LOG_TAG, "permission denied in stopScan");
                        return;
                    }

                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, 10000);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private Map<String, Integer> rssiMap = new HashMap<>();
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
            super.onScanResult(callbackType, result);
            String address = result.getDevice().getAddress();
            int rssi = result.getRssi();
            Log.d(LOG_TAG, address + " / " + rssi);
            rssiMap.put(address, rssi);
            runOnUiThread(() -> updateUI());
        }
    };

    private void updateUI() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> entry : rssiMap.entrySet()) {
            sb.append(entry.getKey())
                    .append(" / ")
                    .append(entry.getValue())
                    .append("\n");
        }

        wifiScanText.setText(sb.toString());
    }

    private static final int BLE_PERMISSION_REQUEST_CODE = 100;

    private void checkAndRequestBlePermissions() {

        List<String> permissionList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionList.toArray(new String[0]),
                    BLE_PERMISSION_REQUEST_CODE
            );
        } else {
            Log.d(LOG_TAG, "권한 이미 있음 → 스캔 시작");
            scanLeDevice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLE_PERMISSION_REQUEST_CODE) {

            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(LOG_TAG, "권한 승인됨 → 스캔 시작");
                scanLeDevice();
            } else {
                Log.d(LOG_TAG, "권한 거부됨 → 스캔 불가");
            }
        }
    }
}
