package com.example.myble.bluetooth;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.myble.Constant;

import java.util.ArrayList;

@SuppressLint("Registered")
public class LeScanService extends Service {
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGattCallback gattCallback;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService gattService;
    private ArrayList<String> storeIDs;

    /**
     * 绑定服务时才会调用
     * 必须要实现的方法
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法。该方法只被调用一次
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化接触记录ID表
        storeIDs = new ArrayList<>();

        // 获取bluetoothManager 及 bluetoothAdapter
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        assert bluetoothManager != null : "Bluetooth function is ensured in the launch activity";
        bluetoothAdapter = bluetoothManager.getAdapter();

        // 确认蓝牙功能打开
        enableBluetooth();

        // 设置leScanCallback
        initLeScanCallBack();
    }

    /**
     * 每次通过startService()方法启动Service时都会被回调。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Handler handler = new Handler();
        // 延时1s后开始广播，等待相关加载工作完成
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.startLeScan(leScanCallback);
            }
        }, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        isScanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    /**
     * 获取记录到的uuid
     * @return 记录的uuid
     */
    public ArrayList<String> getStoreIDs() {
        return this.storeIDs;
    }

    /**
     * 获取ScanService的扫描状态
     * @return 扫描状态
     */
    public boolean getScanState() {
        return this.isScanning;
    }

    /**
     * 确保开启服务之前蓝牙已经打开
     */
    private void enableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    /**
     * 调用startLeScan()方法查找BLE设备，扫描到之后会执行该回调
     */
    private void initLeScanCallBack() {
        // 扫描设备时的回调
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                String devName = device.getName();
                Log.i("leScanCallback", "" + devName);
                bluetoothGatt = device.connectGatt(getBaseContext(), false, gattCallback);
                Log.i("leScanCallback", "service num: " + bluetoothGatt.getServices().size());
                gattService = searchServiceFromGatt(bluetoothGatt);
                if (null != gattService) {
                    // 记录本次接触
                    Log.i("leScanCallback", "try to get gatt service");
                    getCharacteristicUuid(gattService);
                }
//                if (devName != null) {
//                    Log.i("leScanCallback", "bleDevice detected:" + devName);
//                    // 根据DevName判断是同类型设备
//                    if (devName.equals(Constant.AdvName)) {
//                        Log.i("leScanCallback", "specific bleDevice detected!");
//                        bluetoothGatt = device.connectGatt(getBaseContext(), false, gattCallback);
//                        gattService = searchServiceFromGatt(bluetoothGatt);
//                        if (null != gattService) {
//                            // 记录本次接触
//                            Log.i("leScanCallback", "try to get gatt service");
//                            getCharacteristicUuid(gattService);
//                        }
//                    }
//                }
            }
        };

        // 建立连接时的回调
        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // 表明连接已建立
                    gatt.discoverServices();
                    Log.i("gattCallback", "Connected to GATT server.");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // 表明连接已断开
                    Log.i("gattCallback", "disconnect with GATT server.");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                // 发现gatt服务
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("onServicesDiscovered", "GATT service discovered!");
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                // 可以在characteristic字段中交换数据
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("gattCallback", "GATT onCharacteristicRead is called!");
                }
            }
        };
    }

    /**
     * 从连接设备的gatt服务list中获取指定uuid的service
     * @param bluetoothGatt gatt实例
     * @return 指定uuid的service
     */
    private BluetoothGattService searchServiceFromGatt(BluetoothGatt bluetoothGatt) {
        if (null != bluetoothGatt) {
            Log.i("searchServiceFromGatt", "searchServiceFromGatt: find exact service, service uuid: " + bluetoothGatt.getServices().size());
            return bluetoothGatt.getService(Constant.SERVICE_UUID_1);
        } else {
            Log.i("searchServiceFromGatt", "searchServiceFromGatt: service not found!");
            return null;
        }
    }

    /**
     * 从Gatt service中获取第一个Characteristic的uuid，该uuid携带着中心设备自定义ID
     * @param service gattService
     */
    private void getCharacteristicUuid(BluetoothGattService service) {
        BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(0);
        String uuid = characteristic.getUuid().toString();
        Log.i("uuid get success", "target device uuid: " + uuid);
        if (! storeIDs.contains(uuid)) {
            storeConnectRecord(uuid);
            Log.i("uuid get success", "new device uuid is recorded");
            // TODO 在UI部分做出交互动作
        }
    }

    /**
     * 保存本次接触记录
     * @param uuid 接触的uuid
     */
    private void storeConnectRecord(String uuid) {
        storeIDs.add(uuid);
        // TODO 加入时间戳等其他信息
    }


}
