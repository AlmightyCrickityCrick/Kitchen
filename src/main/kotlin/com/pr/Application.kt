package com.pr

import Constants
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.pr.plugins.*
import foodList
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import mu.KotlinLogging

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

var orderList = ConcurrentSkipListMap<String, IntermediateOrder>() //Made to take the small priority fifo tasks first
var chefList = Constants.getChefs()
var menu = Constants.getMenu()
//var apparatusMap = ConcurrentHashMap<String, AtomicInteger>()
var ovenList = ArrayList<CookingApparatus>()
var stoveList = ArrayList<CookingApparatus>()
var log = KotlinLogging.logger{}
var ovenChannel = Channel<IntermediateDetail>()
var stoveChannel = Channel<IntermediateDetail>()
var finishChannel = Channel<IntermediateDetail>()
var manager = Manager()



//var foodList = HashMap<Int, Map<String, ConcurrentSkipListMap<String, IntermediateDetail>>>()
var ovenSem = Semaphore(Constants.NR_OF_OVEN)
var stoveSem = Semaphore(Constants.NR_OF_STOVE)

var surplus = ConcurrentHashMap<Int, LinkedBlockingQueue<IntermediateDetail>>()



var availableFoods = AtomicInteger(0)
@OptIn(DelicateCoroutinesApi::class)
fun main() {
    embeddedServer(Netty, port = 8081) {
        configureSerialization()
        routing {
            post("/order") {
                //Receive request from DiningHall
                val rawOrd = call.receive<String>()
                //Transform into an intermediate order object
                val ord = Json.decodeFromString(Order.serializer(), rawOrd)
                call.respondText("Okay", status= HttpStatusCode.Created)
                //Add the necessary attributes and then generate an IntermediateDetail object for each food in order
                var intOrder = IntermediateOrder(ord.order_id,
                        AtomicInteger(0), ord.items, ord.priority, ord.max_wait,
                        ord.pick_up_time, System.currentTimeMillis(), ord.table_id, ord.waiter_id, ArrayList())
                var i = 0
                for (food in ord.items){
                    var tmp = IntermediateDetail(intOrder.priority.toString() + intOrder.pick_up_time.toString(),food, i, intOrder.priority, null, AtomicLong(0),AtomicInteger(0))
                    intOrder.cooking_details.add(tmp)
                    foodList[menu[tmp.food_id - 1].complexity]?.send(tmp)
                    i++
                }
                //Add the order to the list(might update to queue after implementing priority)
                orderList.put(intOrder.priority.toString() + intOrder.pick_up_time.toString(), intOrder)
                availableFoods.addAndGet(ord.items.size)
                //Answer the dining with an ok
            }
        }

        val appCtx = newSingleThreadContext("Apparatus")
        val managerCtx = newSingleThreadContext("Manager")

        for(i in 0 .. Constants.NR_OF_OVEN-1){
            ovenList.add(CookingApparatus(AtomicBoolean(true), ovenChannel))
            launch(appCtx) { ovenList[i].run() }
        }

        for(i in 0 .. Constants.NR_OF_STOVE - 1){
            stoveList.add(CookingApparatus(AtomicBoolean(true), stoveChannel))
            launch(appCtx){  stoveList[i].run()}
        }
        for (i in 0.. Constants.NR_OF_COOKS-1){
            repeat(chefList[i].proficiency){
                launch{
                    chefList[i].run()
                }
            }
        }

        launch(managerCtx){
            manager.checkOrders()
        }
    }.start(wait = true)




    //apparatusMap["stove"] = AtomicInteger(Constants.NR_OF_STOVE)
    //apparatusMap["oven"] = AtomicInteger(Constants.NR_OF_OVEN)


}
