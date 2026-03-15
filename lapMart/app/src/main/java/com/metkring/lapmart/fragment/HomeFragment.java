package com.metkring.lapmart.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.BrandAdapter;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.databinding.FragmentHomeBinding;
import com.metkring.lapmart.model.Product;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private BrandAdapter brandAdapter;
    private List<String> brands;
    private GoogleMap mMap;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        brands = new ArrayList<>();
        loadBrand(view);

        call();
        loadMap(view);
        loadProduct(view);

    }

    private void loadBrand(View view) {
        brands.add("All");
        brands.add("Dell");
        brands.add("Hp");
        brands.add("Acer");
        brands.add("Lenovo");

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.brandRecyclerView.setLayoutManager(layoutManager);
        brandAdapter = new BrandAdapter(brands);
        binding.brandRecyclerView.setAdapter(brandAdapter);
    }

    private void loadMap(View view) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void call() {
        ImageButton callBtn = binding.branchCallBtn;

        callBtn.setOnClickListener(v -> {
            String phoneNumber = "0371234567";

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
    }

    private void loadProduct(View view) {
        // RecyclerView එක හඳුනා ගැනීම
        RecyclerView productRv = binding.productRecyclerView;

        List<Product> productList = new ArrayList<>();
        productList.add(new Product("MSI Cyborg Gaming 15 AI A1VEK", 440000.0, R.drawable.msi_laptop));
        productList.add(new Product("Asus Vivobook S 15 S5507", 459500.0, R.drawable.msi_laptop));
        productList.add(new Product("MSI Cyborg Gaming 15 AI A1VEK", 440000.0, R.drawable.msi_laptop));
        productList.add(new Product("Asus Vivobook S 15 S5507", 459500.0, R.drawable.msi_laptop));

        productRv.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

        ProductAdapter adapter = new ProductAdapter(productList);
        productRv.setAdapter(adapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng nikaweratiya = new LatLng(7.7494, 80.1174);

        mMap.addMarker(new MarkerOptions().position(nikaweratiya).title("LapMart - Nikaweratiya Branch"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nikaweratiya, 15f));

        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}