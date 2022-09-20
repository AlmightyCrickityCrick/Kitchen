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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicInteger

var orderList = ConcurrentSkipListMap<String, IntermediateOrder>() //Made to take the small priority fifo tasks first
var chefList = Constants.getChefs()
var menu = Constants.getMenu()
fun main() {
    embeddedServer(Netty, port = 8081) {
        configureSerialization()
        routing {
            post("/order") {
                //Receive request from DiningHall
                val rawOrd = call.receive<String>()
                //Transform into an intermediate order object
                val ord = Json.decodeFromString(Order.serializer(), rawOrd)
                //Add the necessary attributes and then generate an IntermediateDetail object for each food in order
                var intOrder = IntermediateOrder(ord.order_id,
                        AtomicInteger(0), ord.items, ord.priority, ord.max_wait,
                        ord.pick_up_time, System.currentTimeMillis(), ord.table_id, ord.waiter_id, ArrayList())
                for (food in ord.items){
                    intOrder.cooking_details.add(IntermediateDetail(food, null, AtomicInteger(0)))
                }
                //Add the order to the list(might update to queue after implementing priority)
                orderList.put(intOrder.priority.toString()+intOrder.pick_up_time.toString(), intOrder)
                //Answer the dining with an ok
                call.respondText("Okay", status= HttpStatusCode.Created)
            }
        }
    }.start(wait = false)

    for (i in 0.. Constants.NR_OF_COOKS-1){
        chefList[i].start()
    }
}
