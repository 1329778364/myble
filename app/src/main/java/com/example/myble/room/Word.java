package com.example.myble.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "english_world")
    private String word;

    @ColumnInfo(name = "chineseMeaning")
    private String chineseMeaning;

    @ColumnInfo(name = "chinese_invisible")
    public Boolean chinese_invisible = true;

    public Word(String word, String chineseMeaning) {
        this.word = word;
        this.chineseMeaning = chineseMeaning;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getChineseMeaning() {
        return chineseMeaning;
    }

    public void setChineseMeaning(String chineseMeaning) {
        this.chineseMeaning = chineseMeaning;
    }

    public Boolean getChinese_invisible() {
        return chinese_invisible;
    }

    public void setChinese_invisible(Boolean chinese_invisible) {
        this.chinese_invisible = chinese_invisible;
    }
}
