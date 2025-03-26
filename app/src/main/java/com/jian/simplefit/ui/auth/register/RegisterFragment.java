package com.jian.simplefit.ui.auth.register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.jian.simplefit.R;
import com.jian.simplefit.databinding.FragmentRegisterBinding;
import com.jian.simplefit.model.user.User;
import com.jian.simplefit.ui.auth.AuthViewModel;
import com.jian.simplefit.util.CloudflareUtils;
import com.jian.simplefit.util.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupRegisterButton();
        observeRegisterResult();

        return binding.getRoot();
    }

    private void setupRegisterButton() {
        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (validateInputs(email, password, confirmPassword)) {
                showLoading(true);
                // Thay đổi từ RecaptchaUtils sang CloudflareUtils
                CloudflareUtils.verifyWithCloudflare(requireActivity(), new CloudflareUtils.OnCloudflareVerificationListener() {
                    @Override
                    public void onSuccess(String token) {
                        User user = new User();
                        user.setEmail(email);
                        authViewModel.registerUserWithCloudflare(email, password, user, token);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showLoading(false);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.btnRegister.setEnabled(!isLoading);
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            binding.etEmail.setError("Email không được để trống");
            return false;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Mật khẩu không được để trống");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Mật khẩu không khớp");
            return false;
        }
        return true;
    }

    private void observeRegisterResult() {
        authViewModel.getRegisterResult().observe(getViewLifecycleOwner(), resource -> {
            showLoading(resource.getStatus() == Resource.Status.LOADING);

            switch (resource.getStatus()) {
                case SUCCESS:
                    Toast.makeText(requireContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    navigateToMainApp();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(),
                            "Đăng ký thất bại: " + resource.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void navigateToMainApp() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_registerFragment_to_exerciseListFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}