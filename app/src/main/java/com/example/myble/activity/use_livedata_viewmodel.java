package com.example.myble.activity;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.myble.R;
import com.example.myble.databinding.ActivityUseLivedataViewmodelBinding;
import com.example.myble.viewmodel.UseLiveDataViewModel;

/**
 * 使用 Livedata databinding ViewModel 实现数据的绑定
 * 并实现数据的临时保存 在应用没有被清理的情况下 数据是不会被重新被覆盖
 * */
public class use_livedata_viewmodel extends AppCompatActivity {

    private ActivityUseLivedataViewmodelBinding binding;
    private UseLiveDataViewModel useLiveDataViewModel;

    private String key_number = "my_data";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_use_livedata_viewmodel);
        useLiveDataViewModel = new ViewModelProvider(this).get(UseLiveDataViewModel.class);
        binding.setData(useLiveDataViewModel);
        binding.setLifecycleOwner(this);

        /* 读取上次存储的数据 */
        if (savedInstanceState != null) {
            useLiveDataViewModel.getNumber().setValue(savedInstanceState.getInt(key_number));
        }
    }

    /**
     * 进程被杀死之后，需要将数据 重新读出 利用 ViewModelStateSave实现
     * 在应用摧毁 时将数据保存
     * */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(key_number, useLiveDataViewModel.getNumber().getValue());
    }
}
