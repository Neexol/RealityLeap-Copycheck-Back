package ru.rtuitlab.copycheck

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


suspend fun recognizeSong(song: File): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val data = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("api_token", System.getenv("AUDD_KEY"))
        .addFormDataPart("file", song.name, song.asRequestBody("audio/mpeg; charset=utf-8".toMediaType()))
        .build()
    val request = Request.Builder().url("https://api.audd.io/")
        .post(data).build()
    val response = client.newCall(request).execute()
    response.body!!.string()
}
