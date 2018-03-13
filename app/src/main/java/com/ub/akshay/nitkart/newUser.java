package com.ub.akshay.nitkart;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.ub.akshay.nitkart.R.id.userRegistrationPageTitle;

public class newUser extends AppCompatActivity {

    private static final String TAG = OpenScreen.class.getSimpleName();
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button mRegister;
    private String email, password, passwordVerification;
    private EditText username, pass, passVerification, firstname, lastname;
    private boolean isRegistrationClicked = false, isSeller = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        username = (EditText) findViewById(R.id.usernameRegistration);
        pass = (EditText) findViewById(R.id.passwordRegistration);
        passVerification = (EditText) findViewById(R.id.passwordRegistrationConfirmation);
        firstname = (EditText) findViewById(R.id.firstName);
        lastname = (EditText) findViewById(R.id.lastName);

        isSeller = getIntent().getExtras().getBoolean("seller");
        if(isSeller){
            ((TextView) findViewById(R.id.userRegistrationPageTitle)).setText("Seller Registration");
        }

        setViews(true);

        progressBar = (ProgressBar) findViewById(R.id.registrationPageProgressBar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    String name = firstname.getText().toString() + " " + lastname.getText().toString();
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().
                            setDisplayName(name).build();
                    user.updateProfile(profileChangeRequest);

                    DatabaseReference myRef;

                    if (!isSeller){
                        myRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                        myRef.child(user.getUid()).push();

                        // As firebase does not accept keys with empty values, I'm putting a dummy item with empty Strings and -1 as ints
                        // Quantity of items in cart is not realtime database quantity but the quantity the user wants
                        ArrayList<ShoppingItem> cart = new ArrayList<>();
                        cart.add(new ShoppingItem("", "", "", "", -1, -1));
                        Map<String, Object> cartItems = new HashMap<>();
                        cartItems.put("cartItems", cart);

                        // Adding a isCartEmpty State Variable for cart window display

                        Map<String, Object> cartState = new HashMap<>();
                        cartState.put("isCartEmpty", Boolean.TRUE);

                        // Updating the database for the user
                        myRef.updateChildren(cartItems);
                        myRef.updateChildren(cartState);
                    } else {
                        myRef = FirebaseDatabase.getInstance().getReference("sellers").child(user.getUid());
                        myRef.child(user.getUid()).push();

//                        Dummy product sold by any seller who has 0 products
                        ArrayList<ShoppingItem> prods = new ArrayList<>();
                        prods.add(new ShoppingItem("", "", "", "", -1, -1));
                        Map<String, Object> prodslist = new HashMap<>();
                        prodslist.put("products", prods);

                        Map<String, Object> state = new HashMap<>();
                        state.put("isEmpty", Boolean.TRUE);

                        // Updating the database for the seller
                        myRef.updateChildren(prodslist);
                        myRef.updateChildren(state);
                    }

                    sendVerificationEmail();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mRegister = (Button) findViewById(R.id.registerButton);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setViews(false);
                email = username.getText().toString();
                password = pass.getText().toString();
                passwordVerification = passVerification.getText().toString();
                if (password.equals(passwordVerification) && !password.equals("") && !passwordVerification.equals("")) {
                    createAccount();
                } else {
                    Snackbar.make(findViewById(R.id.newUserPage), "Passwords don't match", Snackbar.LENGTH_SHORT).show();
                    pass.setText("");
                    passVerification.setText("");
                    setViews(true);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("firstname", firstname.getText().toString());
        outState.putString("lastname", lastname.getText().toString());
        outState.putString("email", username.getText().toString());
        setViews(false);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        firstname.setText(savedInstanceState.getString("firstname"));
        lastname.setText(savedInstanceState.getString("lastname"));
        username.setText(savedInstanceState.getString("email"));
        setViews(true);
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

    private void createAccount() {
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        isRegistrationClicked = true;
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
//                            Toast.makeText(newUser.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                            Toast.makeText(newUser.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            setViews(true);
                            isRegistrationClicked = false;
                            progressBar.setVisibility(View.GONE);
                        }
                        // ...
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Email sent
                            finish();
                        } else {
                            // overridePendingTransition(0, 0);
                            // finish();
                            // overridePendingTransition(0, 0);
                            // startActivity(getIntent());
                            sendVerificationEmail();
                        }
                    }
                });
    }

    @Override
    public void finish() {
        FirebaseAuth.getInstance().signOut();
        progressBar.setVisibility(View.GONE);
        if (isRegistrationClicked) {
            Toast.makeText(getApplicationContext(), "Verify Email and Login", Toast.LENGTH_LONG).show();
        }
        startActivity(new Intent(getApplicationContext(), OpenScreen.class));
        super.finish();
    }

    private void setViews(boolean val) {
        username.setEnabled(val);
        pass.setEnabled(val);
        firstname.setEnabled(val);
        lastname.setEnabled(val);
        passVerification.setEnabled(val);
    }
}
