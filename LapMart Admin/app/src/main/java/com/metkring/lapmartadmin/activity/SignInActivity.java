package com.metkring.lapmartadmin.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.databinding.ActivitySignInBinding;

import es.dmoral.toasty.Toasty;


public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private TextInputEditText etEmail, etPassword;
    private Button btnSignIn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        etEmail = binding.etSignInEmail;
        etPassword = binding.etSignInPassword;
        btnSignIn = binding.btnSignIn;
        progressBar = binding.progressBar;

        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(v -> loginAdmin());
    }


    private void loginAdmin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            binding.tilSignInEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please provide a valid email");
            binding.tilSignInEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            binding.tilSignInPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Min password length should be 6 characters");
            binding.tilSignInPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSignIn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toasty.success(this, "Login Successful!").show();
                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toasty.error(this, "Login failed please check your credentials").show();
                    }
                });
    }
}