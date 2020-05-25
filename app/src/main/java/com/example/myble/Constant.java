package com.example.myble;

import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Constant {
    public static final String AdvName = "Con";

    /*--------------- 广播参数设置 -------------------------*/
    public static final ParcelUuid PARCEL_UUID_1 = ParcelUuid.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVICE_UUID_1 = UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID_1 = UUID.fromString("0000ccc1-0000-1000-8000-00805f9b34fb");
    /*用于通知 notify的专属ID*/
    public static final UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /*自定义厂商数据*/
    public static final int MANUFACTURER_ID = 0xACAC;
    /*自定义广播数据 与厂商数据一起构成 Advertise data  */
    public static byte[] BROADCAST_DATA = "2020".getBytes();
    /*广播时service 携带的数据*/
    public static byte[] send_data = "1234".getBytes();

    /* ----------------- CHARACTERISTIC_DATA -----------------  */
    /* 设置本机 Characteristic 数据 给其他设备访问 在这里可以设置自己的 唯一ID */
    public static byte[] CHARACTERISTIC_DATA = "987654321".getBytes(); /* 会被转为10进制数组 */

    /* -----------------扫描到的设备-------------------------- */
    public static List<String> scanDeviceAddressList; /*地址列表*/
    public static List<String> scanDeviceAddressList_other; /*地址列表*/

    public static ArrayList<String> arrayList;
    /*--------------连接相关---------------------*/
    // 定义需要进行通信的ServiceUUID
    public static UUID mServiceUUID = UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");
    // 定义需要进行通信的CharacteristicUUID
    public static UUID mCharacteristicUUID = UUID.fromString("0000ccc1-0000-1000-8000-00805f9b34fb");

    public static List<String> getScanDeviceAddressList() {
        return scanDeviceAddressList;
    }

    public static void addScanDeviceAddress(String address) {
        scanDeviceAddressList.add(address);
    }

    public static void setScanDeviceAddressList(List<String> scanDeviceAddressList) {
        Constant.scanDeviceAddressList = scanDeviceAddressList;
    }

    public static List<String> getScanDeviceAddressList_other() {
        return scanDeviceAddressList_other;
    }

    public static void setScanDeviceAddressList_other(List<String> scanDeviceAddressList_other) {
        Constant.scanDeviceAddressList_other = scanDeviceAddressList_other;
    }



}
