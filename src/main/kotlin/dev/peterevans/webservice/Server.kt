package dev.peterevans.webservice

import io.ktor.server.netty.*
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.server.engine.*

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/") {
            log.debug("GET /")
            call.respondText("Hello, world!", ContentType.Text.Plain)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        8080,
        module = Application::module
    ).start()
}
