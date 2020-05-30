package com.example.myble.room;

/**
 * 蓝牙设备基本信息类
* */
public class BleDevice {
    public String deviceName;
    public String deviceMac;
    public String deviceRssi;
    public String deviceAdData;
    public String deviceServiceId;
    public Boolean connected;

    public BleDevice(String deviceName, String deviceMac, String deviceRssi, String deviceAdData, String deviceServiceId, Boolean connected) {
        this.deviceName = deviceName;
        this.deviceMac = deviceMac;
        this.deviceRssi = deviceRssi;
        this.deviceAdData = deviceAdData;
        this.deviceServiceId = deviceServiceId;
        this.connected = connected;
    }

    public String toString(){
        return "Name:" + this.deviceName + "Mac:" + "serviceId:" + this.deviceServiceId + this.deviceMac + "Rssi" + this.deviceRssi + "AdData" + this.deviceAdData + "connected:" + String.valueOf(this.connected) + "\n";
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getDeviceRssi() {
        return deviceRssi;
    }

    public void setDeviceRssi(String deviceRssi) {
        this.deviceRssi = deviceRssi;
    }

    public String getDeviceAdData() {
        return deviceAdData;
    }

    public void setDeviceAdData(String deviceAdData) {
        this.deviceAdData = deviceAdData;
    }

    public String getDeviceServiceId() {
        return deviceServiceId;
    }

    public void setDeviceServiceId(String deviceServiceId) {
        this.deviceServiceId = deviceServiceId;
    }
}
