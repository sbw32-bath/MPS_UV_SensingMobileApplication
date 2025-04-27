package com.example.uvmonitor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.uvmonitor.fragments.DashboardFragment;
import com.example.uvmonitor.fragments.HomeFragment;
import com.example.uvmonitor.fragments.NotificationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up the default fragment to be displayed
        loadFragment(new HomeFragment());

        // Set the Bottom Navigation listener to switch between fragments
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if(item.getItemId() == R.id.navigation_home){
                selectedFragment = new HomeFragment();
            }else if(item.getItemId() == R.id.navigation_dashboard){
                selectedFragment = new DashboardFragment();
            }else if(item.getItemId() == R.id.navigation_notifications){
                selectedFragment = new NotificationFragment();
            }

            // Load the selected fragment dynamically
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    // Method to load a fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
