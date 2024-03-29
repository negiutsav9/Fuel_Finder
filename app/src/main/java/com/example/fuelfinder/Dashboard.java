package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        //Setting up action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_dashboard);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        getSupportActionBar().setElevation(0);


        //Calling the ViewPager
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(this));
        //Tab Layout Arrangement
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch(position) {
                            case 0:
                                tab.setText("Logs");
                                break;
                            case 1:
                                tab.setText("Statistics");
                                break;
                        }
                    }
                }).attach();


        // Initialize and assign variable
        NavigationBarView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.dashboard);


        bottomNavigationView.setOnItemSelectedListener( new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.dashboard:
                        return true;
                    case R.id.user:
                        startActivity(new Intent(getApplicationContext(),User.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        ImageView addLog = getSupportActionBar().getCustomView().findViewById(R.id.add_log);
        addLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddLogActivity.class));
            }
        });
    }
}