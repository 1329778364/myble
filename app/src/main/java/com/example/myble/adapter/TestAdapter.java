package com.example.myble.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.myble.R;
import com.example.myble.room.BleDevice;

import java.util.List;

public class TestAdapter extends Adapter<TestAdapter.MyViewHolder> {
    List<BleDevice> mDataList;
    private LayoutInflater layoutInflater;
    private View itemview;
    private String TAG = "adapter";
    private BleDevice bleDeviceItem;

    public TestAdapter(List<BleDevice> mDataList) {
        this.mDataList = mDataList;
    }

        public void setmDataList(List<BleDevice> mDataList) {
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(parent.getContext());
        itemview = layoutInflater.inflate(R.layout.recycleview_item, parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        bleDeviceItem = mDataList.get(position);
        Log.i(TAG, "onBindViewHolder: position:" + String.valueOf(position) + "item: " + bleDeviceItem.toString());
        holder.position.setText(String.valueOf(position));
        holder.deviceName.setText(bleDeviceItem.getDeviceName());
        holder.deviceMac.setText(bleDeviceItem.getDeviceMac());
        holder.deviceAdData.setText(bleDeviceItem.getDeviceAdData());
        holder.deviceServiceId.setText(bleDeviceItem.getDeviceServiceId());
        holder.deviceRssi.setText(bleDeviceItem.getDeviceRssi());
        holder.connected.setOnCheckedChangeListener(null);

        if (bleDeviceItem.getConnected()) {
            holder.connected.setChecked(true);
        } else {
            holder.connected.setChecked(false);
        }
// TODO 重复显示 影响没有解决
        holder.connected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bleDeviceItem.setConnected(true);
                    Log.i(TAG, "onCheckedChanged: position：" + String.valueOf(position));
                    Log.i(TAG, "onCheckedChanged: ble:" + bleDeviceItem.toString());
                } else {
                    bleDeviceItem.setConnected(false);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     * viewholder 基本容器 每一个item的内容
     *
     * */

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private  TextView deviceName;
        private  TextView deviceServiceId;
        private  TextView deviceAdData;
        private  TextView deviceMac;
        private  TextView deviceRssi;
        private  TextView position;
        private  Switch connected;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            position = itemView.findViewById(R.id.position);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceServiceId = itemView.findViewById(R.id.deviceServiceId);
            deviceAdData = itemView.findViewById(R.id.deviceAdData);
            deviceMac = itemView.findViewById(R.id.deviceMac);
            deviceRssi = itemView.findViewById(R.id.deviceRssi);
            connected = itemView.findViewById(R.id.connected);
        }
    }
}
