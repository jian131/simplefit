package com.jian.simplefit.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jian.simplefit.R;
import com.jian.simplefit.databinding.FragmentProfileBinding;
import com.jian.simplefit.model.user.User;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserProfile();
        setupSaveButton();
        setupSignOutButton();

        return binding.getRoot();
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        binding.etName.setText(user.getName());
                        binding.etWeight.setText(user.getWeight() > 0 ? String.valueOf(user.getWeight()) : "");
                        binding.etHeight.setText(user.getHeight() > 0 ? String.valueOf(user.getHeight()) : "");

                        if (user.getWeight() > 0 && user.getHeight() > 0) {
                            calculateBMI(user.getWeight(), user.getHeight());
                        }
                    }
                });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            double weight = parseDouble(binding.etWeight.getText().toString());
            double height = parseDouble(binding.etHeight.getText().toString());

            updateUserProfile(name, weight, height);
        });
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateUserProfile(String name, double weight, double height) {
        String userId = mAuth.getCurrentUser().getUid();

        firestore.collection("users").document(userId)
                .update(
                        "name", name,
                        "weight", weight,
                        "height", height
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    if (weight > 0 && height > 0) {
                        calculateBMI(weight, height);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateBMI(double weight, double height) {
        double bmi = weight / Math.pow(height / 100, 2);
        String bmiCategory = getBMICategory(bmi);
        binding.tvBmiResult.setText(String.format("BMI: %.1f (%s)", bmi, bmiCategory));
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Thiếu cân";
        if (bmi < 25) return "Bình thường";
        if (bmi < 30) return "Thừa cân";
        return "Béo phì";
    }

    private void setupSignOutButton() {
        binding.btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileFragment_to_loginFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}