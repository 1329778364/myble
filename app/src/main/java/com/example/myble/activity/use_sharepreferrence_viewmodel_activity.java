package com.example.myble.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.myble.R;
import com.example.myble.databinding.ActivityUseViewmodelBinding;
import com.example.myble.viewmodel.DataBindingShPViewModel;

/**
 * 使用 ViewModel sharePreferrence 对数据进行存储 使得用户再次进入时 还可以看到数据
 * 即使是重新重启也能看到数据
 * */
public class use_sharepreferrence_viewmodel_activity extends AppCompatActivity  {
    ActivityUseViewmodelBinding binding;
    public DataBindingShPViewModel myViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_use_viewmodel);
        myViewModel = new ViewModelProvider(this).get(DataBindingShPViewModel.class);
        binding.setData(myViewModel);
        binding.setLifecycleOwner(this);
    }

    /**
     * 应用程序到后台时 对数据进行保存
     * */
    @Override
    protected void onPause() {
        super.onPause();
        myViewModel.save();
    }
}
