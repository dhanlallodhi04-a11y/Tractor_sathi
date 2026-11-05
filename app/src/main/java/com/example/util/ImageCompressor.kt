package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageCompressor {
    private const val TAG = "ImageCompressor"
    private const val MAX_DIMENSION = 800
    private const val JPEG_QUALITY = 70 // 0.7 quality

    suspend fun compressAndSaveImage(
        context: Context,
        imageUri: Uri,
        maxDimension: Int = MAX_DIMENSION,
        quality: Int = JPEG_QUALITY
    ): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // 1. Decode bounds only to compute inSampleSize
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            }

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) {
                DiagnosticsManager.logWarning(TAG, "Invalid image dimensions: $origWidth x $origHeight")
                return@withContext null
            }

            // Calculate sample size for low-memory decode
            var sampleSize = 1
            val maxOriginal = maxOf(origWidth, origHeight)
            while ((maxOriginal / (sampleSize * 2)) >= maxDimension) {
                sampleSize *= 2
            }

            // 2. Decode sampled bitmap into RGB_565 or ARGB_8888
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Saves 50% memory on low RAM devices
            }

            val sampledBitmap = contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            } ?: return@withContext null

            // 3. Scale down exactly to maxDimension if still larger
            val currentMax = maxOf(sampledBitmap.width, sampledBitmap.height)
            val finalBitmap = if (currentMax > maxDimension) {
                val scale = maxDimension.toFloat() / currentMax.toFloat()
                val targetWidth = (sampledBitmap.width * scale).toInt().coerceAtLeast(1)
                val targetHeight = (sampledBitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(sampledBitmap, targetWidth, targetHeight, true)
                if (scaled != sampledBitmap) {
                    sampledBitmap.recycle()
                }
                scaled
            } else {
                sampledBitmap
            }

            // 4. Save to app private internal storage: filesDir/images/
            val imagesDir = File(context.filesDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(imagesDir, "img_${UUID.randomUUID()}.jpg")

            FileOutputStream(destinationFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.flush()
            }

            finalBitmap.recycle()

            val fileUri = Uri.fromFile(destinationFile).toString()
            DiagnosticsManager.logInfo(TAG, "Compressed image saved: ${destinationFile.length() / 1024} KB -> $fileUri")
            fileUri
        } catch (e: Exception) {
            DiagnosticsManager.logError(TAG, "Image compression failed", e, context)
            null
        }
    }
}
