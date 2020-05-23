package com.example.myble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    /*------------------------------------------扫描设置---------------------------------*/
    private BluetoothAdapter bluetoothAdapter;
    private Toast toast;
    private boolean scanStart;
    private Button scanButton;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private String TAG = "myBLE";

    private String bleAddress;
    private String blename;

    private boolean isAdvertised = false;

    private List<String> addressList; /*地址列表*/

    /*扫描的设备列表*/
    private ArrayList<ScanResult> mScanResultList;

    /*---------------------------------广播参数设置--------------------------------------------*/
    private final String AdvName = "Con";  /*名字长度不能超过3 否则报 数据过长的错误*/
    private static final ParcelUuid PARCEL_UUID_1 = ParcelUuid.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_UUID_1 = UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID_1 = UUID.fromString("0000ccc1-0000-1000-8000-00805f9b34fb");

    /*自定义厂商数据*/
    private static int MANUFACTURER_ID = 0xAC;
    /*TODO 自定义广播数据 与厂商数据一起构成 Advertise data  */
    private static byte[] BROADCAST_DATA = {0x12, 0x34, 0x56, 0x78};
    /*广播时service 携带的数据*/
    private static byte[] send_data = {0x01};

    /* 往charismatic write写数据  本机唯一 ID */
    private static byte[] return_DATA = "232413".getBytes(); /* 会被转为10进制数组 */
    /*设置char数据*/
    private static byte[] charistic_DATA = "12324".getBytes(); /* 会被转为10进制数组 */


    private BluetoothGattServer bluetoothGattServer;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback advertiseCallback;
    private Button advertise_button;
    private BluetoothManager bluetoothManager;
    private ScanCallback scanCallback;
    private LeScanCallback leScanCallback;
    private List<ScanFilter> filters;
    /* -------------------------------------------蓝牙连接的子线程-----------------------------*/
    /*蓝牙连接的回调*/
    private BluetoothGattCallback bluetoothGattCallback;
    //定义子线程handle，用于在BluetoothGattCallback中回调方法中的操作抛到该线程工作。
    private Handler mHandler;
    //定义handler工作的子线程
    private HandlerThread mHandlerThread;
    private BluetoothGatt mBluetoothGatt;
    private int reConnectionNum;
    private int maxConnectionNum;
    private BluetoothDevice bluetoothDevice;
    // 定义需要进行通信的ServiceUUID
    private UUID mServiceUUID = UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    // 定义需要进行通信的CharacteristicUUID
    private UUID mCharacteristicUUID = UUID.fromString("0000ccc1-0000-1000-8000-00805f9b34fb");
    /*用于通知的专属ID*/
    private static final UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /* 获取到的指定Characteristic实体*/
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        BluetoothUtils Utils = new BluetoothUtils();

        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            showToast("手机支持蓝牙功能");
            bluetoothAdapter.setName(AdvName);
        } else {
            showToast("手机蓝牙功能未开启");
        }

        /*检查是否支持BLE*/
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("手机不支持BLE");
//            finish();
        } else {
            showToast("支持BLE");
        }

        scanStart = false;
        scanButton = this.<Button>findViewById(R.id.start_ble);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!scanStart) {
                    scanButton.setText("停止扫描");
                    scanStart = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        scan(true);
                    }
                } else {
                    scanButton.setText("开始扫描");
                    scanStart = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        scan(false);
                    }
                }
            }
        });

        /*广播*/
        advertise_button = findViewById(R.id.advertise_ble);
        advertise_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdvertised) {
                    startAdvertise();
                    advertise_button.setText("停止广播");
                    isAdvertised = true;
                } else {
                    stopAdvertise();
                    advertise_button.setText("开始广播");
                    isAdvertised = false;
                }
            }
        });
        createAdvertiseCallback();

        /*连接设备*/
        Button connect_button = this.<Button>findViewById(R.id.connect_ble);
        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressList.isEmpty()) {
                    showToast("没有扫描到对应的设备");
                } else {
                    for (String address : addressList) {
                        Log.i(TAG, "onClick: " + address);
                        connectBLE(address);
                    }
                }
            }
        });
        creatBluetoothGattCallback();

        /*断开连接*/
        Button disconnect_button = this.<Button>findViewById(R.id.disconnect_ble);
        disconnect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
            }
        });
    }

    public void connectBLE(String bleAddress){
        //定义重连次数
        reConnectionNum = 0;
        //最多重连次数
        maxConnectionNum = 3;
        Log.i(TAG, "connectBLE: 开始连接");
        // 初始化handler
        mHandlerThread = new HandlerThread("connect");
        mHandlerThread.start();
        //将handler绑定到子线程中
        mHandler = new Handler(mHandlerThread.getLooper());
        /* 获取的蓝牙设备实体 */
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bleAddress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, bluetoothGattCallback);
        }
    }

    /* 创建蓝牙连接回调 中心设备 */
    public void creatBluetoothGattCallback(){
        bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            }

            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyRead(gatt, txPhy, rxPhy, status);
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                //操作成功的情况下
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "onConnectionStateChange: 连接成功");
                    //判断是否连接
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //可延迟发现服务，也可不延迟
                        showToast("连接成功");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mBluetoothGatt.discoverServices();
                            }
                        });
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //判断是否断开连接码
                        showToast("断开连接");
                    }
                } else {
                    if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "onConnectionStateChange: 断开连接");
                    }
                    //重连次数不大于最大重连次数
                    Log.i(TAG, "onConnectionStateChange: 尝试重连");
                    showToast("尝试重连");
                    if (reConnectionNum < maxConnectionNum) {
                        //重连次数自增
                        reConnectionNum++;
                        //连接设备
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this,
                                    false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                        } else {
                            mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, bluetoothGattCallback);
                        }
                    } else {
                        //断开连接，返回连接失败回调
                        showToast("连接异常：status" + status);
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "onServicesDiscovered: 开始发现服务");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(mServiceUUID);
                            Log.i(TAG, String.format("run: 获取的服务 : %s",String.valueOf(bluetoothGattService)));
                            if (bluetoothGattService != null) {
                                bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(mCharacteristicUUID);
                                /* 获取指定特征值成功 */
                                if (bluetoothGattCharacteristic != null) {
                                    //通过Gatt对象读取特定特征（Characteristic）的特征值
                                    mBluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
                                }
                            } else {
                                Log.i(TAG, "run: 获取的数据为空");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //获取读取到的特征值
                    Log.i(TAG, String.format("onCharacteristicRead: 获取的characteristic的值 %s", new String(characteristic.getValue())));
                    /* 读取成功之后 向其写数据 */
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //写入你需要传递给外设的特征值（即传递给外设的信息）
                            bluetoothGattCharacteristic.setValue(return_DATA);
                            //通过GATt实体类将，特征值写入到外设中。
                            mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                            /*读取成功之后 触发 onCharacteristicRead*/
                        }
                    });
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //获取写入到外设的特征值
                    Log.i(TAG, "onCharacteristicWrite: 将要写入的值" + new String(characteristic.getValue()));
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
            }
        };
    }

    public void startAdvertise(){
        /* 设置服务端属性 */
        setService();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        } else {
            showToast("该手机不支持蓝牙广播");
//            finish();
        }
        openBroadcast();
    }

    private void stopAdvertise(){
        if (bluetoothAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            }
        }
    }

    /*设置广播属性*/
    private void setService() {
        bluetoothGattServer = bluetoothManager.openGattServer(this, bluetoothGattServerCallback);
        BluetoothGattService service1 = new BluetoothGattService(SERVICE_UUID_1, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic bluetoothGattCharacteristic1 = new BluetoothGattCharacteristic(CHARACTERISTIC_UUID_1,
                BluetoothGattCharacteristic.PROPERTY_WRITE|
                           BluetoothGattCharacteristic.PROPERTY_NOTIFY|
                           BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE|BluetoothGattCharacteristic.PERMISSION_READ);
        bluetoothGattCharacteristic1.addDescriptor(new BluetoothGattDescriptor(DESCRIPTOR_UUID, BluetoothGattDescriptor.PERMISSION_WRITE));
        bluetoothGattCharacteristic1.setValue(charistic_DATA);
        service1.addCharacteristic(bluetoothGattCharacteristic1);
        bluetoothGattServer.addService(service1);
    }

    public void openBroadcast() {
        if (bluetoothAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
                bluetoothLeAdvertiser.startAdvertising(createAdvertiseSettings(true, 0), createAdvertiseData(BROADCAST_DATA), advertiseCallback);
            }
        }
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
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
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

    private void createAdvertiseCallback(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            advertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Toast.makeText(MainActivity.this, "开启BLE广播成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Toast.makeText(MainActivity.this, "开启BLE广播失败，errorCode：" + errorCode, Toast.LENGTH_SHORT).show();
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
                .addServiceUuid(PARCEL_UUID_1)
                /* 设置特定的UUID和其数据在广播包中。*/
                .addServiceData(PARCEL_UUID_1, send_data)
                /*可以设置特定厂商Id和其数据在广播包中。*/
                .addManufacturerData(MANUFACTURER_ID, broadcastData)
                .build();
                /*的设置中可以看出，如果一个外设需要在不连接的情况下对外广播数据，其数据可以存储在UUID对应的数据中*/
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

    /*----------------------扫描相关函数设置------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            /*蓝牙没有打开则请求打开*/
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scan(boolean enable) {
        /*用于高于5.0的蓝牙设备*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice bluetoothDevice = result.getDevice();
                    blename = bluetoothDevice.getName();
                    bleAddress = bluetoothDevice.getAddress();
                    if (AdvName.equals(blename)) {
                        if (!addressList.contains(bleAddress)) {
                            addressList.add(bleAddress);
                        }
                        // TODO 在这里对扫描到的数据 进行处理
                        Log.i(TAG, "onScanResult: 5.0 以上扫描结果" + bluetoothDevice.getName() + bluetoothDevice.getAddress() + bluetoothDevice.getBondState() +
                                "getScanRecord: " + result.getScanRecord().toString());

                    } else {
                        Log.i(TAG, "其他设备: "+ result.getScanRecord().toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    showToast("扫描出错" + errorCode);
                }
            };
        } else {
            leScanCallback = new LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.i(TAG, "onLeScan: 5.0 以下扫描结果 ：" + Arrays.toString(scanRecord));
                }
            };
        }


        if (enable) {
            /*创建用于接收地址的数组*/
            addressList = new ArrayList<>();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /*可以设置过滤uuid 的方式 暂时没成功 */
                    scanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                            .setReportDelay(10000).build();
                    filters = new ArrayList<>();
                    ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("0000CCC0-0000-1000-8000-00805F9B34FB")).build();
                    filters.add(filter);
                    bluetoothLeScanner.startScan(scanCallback);
                } else {
                    scanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                            .setReportDelay(10000).build();
                    bluetoothLeScanner.startScan(scanCallback);
                }

            } else {
                bluetoothAdapter.startLeScan(leScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner.stopScan(scanCallback);
            } else {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
            showToast("关闭蓝牙扫描");
        }
    }

    public void showToast(String msg) {
        toast.setText(msg);
        toast.show();
    }

    /*动态申请权限*/
    private void requestPermission() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }
    }
}
