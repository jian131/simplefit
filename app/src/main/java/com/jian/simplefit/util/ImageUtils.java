package com.jian.simplefit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Lớp tiện ích xử lý và hiển thị hình ảnh trong ứng dụng
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int JPEG_QUALITY = 85;

    /**
     * Tải hình ảnh từ URL hoặc sử dụng placeholder
     * @param context Context
     * @param url URL của hình ảnh
     * @param imageView ImageView hiển thị hình ảnh
     * @param placeholderId ID resource của hình ảnh mặc định
     */
    public static void loadImage(Context context, String url, ImageView imageView, int placeholderId) {
        if (url != null && !url.isEmpty()) {
            RequestOptions options = new RequestOptions()
                    .placeholder(placeholderId)
                    .error(placeholderId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(url)
                    .apply(options)
                    .into(imageView);
        } else {
            loadDrawableResource(context, placeholderId, imageView);
        }
    }

    /**
     * Tải hình ảnh bài tập vào ImageView
     * @param context Context
     * @param exercise Bài tập cần tải hình ảnh
     * @param imageView ImageView hiển thị hình ảnh
     */
    public static void loadExerciseImage(Context context, Exercise exercise, ImageView imageView) {
        if (exercise == null || imageView == null) return;

        // Ưu tiên tải từ URL
        if (exercise.getImageUrl() != null && !exercise.getImageUrl().toString().isEmpty()) {
            loadImageFromUrl(context, exercise.getImageUrl().toString(), imageView);
            return;
        }

        // Thử tải từ tên tài nguyên
        if (exercise.getImageResource() != null && !exercise.getImageResource().isEmpty()) {
            int resourceId = getResourceIdentifier(context, exercise.getImageResource(), "drawable");
            if (resourceId != 0) {
                loadDrawableResource(context, resourceId, imageView);
                return;
            }
        }

        // Sử dụng hình ảnh mặc định dựa trên nhóm cơ chính
        if (exercise.getPrimaryMuscleGroup() != null) {
            String muscleResourceName = "muscle_" + exercise.getPrimaryMuscleGroup().toLowerCase();
            int muscleResource = getResourceIdentifier(context, muscleResourceName, "drawable");
            if (muscleResource != 0) {
                loadDrawableResource(context, muscleResource, imageView);
                return;
            }
        }

        // Mặc định nếu không có hình ảnh nào khác
        loadDrawableResource(context, R.drawable.ic_exercise_default, imageView);
    }


    /**
     * Tải hình ảnh từ URL sử dụng Glide
     * @param context Context
     * @param url URL của hình ảnh
     * @param imageView ImageView hiển thị hình ảnh
     */
    public static void loadImageFromUrl(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_exercise_default)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * Tải hình ảnh từ tài nguyên drawable
     * @param context Context
     * @param resourceId ID tài nguyên drawable
     * @param imageView ImageView hiển thị hình ảnh
     */
    public static void loadDrawableResource(Context context, int resourceId, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_exercise_default);

        Glide.with(context)
                .load(resourceId)
                .apply(options)
                .into(imageView);
    }

    /**
     * Tải hình ảnh nhóm cơ được highlight
     * @param context Context
     * @param resourceId ID tài nguyên drawable của nhóm cơ highlight
     * @param imageView ImageView hiển thị hình ảnh
     */
    public static void loadHighlightedMuscleImage(Context context, int resourceId, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(resourceId)
                .apply(options)
                .into(imageView);
    }

    /**
     * Lấy ID tài nguyên từ tên
     * @param context Context
     * @param resourceName Tên tài nguyên
     * @param resourceType Loại tài nguyên (drawable, raw, ...)
     * @return ID tài nguyên hoặc 0 nếu không tìm thấy
     */
    public static int getResourceIdentifier(Context context, String resourceName, String resourceType) {
        return context.getResources().getIdentifier(resourceName, resourceType, context.getPackageName());
    }

    /**
     * Tạo đường dẫn mới cho hình ảnh trong bộ nhớ nội bộ ứng dụng
     * @param context Context
     * @param fileName Tên tập tin
     * @return File chứa đường dẫn
     */
    public static File createImageFile(Context context, String fileName) {
        File storageDir = context.getFilesDir();
        return new File(storageDir, fileName + ".jpg");
    }

    /**
     * Nén hình ảnh từ Uri và lưu vào bộ nhớ nội bộ
     * @param context Context
     * @param imageUri Uri của hình ảnh
     * @param fileName Tên tập tin để lưu
     * @return Đường dẫn của tập tin đã lưu hoặc null nếu có lỗi
     */
    public static String compressAndSaveImage(Context context, Uri imageUri, String fileName) {
        try {
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            if (input == null) return null;

            // Đọc bitmap từ input stream
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();

            if (bitmap == null) return null;

            // Thay đổi kích thước nếu cần
            bitmap = resizeBitmapIfNeeded(bitmap);

            // Lưu bitmap vào tập tin
            File outputFile = createImageFile(context, fileName);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);
            outputStream.close();

            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    /**
     * Thay đổi kích thước bitmap nếu cần
     * @param bitmap Bitmap gốc
     * @return Bitmap đã thay đổi kích thước
     */
    private static Bitmap resizeBitmapIfNeeded(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap;
        }

        // Tính toán tỷ lệ mới
        float ratio;
        if (width > height) {
            ratio = (float) MAX_IMAGE_DIMENSION / width;
        } else {
            ratio = (float) MAX_IMAGE_DIMENSION / height;
        }

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Chuyển đổi bitmap thành mảng byte
     * @param bitmap Bitmap cần chuyển đổi
     * @return Mảng byte
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream);
        return stream.toByteArray();
    }

    /**
     * Áp dụng hiệu ứng xám cho một drawable
     * @param context Context
     * @param drawableId ID của drawable
     * @return Drawable đã xử lý
     */
    public static Drawable getGrayedDrawable(Context context, int drawableId) {
        Drawable originalDrawable = ContextCompat.getDrawable(context, drawableId);
        if (originalDrawable == null) return null;

        Drawable.ConstantState state = originalDrawable.getConstantState();
        if (state == null) return originalDrawable;

        Drawable drawable = state.newDrawable().mutate();

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        drawable.setColorFilter(new ColorMatrixColorFilter(matrix));

        return drawable;
    }

    /**
     * Tạo thumbnail từ bitmap với kích thước nhỏ hơn
     * @param bitmap Bitmap gốc
     * @param maxSize Kích thước lớn nhất cho thumbnail
     * @return Bitmap thumbnail
     */
    public static Bitmap createThumbnail(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float ratio;
        if (width > height) {
            ratio = (float) maxSize / width;
        } else {
            ratio = (float) maxSize / height;
        }

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Tải hình ảnh người dùng vào ImageView
     * @param context Context
     * @param photoUrl URL của hình ảnh người dùng
     * @param imageView ImageView hiển thị hình ảnh
     */
    public static void loadUserImage(Context context, String photoUrl, ImageView imageView) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            loadImageFromUrl(context, photoUrl, imageView);
        } else {
            loadDrawableResource(context, R.drawable.ic_user_default, imageView);
        }
    }

    /**
     * Lấy drawable từ ImageView
     * @param imageView ImageView chứa drawable
     * @return Bitmap từ drawable hoặc null
     */
    public static Bitmap getBitmapFromImageView(ImageView imageView) {
        if (imageView == null || imageView.getDrawable() == null) return null;

        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        return null;
    }
}