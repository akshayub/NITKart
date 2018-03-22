package com.ub.akshay.nitkart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OpenScreen extends AppCompatActivity {
    private static final String TAG = OpenScreen.class.getSimpleName();
    ProgressBar progressBar;
    private Button loginButton;
    private EditText user, pass;
    private TextView newUser, resetPassword, newSeller;
    private String username, password;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_screen);
        loginButton = (Button) findViewById(R.id.loginButton);
        user = (EditText) findViewById(R.id.usernameLogin);
        pass = (EditText) findViewById(R.id.passwordLogin);
        setInputs(true);
        newUser = (TextView) findViewById(R.id.newUserRegistration);
        newSeller = (TextView) findViewById(R.id.newSellerRegistration);
        resetPassword = (TextView) findViewById(R.id.forgotPassword);
        progressBar = (ProgressBar) findViewById(R.id.loginPageProgressBar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    FirebaseDatabase.getInstance().getReference("sellers/" + user.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Intent intent = new Intent(getApplicationContext(), MainAppPage.class);
                                    if (dataSnapshot.exists()) {
                                        intent.putExtra("isuserseller", true);
                                    } else {
                                        intent.putExtra("isuserseller", false);
                                    }
                                    Toast.makeText(getApplicationContext(), "Sign in Successful!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Failed to read value
                                    Log.w(TAG, "Failed to read value.", databaseError.toException());
                                }
                            });
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                username = user.getText().toString();
                password = pass.getText().toString();
                setInputs(false);
                signIn(username, password);
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OpenScreen.this, forgotPassword.class));
            }
        });

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newUserReg = new Intent(OpenScreen.this, newUser.class);
                newUserReg.putExtra("seller", false);
                startActivity(newUserReg);
                finish();
            }
        });

        newSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newSellerReg = new Intent(OpenScreen.this, newUser.class);
                newSellerReg.putExtra("seller", true);
                startActivity(newSellerReg);
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

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            setInputs(true);
                        }

                        // ...
                    }
                });
    }

    private void setInputs(boolean val) {
        user.setEnabled(val);
        pass.setEnabled(val);
    }
}
