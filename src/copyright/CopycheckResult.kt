package ru.rtuitlab.copycheck.copyright

import com.google.gson.annotations.SerializedName
import ru.rtuitlab.copycheck.models.RecognitionResult

data class CopycheckResult(
    @SerializedName("recognition_result") val recognitionResult: RecognitionResult,
    @SerializedName("copyright_result") val copyrightResult: CopyrightResult,
    @SerializedName("apple_result") val appleResult: AppleResult?
)