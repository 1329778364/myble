package com.example.myble.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.myble.R;
import com.example.myble.databinding.ActivityUseViewmodelBinding;
import com.example.myble.viewmodel.MyViewModel;

class use_viewmodel_activity extends AppCompatActivity  {
    ActivityUseViewmodelBinding binding;
    private MyViewModel myViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_use_viewmodel);
        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
//        binding.getLifecycleOwner();


    }
}
