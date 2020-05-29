package com.example.myble.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myble.room.Word;
import com.example.myble.room.WordRepository;

import java.util.List;

public class WordViewModel extends AndroidViewModel {

    private WordRepository wordRepository;

    public WordViewModel(@NonNull Application application) {
        super(application);
        wordRepository = new WordRepository(application);
    }

    public LiveData<List<Word>> getAllWordsLive() {
        return wordRepository.getAllWordsLive();
    }

    public void insertWords(Word... words) {
        wordRepository.insertWords(words);
    }
    public void delettWords(Word... words) {
        wordRepository.delettWords(words);
    }
    public void updateWords(Word... words) {
        wordRepository.updateWords(words);
    }
    public void clearAllWords() {
        wordRepository.clearAllWords();
    }

}
