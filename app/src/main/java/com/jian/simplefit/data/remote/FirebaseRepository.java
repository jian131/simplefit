package com.jian.simplefit.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

/**
 * Base repository class for Firebase operations
 * Provides common methods for Firebase Firestore and Storage interactions
 */
public abstract class FirebaseRepository {
    protected FirebaseFirestore db;
    protected FirebaseStorage storage;
    protected StorageReference storageRef;

    public FirebaseRepository() {
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Configure Firestore settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    /**
     * Add a new document to a collection
     * @param collection Collection name
     * @param document Document data object
     * @param listener Callback listener
     * @param <T> Type of document
     */
    protected <T> void addDocument(String collection, T document, OnCompleteListener<Void> listener) {
        db.collection(collection)
                .document()
                .set(document)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Add a document with specific ID to a collection
     * @param collection Collection name
     * @param id Document ID
     * @param document Document data object
     * @param listener Callback listener
     * @param <T> Type of document
     */
    protected <T> void addDocumentWithId(String collection, String id, T document, OnCompleteListener<Void> listener) {
        db.collection(collection)
                .document(id)
                .set(document)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Update fields in an existing document
     * @param collection Collection name
     * @param id Document ID
     * @param updates Map of field updates
     * @param listener Callback listener
     */
    protected void updateDocument(String collection, String id, Map<String, Object> updates, OnCompleteListener<Void> listener) {
        db.collection(collection)
                .document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Delete a document from a collection
     * @param collection Collection name
     * @param id Document ID
     * @param listener Callback listener
     */
    protected void deleteDocument(String collection, String id, OnCompleteListener<Void> listener) {
        db.collection(collection)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Get a document by ID
     * @param collection Collection name
     * @param id Document ID
     * @param listener Callback listener
     */
    protected void getDocument(String collection, String id, OnDocumentListener listener) {
        db.collection(collection)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onSuccess(documentSnapshot);
                    } else {
                        listener.onFailure(new Exception("Document not found"));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Interface for operation completion callbacks
     * @param <T> Type of result
     */
    public interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    /**
     * Interface for document retrieval callbacks
     */
    public interface OnDocumentListener {
        void onSuccess(com.google.firebase.firestore.DocumentSnapshot document);
        void onFailure(Exception e);
    }
}