package com.ordra.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ordra.core.ocr.OcrEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/** ML Kit on-device text recognition. No network, no upload of the image. */
@Singleton
class MlKitOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : OcrEngine {

    override suspend fun recognize(imageUri: String): String? = runCatching {
        val image = InputImage.fromFilePath(context, Uri.parse(imageUri))
        val result = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(image).await()
        result.text.takeIf { it.isNotBlank() }
    }.getOrNull()
}
