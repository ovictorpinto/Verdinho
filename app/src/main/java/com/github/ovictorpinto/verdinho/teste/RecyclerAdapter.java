package com.github.ovictorpinto.verdinho.teste;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ovictorpinto.verdinho.R;

import java.util.List;

/**
 * Created by obaro on 10/04/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mItemList;

    public RecyclerAdapter(List<String> itemList) {
        mItemList = itemList;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ly_teste_item, parent, false);
        return RecyclerItemViewHolder.newInstance(view);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecyclerItemViewHolder holder = (RecyclerItemViewHolder) viewHolder;
        String itemText = mItemList.get(position);
        holder.setItemText(itemText);
    }
    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

}