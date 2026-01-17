package com.example.myapplication.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.provider.ContactsContract
import com.example.myapplication.R
import com.example.myapplication.data.enumm.MediaFile

object RingtoneUtils {

    fun setAsRingtone(context: Context, mediaFile: MediaFile?) {
        if (mediaFile?.uri == null) {
            context.showToast(context.getString(R.string.audio_file_not_available))
            return
        }

        try {
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                mediaFile.uri
            )
            context.showToast(context.getString(R.string.set_as_ringtone_successfully))
        } catch (e: Exception) {
            context.showToast(context.getString(R.string.failed_to_set_contact_ringtone))
        }
    }

    fun setAsAlarm(context: Context, mediaFile: MediaFile?) {
        if (mediaFile?.uri == null) {
            context.showToast(context.getString(R.string.audio_file_not_available))
            return
        }

        try {
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_ALARM,
                mediaFile.uri
            )
            context.showToast(context.getString(R.string.set_as_alarm_tone_successfully))
        } catch (e: Exception) {
            context.showToast(context.getString(R.string.failed_to_set_as_alarm))
        }
    }

    fun setAsNotification(context: Context, mediaFile: MediaFile?) {
        if (mediaFile?.uri == null) {
            context.showToast(context.getString(R.string.audio_file_not_available))
            return
        }

        try {
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_NOTIFICATION,
                mediaFile.uri
            )
            context.showToast(context.getString(R.string.set_as_notification_successfully))
        } catch (e: Exception) {
            context.showToast(context.getString(R.string.failed_to_set_as_notification))
        }
    }

    fun getContactPickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        return intent
    }

    fun setContactRingtone(context: Context, contactUri: Uri, mediaFile: MediaFile?): Boolean {
        if (mediaFile?.uri == null) {
            context.showToast(context.getString(R.string.audio_file_not_available))
            return false
        }

        try {

            // Lấy contact ID từ contact picker URI
            val contactId = getContactIdFromContactUri(context, contactUri)
            if (contactId == null) {
                context.showToast("Cannot get contact ID from URI: $contactUri")
                return false
            }

            // Cập nhật contact với nhạc chuông mới sử dụng URI gốc
            val updateValues = ContentValues().apply {
                put(ContactsContract.Contacts.CUSTOM_RINGTONE, mediaFile.uri.toString())
            }

            val contactUpdateUri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI,
                contactId.toLong()
            )
            val updated = context.contentResolver.update(contactUpdateUri, updateValues, null, null)

            if (updated > 0) {
                context.showToast(context.getString(R.string.set_as_contact_tone_successfully))
                return true
            } else {
                // Thử phương pháp alternative: Cập nhật raw contact
                return setContactRingtoneAlternative(context, contactId, mediaFile.uri)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            context.showToast("Error: ${e.message}")
            return false
        }
    }

    private fun setContactRingtoneAlternative(
        context: Context,
        contactId: String,
        ringtoneUri: Uri
    ): Boolean {
        try {
            // Thử cập nhật tất cả raw contacts của contact này
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    arrayOf(ContactsContract.RawContacts._ID),
                    "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )

                var updatedAny = false
                while (cursor?.moveToNext() == true) {
                    val rawContactId = cursor.getString(0)
                    val values = ContentValues().apply {
                        put(ContactsContract.RawContacts.CUSTOM_RINGTONE, ringtoneUri.toString())
                    }

                    val rawContactUri = ContentUris.withAppendedId(
                        ContactsContract.RawContacts.CONTENT_URI,
                        rawContactId.toLong()
                    )
                    val updated = context.contentResolver.update(rawContactUri, values, null, null)
                    if (updated > 0) updatedAny = true
                }

                if (updatedAny) {
                    context.showToast(context.getString(R.string.set_as_contact_tone_successfully))
                    return true
                } else {
                    context.showToast("Cannot update contact ringtone - no raw contacts found")
                    return false
                }

            } finally {
                cursor?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.showToast("Alternative method failed: ${e.message}")
            return false
        }
    }

    private fun getContactIdFromPhone(context: Context, phoneUri: Uri): String? {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                phoneUri,
                arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID),
                null, null, null
            )
            return if (cursor?.moveToFirst() == true) {
                val contactIdIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                if (contactIdIndex >= 0) {
                    cursor.getString(contactIdIndex)
                } else null
            } else null
        } catch (e: Exception) {
            return null
        } finally {
            cursor?.close()
        }
    }

    private fun getContactIdFromContactUri(context: Context, contactUri: Uri): String? {
        return try {
            when {
                contactUri.toString().contains("data/") -> {
                    // URI từ phone picker - lấy CONTACT_ID
                    getContactIdFromPhone(context, contactUri)
                }

                contactUri.toString().contains("contacts/") -> {
                    // URI từ contact picker - lấy trực tiếp ID
                    contactUri.lastPathSegment
                }

                else -> {
                    // Thử query để lấy contact ID
                    var cursor: Cursor? = null
                    try {
                        cursor = context.contentResolver.query(
                            contactUri,
                            arrayOf(ContactsContract.Contacts._ID),
                            null, null, null
                        )
                        if (cursor?.moveToFirst() == true) {
                            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                            if (idIndex >= 0) cursor.getString(idIndex) else null
                        } else null
                    } finally {
                        cursor?.close()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}