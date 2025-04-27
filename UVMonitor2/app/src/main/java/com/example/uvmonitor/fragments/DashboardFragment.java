package com.example.uvmonitor.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.uvmonitor.R;
import com.example.uvmonitor.UVDetailActivity;
import com.example.uvmonitor.adapters.UVSummaryAdapter;
import com.example.uvmonitor.database.UVDatabase;
import com.example.uvmonitor.database.UVSummary;

import java.util.List;

public class DashboardFragment extends Fragment implements UVSummaryAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private UVSummaryAdapter adapter;
    private UVDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        emptyText = root.findViewById(R.id.empty_text);
        database = UVDatabase.getInstance(requireContext());

        adapter = new UVSummaryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadSummaryData();

        return root;
    }

    private void loadSummaryData() {
        database.uvDataDao().getSummarizedLast7Days().observe(getViewLifecycleOwner(), new Observer<List<UVSummary>>() {
            @Override
            public void onChanged(List<UVSummary> summaries) {
                if (summaries.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);
                    adapter.submitList(summaries);
                }
            }
        });
    }

    @Override
    public void onItemClick(String date) {
        Intent intent = new Intent(getContext(), UVDetailActivity.class);
        intent.putExtra("date", date);
        startActivity(intent);
    }
}
