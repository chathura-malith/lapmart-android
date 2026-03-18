package com.metkring.lapmart.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.BrandAdapter;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.databinding.ActivityMainBinding;
import com.metkring.lapmart.fragment.CartFragment;
import com.metkring.lapmart.fragment.CategoryFragment;
import com.metkring.lapmart.fragment.HomeFragment;
import com.metkring.lapmart.fragment.SignInFragment;
import com.metkring.lapmart.fragment.SignUpFragment;
import com.metkring.lapmart.fragment.UserFragment;
import com.metkring.lapmart.model.Product;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
            //home logic here
            loadFragment(new HomeFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        }
        else if (id == R.id.bottom_nav_category) {
            loadFragment(new CategoryFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_category).setChecked(true);
        }
        else if (id == R.id.bottom_nav_you) {
            loadFragment(new UserFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_you).setChecked(true);
        }
        else if (id == R.id.bottom_nav_cart) {
            loadFragment(new CartFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_cart).setChecked(true);
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