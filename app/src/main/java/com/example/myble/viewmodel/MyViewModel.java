package com.example.myble.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import java.util.Timer;

public class MyViewModel extends AndroidViewModel {
    SavedStateHandle savedStateHandle;
    public MutableLiveData<Integer> number ;
    private Timer timer = new Timer();
    private long time = 1000;

    public MyViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;
    }

    public MutableLiveData<Integer> getNumber() {
        if (number == null) {
            number = new MutableLiveData<>();
            number.setValue(10);
        }
        return number;
    }
    public void add(){
        number.setValue(number.getValue() + 1);
    }
}
