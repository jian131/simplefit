package com.jian.simplefit.ui.auth.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.jian.simplefit.R;
import com.jian.simplefit.ui.auth.AuthViewModel;
import com.jian.simplefit.util.CloudflareUtils;
import com.jian.simplefit.util.Resource;
import com.jian.simplefit.util.ValidationUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Khởi tạo các thành phần UI
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvRegister = view.findViewById(R.id.tv_register);
        progressBar = view.findViewById(R.id.progress_bar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Thiết lập listener và observer
        setupListeners();
        observeLoginResult();

        return view;
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(email, password)) {
                showLoading(true);
                // Xác thực với Cloudflare Turnstile
                CloudflareUtils.verifyWithCloudflare(requireActivity(), new CloudflareUtils.OnCloudflareVerificationListener() {
                    @Override
                    public void onSuccess(String token) {
                        authViewModel.loginUserWithCloudflare(email, password, token);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showLoading(false);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tvRegister.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.registerFragment);
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!isLoading);
    }

    private boolean validateInputs(String email, String password) {
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Email không hợp lệ");
            return false;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        return true;
    }

    private void observeLoginResult() {
        authViewModel.getLoginResult().observe(getViewLifecycleOwner(), resource -> {
            showLoading(resource.getStatus() == Resource.Status.LOADING);

            switch (resource.getStatus()) {
                case SUCCESS:
                    Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_loginFragment_to_exerciseListFragment);
                    break;
                case ERROR:
                    Toast.makeText(requireContext(),
                            "Đăng nhập thất bại: " + resource.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}