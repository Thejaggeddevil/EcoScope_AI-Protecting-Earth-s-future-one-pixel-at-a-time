package com.himanshu.ecoscope


import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {
    private val db = FirebaseFirestore.getInstance()

    fun saveToFirestore(collection: String, document: String, data: Map<String, Any>) {
        db.collection(collection).document(document).set(data)
    }

    fun saveToFirestore(
        collection: String,
        document: String,
        data: Map<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        db.collection(collection).document(document).set(data)
            .addOnSuccessListener { onSuccess?.invoke() }
            .addOnFailureListener { onFailure?.invoke(it) }
    }

    fun updateDocument(
        collection: String,
        document: String,
        updates: Map<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        db.collection(collection).document(document).update(updates)
            .addOnSuccessListener { onSuccess?.invoke() }
            .addOnFailureListener { onFailure?.invoke(it) }
    }

    fun deleteDocument(
        collection: String,
        document: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        db.collection(collection).document(document).delete()
            .addOnSuccessListener { onSuccess?.invoke() }
            .addOnFailureListener { onFailure?.invoke(it) }
    }
}