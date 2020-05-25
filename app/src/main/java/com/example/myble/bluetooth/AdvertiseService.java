package com.example.myble.bluetooth;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.myble.Constant;

@SuppressLint("Registered")
public class AdvertiseService extends Service {
    private Toast toast;
    private String TAG = "myBLE";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback advertiseCallback;
    private BluetoothManager bluetoothManager;

    /*---------------------------------广播参数设置--------------------------------------------*/
//    private static final ParcelUuid PARCEL_UUID_1 = Constant.PARCEL_UUID_1;
//    private static final UUID SERVICE_UUID_1 = Constant.SERVICE_UUID_1;
//    private static final UUID CHARACTERISTIC_UUID_1 = Constant.CHARACTERISTIC_UUID_1;
//    /*用于通知 notify的专属ID*/
//    private static final UUID DESCRIPTOR_UUID = Constant.DESCRIPTOR_UUID;
//
//    /*自定义厂商数据*/
//    private static int MANUFACTURER_ID = 0xACAC;
//    /*TODO 自定义广播数据 与厂商数据一起构成 Advertise data  */
//    private static byte[] BROADCAST_DATA = "2020".getBytes();
//    /*广播时service 携带的数据*/
//    private static byte[] send_data = "1234".getBytes();
//
//    /*设置 Characteristic 数据 给其他设备访问 在这里可以设置自己的 唯一ID */
//    private static byte[] charistic_DATA = "1234".getBytes(); /* 会被转为10进制数组 */

    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();

        /*设置广播属性*/
        setService();
        /*创建广播回调函数*/
        createAdvertiseCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*获取 bluetoothLeAdvertiser */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            Log.i(TAG, "onStartCommand: 3s 后开始广播");
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    Log.i(TAG, "run: 开始广播");
                    openBroadcast();
                }
            }, 3000);
        }else {
            showToast("该手机不支持蓝牙广播");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /*设置广播属性*/
    private void setService() {
        bluetoothGattServer = bluetoothManager.openGattServer(this, bluetoothGattServerCallback);
        BluetoothGattService service1 = new BluetoothGattService(Constant.SERVICE_UUID_1, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic bluetoothGattCharacteristic1 = new BluetoothGattCharacteristic(Constant.CHARACTERISTIC_UUID_1,
                BluetoothGattCharacteristic.PROPERTY_WRITE|
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY|
                        BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE|BluetoothGattCharacteristic.PERMISSION_READ);
        bluetoothGattCharacteristic1.addDescriptor(new BluetoothGattDescriptor(Constant.DESCRIPTOR_UUID, BluetoothGattDescriptor.PERMISSION_WRITE));
        bluetoothGattCharacteristic1.setValue(Constant.CHARACTERISTIC_DATA);
        service1.addCharacteristic(bluetoothGattCharacteristic1);
        bluetoothGattServer.addService(service1);
    }

    public void openBroadcast() {
        if (bluetoothAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
                bluetoothLeAdvertiser.startAdvertising(createAdvertiseSettings(true, 0), createAdvertiseData(Constant.BROADCAST_DATA), advertiseCallback);
            }
        }
    }

    private void createAdvertiseCallback(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            advertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Toast.makeText(AdvertiseService.this, "开启BLE广播成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Toast.makeText(AdvertiseService.this, "开启BLE广播失败，errorCode：" + errorCode, Toast.LENGTH_SHORT).show();
                }
            };
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseSettings createAdvertiseSettings(boolean connectable, int timeoutMillis) {
        // 设置广播的模式，低功耗，平衡和低延迟三种模式：对应  AdvertiseSettings.ADVERTISE_MODE_LOW_POWER  ,ADVERTISE_MODE_BALANCED ,ADVERTISE_MODE_LOW_LATENCY
        // 从左右到右，广播的间隔会越来越短
        return new AdvertiseSettings.Builder()
                //设置广播模式，以控制广播的功率和延迟。
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                // 设置是否可以连接。广播分为可连接广播和不可连接广播，一般不可连接广播应用在iBeacon设备上，这样APP无法连接上iBeacon设备
                .setConnectable(connectable)
                // 设置广播的信号强度，从左到右分别表示强度越来越强.。
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                // 设置广播的最长时间，最大值为常量AdvertiseSettings.LIMITED_ADVERTISING_MAX_MILLIS = 180 * 1000;  180秒
                // 设为0时，代表无时间限制会一直广播
                .setTimeout(timeoutMillis)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData createAdvertiseData(byte[] broadcastData) {
        return new AdvertiseData.Builder()
            //设置广播设备名称
            .setIncludeDeviceName(true)
            /* 设置广播包中是否包含蓝牙的发射功率。 */
            .setIncludeTxPowerLevel(true)
            /*以设置特定的UUID*/
            .addServiceUuid(Constant.PARCEL_UUID_1)
            /* 设置特定的UUID和其数据在广播包中。*/
            .addServiceData(Constant.PARCEL_UUID_1, Constant.send_data)
            /*可以设置特定厂商Id和其数据在广播包中。*/
            .addManufacturerData(Constant.MANUFACTURER_ID, broadcastData)
            .build();
            /*的设置中可以看出，如果一个外设需要在不连接的情况下对外广播数据，其数据可以存储在UUID对应的数据中*/
    }

    /* 从设备数据状态变化回调函数 */
    private final BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            // 这个device是中央设备， mac地址会 因为 中央（手机）蓝牙重启而变化
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "连接成功");
                Log.i(TAG, "onConnectionStateChange: " + status + " newState:" + newState + " deviceName:" + device.getName() + " mac:" + device.getAddress());
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    Log.i(TAG, "连接之后关闭广播");
//                    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
//                }
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "断开连接之后重新开启广播");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
//                    bluetoothLeAdvertiser.startAdvertising(createAdvertiseSettings(true, 0), createAdvertiseData(Constant.BROADCAST_DATA), advertiseCallback);
//                }
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.i(TAG, " onServiceAdded status:" + status + " service:" + service.getUuid().toString());
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.i(TAG, String.format(" onCharacteristicReadRequest requestId:%d offset:%d characteristic:%s value: %s",
                    requestId, offset, characteristic.getUuid().toString(), new String(characteristic.getValue())));
            bluetoothGattServer.sendResponse(device, requestId, 0, offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.i(TAG, " onCharacteristicWriteRequest requestId:" + requestId + " preparedWrite:" + preparedWrite + " responseNeeded:" + responseNeeded + " offset:" + offset +
                    " characteristic:" + characteristic.getUuid().toString() +
                    " value:" + new String(value));
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.i(TAG, " onCharacteristicReadRequest requestId:" + requestId + " offset:" + offset + " descriptor:" + descriptor.getUuid().toString());
        }

        int i = 0;

        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            Log.i(TAG, " onDescriptorWriteRequest requestId:" + requestId + " preparedWrite:" + preparedWrite + " responseNeeded:" + responseNeeded + " offset:" + offset + " value:" + toHexString(value) + " characteristic:" + descriptor.getUuid().toString());
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            Log.i(TAG, " onExecuteWrite requestId:" + requestId + " execute:" + execute);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.i(TAG, " onNotificationSent status:" + status);
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showToast(String msg) {
        toast.setText(msg);
        toast.show();
    }

    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1) return "";
        final StringBuilder hexString = new StringBuilder();
        for (byte aByteArray : byteArray) {
            if ((aByteArray & 0xff) < 0x10)//0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & aByteArray));
        }
        return hexString.toString().toLowerCase();
    }

}
