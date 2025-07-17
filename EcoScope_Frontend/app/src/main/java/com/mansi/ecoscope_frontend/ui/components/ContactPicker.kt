package com.mansi.ecoscope_frontend.ui.components




import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactPicker {

    data class ContactData(val name: String, val number: String)

    fun extractContact(context: Context, uri: Uri?): ContactData? {
        if (uri == null) return null

        // Step 1: Get contact ID
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
            null,
            null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val contactId = cursor.getString(idIndex)
            val name = cursor.getString(nameIndex) ?: "Unknown"
            cursor.close()

            // Step 2: Now query phone number using contact ID
            val phonesCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )

            phonesCursor?.use {
                if (it.moveToFirst()) {
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val number = it.getString(numberIndex)?.replace("\\s|-".toRegex(), "") ?: return null
                    return ContactData(name = name, number = number)
                }
            }
        }

        return null
    }
}
