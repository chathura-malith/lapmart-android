package com.metkring.lapmart.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.metkring.lapmart.R;
import com.metkring.lapmart.databinding.FragmentSignInBinding;

import es.dmoral.toasty.Toasty;

public class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavHide();
        binding.btnSignIn.setOnClickListener(v -> signInWithEmail());
        binding.btnGoogleSignIn.setOnClickListener(v -> googleSignIn());
        goToSignUp();
    }

    private void signInWithEmail() {
        String email = binding.etSignInEmail.getText().toString().trim();
        String password = binding.etSignInPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignInEmail.setError("Valid email is required");
            binding.tilSignInEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            binding.etSignInPassword.setError("Password is required");
            binding.tilSignInPassword.requestFocus();
            return;
        }
        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toasty.success(requireContext(), "Login Successful!").show();
                        navigateToHome();
                    } else {
                        Toasty.error(requireContext(), "Login failed please check your credentials").show();
                    }
                });
    }

    private void googleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CredentialManager credentialManager = CredentialManager.create(requireContext());

        setLoading(true);
        credentialManager.getCredentialAsync(requireActivity(), request, null, Runnable::run,
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignIn(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e("signInError", e.getMessage());
                    }
                });
    }

    private void handleGoogleSignIn(GetCredentialResponse result) {
        try {
            GoogleIdTokenCredential credential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
            AuthCredential authCredential = GoogleAuthProvider.getCredential(credential.getIdToken(), null);

            mAuth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    Toasty.success(requireContext(), "Login Successful!").show();
                    navigateToHome();
                }
            });
        } catch (Exception e) {
            setLoading(false);
            Log.e("signInError", e.getMessage());
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSignIn.setEnabled(false);
            binding.btnGoogleSignIn.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSignIn.setEnabled(true);
            binding.btnGoogleSignIn.setEnabled(true);
        }
    }

    private void navigateToHome() {
        loadSignUpFragment(new UserFragment());
    }

    private void goToSignUp() {
        binding.tvGoToSignUp.setOnClickListener(v -> {
            loadSignUpFragment(new SignUpFragment());
        });
    }

    private void loadSignUpFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void bottomNavHide() {
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }
}