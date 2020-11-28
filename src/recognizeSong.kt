package ru.rtuitlab.copycheck

import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


suspend fun recognizeSong(song: File) = withContext(Dispatchers.IO) {
    HttpClient().submitFormWithBinaryData<String>("https://api.audd.io/recognize?api_token=fad95aeeec44aef5a5128ba7e2ffd826", formData {
        append("file", song.readBytes(), headersOf("Content-Type", "multipart/form-data"))
    })
}