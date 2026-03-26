package com.metkring.lapmart.activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.metkring.lapmart.R;

import com.metkring.lapmart.databinding.ActivityMainBinding;
import com.metkring.lapmart.fragment.CartFragment;
import com.metkring.lapmart.fragment.CategoryFragment;
import com.metkring.lapmart.fragment.HomeFragment;
import com.metkring.lapmart.fragment.ProductDetailFragment;
import com.metkring.lapmart.fragment.UserFragment;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d("FCM", "Permission Granted");
                        } else {
                            Toast.makeText(this, "Notification permission denied!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView=binding.bottomNavigationView;
        drawerLayout=binding.drawerLayout;

        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        checkAndRequestPermission();


        FirebaseMessaging.getInstance().subscribeToTopic("new_products")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Successfully subscribed to new_products topic!");
                    } else {
                        Log.e("FCM", "Subscription failed!", task.getException());
                    }
                });


        handleNotificationIntent(getIntent());

        if (savedInstanceState == null && (getIntent() == null ||
                !getIntent().hasExtra("productId"))) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("productId")) {
            String incomingProductId = intent.getStringExtra("productId");
            if (incomingProductId != null && !incomingProductId.isEmpty()) {
                Log.d("FCM", "Notification Tapped! Product ID: " + incomingProductId);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();

                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
                }

                Bundle bundle = new Bundle();
                bundle.putString("product_id", incomingProductId);
                ProductDetailFragment detailFragment = new ProductDetailFragment();
                detailFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
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