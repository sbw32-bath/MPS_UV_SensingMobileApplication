package com.example.uvmonitor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uvmonitor.R;
import com.example.uvmonitor.database.UVSummary;

import java.util.ArrayList;
import java.util.List;

public class UVSummaryAdapter extends RecyclerView.Adapter<UVSummaryAdapter.ViewHolder> {

    private List<UVSummary> summaryList = new ArrayList<>();
    private OnItemClickListener listener;

    public UVSummaryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String date);
    }

    public void submitList(List<UVSummary> summaries) {
        this.summaryList = summaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UVSummaryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_uv_summary, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UVSummaryAdapter.ViewHolder holder, int position) {
        UVSummary summary = summaryList.get(position);
        holder.dateText.setText(summary.date);
        holder.highText.setText("High: " + summary.maxUv);
        holder.lowText.setText("Low: " + summary.minUv);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(summary.date));
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, highText, lowText;

        ViewHolder(View view) {
            super(view);
            dateText = view.findViewById(R.id.date_text);
            highText = view.findViewById(R.id.high_text);
            lowText = view.findViewById(R.id.low_text);
        }
    }
}
