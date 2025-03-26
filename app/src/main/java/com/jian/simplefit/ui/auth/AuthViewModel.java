package com.jian.simplefit.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jian.simplefit.model.user.User;
import com.jian.simplefit.repository.user.AuthRepository;
import com.jian.simplefit.util.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<User>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<User>> loginResult = new MutableLiveData<>();

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Resource<User>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<Resource<User>> getLoginResult() {
        return loginResult;
    }

    /**
     * Đăng nhập với xác thực Cloudflare Turnstile
     */
    public void loginUserWithCloudflare(String email, String password, String cloudflareToken) {
        loginResult.setValue(Resource.loading(null));
        authRepository.loginUserWithCloudflare(email, password, cloudflareToken)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User();
                        user.setEmail(email);
                        loginResult.setValue(Resource.success(user));
                    } else {
                        loginResult.setValue(Resource.error(
                                task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại",
                                null
                        ));
                    }
                });
    }

    /**
     * Đăng ký với xác thực Cloudflare Turnstile
     */
    public void registerUserWithCloudflare(String email, String password, User user, String cloudflareToken) {
        registerResult.setValue(Resource.loading(null));
        authRepository.registerUserWithCloudflare(email, password, user, cloudflareToken)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        registerResult.setValue(Resource.success(user));
                    } else {
                        registerResult.setValue(Resource.error(
                                task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại",
                                null
                        ));
                    }
                });
    }

    // Các phương thức gốc để tương thích ngược
    public void registerUser(String email, String password, User user) {
        registerResult.setValue(Resource.loading(null));
        authRepository.registerUser(email, password, user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        registerResult.setValue(Resource.success(user));
                    } else {
                        registerResult.setValue(Resource.error(
                                task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại",
                                null
                        ));
                    }
                });
    }

    public void loginUser(String email, String password) {
        loginResult.setValue(Resource.loading(null));
        authRepository.loginUser(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User();
                        user.setEmail(email);
                        loginResult.setValue(Resource.success(user));
                    } else {
                        loginResult.setValue(Resource.error(
                                task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại",
                                null
                        ));
                    }
                });
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    public void logout() {
        authRepository.logoutUser();
    }
}