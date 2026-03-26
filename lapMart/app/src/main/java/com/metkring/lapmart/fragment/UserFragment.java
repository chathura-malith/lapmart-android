package com.metkring.lapmart.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.databinding.FragmentUserBinding;
import com.metkring.lapmart.helper.CartManager;
import com.metkring.lapmart.model.Product;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadImageToFirebase();
                    }
                }
            }
    );


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        productList = new ArrayList<>();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUserStatus();
        backOption();
        loadProduct(view);
        binding.btnGoToLogin.setOnClickListener(v -> loadFragment(new SignInFragment()));
        binding.btnLogOutIcon.setOnClickListener(v -> logoutUser());
        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
        binding.btnAddress.setOnClickListener(v-> loadFragment(new AddAddressFragment()));
        binding.btnOrders.setOnClickListener(v-> loadFragment(new OrderFragment()));
    }

    private void uploadImageToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || imageUri == null) return;

        StorageReference fileRef = storageRef.child("users/" + user.getUid() + ".jpg");

        setLoading(true);
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateFirestoreUrl(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toasty.error(requireContext(), "Upload failed").show();
                });
    }

    private void updateFirestoreUrl(String url) {
        db.collection("users").document(mAuth.getUid())
                .update("profileImage", url)
                .addOnSuccessListener(aVoid -> {
                    Glide.with(requireContext()).load(url).into(binding.profileImage);
                    Toasty.success(requireContext(), "Profile Updated!").show();
                    setLoading(false);
                });
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            binding.layoutLoggedIn.setVisibility(View.VISIBLE);
            binding.layoutLoggedOut.setVisibility(View.GONE);
            fetchUserDetails(currentUser.getUid());
            new CartManager(requireContext()).syncLocalCartToFirebase(requireContext());
        } else {
            binding.layoutLoggedIn.setVisibility(View.GONE);
            binding.layoutLoggedOut.setVisibility(View.VISIBLE);
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toasty.info(requireContext(), "Logged Out!").show();
        checkUserStatus();
    }

    private void fetchUserDetails(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("firstName");
                        String imageUrl = documentSnapshot.getString("profileImage");

                        binding.userName.setText("Hi, " + name);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_user)
                                    .into(binding.profileImage);
                        }else{
                            binding.profileImage.setImageResource(R.drawable.default_user);
                        }
                    }
                });
    }


    private void loadFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadProduct(View view) {
        setLoading(true);
        RecyclerView productRv = binding.productRecyclerView;
        productRv.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

        adapter = new ProductAdapter(productList);
        productRv.setAdapter(adapter);

        db.collection("products").orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    setLoading(false);
                    productList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Product product = document.toObject(Product.class);

                        if (product != null) {
                            product.setId(document.getId());
                            productList.add(product);
                        }
                    }
                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("FirestoreError", "Error getting documents: " + e.getMessage());
                });
    }

    private void backOption(){
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);

                    if (bottomNav.getSelectedItemId() != R.id.bottom_nav_home) {
                        bottomNav.setSelectedItemId(R.id.bottom_nav_home);
                    } else {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                }
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.homeProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.homeProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }
}