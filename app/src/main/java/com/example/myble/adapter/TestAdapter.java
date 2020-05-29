package com.example.myble.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.myble.R;

import java.util.List;

class TestAdapter extends Adapter<TestAdapter.MyViewHolder> {
    public List<String> mDataList;
    private LayoutInflater layoutInflater;
    private View itemview;

    public TestAdapter(List<String> mDataList) {
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(parent.getContext());
        itemview = layoutInflater.inflate(R.layout.recycleview_item, parent, false);
        return new MyViewHolder(itemview);
//        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(mDataList.get(position));

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     * viewholder 基本容器 每一个item的内容
     *
     * */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

}
