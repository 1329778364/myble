package com.example.myble.bluetooth;


import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.myble.Constant;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ScanService extends Service {
    private Toast toast;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    /*连接*/
    private BluetoothGattCallback bluetoothGattCallback;
    private BluetoothDevice bluetoothDevice;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private int reConnectionNum;
    private int maxConnectionNum;

    private String TAG = "myBLE";

    private String bleAddress;
    private String blename;

//    private List<String> addressList ; /*地址列表*/
    private List<String> addressList = new LinkedList<>();
    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();

        /*初始化 扫描回调*/
        initScanCallback();

        /*创建连接外围设备之后的回调*/
        creatConnectCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand:开始扫描服务");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "3s后开始扫描");
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    startScan();
                }
            }, 1000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 关闭广播
     */
    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopScan();
        }
        super.onDestroy();
    }

    /**
     * 初始化扫描回调函数
     */
    private void initScanCallback(){
        /*用于高于5.0的蓝牙设备*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice bluetoothDevice = result.getDevice();
                    blename = bluetoothDevice.getName();
                    bleAddress = bluetoothDevice.getAddress();
                    if (Constant.AdvName.equals(blename)) {
                        if (!addressList.contains(bleAddress)) {
                            addressList.add(bleAddress);
                            Log.i(TAG, "onScanResult: 第一次连接");
                            connectBLE(bleAddress);
                        }
                        // TODO 在这里对扫描到的数据 进行处理
                        Log.i(TAG, " 目标设备 Name: " + bluetoothDevice.getName() +" " + bluetoothDevice.getAddress() +
                                " 广播数据详情: " + Objects.requireNonNull(result.getScanRecord()).toString());
                    } else {
                        /* 其他设备使用另外一个队列存储 */
                        Log.i(TAG, String.format("其他设备: %s", bleAddress));
//                            if (!Constant.scanDeviceAddressList_other.contains(bleAddress)) {
//                                Constant.scanDeviceAddressList_other.add(bleAddress);
//                            }
                    }
                }
                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    showToast("扫描出错" + errorCode);
                }
            };
        } else {
            leScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.i(TAG, "onLeScan: 5.0 以下扫描结果 ：" + Arrays.toString(scanRecord));
                }
            };
        }
    }

    /**
     * 中心设备 扫描之后 连接从设备 状态变化的回调函数
     */
    public void creatConnectCallback(){
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
                    //判断是否连接
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "连接成功：" + newState + " state： " + status);
                        //可延迟发现服务，也可不延迟
                        showToast("连接成功");
                        /* 连接成功之后 停止扫描 */
//                        stopScan();
                        /*发现服务*/
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
                }
                else {
                    //重连次数不大于最大重连次数
                    Log.i(TAG, "onConnectionStateChange: 尝试重连");
                    showToast("尝试重连");
                    if (reConnectionNum < maxConnectionNum) {
                        //重连次数自增
                        reConnectionNum++;
                        //连接设备
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mBluetoothGatt = bluetoothDevice.connectGatt(ScanService.this,
                                    false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_AUTO);
                        } else {
                            mBluetoothGatt = bluetoothDevice.connectGatt(ScanService.this, false, bluetoothGattCallback);
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
                            BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(Constant.mServiceUUID);
                            Log.i(TAG, String.format("run: 获取的服务 : %s",String.valueOf(bluetoothGattService)));
                            if (bluetoothGattService != null) {
                                bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(Constant.mCharacteristicUUID);
                                /* 获取指定特征值成功 */
                                if (bluetoothGattCharacteristic != null) {
                                    //通过Gatt对象读取特定特征（Characteristic）的特征值
                                    mBluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
                                }
                            } else {
                                Log.i(TAG, "run: 获取的服务为空");
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
                    Log.i(TAG, String.format("onCharacteristicRead: 获取的characteristic值：%s", new String(characteristic.getValue())));
                    /* 读取成功之后 向其写数据 */
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //写入你需要传递给外设的特征值（即传递给外设的信息）
                            bluetoothGattCharacteristic.setValue(Constant.CHARACTERISTIC_DATA);
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
                    /*重新开启扫描*/
                    gatt.disconnect();
                    gatt.close();
                    startScan();
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

    public void connectBLE(String bleAddress){
        //定义重连次数
        reConnectionNum = 0;
        //最多重连次数
        maxConnectionNum = 3;
        Log.i(TAG, "connectBLE: 开始连接新设备：MAC "+ bleAddress);
        // 初始化handler
        //定义handler工作的子线程
        HandlerThread mHandlerThread = new HandlerThread("connect");
        mHandlerThread.start();
        //将handler绑定到子线程中
        mHandler = new Handler(mHandlerThread.getLooper());
        /* 获取的蓝牙设备实体 */
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bleAddress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = bluetoothDevice.connectGatt(ScanService.this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = bluetoothDevice.connectGatt(ScanService.this, false, bluetoothGattCallback);
        }
    }

    private void startScan() {
        /* 停止上次的连接再开启扫描*/
        showToast("开启扫描");
        Log.i(TAG, "startScan: 开始扫描");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*api 21 以上的扫描方式*/
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//            ScanSettings scanSettings;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
/*                scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                        .setReportDelay(10000).build();
                List<ScanFilter> filters = new ArrayList<>();
                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(Constant.PARCEL_UUID_1).build();
                filters.add(filter);*/
                bluetoothLeScanner.startScan(scanCallback);
            } else {
/*                scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .setReportDelay(10000).build();*/
                bluetoothLeScanner.startScan(scanCallback);
            }
        } else {
            /*api 21 以下的扫描方式*/
            bluetoothAdapter.startLeScan(leScanCallback);
        }
    }

    /**
     * 关闭扫描
     */
    public void stopScan(){
        showToast("关闭扫描");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        } else {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        }
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
}
