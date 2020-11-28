package ru.rtuitlab.copycheck

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun recognizeSong(songUrl: String) = withContext(Dispatchers.IO) {
    HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }.get<String>("https://api.audd.io/recognize?api_token=${System.getenv("AUDD_KEY")}&url=$songUrl")
}