package com.metkring.lapmart.fragment;

import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.metkring.lapmart.R;
import com.metkring.lapmart.databinding.FragmentSignUpBinding;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signUp();
        googleSignIn();
        goToLogin();
    }

    private void signUp(){
        binding.btnSignUp.setOnClickListener(v -> {
            registerUser();
        });
    }

    private void googleSignIn(){

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CredentialManager credentialManager = CredentialManager.create(requireContext());

        binding.btnGoogleSignUp.setOnClickListener(v -> {
            setLoading(true);
            credentialManager.getCredentialAsync(
                    requireActivity(),
                    request,
                    null,
                    Runnable::run,
                    new CredentialManagerCallback<>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            handleSignIn(result);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            setLoading(false);
                            Log.e("signInGoogleError", "Error signing in with Google", e);
                        }
                    }
            );
        });
    }

    private void handleSignIn(GetCredentialResponse result) {
        try {
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
            String idToken = googleIdTokenCredential.getIdToken();


            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                save(user);
                            }
                        } else {
                            setLoading(false);
                            Toasty.error(requireContext(), "Auth failed try again").show();
                        }
                    });
        } catch (Exception e) {
            Log.e("signInGoogleError", "Error signing in with Google", e);
        }
    }

    private void save(FirebaseUser user) {
        String fullName = user.getDisplayName();
        String firstName = "";
        String lastName = "";

        if (fullName != null && fullName.contains(" ")) {
            String[] nameParts = fullName.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts[1];
        } else {
            firstName = fullName;
        }
        saveUserToFirestore(user, firstName, lastName);

    }

    private void registerUser() {
        String firstName = binding.etSignUpFirstName.getText().toString().trim();
        String lastName = binding.etSignUpLastName.getText().toString().trim();
        String email = binding.etSignUpEmail.getText().toString().trim();
        String password = binding.etSignUpPassword.getText().toString().trim();
        String confirmPass = binding.etSignUpConfirmPassword.getText().toString().trim();

        if (firstName.isEmpty()) {
            binding.etSignUpFirstName.setError("First name is required");
            binding.tilSignUpFirstName.requestFocus();
            return;
        } if (lastName.isEmpty()) {
            binding.etSignUpLastName.setError("Last name is required");
            binding.tilSignUpLastName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            binding.etSignUpEmail.setError("Email is required");
            binding.tilSignUpEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignUpEmail.setError("Invalid email");
            binding.tilSignUpEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            binding.etSignUpPassword.setError("Password must be at least 6 characters");
            binding.tilSignUpPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPass)) {
            binding.etSignUpConfirmPassword.setError("Passwords do not match");
            binding.tilSignUpConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, firstName, lastName);
                        }
                    } else {
                        setLoading(false);
                        Toasty.error(requireContext(), "Registration Failed: " + task.getException().getMessage()).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String firstName, String lastName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("email", user.getEmail());
        userMap.put("profileImage", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userMap.put("role", "user");


        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toasty.success(getContext(), "You're successfully registered",
                            Toast.LENGTH_SHORT).show();
                    loadSignUpFragment(new SignInFragment());
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("FirestoreError", "Error saving user", e);
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSignUp.setEnabled(false);
            binding.btnGoogleSignUp.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSignUp.setEnabled(true);
            binding.btnGoogleSignUp.setEnabled(true);
        }
    }

    private void goToLogin(){
        binding.tvGoToLogin.setOnClickListener(v -> {
            loadSignUpFragment(new SignInFragment());
        });
    }

    private void loadSignUpFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
