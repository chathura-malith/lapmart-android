package com.metkring.lapmartadmin.activity;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.databinding.ActivityMainBinding;
import com.metkring.lapmartadmin.fragment.AddProductFragment;
import com.metkring.lapmartadmin.fragment.HomeFragment;
import com.metkring.lapmartadmin.fragment.OrderFragment;
import com.metkring.lapmartadmin.fragment.StockFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView=binding.bottomNavigationView;
        drawerLayout=binding.drawerLayout;

        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Menu bottomNavMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < bottomNavMenu.size(); i++) {
            bottomNavMenu.getItem(i).setChecked(false);
        }

        if ( id == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        }
        else if (id == R.id.bottom_nav_add) {
            loadFragment(new AddProductFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_add).setChecked(true);
        }
        else if (id == R.id.bottom_nav_stock) {
            loadFragment(new StockFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_stock).setChecked(true);
        }
        else if (id == R.id.bottom_nav_order) {
            loadFragment(new OrderFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_order).setChecked(true);
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}