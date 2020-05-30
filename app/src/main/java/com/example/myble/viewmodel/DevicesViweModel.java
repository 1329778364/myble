package com.example.myble.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.myble.room.BleDevice;

import java.util.List;

public class DevicesViweModel extends ViewModel {
    public LiveData<List<BleDevice>> Devices;

    public DevicesViweModel(LiveData<List<BleDevice>> devices) {
        Devices = devices;
    }

    public LiveData<List<BleDevice>> getDevices() {
        return Devices;
    }

    public void setDevices(LiveData<List<BleDevice>> devices) {
        Devices = devices;
    }
}
