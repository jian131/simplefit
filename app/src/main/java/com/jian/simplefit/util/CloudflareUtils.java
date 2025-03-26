package com.jian.simplefit.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;
import java.util.UUID;

public class CloudflareUtils {
    private static final String TAG = "CloudflareUtils";
    private static final boolean DEV_MODE = true; // Set to false for production

    public interface OnCloudflareVerificationListener {
        void onSuccess(String token);
        void onError(String errorMessage);
    }

    public static void verifyWithCloudflare(Activity activity, OnCloudflareVerificationListener listener) {
        if (activity == null || activity.isFinishing()) {
            if (listener != null) {
                listener.onError("Activity không hợp lệ");
            }
            return;
        }

        if (DEV_MODE) {
            // Show a brief message for UX
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Đang xác thực bảo mật...", Toast.LENGTH_SHORT).show()
            );

            // Simulate verification delay (shorter for better UX)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                String simulatedToken = generateDevelopmentToken();
                Log.d(TAG, "DEV MODE: Generated token: " + simulatedToken);

                if (listener != null) {
                    listener.onSuccess(simulatedToken);
                }
            }, 800);
            return;
        }

        // For production: Request token from your backend
        requestTokenFromBackend(activity, listener);
    }

    private static String generateDevelopmentToken() {
        // Generate a token similar to Cloudflare format
        return UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().substring(0, 8);
    }

    private static void requestTokenFromBackend(Activity activity, OnCloudflareVerificationListener listener) {
        // This would be implemented with your actual API client
        // Here's a placeholder that simulates a network request

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Simulate network failure sometimes
            if (new Random().nextInt(10) < 1) { // 10% failure rate
                if (listener != null) {
                    listener.onError("Xác thực thất bại - Vui lòng thử lại");
                }
                return;
            }

            // Simulate success with token from server
            String token = "CF_TOKEN_" + System.currentTimeMillis();
            if (listener != null) {
                listener.onSuccess(token);
            }
        }, 1500);
    }
}