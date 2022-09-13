package com.pr

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
        lock.lock()
    //Goes through order list
        for (i in 0..orderList.size - 1){
            //Checks if there are foods with unassigned chefs
            if (orderList[i].hasUnpreparedFood()){
                //Checks each food if its assigned
                for (food in 0..orderList[i].cooking_details!!.size - 1) {
                    //If food is not assigned, and chef still has proficiency, cooks it and assigns himself
                    var curCookDet = orderList[i].cooking_details!![food]
                    if (curCookDet.cook_id == null && activeTask.get() < this.proficiency ){
                        orderList[i].cooking_details!![food].cook_id = this.cookId
                        println("Cook ${this.cookId} is cooking $food from ${orderList[i].order_id} order")
                        cook(menu[curCookDet.food_id - 1])
                        lock.unlock()
                        return

                    }

                }
                //If the food has no unnasigned chefs, sends it back to dining
            }else sendFood(i)
        }
        lock.unlock()
    }
//Function to cook the food. Starts a proficiency thread and sleeps the time units needed
    fun cook(f: Food){
        activeTask.incrementAndGet()
        val task = thread {
            sleep((f.preparationTime * Constants.TIME_UNIT).toLong())
            activeTask.decrementAndGet()
        }

    }
//Function to send the food back to the DinningHall
    fun sendFood(i:Int){
        var ord = orderList[i]
        ord.cooking_time = (System.currentTimeMillis() - ord.pick_up_time).toInt()
        orderList.removeAt(i)
        val serilizedOrder = Json.encodeToString(FinishedOrder.serializer(), ord)
        println(serilizedOrder)
        val client = HttpClient()
        println("${ord.order_id} is done")
        runBlocking {
            var job = launch {
                val resp: HttpResponse = client.post(Constants.DINING_URL+"/distribution") {
                    setBody(serilizedOrder)
                }
            }}
        client.close()
    }
}