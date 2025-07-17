package com.mansi.ecoscope_frontend.ui.components





import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class ContactInfo(
    val name: String,
    val number: String
)

object ContactManager {

    @SuppressLint("Range")
    fun getAllContacts(context: Context): List<ContactInfo> {
        val contactList = mutableListOf<ContactInfo>()

        // Permission check
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)?.trim() ?: continue
                val number = it.getString(numberIndex)?.replace("\\s|-".toRegex(), "") ?: continue
                contactList.add(ContactInfo(name, number))
            }
        }

        return contactList
    }
}
