package com.example.myble.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myble.R;
import com.example.myble.adapter.MyAdapter;
import com.example.myble.databinding.ActivityShowResultBinding;
import com.example.myble.room.Word;
import com.example.myble.viewmodel.WordViewModel;

import java.util.List;

public class show_result_activity extends AppCompatActivity {
    ActivityShowResultBinding binding;
    private WordViewModel wordViewModel;

    RecyclerView recyclerView;
    MyAdapter myAdapter1, myAdapter2 ;

    Switch aSwitch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 绑定ViewModel */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_result);
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        binding.setLifecycleOwner(this);

        /* 实现循环列表 adapter 提供数据给list */
        recyclerView = findViewById(R.id.recyclerView);
        myAdapter1 = new MyAdapter(false, wordViewModel);
        myAdapter2 = new MyAdapter(true, wordViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        aSwitch = findViewById(R.id.switch1);

        /* 显示默认 adapter */
        recyclerView.setAdapter(myAdapter1);
        /* 根据switch显示list*/
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recyclerView.setAdapter(myAdapter2);
                } else {
                    recyclerView.setAdapter(myAdapter1);
                }
            }
        });

        /* 检测到数据更新 渲染到界面 */
        wordViewModel.getAllWordsLive().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                myAdapter1.setAllWords(words);
                myAdapter2.setAllWords(words);
                myAdapter1.notifyDataSetChanged();
                myAdapter2.notifyDataSetChanged();
            }
        });

        /* 往Model中添加数据 */
        findViewById(R.id.insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Word word1 = new Word("hello", "您好");
//                Word word2 = new Word("world", "世界");
                String[] english = {
                        "Hello",
                        "World",
                        "Android",
                        "Google",
                        "Studio",
                        "Project",
                        "Database",
                        "Recycler",
                        "View",
                        "String",
                        "Value",
                        "Integer"
                };
                String[] chinese = {
                        "你好",
                        "世界",
                        "安卓系统",
                        "谷歌公司",
                        "工作室",
                        "项目",
                        "数据库",
                        "回收站",
                        "视图",
                        "字符串",
                        "价值",
                        "整数类型"
                };
                for (int i = 0; i < english.length; i++) {
                    wordViewModel.insertWords(new Word(english[i], chinese[i]));
                }
            }
        });

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordViewModel.clearAllWords();
            }
        });

        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Word word = new Word("helloo", "ssahohv");
                word.setId(32);
                wordViewModel.updateWords(word);
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Word word = new Word("asih","sh");
                word.setId(34);
                wordViewModel.delettWords(word);
            }
        });

    }
}
