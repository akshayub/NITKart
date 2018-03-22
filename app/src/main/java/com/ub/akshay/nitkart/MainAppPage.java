package com.ub.akshay.nitkart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class MainAppPage extends AppCompatActivity {

    public final String TAG = MainAppPage.class.getSimpleName();
    ListView shoppingItemView;
    ShoppingListAdapter adapter;
    ProgressBar progressBar;
    EditText searchbar;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    TextView ifSellerListEmpty;
    Button addProduct;

    private Boolean exit = false;
    private ArrayList<ShoppingItem> shoppingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(getIntent().getExtras().getBoolean("isuserseller")){
            setContentView(R.layout.activity_seller_main_page);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("NITKart Sellers");
            setSupportActionBar(toolbar);

            searchbar = (EditText)findViewById(R.id.searchBar);

            progressBar = (ProgressBar) findViewById(R.id.sellerPageProgressBar);
            progressBar.setVisibility(View.VISIBLE);

            addProduct = (Button) findViewById(R.id.sellerAddProduct);
            shoppingItemView = (ListView) findViewById(R.id.sellerProductList);

            ifSellerListEmpty = (TextView) findViewById(R.id.ifSellerListEmpty);

            addProduct = (Button) findViewById(R.id.sellerAddProduct);

            DatabaseReference myref = database.getReference("sellers/" +
                    FirebaseAuth.getInstance().getCurrentUser().getUid());
            myref.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(Boolean.valueOf(dataSnapshot.child("isProdsEmpty").getValue().toString())){
                        shoppingItemView.setVisibility(View.GONE);
                        searchbar.setVisibility(View.GONE);
                        ifSellerListEmpty.setVisibility(View.VISIBLE);
                    } else {
                        shoppingItemView.setVisibility(View.VISIBLE);
                        ifSellerListEmpty.setVisibility(View.GONE);

                        shoppingItems = setUpList(dataSnapshot.child("products"));
                        adapter = new ShoppingListAdapter(getApplicationContext(), shoppingItems);
                        shoppingItemView.setAdapter(adapter);

                        shoppingItemView.setTextFilterEnabled(true);
                        searchbar.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                int textlength = charSequence.length();
                                ArrayList<ShoppingItem> tempShoppingItems = new ArrayList<>();
                                for(ShoppingItem x: shoppingItems){
                                    if (textlength <= x.getTitle().length()) {
                                        if (x.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                            tempShoppingItems.add(x);
                                        }
                                    }
                                }
                                shoppingItemView.setAdapter(new ShoppingListAdapter(getApplicationContext(), tempShoppingItems));
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Failed to read value.", databaseError.toException());
                }
            });

            addProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // add new layout and add product
                    // then search
                    startActivity(new Intent(MainAppPage.this, AddProductForm.class));
                }
            });

            shoppingItemView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent productIntent = new Intent(MainAppPage.this, IndividualProductSeller.class);
                    productIntent.putExtra("product", shoppingItems.get(i));
                    startActivity(productIntent);
                }
            });

        } else {
            setContentView(R.layout.activity_main_app_page);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("NITKart");
            setSupportActionBar(toolbar);

            searchbar = (EditText)findViewById(R.id.searchBar);

            FloatingActionButton shoppingCart = (FloatingActionButton) findViewById(R.id.cartMainPage);
            shoppingCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), ShoppingCartWindow.class));
                }
            });

            progressBar = (ProgressBar) findViewById(R.id.mainPageProgressBar);
            shoppingItemView = (ListView) findViewById(R.id.shoppingList);

            DatabaseReference myRef = database.getReference("items");
            myRef.addValueEventListener(new ValueEventListener() {
                // This listener is only for database with reference of key "items"
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    // Now the Shopping List gets updated whenever the data changes in the server
                    shoppingItems = getAllItems(dataSnapshot);
                    adapter = new ShoppingListAdapter(getApplicationContext(), shoppingItems);
                    progressBar.setVisibility(View.GONE);
                    shoppingItemView.setAdapter(adapter);

                    shoppingItemView.setTextFilterEnabled(true);
                    searchbar.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            int textlength = charSequence.length();
                            ArrayList<ShoppingItem> tempShoppingItems = new ArrayList<>();
                            for(ShoppingItem x: shoppingItems){
                                if (textlength <= x.getTitle().length()) {
                                    if (x.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                        tempShoppingItems.add(x);
                                    }
                                }
                            }
                            shoppingItemView.setAdapter(new ShoppingListAdapter(getApplicationContext(), tempShoppingItems));
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            shoppingItemView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent productIntent = new Intent(MainAppPage.this, IndividualProduct.class);
                    productIntent.putExtra("product", shoppingItems.get(i));
                    startActivity(productIntent);
                }
            });

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_app_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logoutItem) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), OpenScreen.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // For exiting the application
    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            Snackbar.make(findViewById(R.id.main_content), "Press back again to exit", Snackbar.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 2000);
        }
    }

    public static ArrayList<ShoppingItem> getAllItems(DataSnapshot dataSnapshot){

        ArrayList<ShoppingItem> items  = new ArrayList<ShoppingItem>();

        for (DataSnapshot item : dataSnapshot.getChildren()) {

            items.add(new ShoppingItem(
                    item.child("productID").getValue().toString(),
                    item.child("name").getValue().toString(),
                    item.child("type").getValue().toString(),
                    item.child("description").getValue().toString(),
                    Integer.valueOf(item.child("price").getValue().toString()),
                    Integer.valueOf(item.child("quantity").getValue().toString())
            ));

        }

        return items;
    }

    public static ArrayList<ShoppingItem> setUpList(DataSnapshot dataSnapshot) {

        ArrayList<ShoppingItem> items  = new ArrayList<ShoppingItem>();

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
                    snap.child("productID").getValue().toString(),
                    snap.child("title").getValue().toString(),
                    snap.child("type").getValue().toString(),
                    snap.child("description").getValue().toString(),
                    itemPrice,
                    quantity
            ));
        }

        return items;

    }

}
