package com.pr

import Constants
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

var lock = ReentrantLock()

class Chef : Thread() {
    var cookId = 0
    var rank = 0
    var proficiency = 0
    var cookName = ""
    var catchPhrase = ""
    var activeTask = AtomicInteger()

    override fun run() {
        while(true) if (this.activeTask.get() < this.proficiency && orderList.size>0) searchTask()
    }

    //Function to add information to each chef
    fun setCook(id: Int, rank: Int, proficiency: Int, name: String, catchPhrase: String, ) {
        this.cookId = id
        this.rank = rank
        this.proficiency = proficiency
        this.cookName = name
        this.catchPhrase = catchPhrase
        this.activeTask.set(0)
    }
//Function to search for orders that can be cooked
    fun searchTask(){
    //Goes through order list
        for (i in orderList.navigableKeySet()){
            try {
                //lock.lock()
                //Checks if the order is not ready to be served
                if (orderList[i]?.checkIfReady() == false) {
                    //Checks each food if its taken
                    for (food in 0 until (orderList[i]?.cooking_details?.size ?: 0)) {
                        //If food is not ready, chef still has proficiency, and needed rank cooks it and assigns himself
                        var curCookDet = orderList[i]?.cooking_details?.get(food)
                        if (curCookDet?.state?.get() == 0
                            && activeTask.get() < this.proficiency
                            && menu[curCookDet.food_id -1].complexity <= rank
                            ) {

                            orderList[i]?.cooking_details?.get(food)?.state?.set(1)
                            orderList[i]?.cooking_details?.get(food)?.cook_id = this.cookId
                            log.info{"Cook ${this.cookId} is cooking $food (prof ${menu[curCookDet.food_id - 1].complexity})from ${orderList[i]?.order_id} order"}
                            cook(menu[curCookDet.food_id - 1], food, i)
                           // lock.unlock()
                            return

                        }

                    }
                    //If the food has no unnasigned chefs, sends it back to dining
                } else sendFood(i)
            } catch(e:NullPointerException){ print("")}
            //lock.unlock()
        }
    }
//Function to cook the food. Starts a proficiency thread and sleeps the time units needed
    fun cook(f: Food, fId: Int, oId:String){
    if (f.cookingApparatus == null){
        activeTask.incrementAndGet()
        cookSingle(f, fId, oId)

        } else if (f.cookingApparatus == "oven"){
            var nr = 0
            for (o in 0 .. ovenList.size -1)
                if(ovenList[o].apOrders.keys.size < ovenList[nr].apOrders.keys.size) {
                    nr = o
                }
            ovenList[nr].apOrders.put(oId+fId, ApplianceOrder(f, fId, oId))
        orderList[oId]?.cooking_details?.get(fId)?.state?.set(2)
    } else if (f.cookingApparatus == "stove"){
        var nr = 0
        for (s in 0..stoveList.size-1)
            if(stoveList[s].apOrders.keys.size < stoveList[nr].apOrders.keys.size) {
                nr = s
            }
        stoveList[nr].apOrders.put(oId+fId, ApplianceOrder(f, fId, oId))
        orderList[oId]?.cooking_details?.get(fId)?.state?.set(2)
    }

    if(orderList[oId]?.checkIfReady() == true) sendFood(oId)

    }
    fun cookSingle(f: Food, fId: Int, oId:String){
        val task = thread {
                var t = f.preparationTime / orderList[oId]?.priority!!
//                    if ((orderList[oId]?.priority!! * 200) < temp) orderList[oId]?.priority else ((f.preparationTime * Constants.TIME_UNIT) - orderList[oId]?.cooking_details?.get(
//                        fId
//                    )?.cooking_time?.get()!!)
                (t)?.toLong()?.let { sleep(it * Constants.TIME_UNIT) }
                //Set food as finished
                if (t != null) {
                    orderList[oId]?.cooking_details?.get(fId)?.advanceCooking(t.toLong() * Constants.TIME_UNIT)
                    if (orderList[oId]?.cooking_details?.get(fId)?.isFinished() == false
                    ) orderList[oId]?.cooking_details?.get(fId)?.state?.set(0)
                }
                activeTask.decrementAndGet()
        }
    }

//Function to send the food back to the DinningHall
    fun sendFood(i:String) {
    var ord = orderList[i]?.toFinished()
    orderList.remove(i)
    if (ord != null) {
        val serilizedOrder = Json.encodeToString(FinishedOrder.serializer(), ord)
        println(serilizedOrder)
        val client = HttpClient()
        log.info {  "${ord?.order_id} is done"}
        runBlocking {
            var job = launch {
                val resp: HttpResponse = client.post(Constants.DINING_URL + "/distribution") {
                    setBody(serilizedOrder)
                }
            }
        }
        client.close()
    }
}
}