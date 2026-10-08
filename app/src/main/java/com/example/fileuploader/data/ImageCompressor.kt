package com.example.fileuploader.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class ImageCompressor(
    private val context: Context
) {

    suspend fun compress(
        uri: Uri,
        maxSize: Int = 1280,
        quality: Int = 75
    ): File = withContext(Dispatchers.IO) {

        val bitmap = context.contentResolver
            .openInputStream(uri)
            ?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            ?: throw IllegalArgumentException("Unable to decode image")

        val resizedBitmap = resizeBitmap(
            bitmap = bitmap,
            maxSize = maxSize
        )

        val outputFile = File(
            context.cacheDir,
            "compressed_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(outputFile).use { outputStream ->
            resizedBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                outputStream
            )
        }

        if (resizedBitmap !== bitmap) {
            resizedBitmap.recycle()
        }

        bitmap.recycle()

        outputFile
    }

    private fun resizeBitmap(
        bitmap: Bitmap,
        maxSize: Int
    ): Bitmap {

        val width = bitmap.width
        val height = bitmap.height

        val longestSide = maxOf(width, height)

        if (longestSide <= maxSize) {
            return bitmap
        }

        val scale = maxSize.toFloat() / longestSide

        val newWidth = (width * scale).roundToInt()
        val newHeight = (height * scale).roundToInt()

        return Bitmap.createScaledBitmap(
            bitmap,
            newWidth,
            newHeight,
            true
        )
    }
}