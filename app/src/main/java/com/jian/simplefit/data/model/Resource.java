package com.jian.simplefit.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A generic class that holds a value with its loading status.
 * @param <T> Type of the resource data
 */
public class Resource<T> {

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * Creates a success resource with data
     * @param data The data to be included in the resource
     * @param <T> Type of the data
     * @return A success Resource with data
     */
    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    /**
     * Creates an error resource with an error message and optional data
     * @param msg The error message
     * @param data Optional data to be included in the resource
     * @param <T> Type of the data
     * @return An error Resource with message and optional data
     */
    public static <T> Resource<T> error(@Nullable String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    /**
     * Creates an error resource with an error message
     * @param msg The error message
     * @param <T> Type of the data
     * @return An error Resource with error message
     */
    public static <T> Resource<T> error(@Nullable String msg) {
        return new Resource<>(Status.ERROR, null, msg);
    }

    /**
     * Creates a loading resource with optional data
     * @param data Optional data to be included in the resource
     * @param <T> Type of the data
     * @return A loading Resource with optional data
     */
    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    /**
     * Creates a loading resource with a message but no data
     * This is the new method that will fix your compilation errors
     * @param message Loading message to display
     * @param <T> Type of the data (will be null in this case)
     * @return A loading Resource with the message
     */
    public static <T> Resource<T> loading(@Nullable String message) {
        return new Resource<>(Status.LOADING, null, message);
    }

    /**
     * Creates a loading resource with both data and a message
     * @param data Optional data to be included in the resource
     * @param message Loading message
     * @param <T> Type of the data
     * @return A loading Resource with data and message
     */
    public static <T> Resource<T> loading(@Nullable T data, @Nullable String message) {
        return new Resource<>(Status.LOADING, data, message);
    }

    /**
     * Get the status of the resource
     * @return The status
     */
    @NonNull
    public Status getStatus() {
        return status;
    }

    /**
     * Check if the resource is in loading state
     * @return True if loading, false otherwise
     */
    public boolean isLoading() {
        return status == Status.LOADING;
    }

    /**
     * Check if the resource is in success state
     * @return True if success, false otherwise
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * Check if the resource is in error state
     * @return True if error, false otherwise
     */
    public boolean isError() {
        return status == Status.ERROR;
    }

    /**
     * Enum representing the status of a resource
     */
    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}
