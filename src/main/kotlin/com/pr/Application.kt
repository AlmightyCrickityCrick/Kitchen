package com.pr

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.pr.plugins.*

fun main() {
    embeddedServer(Netty, port = 8081) {
        configureSerialization()
        configureRouting()
    }.start(wait = false)
}
