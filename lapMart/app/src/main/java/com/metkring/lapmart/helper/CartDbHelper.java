package com.metkring.lapmart.helper;

import static java.security.AccessController.getContext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.metkring.lapmart.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LapMartCart.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "cart";

    public CartDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id TEXT, " +
                "name TEXT, " +
                "image TEXT, " +
                "price REAL, " +
                "quantity INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


public void addToCart(CartItem item) {
    SQLiteDatabase db = this.getWritableDatabase();

    Cursor cursor = db.rawQuery("SELECT quantity FROM " + TABLE_NAME + " WHERE product_id = ?",
            new String[]{item.getProductId()});

    if (cursor.moveToFirst()) {
        int currentQty = cursor.getInt(0);
        int newQty = currentQty + item.getQuantity();

        ContentValues values = new ContentValues();
        values.put("quantity", newQty);

        db.update(TABLE_NAME, values, "product_id = ?", new String[]{item.getProductId()});
    } else {
        ContentValues values = new ContentValues();
        values.put("product_id", item.getProductId());
        values.put("name", item.getProductName());
        values.put("image", item.getProductImage());
        values.put("price", item.getPrice());
        values.put("quantity", item.getQuantity());

        db.insert(TABLE_NAME, null, values);
    }

    cursor.close();
     db.close();
}

    public List<CartItem> getAllItems() {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getInt(5)
                );
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

    public void updateQuantity(String productId, int newQty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", newQty);
        db.update(TABLE_NAME, values, "product_id = ?", new String[]{productId});
        db.close();
    }

    public void deleteItem(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "product_id = ?", new String[]{productId});
        db.close();
    }
}