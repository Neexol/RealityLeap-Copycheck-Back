package ru.rtuitlab.copycheck

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import ru.rtuitlab.copycheck.models.RecognitionResult
import java.io.File


suspend fun recognizeSong(song: File): RecognitionResult = withContext(Dispatchers.IO) {
    val data = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("api_token", System.getenv("AUDD_KEY"))
        .addFormDataPart("file", song.name, song.asRequestBody("audio/mpeg; charset=utf-8".toMediaType()))
        .build()
    val request = Request.Builder()
        .url("https://api.audd.io/")
        .post(data)
        .build()
    val response = OkHttpClient().newCall(request).execute().body!!.string()
    Gson().fromJson(response, RecognitionResult::class.java)
}
