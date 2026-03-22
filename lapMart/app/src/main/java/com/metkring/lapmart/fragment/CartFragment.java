package com.metkring.lapmart.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.CartAdapter;
import com.metkring.lapmart.databinding.FragmentCartBinding;
import com.metkring.lapmart.helper.CartDbHelper;
import com.metkring.lapmart.model.CartItem;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CartFragment extends Fragment implements CartAdapter.OnCartActionListener {

    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private CartDbHelper dbHelper;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(inflater, container, false);
        dbHelper = new CartDbHelper(requireContext());
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideBottomNavigation();
        backOption();

        setupRecyclerView();
        loadCartData();
        updateTotalPrice();

        binding.btnBack.setOnClickListener(v -> navigateToHome());
        binding.btnShopNow.setOnClickListener(v -> navigateToHome());

        binding.btnCheckOut.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toasty.warning(requireContext(), "Please Sign to checkout", Toast.LENGTH_SHORT).show();
                SignInFragment signInFragment = new SignInFragment();
                Bundle args = new Bundle();
                args.putBoolean("fromCart", true);
                signInFragment.setArguments(args);

                changeFragment(signInFragment);
            } else {
                validateStockAndProceed();
            }
        });
    }

    private void changeFragment(Fragment fragment) {
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

    private void validateStockAndProceed() {
        binding.cartProgressBar.setVisibility(View.VISIBLE);
        binding.btnCheckOut.setEnabled(false);

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (CartItem item : cartItemList) {
            Task<DocumentSnapshot> task = db.collection("products")
                    .document(item.getProductId()).get();
            tasks.add(task);
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            boolean allInStock = true;
            String outOfStockItemName = "";
            Long availableStock = 0L;

            for (int i = 0; i < objects.size(); i++) {
                DocumentSnapshot doc = (DocumentSnapshot) objects.get(i);
                CartItem cartItem = cartItemList.get(i);

                if (doc.exists()) {
                    availableStock = doc.getLong("qty");

                    if (availableStock == null || cartItem.getQuantity() > availableStock) {
                        allInStock = false;
                        outOfStockItemName = cartItem.getProductName();
                        break;
                    }
                } else {
                    allInStock = false;
                    outOfStockItemName = cartItem.getProductName() + " (Not Found)";
                    break;
                }
            }

            binding.cartProgressBar.setVisibility(View.GONE);
            binding.btnCheckOut.setEnabled(true);

            if (allInStock) {
                changeFragment(new CheckoutFragment());
            } else {
                Toasty.error(requireContext(),
                        "Not enough stock for: " + outOfStockItemName + "we have only "
                                + availableStock + " in stock", Toast.LENGTH_LONG).show();
            }

        }).addOnFailureListener(e -> {
            binding.cartProgressBar.setVisibility(View.GONE);
            binding.btnCheckOut.setEnabled(true);
            Toasty.error(requireContext(), "Failed to verify stock",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCartData() {
        binding.cartProgressBar.setVisibility(View.VISIBLE);

        if (mAuth.getCurrentUser() != null) {
            fetchCloudCart();
        } else {
            fetchLocalCart();
        }
    }

    private void fetchLocalCart() {
        cartItemList.clear();
        cartItemList.addAll(dbHelper.getAllItems());

        binding.cartProgressBar.setVisibility(View.GONE);
        updateUI(cartItemList);
    }

    private void fetchCloudCart() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("cart")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItemList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        CartItem item = doc.toObject(CartItem.class);
                        if (item != null) {
                            cartItemList.add(item);
                        }
                    }
                    binding.cartProgressBar.setVisibility(View.GONE);
                    updateUI(cartItemList);
                }).addOnFailureListener(e -> {
                    binding.cartProgressBar.setVisibility(View.GONE);
                });
    }

    private void updateUI(List<CartItem> items) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        updateTotalPrice();

        binding.cartProgressBar.setVisibility(View.GONE);
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().
                    findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().
                    findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.bottom_nav_home);
            }
        }
    }

    private void backOption() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToHome();
                    }
                });
    }

    private void setupRecyclerView() {
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(cartItemList, this);
        binding.rvCartItems.setAdapter(adapter);
    }


    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += (item.getPrice() * item.getQuantity());
        }

        binding.tvTotalPrice.setText("Rs. " + String.format("%,.2f", total));
        binding.tvCartCount.setText("(" + cartItemList.size() + ")");


        if (cartItemList.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.scrollViewCart.setVisibility(View.GONE);
            binding.cardBottom.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.scrollViewCart.setVisibility(View.VISIBLE);
            binding.cardBottom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity) {
        CartItem item = cartItemList.get(position);
        String productId = item.getProductId();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid)
                    .collection("cart")
                    .document(productId).update("quantity", newQuantity)
                    .addOnSuccessListener(aVoid -> Toasty.success(requireContext(),
                            "Quantity updated").show())
                    .addOnFailureListener(e -> Toasty.error(requireContext(),
                            "Failed to update quantity").show());
        } else {
            dbHelper.updateQuantity(productId, newQuantity);
            Toasty.success(requireContext(), "Quantity updated").show();
        }

        item.setQuantity(newQuantity);
        adapter.notifyItemChanged(position);
        updateTotalPrice();


    }

    @Override
    public void onRemoveItem(int position) {
        CartItem item = cartItemList.get(position);
        String productId = item.getProductId();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).collection("cart")
                    .document(productId).delete().addOnSuccessListener(
                            aVoid -> Toasty.success(requireContext(),
                                    "Removed from Cart").show()).addOnFailureListener(
                            e -> Toasty.error(requireContext(),
                                    "Failed to remove from Cart").show());
        } else {
            dbHelper.deleteItem(productId);
            Toasty.success(requireContext(), "Removed from  Cart").show();
        }

        cartItemList.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, cartItemList.size());
        updateTotalPrice();
    }
}