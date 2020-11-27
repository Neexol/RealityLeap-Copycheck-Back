package ru.rtuitlab.copycheck

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.http.content.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
    }

    routing {
        get("/music") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
        static {
            resources("music")
        }
    }
}

data class MySession(val count: Int = 0)

