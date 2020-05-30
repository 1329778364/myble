package com.example.myble.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myble.R;
import com.example.myble.adapter.TestAdapter;
import com.example.myble.databinding.CreateCycleviewActivityBinding;
import com.example.myble.room.BleDevice;
import com.example.myble.viewmodel.DevicesViweModel;

import java.util.ArrayList;
import java.util.List;

public class create_cyclerview_activity extends AppCompatActivity {

    private List<BleDevice> mdataList;
    private RecyclerView recyclerView;
    TestAdapter testAdapter;
    private BleDevice bleDevice;
    CreateCycleviewActivityBinding binding;
    private DevicesViweModel devicesViweModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.create_cycleview_activity);
        devicesViweModel = new ViewModelProvider(this).get(DevicesViweModel.class);
        binding.setData(devicesViweModel);
        binding.setLifecycleOwner(this);

        initData();

        initView();

        devicesViweModel.getDevices().observe(this, new Observer<List<BleDevice>>() {
            @Override
            public void onChanged(List<BleDevice> bleDevices) {
                testAdapter = new TestAdapter(bleDevices);
                testAdapter.notifyDataSetChanged();
            }
        });

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

    public void initView() {
        recyclerView = findViewById(R.id.recyclerView_test);
        testAdapter = new TestAdapter(mdataList);

//        testAdapter.setHasStableIds(true);
        recyclerView.setAdapter(testAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
