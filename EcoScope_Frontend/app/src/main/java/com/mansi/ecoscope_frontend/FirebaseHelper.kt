package com.mansi.ecoscope_frontend





import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()

    private fun getUid(): String? = FirebaseService.getCurrentUserId()

    fun saveContact(number: String, name: String? = null) {
        val uid = getUid()
        if (uid == null) {
            Log.e("FirebaseHelper", "❌ Cannot save contact: User not logged in.")
            return
        }

        val data = hashMapOf(
            "contact" to number,
            "name" to (name ?: "Unknown"),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection("contacts")
            .add(data)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "✅ Contact saved successfully.")
            }
            .addOnFailureListener {
                Log.e("FirebaseHelper", "❌ Failed to save contact: ${it.message}")
            }
    }

    fun deleteContact(context: Context, number: String) {
        val uid = getUid()
        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(uid)
            .collection("contacts")
            .whereEqualTo("contact", number)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot.documents) {
                    doc.reference.delete()
                }
                Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()
                Log.d("FirebaseHelper", "✅ Contact deleted.")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "❌ Failed to delete contact", e)
                Toast.makeText(context, "Failed to delete contact", Toast.LENGTH_SHORT).show()
            }
    }







    fun debugPrintUserName(context: Context) {
        val uid = getUid() ?: return
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name")
                Log.d("FirebaseHelper", "👤 Firestore name = $name")
                Toast.makeText(context, "Firestore name: $name", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Log.e("FirebaseHelper", "❌ Failed to fetch user document: ${it.message}")
            }
    }


}
