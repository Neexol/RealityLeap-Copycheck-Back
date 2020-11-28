package ru.rtuitlab.copycheck

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.utils.io.errors.*
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@InternalAPI
@KtorExperimentalAPI
@Suppress("unused")
fun Application.module() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    val uploadDir = File(environment.config.config("copycheck").property("upload.dir").getString())

    if (!uploadDir.mkdirs() && !uploadDir.exists()) {
        throw IOException("Failed to create directory ${uploadDir.absolutePath}")
    }

    routing {
        recognizeRoute(uploadDir)
        static("/music") {
            resources("music")
        }
    }
}

