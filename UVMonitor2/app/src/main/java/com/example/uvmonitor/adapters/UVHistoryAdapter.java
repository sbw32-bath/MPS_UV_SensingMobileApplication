package com.example.uvmonitor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uvmonitor.R;
import com.example.uvmonitor.database.UVDataEntity;

import java.util.ArrayList;
import java.util.List;

public class UVHistoryAdapter extends RecyclerView.Adapter<UVHistoryAdapter.ViewHolder> {

    private List<UVDataEntity> dataList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UVDataEntity item);
    }

    public UVHistoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<UVDataEntity> data) {
        this.dataList = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UVHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uvhistory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UVHistoryAdapter.ViewHolder holder, int position) {
        UVDataEntity item = dataList.get(position);
        holder.date.setText(item.date);
        holder.uvValue.setText("Peak UV: " + item.uvValue);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, uvValue;

        ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.history_date);
            uvValue = itemView.findViewById(R.id.history_value);
        }
    }
}
