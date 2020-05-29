package com.example.myble.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myble.R;

import java.util.ArrayList;
import java.util.List;

public class create_cyclerview_activity extends AppCompatActivity {

    private List<String> mdataList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_cycleview_activity);

        initData();
        initView();

    }

    public void initData(){
        mdataList = new ArrayList<>();
        for (int i = 65; i < 98; i++) {
            mdataList.add("" + (char) i);
        }
    }

    public void initView() {
        recyclerView = findViewById(R.id.recyclerView_test);


    }
}
