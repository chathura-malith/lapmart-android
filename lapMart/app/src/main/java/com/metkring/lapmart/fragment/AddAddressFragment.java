package com.metkring.lapmart.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.metkring.lapmart.R;
import com.metkring.lapmart.databinding.FragmentAddAddressBinding;
import com.metkring.lapmart.model.Address;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class AddAddressFragment extends Fragment {

    private FragmentAddAddressBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddAddressBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideBottomNavigation();
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        billingChecked();
        if (getArguments() != null) {
            Address existingShipping = (Address) getArguments().getSerializable("shippingAddress");
            Address existingBilling = (Address) getArguments().getSerializable("billingAddress");

            if (existingShipping != null) {
                prefillShippingAddress(existingShipping);
            }

            if (existingBilling != null) {
                if (!isAddressesEqual(existingShipping, existingBilling)) {
                    binding.cbSameAsShipping.setChecked(false);
                    prefillBillingAddress(existingBilling);
                }
            }
        }
        binding.btnSaveAddress.setOnClickListener(v -> saveAddressToFirebase());
    }

    private void prefillShippingAddress(Address address) {
        binding.etShippingName.setText(address.getFullName());
        binding.etShippingEmail.setText(address.getEmail());
        binding.etShippingContact.setText(address.getContactNo());
        binding.etShippingAddress1.setText(address.getAddressLine1());
        binding.etShippingAddress2.setText(address.getAddressLine2());
        binding.etShippingCity.setText(address.getCity());
        binding.etShippingPostCode.setText(address.getPostCode());
    }

    private void prefillBillingAddress(Address address) {
        binding.etBillingName.setText(address.getFullName());
        binding.etBillingEmail.setText(address.getEmail());
        binding.etBillingContact.setText(address.getContactNo());
        binding.etBillingAddress1.setText(address.getAddressLine1());
        binding.etBillingAddress2.setText(address.getAddressLine2());
        binding.etBillingCity.setText(address.getCity());
        binding.etBillingPostCode.setText(address.getPostCode());
    }

    private boolean isAddressesEqual(Address a1, Address a2) {
        if (a1 == null || a2 == null) return false;
        return a1.getAddressLine1().equals(a2.getAddressLine1()) &&
                a1.getCity().equals(a2.getCity());
    }

    private void saveAddressToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toasty.warning(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String sName = binding.etShippingName.getText().toString().trim();
        String sEmail = binding.etShippingEmail.getText().toString().trim();
        String sContact = binding.etShippingContact.getText().toString().trim();
        String sAddress1 = binding.etShippingAddress1.getText().toString().trim();
        String sAddress2 = binding.etShippingAddress2.getText().toString().trim();
        String sCity = binding.etShippingCity.getText().toString().trim();
        String sPostCode = binding.etShippingPostCode.getText().toString().trim();

        if (
                TextUtils.isEmpty(sName) ||
                        TextUtils.isEmpty(sEmail) ||
                        TextUtils.isEmpty(sContact) ||
                        TextUtils.isEmpty(sAddress1) ||
                        TextUtils.isEmpty(sCity)
                        || TextUtils.isEmpty(sPostCode)
        ) {
            Toasty.error(requireContext(), "Please fill all required shipping details",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(sEmail)) {
            Toasty.error(requireContext(), "Please enter a valid Shipping Email",
                    Toast.LENGTH_SHORT).show();
            binding.etShippingEmail.requestFocus();
            return;
        }
        if (!isValidSLMobile(sContact)) {
            Toasty.error(requireContext(), "Invalid Shipping Mobile Number.",
                    Toast.LENGTH_LONG).show();
            binding.etShippingContact.requestFocus();
            return;
        }

        Address shippingAddress = new Address(sName, sEmail, sContact, sAddress1,
                sAddress2, sCity, sPostCode);
        Address billingAddress;


        if (binding.cbSameAsShipping.isChecked()) {
            billingAddress = shippingAddress;
        } else {
            String bName = binding.etBillingName.getText().toString().trim();
            String bEmail = binding.etBillingEmail.getText().toString().trim();
            String bContact = binding.etBillingContact.getText().toString().trim();
            String bAddress1 = binding.etBillingAddress1.getText().toString().trim();
            String bAddress2 = binding.etBillingAddress2.getText().toString().trim();
            String bCity = binding.etBillingCity.getText().toString().trim();
            String bPostCode = binding.etBillingPostCode.getText().toString().trim();

            if (
                    TextUtils.isEmpty(bName) ||
                            TextUtils.isEmpty(bEmail) ||
                            TextUtils.isEmpty(bContact) ||
                            TextUtils.isEmpty(bAddress1) ||
                            TextUtils.isEmpty(bCity)
                            || TextUtils.isEmpty(bPostCode)
            ) {
                Toasty.error(requireContext(), "Please fill all required billing details",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(bEmail)) {
                Toasty.error(requireContext(), "Please enter a valid Billing Email",
                        Toast.LENGTH_SHORT).show();
                binding.etBillingEmail.requestFocus();
                return;
            }
            if (!isValidSLMobile(bContact)) {
                Toasty.error(requireContext(), "Invalid Billing Mobile Number.",
                        Toast.LENGTH_LONG).show();
                binding.etBillingContact.requestFocus();
                return;
            }
            billingAddress = new Address(bName, bEmail, bContact, bAddress1,
                    bAddress2, bCity, bPostCode);
        }


        Map<String, Object> addressData = new HashMap<>();
        addressData.put("shipping", shippingAddress);
        addressData.put("billing", billingAddress);

        db.collection("users").document(currentUser.getUid())
                .update("address", addressData)
                .addOnSuccessListener(aVoid -> {
                    Toasty.success(requireContext(), "Address saved successfully",
                            Toast.LENGTH_SHORT).show();
                    if (getActivity() != null)
                        getActivity().getOnBackPressedDispatcher().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    db.collection("users").document(currentUser.getUid())
                            .set(new HashMap<String, Object>() {{
                                put("address", addressData);
                            }})
                            .addOnSuccessListener(aVoid -> {
                                Toasty.success(requireContext(),
                                        "Address saved successfully",
                                        Toast.LENGTH_SHORT).show();
                                if (getActivity() != null)
                                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                            })
                            .addOnFailureListener(e1 -> Toasty.error(requireContext(),
                                    "Failed to save address", Toast.LENGTH_SHORT).show());
                });
    }

    private void billingChecked() {
        binding.cbSameAsShipping.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
            if (isChecked) {
                binding.layoutBillingSection.setVisibility(View.GONE);
            } else {
                binding.layoutBillingSection.setVisibility(View.VISIBLE);
                binding.getRoot().post(() -> {
                    if (binding.layoutBillingSection.getParent() != null) {
                        binding.layoutBillingSection.getParent().requestChildFocus(
                                binding.layoutBillingSection,
                                binding.layoutBillingSection
                        );
                    }
                });
            }
        });
    }

    private boolean isValidSLMobile(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        String regex = "^(?:0|\\+94|94)?7[01245678]\\d{7}$";
        return phone.matches(regex);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav
                    = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }
}