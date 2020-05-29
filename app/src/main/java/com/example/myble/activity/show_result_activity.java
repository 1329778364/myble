package com.example.myble.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
    TextView textView ;
    ActivityShowResultBinding binding;
    private WordViewModel wordViewModel;

    RecyclerView recyclerView;
    MyAdapter myAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_result);
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);
        binding.setLifecycleOwner(this);

        recyclerView = findViewById(R.id.recyclerView);
        myAdapter = new MyAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapter);

        wordViewModel.getAllWordsLive().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                myAdapter.setAllWords(words);
                myAdapter.notifyDataSetChanged();
            }
        });
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
