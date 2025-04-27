package com.example.uvmonitor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uvmonitor.R;
import com.example.uvmonitor.adapters.NotificationAdapter;
import com.example.uvmonitor.utils.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.notification_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dummy notifications
        notificationList = new ArrayList<>();
        notificationList.add(new NotificationItem("Bluetooth is OFF", "Please enable Bluetooth to continue."));
        notificationList.add(new NotificationItem("Device Connected", "Successfully paired with UV Sensor."));
        notificationList.add(new NotificationItem("UV Reading", "UV Intensity: 5.2"));

        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        return view;
    }
}
