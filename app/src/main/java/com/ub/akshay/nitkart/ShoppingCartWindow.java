package com.ub.akshay.nitkart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCartWindow extends AppCompatActivity {

    private final String TAG = ShoppingCartWindow.class.getSimpleName();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    Boolean isCartEmpty = true;
    TextView priceView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    int totalAmount = 0;
    ArrayList<ShoppingItem> items;// =

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart_window);

        priceView = (TextView) findViewById(R.id.totalPriceCheckout);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    myRef = database.getReference("users/" + user.getUid());

                    // adding value event listener for myRef
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getKey().equals(user.getUid())) {
                                isCartEmpty = (Boolean) dataSnapshot.child("isCartEmpty").getValue();
                                if (isCartEmpty) {
                                    priceView.setText(NumberFormat.getCurrencyInstance().format(0));
                                } else {
                                    setUpShoppingCart(dataSnapshot.child("cartItems"));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        (findViewById(R.id.returnToPrevPage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        (findViewById(R.id.checkOut)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CheckOutScreen.class));
            }
        });

        (findViewById(R.id.clearCart)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(findViewById(R.id.shoppingCartWindowLayout),
                        "Cleared!",
                        Snackbar.LENGTH_SHORT).show();
                clearCart();
                finish();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setUpShoppingCart(DataSnapshot dataSnapshot) {

        totalAmount = 0;

        if (items != null){
            items.clear();
        } else {
            items = new ArrayList<>();
        }

        for (DataSnapshot snap : dataSnapshot.getChildren()){

            int itemPrice = -1, quantity = 0;

            try{
                itemPrice = Integer.valueOf(NumberFormat.getCurrencyInstance()
                        .parse(String.valueOf(snap.child("price").getValue()))
                        .toString());
            } catch (ParseException e){
                e.printStackTrace();
            }

            quantity = Integer.valueOf(snap.child("quantity").getValue().toString());

            items.add(new ShoppingItem(
                    Integer.valueOf(snap.child("productID").getValue().toString()),
                    snap.child("title").getValue().toString(),
                    snap.child("type").getValue().toString(),
                    snap.child("description").getValue().toString(),
                    itemPrice,
                    quantity
            ));

            totalAmount += quantity*itemPrice;
        }

        ListView view = (ListView) findViewById(R.id.shoppingCartList);
        // Now the Cart gets updated whenever the data changes in the server
        view.setAdapter(new ShoppingCartAdapter(getApplicationContext(), items));

        priceView.setText(NumberFormat.getCurrencyInstance().format(totalAmount));
    }

    private void clearCart() {

        if (!isCartEmpty) {
            DatabaseReference myRefClear = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            myRefClear.child(user.getUid()).push();

            // As firebase does not accept keys with empty values, I'm putting a dummy item with empty Strings and -1 as ints
            // Quantity of items in cart is not realtime database quantity but the quantity the user wants
            ArrayList<ShoppingItem> cart = new ArrayList<>();
            cart.add(new ShoppingItem(-1, "", "", "", -1, -1));
            Map<String, Object> cartItems = new HashMap<>();
            cartItems.put("cartItems", cart);

            // Adding a isCartEmpty State Variable for cart window display

            Map<String, Object> cartState = new HashMap<>();
            cartState.put("isCartEmpty", Boolean.TRUE);

            // Updating the database for the user
            myRefClear.updateChildren(cartItems);
            myRefClear.updateChildren(cartState);

            isCartEmpty = true;
        }
    }

}
