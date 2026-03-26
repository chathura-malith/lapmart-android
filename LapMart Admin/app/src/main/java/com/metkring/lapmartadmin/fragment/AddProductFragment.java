package com.metkring.lapmartadmin.fragment;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.activity.MainActivity;
import com.metkring.lapmartadmin.adapter.ProductImageAdapter;
import com.metkring.lapmartadmin.databinding.FragmentAddProductBinding;
import com.metkring.lapmartadmin.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class AddProductFragment extends Fragment {

    private FragmentAddProductBinding binding;
    private List<Uri> selectedImageUris;
    private ProductImageAdapter imageAdapter;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Product productToUpdate = null;
    private boolean isUpdateMode = false;
    private boolean isImageUpdated = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private long lastShakeTime = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddProductBinding.inflate(inflater, container, false);

        selectedImageUris = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            productToUpdate = (Product) getArguments().getSerializable("product_to_update");
            if (productToUpdate != null) {
                isUpdateMode = true;
            }
        }

        registerImagePicker();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        hideBottomNavigation();
        binding.btnBack.setOnClickListener(v -> navigateBackToHome());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToHome();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            if (imeVisible) {
                v.setPadding(0, 0, 0, imeHeight);

                if (binding.etProductDescription.hasFocus()) {
                    binding.nestedScrollView.postDelayed(() -> {
                        binding.nestedScrollView.smoothScrollTo(
                                0, binding.etProductDescription.getBottom());
                    }, 100);
                }
            } else {
                v.setPadding(0, 0, 0, navBarHeight);
            }
            return insets;
        });

        binding.fabAddProductImages.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        binding.btnAddBrand.setOnClickListener(v -> showAddDialog(
                "Add New Brand", "brands", binding.actvBrand));
        binding.btnAddItem.setOnClickListener(v -> showAddDialog(
                "Add New Category", "categories", binding.actvItem));
        binding.btnAddProcessor.setOnClickListener(v -> showAddDialog(
                "Add New Processor", "processors", binding.actvProcessor));
        binding.btnAddRam.setOnClickListener(v -> showAddDialog(
                "Add New RAM", "rams", binding.actvRam));
        binding.btnAddGpu.setOnClickListener(v -> showAddDialog(
                "Add New GPU", "gpus", binding.actvGpu));
        binding.btnAddStorage.setOnClickListener(v -> showAddDialog(
                "Add New Storage", "storages", binding.actvStorage));

        setupImageSlider();

        loadDropdownData("brands", binding.actvBrand);
        loadDropdownData("categories", binding.actvItem);
        loadDropdownData("processors", binding.actvProcessor);
        loadDropdownData("rams", binding.actvRam);
        loadDropdownData("gpus", binding.actvGpu);
        loadDropdownData("storages", binding.actvStorage);

        binding.btnAddProduct.setOnClickListener(v -> validateAndUploadData());
        binding.btnResetProduct.setOnClickListener(v -> resetForm());

        if (isUpdateMode) {
            loadProductDataForUpdate();
        }else{
            binding.tvHeaderTitle.setText("Add Product");
            binding.btnAddProduct.setText("Add");
        }
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;

            if (acceleration > 12) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > 1500) {
                    lastShakeTime = currentTime;
                    handleShakeEvent();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };

    private void handleShakeEvent() {
        Vibrator vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }
        resetForm();
        Toasty.info(requireContext(), "Form Reset by Shake!", Toast.LENGTH_SHORT, true).show();
    }

    private void loadProductDataForUpdate() {
        if (binding.tvHeaderTitle != null) {
            binding.tvHeaderTitle.setText("Update Product");
        }
        binding.btnAddProduct.setText("Update");

        binding.actvBrand.setText(productToUpdate.getBrand(), false);
        binding.actvItem.setText(productToUpdate.getModel(), false);
        binding.actvProcessor.setText(productToUpdate.getProcessor(), false);
        binding.actvRam.setText(productToUpdate.getRam(), false);
        binding.actvGpu.setText(productToUpdate.getGpu(), false);
        binding.actvStorage.setText(productToUpdate.getStorage(), false);

        binding.etProductPrice.setText(String.valueOf(productToUpdate.getPrice()));
        binding.etProductQty.setText(String.valueOf(productToUpdate.getQty()));
        binding.etProductDescription.setText(productToUpdate.getDescription());
        binding.etBuyingPrice.setText(String.valueOf(productToUpdate.getBuyingPrice()));

        if (productToUpdate.getImageUrls() != null && !productToUpdate.getImageUrls().isEmpty()) {
            selectedImageUris.clear();
            for (String url : productToUpdate.getImageUrls()) {
                selectedImageUris.add(Uri.parse(url));
            }
            imageAdapter.notifyDataSetChanged();
            binding.spiDotsIndicator.attachToPager(binding.vpProductImages);

            binding.llImagePlaceholder.setVisibility(View.GONE);
            binding.vpProductImages.setVisibility(View.VISIBLE);

            if (selectedImageUris.size() > 1) {
                binding.spiDotsIndicator.setVisibility(View.VISIBLE);
            } else {
                binding.spiDotsIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void validateAndUploadData() {
        String brand = binding.actvBrand.getText().toString().trim();
        String item = binding.actvItem.getText().toString().trim();
        String processor = binding.actvProcessor.getText().toString().trim();
        String ram = binding.actvRam.getText().toString().trim();
        String gpu = binding.actvGpu.getText().toString().trim();
        String storageCap = binding.actvStorage.getText().toString().trim();
        String priceStr = binding.etProductPrice.getText().toString().trim();
        String qtyStr = binding.etProductQty.getText().toString().trim();
        String description = binding.etProductDescription.getText().toString().trim();
        String buyingPriceStr = binding.etBuyingPrice.getText().toString().trim();

        if (selectedImageUris.isEmpty()) {
            Toasty.warning(requireContext(), "Please select at least one product image",
                    Toast.LENGTH_SHORT, true).show();
            return;
        }
        if (brand.isEmpty() || item.isEmpty() || processor.isEmpty() || ram.isEmpty() ||
                gpu.isEmpty() || storageCap.isEmpty() || priceStr.isEmpty() ||
                buyingPriceStr.isEmpty() || qtyStr.isEmpty()
                || description.isEmpty()) {
            Toasty.warning(requireContext(), "Please fill all fields",
                    Toast.LENGTH_SHORT, true).show();
            return;
        }

        double buyingPrice = Double.parseDouble(buyingPriceStr);
        double price = Double.parseDouble(priceStr);
        int quantity = Integer.parseInt(qtyStr);

        showProgress("Uploading Images (0/" + selectedImageUris.size() + ")...");

        if (isUpdateMode && !isImageUpdated) {
            showProgress("Updating Product...");
            saveOrUpdateProduct(brand, item, processor, ram, gpu, storageCap, price,buyingPrice,
                    quantity, description, productToUpdate.getImageUrls());
        } else {
            showProgress(isUpdateMode ? "Uploading New Images..." : "Uploading Images (0/"
                    + selectedImageUris.size() + ")...");
            uploadImagesAndSaveProduct(brand, item, processor, ram, gpu, storageCap,buyingPrice,
                    price, quantity, description);
        }

    }

//private void uploadImagesAndSaveProduct(
//        String brand, String category, String processor,
//        String ram, String gpu, String storageCap,double buyingPrice,
//        double price, int quantity, String description
//) {
//    List<String> uploadedImageUrls = new ArrayList<>();
//    int totalImages = selectedImageUris.size();
//    final int[] uploadedCount = {0};
//
//    for (int i = 0; i < totalImages; i++) {
//        Uri imageUri = selectedImageUris.get(i);
//
//        String uriString = imageUri.toString();
//        if (uriString.startsWith("https")) {
//            uploadedImageUrls.add(uriString);
//            uploadedCount[0]++;
//            checkAndSaveIfAllUploaded(uploadedCount[0], totalImages, brand, category, processor,
//                    ram, gpu, storageCap, buyingPrice, price, quantity, description, uploadedImageUrls);
//            continue;
//        }
//
//        String fileName = UUID.randomUUID().toString() + ".jpg";
//        StorageReference imageRef = storage.getReference().child("product_images/" + fileName);
//
//        imageRef.putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> {
//                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        uploadedImageUrls.add(uri.toString());
//                        uploadedCount[0]++;
//
//                        binding.tvProgressText.setText("Uploading Images ("
//                                + uploadedCount[0] + "/" + totalImages + ")...");
//
//                        checkAndSaveIfAllUploaded(uploadedCount[0], totalImages, brand, category,
//                                processor, ram, gpu, storageCap, buyingPrice, price, quantity
//                                , description, uploadedImageUrls);
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    hideProgress();
//                    Toasty.error(requireContext(), "Failed to upload an image: "
//                            + e.getMessage(), Toast.LENGTH_LONG, true).show();
//                });
//    }
//}

    private void uploadImagesAndSaveProduct(
            String brand, String category, String processor,
            String ram, String gpu, String storageCap, double buyingPrice,
            double price, int quantity, String description
    ) {
        int totalImages = selectedImageUris.size();
        String[] uploadedImageUrlsArray = new String[totalImages];
        final int[] uploadedCount = {0};

        for (int i = 0; i < totalImages; i++) {
            Uri imageUri = selectedImageUris.get(i);
            final int currentIndex = i;

            String uriString = imageUri.toString();
            if (uriString.startsWith("https")) {
                uploadedImageUrlsArray[currentIndex] = uriString;
                uploadedCount[0]++;
                checkAndSaveIfAllUploaded(uploadedCount[0], totalImages, brand, category, processor,
                        ram, gpu, storageCap, buyingPrice, price, quantity, description, getListFromArray(uploadedImageUrlsArray));
                continue;
            }

            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference().child("product_images/" + fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedImageUrlsArray[currentIndex] = uri.toString();
                            uploadedCount[0]++;

                            binding.tvProgressText.setText("Uploading Images ("
                                    + uploadedCount[0] + "/" + totalImages + ")...");

                            checkAndSaveIfAllUploaded(uploadedCount[0], totalImages, brand, category,
                                    processor, ram, gpu, storageCap, buyingPrice, price, quantity,
                                    description, getListFromArray(uploadedImageUrlsArray));
                        });
                    })
                    .addOnFailureListener(e -> {
                        hideProgress();
                        Toasty.error(requireContext(), "Failed to upload an image: "
                                + e.getMessage(), Toast.LENGTH_LONG, true).show();
                    });
        }
    }

    private List<String> getListFromArray(String[] array) {
        List<String> list = new ArrayList<>();
        for (String url : array) {
            if (url != null) {
                list.add(url);
            }
        }
        return list;
    }

    private void checkAndSaveIfAllUploaded(
            int count, int total, String brand, String category, String processor, String ram,
            String gpu, String storageCap, double buyingPrice, double price, int quantity,
            String description, List<String> imageUrls
    ) {
        if (count == total) {
            saveOrUpdateProduct(brand, category, processor, ram, gpu,
                    storageCap, buyingPrice, price, quantity, description, imageUrls);
        }
    }



    private void saveOrUpdateProduct(
            String brand, String category, String processor,
            String ram, String gpu, String storageCap,double buyingPrice, double price, int quantity,
            String description, List<String> imageUrls
    ) {

        binding.tvProgressText.setText(isUpdateMode ? "Updating Details..." : "Saving Product...");

        Map<String, Object> product = new HashMap<>();
        product.put("brand", brand);
        product.put("model", category);
        product.put("processor", processor);
        product.put("ram", ram);
        product.put("gpu", gpu);
        product.put("storage", storageCap);
        product.put("buyingPrice", buyingPrice);
        product.put("price", price);
        product.put("qty", quantity);
        product.put("description", description);
        product.put("imageUrls", imageUrls);

        if (isUpdateMode) {
            db.collection("products").document(productToUpdate.getProductId())
                    .update(product)
                    .addOnSuccessListener(aVoid -> {
                        hideProgress();
                        Toasty.success(requireContext(), "Product Updated Successfully!",
                                Toast.LENGTH_LONG, true).show();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new StockFragment())
                                .addToBackStack(null)
                                .commit();
                    })
                    .addOnFailureListener(e -> {
                        hideProgress();
                    });
        } else {
            product.put("timestamp", System.currentTimeMillis());

            db.collection("products").add(product)
                    .addOnSuccessListener(documentReference -> {
                        hideProgress();
                        Toasty.success(requireContext(), "Product Added Successfully!",
                                Toast.LENGTH_LONG, true).show();
                        resetForm();
                    })
                    .addOnFailureListener(e -> {
                        hideProgress();
                    });
        }
    }

    private void resetForm() {
        binding.actvBrand.setText("");
        binding.actvItem.setText("");
        binding.actvProcessor.setText("");
        binding.actvRam.setText("");
        binding.actvGpu.setText("");
        binding.actvStorage.setText("");
        binding.etProductPrice.setText("");
        binding.etProductQty.setText("");
        binding.etProductDescription.setText("");

        binding.spiDotsIndicator.detachFromPager();
        binding.spiDotsIndicator.setVisibility(View.GONE);
        binding.vpProductImages.setVisibility(View.GONE);

        selectedImageUris.clear();
        if (imageAdapter != null) {
            imageAdapter.notifyDataSetChanged();
        }

        binding.llImagePlaceholder.setVisibility(View.VISIBLE);
        binding.actvBrand.clearFocus();
        binding.etBuyingPrice.setText("");
        binding.etProductPrice.clearFocus();
    }



    private void showAddDialog(String title, String collectionName, AutoCompleteTextView dropdown) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etInput = dialogView.findViewById(R.id.etDialogInput);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnAdd = dialogView.findViewById(R.id.btnDialogAdd);

        tvTitle.setText(title);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String inputText = etInput.getText().toString().trim();

            if (inputText.isEmpty()) {
                Toasty.warning(requireContext(), "Please enter a value!",
                        Toast.LENGTH_SHORT, true).show();
            } else {
                showProgress("Adding " + inputText + "...");

                Map<String, Object> data = new HashMap<>();
                data.put("name", inputText);

                db.collection(collectionName).add(data)
                        .addOnSuccessListener(documentReference -> {
                            hideProgress();
                            Toasty.success(requireContext(), inputText +
                                    " added successfully!", Toast.LENGTH_SHORT, true).show();
                            dialog.dismiss();
                            loadDropdownData(collectionName, dropdown);
                        })
                        .addOnFailureListener(e -> {
                            hideProgress();
                            Toasty.error(requireContext(), "Failed to add: "
                                    + e.getMessage(), Toast.LENGTH_LONG, true).show();
                        });
            }
        });

        dialog.show();
    }

    private void loadDropdownData(String collectionName, AutoCompleteTextView dropdown) {
        db.collection(collectionName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        if (name != null) {
                            list.add(name);
                        }
                    }
                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                list
                        );
                        dropdown.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load "
                            + collectionName, Toast.LENGTH_SHORT).show();
                });
    }

    private void registerImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImageUris.clear();
                        selectedImageUris.addAll(uris);

                        imageAdapter.notifyDataSetChanged();
                        binding.spiDotsIndicator.attachToPager(binding.vpProductImages);

                        binding.llImagePlaceholder.setVisibility(View.GONE);
                        binding.vpProductImages.setVisibility(View.VISIBLE);

                        if (uris.size() > 1) {
                            binding.spiDotsIndicator.setVisibility(View.VISIBLE);
                        } else {
                            binding.spiDotsIndicator.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }


    private void setupImageSlider() {
        imageAdapter = new ProductImageAdapter(requireContext(), selectedImageUris);
        binding.vpProductImages.setAdapter(imageAdapter);
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    private void navigateBackToHome() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();

            BottomNavigationView bottomNav = mainActivity.findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.bottom_nav_home);
            }
        }
    }

    private void showProgress(String message) {
        if (binding == null) return;
        binding.tvProgressText.setText(message);
        binding.rlProgressContainer.setVisibility(View.VISIBLE);
        binding.btnAddProduct.setEnabled(false);
        binding.btnResetProduct.setEnabled(false);
    }

    private void hideProgress() {
        if (binding == null) return;
        binding.rlProgressContainer.setVisibility(View.GONE);
        binding.btnAddProduct.setEnabled(true);
        binding.btnResetProduct.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBottomNavigation();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}