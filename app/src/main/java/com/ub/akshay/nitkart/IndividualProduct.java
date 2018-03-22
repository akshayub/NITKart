package com.ub.akshay.nitkart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IndividualProduct extends AppCompatActivity {

    private final String TAG = IndividualProduct.class.getSimpleName();

    private int quantity=1;
    String ip;
    ShoppingItem item;
    Button add, sub;
    TextView name, description, quantityView;
    FloatingActionButton addToCart, shoppingCart;
    ImageView productImage;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private DataSnapshot dataSnapshot;

    private ArrayList<ShoppingItem> cartItems;
    private Boolean isCartEmpty, isItemAlreadyInCart = false;
    int indexOfAlreadyPresentItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_product);

        progressBar = (ProgressBar) findViewById(R.id.individualProductPageProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        item = (ShoppingItem) getIntent().getSerializableExtra("product");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    myRef = database.getReference("users/"+user.getUid());

                    // adding value event listener for myRef
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            IndividualProduct.this.dataSnapshot = dataSnapshot;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        ip = getResources().getString(R.string.ip);

        name = (TextView) findViewById(R.id.productNameIndividualProduct);
        name.setText(item.getTitle());

        description = (TextView) findViewById(R.id.productDescriptionIndividualProduct);
        description.setText(item.getDescription());

        quantityView = (TextView) findViewById(R.id.quantityProductPage);
        quantityView.setText(String.valueOf(quantity));

        ((TextView) findViewById(R.id.productPriceIndividualProduct))
                .setText(item.getPrice());

        productImage = (ImageView) findViewById(R.id.productImageIndividualProduct);
        Picasso.with(getApplicationContext())
                .load(ip + String.valueOf(item.getProductID()) + ".jpg")
                .fit()
                .into(productImage);

        add = (Button) findViewById(R.id.incrementQuantity);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
            }
        });

        sub = (Button) findViewById(R.id.decrementQuantity);
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement();
            }
        });

        addToCart = (FloatingActionButton) findViewById(R.id.addToCartProductPage);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                // Probably a redundant if condition below, but I don't trust computers. :P
                if (dataSnapshot.getKey().equals(user.getUid())){
                    isCartEmpty = (Boolean) dataSnapshot.child("isCartEmpty").getValue();
                    // if is cart empty, create new arraylist when user wants to add to cart
                    //      this is to prevent creation of new cart every time user wants to see stuff
                    // else create a new cart and update

                    if (!isCartEmpty){
                        // Get the cart contents and then update as necessary
                        cartItems = new ArrayList<>();
                        int tempIndex = 0;
                        for (DataSnapshot snap : dataSnapshot.child("cartItems").getChildren()){

                            int itemPrice = -1;
                            try{
                                itemPrice = Integer.valueOf(NumberFormat.getCurrencyInstance()
                                        .parse(String.valueOf(snap.child("price").getValue()))
                                        .toString());
                            } catch (ParseException e){
                                e.printStackTrace();
                            }

                            String productID = snap.child("productID").getValue().toString();

                            if (productID == item.getProductID()){
                                isItemAlreadyInCart = true;
                                indexOfAlreadyPresentItem = tempIndex;
                            }

                            cartItems.add(new ShoppingItem(
                                    productID,
                                    snap.child("title").getValue().toString(),
                                    snap.child("type").getValue().toString(),
                                    snap.child("description").getValue().toString(),
                                    itemPrice,
                                    Integer.valueOf(snap.child("quantity").getValue().toString())
                            ));

                            tempIndex++;
                        }
                    }
                }

                Snackbar.make(
                        findViewById(R.id.addToCartProductPage),
                        "Adding to cart " + quantity + " items.",
                        Snackbar.LENGTH_SHORT).show();

                // if cart is empty, then create new cart and push when user adds stuff
                if(isCartEmpty){
                    cartItems = new ArrayList<>();
                    Map<String, Object> cartState = new HashMap<>();
                    cartState.put("isCartEmpty", Boolean.FALSE);
                    myRef.updateChildren(cartState);
                }

                if (isItemAlreadyInCart){
                    cartItems.get(indexOfAlreadyPresentItem)
                            .setQuantity(cartItems.get(indexOfAlreadyPresentItem).getQuantity() + quantity);
                } else {
                    item.setQuantity(quantity);
                    cartItems.add(item);
                }

                Map<String, Object> cartItemsMap = new HashMap<>();
                cartItemsMap.put("cartItems", cartItems);

                myRef.updateChildren(cartItemsMap);

                progressBar.setVisibility(View.GONE);
            }
        });

        shoppingCart = (FloatingActionButton) findViewById(R.id.cartProductPage);
        shoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ShoppingCartWindow.class));
            }
        });

        progressBar.setVisibility(View.GONE);
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

    void increment(){
        if (quantity < 5){
            quantity++;
            quantityView.setText(String.valueOf(quantity));
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Limit of 5 products only",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    void decrement(){
        if (quantity > 1){
            quantity--;
            quantityView.setText(String.valueOf(quantity));
        }
    }

}