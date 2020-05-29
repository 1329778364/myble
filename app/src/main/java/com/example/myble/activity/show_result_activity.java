package com.example.myble.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myble.R;
import com.example.myble.databinding.ActivityShowResultBinding;
import com.example.myble.room.Word;
import com.example.myble.viewmodel.WordViewModel;

import java.util.List;

public class show_result_activity extends AppCompatActivity {
    TextView textView ;
    ActivityShowResultBinding binding;
    private WordViewModel wordViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_result);
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        binding.setLifecycleOwner(this);

        textView = findViewById(R.id.display);
        wordViewModel.getAllWordsLive().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < words.size(); i++) {
                    Word word = words.get(i);
                    text.append(word.getId()).append(":").append(word.getWord()).append("=").append(word.getChineseMeaning()).append("\n");
                }
                textView.setText(text.toString());
            }
        });
        findViewById(R.id.insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Word word1 = new Word("hello", "您好");
                Word word2 = new Word("world", "世界");
                wordViewModel.insertWords(word1, word2);
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
