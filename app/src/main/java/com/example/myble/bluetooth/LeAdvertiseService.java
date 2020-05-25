package com.example.myble.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.myble.Constant;

import java.util.UUID;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class LeAdvertiseService extends Service {
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    AdvertiseCallback advertiseCallback;
    BluetoothGattServer bluetoothGattServer;
    BluetoothGattServerCallback bluetoothGattServerCallback;
    BluetoothGattCharacteristic bluetoothGattCharacteristic;
    BluetoothGattService bluetoothGattService;
    BluetoothLeAdvertiser bluetoothLeAdvertiser;
    AdvertiseSettings advertiseSettings;
    AdvertiseData advertiseData;
    private final String deviceInfo = "Ryougi";


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
    @RequiresApi(api = LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        assert bluetoothManager != null : "Bluetooth function is ensured in the launch activity";
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.setName(Constant.AdvName);

        // 确认蓝牙功能打开
        enableBluetooth();
        // 初始化回调
        initLeAdvertiseCallBack();

        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        // 生成用于传递信息的特征Characteristic
        bluetoothGattCharacteristic = generateCharacteristicMessage(Constant.CHARACTERISTIC_UUID_1.toString());
        // 生成用于传输Characteristic的Service并向其中添加Characteristic
        bluetoothGattService = new BluetoothGattService(Constant.SERVICE_UUID_1, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic);

        bluetoothGattServer = bluetoothManager.openGattServer(this, bluetoothGattServerCallback);
        // 获取用于连接外围设备的Server实例，并添加Service
        bluetoothGattServer.addService(bluetoothGattService);

        initAdvertiseSettings(true, 0);
        initAdvertiseData(new byte[]{0x12, 0x12, 0x12, 0x12}, new byte[]{0x12, 0x12, 0x12, 0x12});
    }

    /**
     * 每次通过startService()方法启动Service时都会被回调。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @RequiresApi(api = LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Handler handler = new Handler();
        // 延时1s后开始广播，等待相关加载工作完成
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
                Log.i("gattDebug", "service size: " + bluetoothGattServer.getServices().size());
            }
        }, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开始广播
     */
    private void startAdvertising() {

    }


    /**
     * 服务销毁时的回调
     */
    @RequiresApi(api = LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
//        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }

    /**
     * 初始化回调方法
     */
    @RequiresApi(api = LOLLIPOP)
    private void initLeAdvertiseCallBack() {
        // 初始化广播回调
        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.i("AdvertiseCallback", "AdvertiseCallback");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.i("AdvertiseCallback", "onStartFailure");
            }
        };

        // 服务端实例回调
        bluetoothGattServerCallback = new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("gattServerCallback", "connection established");
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                Log.d("gattServerCallback", "service added callback, uuid: " + service.getUuid().toString());
                super.onServiceAdded(status, service);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                    BluetoothGattCharacteristic characteristic) {
                Log.d("gattServerCallback", "onCharacteristicReadRequest");
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                     int offset, byte[] value) {
                Log.d("gattServerCallback", "onCharacteristicWriteRequest");
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattDescriptor descriptor) {
                Log.d("gattServerCallback", "onDescriptorReadRequest");
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattDescriptor descriptor, boolean preparedWrite,
                                                 boolean responseNeeded, int offset, byte[] value) {
                Log.d("gattServerCallback", "onDescriptorWriteRequest");
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                Log.d("gattServerCallback", "onExecuteWrite");
                super.onExecuteWrite(device, requestId, execute);
            }
        };
    }

    /**
     * 构造方法基本选用默认参数
     */
    @RequiresApi(api = LOLLIPOP)
    private void initAdvertiseSettings(boolean connectable, int timeout) {
        advertiseSettings = new AdvertiseSettings.Builder()
                //设置广播模式，以控制广播的功率和延迟。
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                // 设置是否可以连接。广播分为可连接广播和不可连接广播，一般不可连接广播应用在iBeacon设备上，这样APP无法连接上iBeacon设备
                .setConnectable(connectable)
                // 设置广播的信号强度，从左到右分别表示强度越来越强.。
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                // 设置广播的最长时间，最大值为常量AdvertiseSettings.LIMITED_ADVERTISING_MAX_MILLIS = 180 * 1000;  180秒
                // 设为0时，代表无时间限制会一直广播
                .setTimeout(timeout)
                .build();
    }

    /**
     * 初始化Advertiser参数
     */
    @RequiresApi(api = LOLLIPOP)
    private void initAdvertiseData(byte[] serviceData, byte[] manufacturerData) {
        advertiseData = new AdvertiseData.Builder()
                //设置广播设备名称
                .setIncludeDeviceName(true)
                /* 设置广播包中是否包含蓝牙的发射功率。 */
                .setIncludeTxPowerLevel(true)
                /*以设置特定的UUID*/
                .addServiceUuid(Constant.PARCEL_UUID_1)
                /* 设置特定的UUID和其数据在广播包中。*/
                .addServiceData(Constant.PARCEL_UUID_1, serviceData)
                /*可以设置特定厂商Id和其数据在广播包中。*/
                .addManufacturerData(Constant.MANUFACTURER_ID, manufacturerData)
                .build();
    }

    /**
     * @param message 需要发送的消息作为Characteristic的uuid
     * @return 用于发送的Characteristic实例
     */
    private BluetoothGattCharacteristic generateCharacteristicMessage(String message) {
        if (message != null && !message.equals("")) {
            return new BluetoothGattCharacteristic(UUID.fromString(message),
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        } else {
            Log.i("generateMessage", "invalid message!");
            return null;
        }
    }

    /**
     * 确保开启服务之前蓝牙已经打开
     */
    private void enableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }
}
