package com.pr

import Constants
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.pr.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.*

var orderList = ConcurrentSkipListMap<String, IntermediateOrder>() //Made to take the small priority fifo tasks first
var chefList = Constants.getChefs()
var menu = Constants.getMenu()
//var apparatusMap = ConcurrentHashMap<String, AtomicInteger>()
var ovenList = ArrayList<CookingApparatus>()
var stoveList = ArrayList<CookingApparatus>()
var log = KotlinLogging.logger{}

//var foodList = HashMap<Int, Map<String, ConcurrentSkipListMap<String, IntermediateDetail>>>()
var foodList = ConcurrentHashMap<Int, Map<String, BlockingQueue<IntermediateDetail>>>()
var ovenSem = Semaphore(Constants.NR_OF_OVEN)
var stoveSem = Semaphore(Constants.NR_OF_STOVE)

var surplus = ConcurrentHashMap<Int, LinkedBlockingQueue<IntermediateDetail>>()
lateinit var rest : Self


var availableFoods = AtomicInteger(0)
@OptIn(DelicateCoroutinesApi::class)
fun main() {
    foodList.put(1, mapOf("oven" to LinkedBlockingQueue<IntermediateDetail>(), "stove" to LinkedBlockingQueue<IntermediateDetail>(), "null" to LinkedBlockingQueue<IntermediateDetail>()))
    foodList.put(2, mapOf("oven" to LinkedBlockingQueue<IntermediateDetail>(), "stove" to LinkedBlockingQueue<IntermediateDetail>(), "null" to LinkedBlockingQueue<IntermediateDetail>()))
    foodList.put(3, mapOf("oven" to LinkedBlockingQueue<IntermediateDetail>(), "stove" to LinkedBlockingQueue<IntermediateDetail>(), "null" to LinkedBlockingQueue<IntermediateDetail>()))
    for (i in 1..13){
        surplus.put(i, LinkedBlockingQueue())
    }
    var conf = File("config/config.json").inputStream().readBytes().toString(Charsets.UTF_8)
    rest = Json{coerceInputValues = true}.decodeFromString(Self.serializer(), conf)
    embeddedServer(Netty, port = rest.kitchen_port) {
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
                var i = 0
                for (food in ord.items){
                    var tmp = IntermediateDetail(intOrder.pick_up_time.toString()+ intOrder.priority.toString(),food, i, intOrder.priority, null, AtomicLong(0),AtomicInteger(0))
                    intOrder.cooking_details.add(tmp)
                    if(menu[food - 1].cookingApparatus == null)foodList[menu[food - 1].complexity]?.get("null")?.put(tmp)
                    else foodList[menu[food - 1].complexity]?.get(menu[food-1].cookingApparatus)?.put(tmp)
                    i++
                }
                //Add the order to the list(might update to queue after implementing priority)
                orderList.put(intOrder.pick_up_time.toString()+ intOrder.priority.toString(), intOrder)
                availableFoods.addAndGet(ord.items.size)
                //Answer the dining with an ok
                call.respondText("Okay", status= HttpStatusCode.Created)
            }
        }


    }.start(wait = false)
//    foodList.put(1, mapOf("oven" to ConcurrentSkipListMap<String,IntermediateDetail>(), "stove" to ConcurrentSkipListMap<String,IntermediateDetail>(), "null" to ConcurrentSkipListMap<String,IntermediateDetail>()))
//    foodList.put(2, mapOf("oven" to ConcurrentSkipListMap<String,IntermediateDetail>(), "stove" to ConcurrentSkipListMap<String,IntermediateDetail>(), "null" to ConcurrentSkipListMap<String,IntermediateDetail>()))
//    foodList.put(3, mapOf("oven" to ConcurrentSkipListMap<String,IntermediateDetail>(), "stove" to ConcurrentSkipListMap<String,IntermediateDetail>(), "null" to ConcurrentSkipListMap<String,IntermediateDetail>()))

    for(i in 0 .. Constants.NR_OF_OVEN-1){
        ovenList.add(CookingApparatus())
        ovenList[i].start()
    }

    for(i in 0 .. Constants.NR_OF_STOVE - 1){
        stoveList.add(CookingApparatus())
        stoveList[i].start()
    }

    for (c in chefList) c.start()


    //apparatusMap["stove"] = AtomicInteger(Constants.NR_OF_STOVE)
    //apparatusMap["oven"] = AtomicInteger(Constants.NR_OF_OVEN)



}
