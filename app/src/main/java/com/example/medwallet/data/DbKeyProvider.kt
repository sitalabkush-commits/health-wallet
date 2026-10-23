package com.example.medwallet.data

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Generates a random database passphrase once and keeps it in EncryptedSharedPreferences
 * (protected by an Android Keystore master key). The passphrase never leaves the device.
 */
object DbKeyProvider {
    private const val PREFS = "secure_prefs"
    private const val KEY = "db_passphrase"

    fun passphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context, PREFS, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val existing: String? = prefs.getString(KEY, null)
        val pass: String = existing ?: run {
            val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
            val fresh: String = Base64.encodeToString(bytes, Base64.NO_WRAP)
            prefs.edit().putString(KEY, fresh).commit() // commit: must be persisted before the DB is created
            fresh
        }
        return pass.toByteArray(Charsets.UTF_8)
    }
}
