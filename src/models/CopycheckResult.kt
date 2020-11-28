package ru.rtuitlab.copycheck.models

import com.google.gson.annotations.SerializedName

data class CopycheckResult(
        @SerializedName("recognition_result") val recognitionResult: RecognitionResult,
        @SerializedName("copyright_result") val copyrightResult: CopyrightResult,
        @SerializedName("apple_result") val appleResult: AppleResult?
)