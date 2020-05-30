package com.example.myble.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myble.room.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class DevicesViweModel extends AndroidViewModel {
    BleDevice bleDevice = new BleDevice("王立强",
            "12:23:32:34:45", "-16", "3123",
            "0000ccc0-0000-1000-8000-00805f9b34fb", true);
    /*TODO 没有解决 LiveData的显示问题*/
    public MutableLiveData<List<BleDevice>> Devices;
    private List<BleDevice> mdataList;

    public DevicesViweModel(@NonNull Application application) {
        super(application);
        initData();
        this.Devices = new MutableLiveData<>();
        this.Devices.setValue(mdataList);
    }

    public MutableLiveData<List<BleDevice>> getDevices() {
        return Devices;
    }

    public void initData(){
        mdataList = new ArrayList<>();
        for (int i = 1; i < 20; i++) {
            bleDevice = new BleDevice("王立强",
                    "12:23:32:34:45", "-16", "3123",
                    "0000ccc0-0000-1000-8000-00805f9b34fb", i % 3 == 0);
            mdataList.add(bleDevice);
        }
    }

    public void add() {
        Devices.getValue().add(bleDevice);
    }
}
