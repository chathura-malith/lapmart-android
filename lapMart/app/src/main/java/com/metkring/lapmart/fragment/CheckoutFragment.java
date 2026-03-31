package com.metkring.lapmart.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.metkring.lapmart.R;
import com.metkring.lapmart.databinding.FragmentCheckoutBinding;
import com.metkring.lapmart.dto.OrderItemDto;
import com.metkring.lapmart.dto.OrderRequest;
import com.metkring.lapmart.dto.StandardResponseDto;
import com.metkring.lapmart.model.Address;
import com.metkring.lapmart.service.RetrofitService;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import retrofit2.Call;


public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private double shippingCost = 500.00;
    private double subTotal = 0.0;
    private double totalPayment = 0.0;

    private Address currentShippingAddress;
    private Address currentBillingAddress;
    private List<Map<String, Object>> cartItemsList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        PHResponse<StatusResponse> response = (PHResponse<StatusResponse>)
                                data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (response != null && response.isSuccess()) {
                            saveOrderToFirebase();
                        } else {
                            Toasty.error(requireContext(), "Payment Failed: "
                                    + (response != null ? response.toString() :
                                    "Unknown Error"), Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toasty.warning(requireContext(), "Payment Canceled",
                            Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCheckoutBinding.inflate(inflater, container,false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if (mAuth.getCurrentUser() != null) {
            loadUserAddress();
            calculateOrderSummary();
        } else {
            Toasty.warning(requireContext(), "User not logged in!",
                    Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        }

        binding.layoutAddShippingAddress.setOnClickListener(v -> navigateToAddAddress());
        binding.btnChangeShippingAddress.setOnClickListener(v -> navigateToAddAddress());
        binding.layoutAddBillingAddress.setOnClickListener(v -> navigateToAddAddress());
        binding.btnChangeBillingAddress.setOnClickListener(v -> navigateToAddAddress());

        binding.btnPlaceOrder.setOnClickListener(v -> {
            if (currentShippingAddress == null || currentBillingAddress == null) {
                Toasty.warning(requireContext(),
                        "Please add Shipping and Billing addresses",
                        Toast.LENGTH_SHORT).show();
            } else if (subTotal == 0.0) {
                Toasty.warning(requireContext(), "Your cart is empty",
                        Toast.LENGTH_SHORT).show();
            } else {
                startPayHerePayment();
            }
        });
    }

    private void navigateToAddAddress() {
        AddAddressFragment fragment = new AddAddressFragment();

        if (currentShippingAddress != null || currentBillingAddress != null) {
            Bundle bundle = new Bundle();
            if (currentShippingAddress != null) {
                bundle.putSerializable("shippingAddress", currentShippingAddress);
            }
            if (currentBillingAddress != null) {
                bundle.putSerializable("billingAddress", currentBillingAddress);
            }
            fragment.setArguments(bundle);
        }
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadUserAddress() {
        setLoading(true);
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
            setLoading(false);
            if (documentSnapshot.exists() && documentSnapshot.contains("address")) {
                Map<String, Object> addressMap = (Map<String, Object>)
                        documentSnapshot.get("address");

                if (addressMap != null) {
                    if (addressMap.containsKey("shipping")) {
                        Map<String, Object> shipping = (Map<String, Object>)
                                addressMap.get("shipping");
                        currentShippingAddress = convertMapToAddress(shipping);
                        setShippingAddressUI(shipping);
                    }

                    if (addressMap.containsKey("billing")) {
                        Map<String, Object> billing = (Map<String, Object>)
                                addressMap.get("billing");
                        currentBillingAddress = convertMapToAddress(billing);
                        setBillingAddressUI(billing);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            setLoading(false);
            Toasty.error(requireContext(), "Failed to load address",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private Address convertMapToAddress(Map<String, Object> map) {
        if (map == null) return null;
        return new Address(
                (String) map.get("fullName"),
                (String) map.get("email"),
                (String) map.get("contactNo"),
                (String) map.get("addressLine1"),
                (String) map.get("addressLine2"),
                (String) map.get("city"),
                (String) map.get("postCode")
        );
    }

    private void setShippingAddressUI(Map<String, Object> shipping) {
        if (shipping == null) return;

        binding.layoutAddShippingAddress.setVisibility(View.GONE);
        binding.cardShippingAddress.setVisibility(View.VISIBLE);

        String name = (String) shipping.get("fullName");
        String add1 = (String) shipping.get("addressLine1");
        String add2 = (String) shipping.get("addressLine2");
        String city = (String) shipping.get("city");
        String postCode = (String) shipping.get("postCode");

        binding.tvShippingName.setText(name);
        String fullAddress = add1 + (add2 != null && !add2.isEmpty() ? ", " +
                "" + add2 + "," : "") + "\n" + city + ", " + postCode + ".";
        binding.tvShippingAddress.setText(fullAddress);
    }

    private void setBillingAddressUI(Map<String, Object> billing) {
        if (billing == null) return;

        binding.layoutAddBillingAddress.setVisibility(View.GONE);
        binding.cardBillingAddress.setVisibility(View.VISIBLE);

        String name = (String) billing.get("fullName");
        String add1 = (String) billing.get("addressLine1");
        String add2 = (String) billing.get("addressLine2");
        String city = (String) billing.get("city");
        String postCode = (String) billing.get("postCode");

        binding.tvBillingName.setText(name);
        String fullAddress = add1 + (add2 != null && !add2.isEmpty() ? "," +
                " " + add2 + "," : "") + "\n" + city + ", " + postCode + ".";
        binding.tvBillingAddress.setText(fullAddress);
    }

    private void calculateOrderSummary() {
        setLoading(true);
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection(
                "cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    subTotal = 0.0;
                    cartItemsList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double price = doc.getDouble("price");
                        Long quantity = doc.getLong("quantity");

                        if (price != null && quantity != null) {
                            subTotal += (price * quantity);

                            Map<String, Object> itemMap = doc.getData();
                            if (itemMap != null) {
                                itemMap.put("productId", doc.getId());
                                cartItemsList.add(itemMap);
                            }
                        }
                    }

                    updateSummaryUI();
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toasty.error(requireContext(), "Failed to calculate total",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateSummaryUI() {
        totalPayment = subTotal + shippingCost;
        binding.tvSubtotal.setText("Rs. " + String.format("%,.2f", subTotal));
        binding.tvShipping.setText("Rs. " + String.format("%,.2f", shippingCost));
        binding.tvTotal.setText("Rs. " + String.format("%,.2f", totalPayment));
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.checkoutProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.checkoutProgressBar.setVisibility(View.GONE);
        }
    }


    private void startPayHerePayment() {
        String fullName = currentBillingAddress.getFullName().trim();
        String firstName = fullName;
        String lastName = "Customer";

        if (fullName.contains(" ")) {
            int spaceIndex = fullName.indexOf(" ");
            firstName = fullName.substring(0, spaceIndex);
            lastName = fullName.substring(spaceIndex + 1);
        }

        String orderId = generateOrderId();

        InitRequest req = new InitRequest();
        req.setSandBox(true);

        req.setMerchantId("1234655");
        req.setMerchantSecret("MzI1OTUyMjU0NzI0MzA4MTU5MjMyOTA3NDU1NjIxMzU0NjYwMDI4");
        req.setCurrency("LKR");
        req.setAmount(totalPayment);
        req.setOrderId(orderId);
        req.setItemsDescription("LapMart Order: " + orderId);
        req.setCustom1("Any custom detail");
        req.setCustom2("Any custom detail");

        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(currentBillingAddress.getEmail());
        req.getCustomer().setPhone(currentBillingAddress.getContactNo());
        req.getCustomer().getAddress().setAddress(currentBillingAddress.getAddressLine1());
        req.getCustomer().getAddress().setCity(currentBillingAddress.getCity());
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(requireContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

        payHereLauncher.launch(intent);
    }

    private void saveOrderToFirebase() {
        setLoading(true);
        String uid = mAuth.getCurrentUser().getUid();
        String orderId = generateOrderId();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", uid);
        orderData.put("totalAmount", totalPayment);
        orderData.put("status", "Pending"); // Order Status
        orderData.put("timestamp", FieldValue.serverTimestamp());
        orderData.put("shippingAddress", currentShippingAddress);
        orderData.put("billingAddress", currentBillingAddress);
        orderData.put("items", cartItemsList);

        WriteBatch batch = db.batch();


        batch.set(db.collection("orders").document(orderId), orderData);

        for (Map<String, Object> item : cartItemsList) {
            String productId = (String) item.get("productId");
            Long purchasedQuantity = (Long) item.get("quantity");
            if (productId != null) {
                batch.delete(db.collection("users").
                        document(uid).collection("cart").document(productId));

                if (purchasedQuantity != null) {
                    batch.update(db.collection("products").document(productId),
                            "qty", FieldValue.increment(-purchasedQuantity));
                }
            }
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            setLoading(false);
            Toasty.success(requireContext(), "Order Placed Successfully!",
                    Toast.LENGTH_SHORT).show();


            List<OrderItemDto> itemDtos = new ArrayList<>();
            for (Map<String, Object> itemMap : cartItemsList) {
                String name = (String) itemMap.get("productName");

                Object priceObj = itemMap.get("price");
                Double price = (priceObj instanceof Long) ? ((Long) priceObj).doubleValue() :
                        (Double) priceObj;

                Object qtyObj = itemMap.get("quantity");
                Integer qty = (qtyObj instanceof Long) ? ((Long) qtyObj).intValue() :
                        (Integer) qtyObj;

                itemDtos.add(new OrderItemDto(name, price, qty));
            }

            OrderRequest orderRequest = new OrderRequest(
                    orderId,
                    currentBillingAddress.getFullName(),
                    currentBillingAddress.getEmail(),
                    totalPayment,
                    itemDtos
            );

            RetrofitService.getEmailApi().sendInvoiceEmail(orderRequest).
                    enqueue(new retrofit2.Callback<StandardResponseDto>() {
                @Override
                public void onResponse(Call<StandardResponseDto> call,
                                       retrofit2.Response<StandardResponseDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Email එක ගියා!
                        Log.d("EmailService", "Success: " + response.body().getMessage());
                        Toasty.success(requireContext(), "Invoice sent to email!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("EmailService", "Failed: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponseDto> call, Throwable t) {
                    Log.e("EmailService", "Error: " + t.getMessage());
                }
            });


            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrderFragment())
                    .commit();

        }).addOnFailureListener(e -> {
            setLoading(false);
            Toasty.error(requireContext(), "Failed to save order",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private String generateOrderId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        while (sb.length() < 7) {
            int index = (int) (rnd.nextFloat() * chars.length());
            sb.append(chars.charAt(index));
        }
        return "LM-" + sb.toString();
    }

}