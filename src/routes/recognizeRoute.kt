package ru.rtuitlab.copycheck.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import ru.rtuitlab.copycheck.copyright.checkCopyright
import ru.rtuitlab.copycheck.recognition.recognizeSong
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@InternalAPI
fun Route.recognizeRoute(uploadDir: File) {
    post("/recognize") {
        val multipart = call.receiveMultipart()
        var receivedFile = File("")

        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = File(part.originalFileName!!).extension
                val file = File(
                    uploadDir,
                    "upload-${System.currentTimeMillis()}.$ext"
                )
                part.streamProvider().use { input ->
                    file.outputStream().buffered().use { output ->
                        input.copyToSuspend(output)
                    }
                }
                receivedFile = file
            }
            part.dispose()
        }

        val recognitionResult = recognizeSong(receivedFile)
        receivedFile.delete()

        recognitionResult.result?.let {
            call.respond(checkCopyright(recognitionResult))
        } ?: call.respond(HttpStatusCode.NotFound)
    }
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}