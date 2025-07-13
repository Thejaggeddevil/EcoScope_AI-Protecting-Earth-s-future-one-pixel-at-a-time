package com.himanshu.ecoscope

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    fun writeDocument(
        collection: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection(collection).add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun readCollection(
        collection: String,
        onComplete: (List<Map<String, Any>>) -> Unit
    ) {
        db.collection(collection).get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.map { it.data }
                onComplete(data)
            }
    }

    fun readDocument(
        collection: String,
        document: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection(collection).document(document).get()
            .addOnSuccessListener { documentSnapshot ->
                onSuccess(documentSnapshot.data)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateDocument(
        collection: String,
        document: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection(collection).document(document).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteDocument(
        collection: String,
        document: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection(collection).document(document).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
