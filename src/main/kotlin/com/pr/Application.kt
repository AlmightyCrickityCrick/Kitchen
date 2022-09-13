package com.pr

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.pr.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

var orderList = ArrayList<FinishedOrder>()
var chefList = Constants.getChefs()
var menu = Constants.getMenu()
fun main() {
    embeddedServer(Netty, port = 8081) {
        configureSerialization()
        routing {
            post("/order") {
                //Receive request from DiningHall
                val rawOrd = call.receive<String>()
                //Transform into a finished order object
                val ord = Json.decodeFromString(FinishedOrder.serializer(), rawOrd)
                //Add cooking_details attribute and then generate a CookingDetail object for each food in order
                ord.cooking_details = ArrayList()
                for (food in ord.items){
                    ord.cooking_details!!.add(CookingDetail(food, null))
                }
                //Add the order to the list(might update to queue after implementing priority)
                orderList.add(ord)
                //Answer the dining with an ok
                call.respondText("Okay", status= HttpStatusCode.Created)
            }
        }
    }.start(wait = false)
}
