package com.example.myble.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Timer;

public class MyViewModel extends ViewModel {
    public MutableLiveData<Integer> number ;
    private Timer timer = new Timer();
    private long time = 1000;

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
