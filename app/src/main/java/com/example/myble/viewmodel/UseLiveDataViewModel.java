package com.example.myble.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

@SuppressWarnings("ConstantConditions")
public class UseLiveDataViewModel extends ViewModel {
    public MutableLiveData<Integer> number;

    public UseLiveDataViewModel() {
        this.number = new MutableLiveData<>();
        this.number.setValue(100);
    }

    public MutableLiveData<Integer> getNumber() {
        return number;
    }

    public void addNumber(int x){
        this.number.setValue(this.number.getValue() + x);
    }
}
