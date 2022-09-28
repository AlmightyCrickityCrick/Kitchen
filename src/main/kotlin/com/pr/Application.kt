package com.pr

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.pr.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.netty.util.Constant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import mu.KotlinLogging

var orderList = ConcurrentSkipListMap<String, IntermediateOrder>() //Made to take the small priority fifo tasks first
var chefList = Constants.getChefs()
var menu = Constants.getMenu()
var apparatusMap = ConcurrentHashMap<String, AtomicInteger>()
var ovenList = ArrayList<CookingApparatus>()
var stoveList = ArrayList<CookingApparatus>()
var log = KotlinLogging.logger{}


var availableFoods = AtomicInteger(0)
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
                    intOrder.cooking_details.add(IntermediateDetail(food, null, AtomicLong(0),AtomicInteger(0)))
                }
                //Add the order to the list(might update to queue after implementing priority)
                orderList.put(intOrder.pick_up_time.toString()+ intOrder.priority.toString(), intOrder)
                availableFoods.addAndGet(ord.items.size)
                //Answer the dining with an ok
                call.respondText("Okay", status= HttpStatusCode.Created)
            }
        }
    }.start(wait = false)

    apparatusMap["stove"] = AtomicInteger(Constants.NR_OF_STOVE)
    apparatusMap["oven"] = AtomicInteger(Constants.NR_OF_OVEN)

    for(i in 0 .. Constants.NR_OF_OVEN-1){
        ovenList.add(CookingApparatus())
        ovenList[i].start()
    }

    for(i in 0 .. Constants.NR_OF_STOVE - 1){
        stoveList.add(CookingApparatus())
        stoveList[i].start()
    }

    for (i in 0.. Constants.NR_OF_COOKS-1){
        chefList[i].start()
    }
}
