package com.metkring.lapmart.helper;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.metkring.lapmart.model.CartItem;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class CartManager {
    private CartDbHelper dbHelper;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    public CartManager(Context context) {
        dbHelper = new CartDbHelper(context);
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void addItem(CartItem item, Context context) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference docRef = firestore.collection("users")
                    .document(user.getUid())
                    .collection("cart")
                    .document(item.getProductId());

            docRef.update("quantity", FieldValue.increment(item.getQuantity()))
                    .addOnSuccessListener(aVoid -> {
                        Toasty.success(context, "Added to Cart!").show();
                    })
                    .addOnFailureListener(e -> {
                        docRef.set(item)
                                .addOnSuccessListener(aVoid -> Toasty.success(context,
                                        "Added to Cart!").show())
                                .addOnFailureListener(err -> Toasty.error(context,
                                        "Failed to add to Cart").show());
                    });
        } else {
            dbHelper.addToCart(item);
            Toasty.info(context, "Added to Cart!").show();
        }
    }

    public void syncLocalCartToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        List<CartItem> localItems = dbHelper.getAllItems();
        if (localItems.isEmpty()) return;

        for (CartItem item : localItems) {
            DocumentReference docRef = firestore.collection("users")
                    .document(user.getUid())
                    .collection("cart")
                    .document(item.getProductId());

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        docRef.update("quantity", FieldValue.increment(item.getQuantity()))
                                .addOnSuccessListener(aVoid -> Log.d("Sync",
                                        "Quantity updated for: " + item.getProductName()));
                    } else {
                        docRef.set(item)
                                .addOnSuccessListener(aVoid -> Log.d("Sync",
                                        "New item synced: " + item.getProductName()));
                    }
                }
            });
        }

        dbHelper.clearCart();
    }
}