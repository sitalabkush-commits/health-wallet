package com.example.medwallet.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.math.max

/**
 * Stores document images in the app-private directory (context.filesDir/documents),
 * which other apps and file managers cannot read. Files are additionally encrypted
 * with a Keystore-backed key (AES-256-GCM).
 */
class DocumentStore(context: Context) {
    private val ctx = context.applicationContext
    private val dir = File(ctx.filesDir, "documents").apply { mkdirs() }
    private val masterKey by lazy {
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    }

    private fun encrypted(file: File) = EncryptedFile.Builder(
        ctx, file, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()

    /** Copies the picked/captured image into private storage and returns the stored file name. */
    fun saveFrom(uri: Uri): String {
        val name = UUID.randomUUID().toString() + ".enc"
        val target = File(dir, name)
        val input = ctx.contentResolver.openInputStream(uri) ?: throw IOException("Cannot open $uri")
        input.use { src -> encrypted(target).openFileOutput().use { out -> src.copyTo(out) } }
        return name
    }

    /** Decrypts and decodes a downsampled bitmap (longest side <= [maxSide]). Returns null on failure. */
    fun loadBitmap(name: String, maxSide: Int): Bitmap? = runCatching {
        val bytes = encrypted(File(dir, name)).openFileInput().use { it.readBytes() }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2

        val bmp = BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: return@runCatching null

        // Camera photos are often stored rotated + an EXIF flag; honour it.
        val orientation = ExifInterface(ByteArrayInputStream(bytes))
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) bmp
        else Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(degrees) }, true)
    }.getOrNull()

    fun delete(name: String) {
        File(dir, name).delete()
    }
}
