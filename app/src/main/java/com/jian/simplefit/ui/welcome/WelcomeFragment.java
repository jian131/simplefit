package com.jian.simplefit.ui.welcome;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.jian.simplefit.R;
import com.jian.simplefit.databinding.FragmentWelcomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WelcomeFragment extends Fragment {
    private FragmentWelcomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupContinueButton();
        checkFirstLaunch();

        return view;
    }

    private void setupContinueButton() {
        binding.btnContinue.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_welcomeFragment_to_loginFragment)
        );
    }

    private void checkFirstLaunch() {
        // Kiểm tra lần đầu tiên mở ứng dụng
        Context context = requireContext();
        boolean isFirstLaunch = context.getSharedPreferences("SimpleFitPrefs", Context.MODE_PRIVATE)
                .getBoolean("isFirstLaunch", true);

        if (!isFirstLaunch) {
            // Nếu không phải lần đầu, chuyển thẳng đến màn hình đăng nhập
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_welcomeFragment_to_loginFragment);
        } else {
            // Đánh dấu đã không phải lần đầu tiên
            context.getSharedPreferences("SimpleFitPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstLaunch", false)
                    .apply();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}