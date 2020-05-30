package com.example.myble.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.myble.R;

/**
 * 使用ViewModel LiveData DataBinding SharedPreferences  savedState实现数据的存储
 * 将数据较为永久保存 用户开关机之后依旧能够刦到数据，但是卸载之后无法读取数据
 * */
public class DataBindingShPViewModel extends AndroidViewModel {
    SavedStateHandle savedStateHandle;
    /* 获取资源key值 */
    String key = getApplication().getResources().getString(R.string.data_key);
    String shpName = getApplication().getResources().getString(R.string.shp_name);

    public DataBindingShPViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;
        if (!this.savedStateHandle.contains(key)) {
            load();
        }
    }

    public LiveData<Integer> getNumber() {
        return savedStateHandle.getLiveData(key);
    }

    public void load() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(shpName, Context.MODE_PRIVATE);
        int x = sharedPreferences.getInt(key, 10);
        savedStateHandle.set(key, x);
    }

    public void save() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(shpName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, getNumber().getValue());
        editor.apply();
    }

    public void add(int x) {
        savedStateHandle.set(key, getNumber().getValue() + x);
    }
}
