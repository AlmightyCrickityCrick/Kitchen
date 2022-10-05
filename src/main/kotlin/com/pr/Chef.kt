package com.pr

import Constants
import foodList
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

var lock = ReentrantLock()
class Chef {
    var cookId = 0
    var rank = 0
    var proficiency = 0
    var cookName = ""
    var catchPhrase = ""
    var activeTask = AtomicInteger()
    var channel = ArrayList<Channel<IntermediateDetail>>()
    fun setCook(id: Int, rank: Int, proficiency: Int, name: String, catchPhrase: String, channels:MutableList<Channel<IntermediateDetail>> ) {
        this.cookId = id
        this.rank = rank
        this.proficiency = proficiency
        this.cookName = name
        this.catchPhrase = catchPhrase
        this.activeTask.set(0)
        this.channel.addAll(channels)
    }
    suspend fun run() {
        when(this.rank){
            1 -> { while(true) getFood1() }
            2 -> {while(true) getFood2()}
            3 ->{while (true) getFood3()}
        }
                //if(!i) rank = if (rank>1) rank - 1 else this.rank
            //}
    }

    suspend fun getFood1(){
        sendFoodToCook(channel[0].receive())
    }
    suspend fun getFood2(){
        select{
            channel[0].onReceive{sendFoodToCook(it)}
            channel[1].onReceive{sendFoodToCook(it)}
        }
    }

    suspend fun getFood3(){
        select{
            channel[0].onReceive{sendFoodToCook(it)}
            channel[1].onReceive{sendFoodToCook(it)}
            channel[2].onReceive{sendFoodToCook(it)}
        }
    }


     suspend fun sendFoodToCook(f : IntermediateDetail){
         if (menu[f.food_id -1]?.cookingApparatus == "oven") ovenChannel.send(f)
         else if(menu[f.food_id -1]?.cookingApparatus == "stove") stoveChannel.send(f)
         else cookSingle(f)
    }

     suspend fun cookSingle(f: IntermediateDetail){
       // val task = thread {
            var t = menu[f.food_id -1].preparationTime / f.priority!!
//                    if ((orderList[oId]?.priority!! * 200) < temp) orderList[oId]?.priority else ((f.preparationTime * Constants.TIME_UNIT) - orderList[oId]?.cooking_details?.get(
//                        fId
//                    )?.cooking_time?.get()!!)
            log.info { "Chef ${this.cookId} is cooking ${f.food_in_order_id} from ${f.order_id} (complexity ${menu[f.food_id -1].complexity})" }
            (t)?.toLong()?.let { delay(it *Constants.TIME_UNIT) }
            activeTask.decrementAndGet()
            //Set food as finished
                orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.advanceCooking(t.toLong() *Constants.TIME_UNIT)
                f.advanceCooking((t).toLong() *Constants.TIME_UNIT)
                if (f.isFinished() == false) {
                    f.state.set(0)
                    foodList[menu[f.food_id -1].complexity]?.send(f)
                    availableFoods.decrementAndGet()
                } else finishChannel.send(f)
       // }
    }
    //Function to add information to each chef
//Function to search for orders that can be cooked
//    fun searchTask(){
//    //Goes through order list
//        for (i in orderList.navigableKeySet()){
//            try {
//                //lock.lock()
//                //Checks if the order is not ready to be served
//                if (orderList[i]?.checkIfReady() == false) {
//                    //Checks each food if its taken
//
//                    for (food in 0 until (orderList[i]?.cooking_details?.size ?: 0)) {
//                        //If food is not ready, chef still has proficiency, and needed rank cooks it and assigns himself
//                        //lock.lock()
//                        if (orderList[i]?.cooking_details?.get(food)?.state?.get() == 0
//                            && activeTask.get() < this.proficiency
//                            && menu[orderList[i]?.cooking_details?.get(food)?.food_id?.minus(1)!!].complexity <= rank
//                            && (menu[orderList[i]?.cooking_details?.get(food)?.food_id?.minus(1)!!].cookingApparatus== null||(menu[orderList[i]?.cooking_details?.get(food)?.food_id?.minus(1)!!].cookingApparatus!= null && apparatusMap[(menu[orderList[i]?.cooking_details?.get(food)?.food_id?.minus(1)!!].cookingApparatus)]!!.get()>0))
//                            ) {
//                            orderList[i]?.cooking_details?.get(food)?.state?.set(1)
//                            //lock.unlock()
//                            orderList[i]?.cooking_details?.get(food)?.cook_id = this.cookId
//                           log.info{"Cook ${this.cookId} is cooking $food (prof ${menu[(orderList[i]?.cooking_details?.get(food)?.food_id!!) - 1].complexity})from ${orderList[i]?.order_id} order"}
//                            cook(menu[orderList[i]?.cooking_details?.get(food)!!.food_id - 1], food, i)
//                           // lock.unlock()
//                            return
//
//                        } //else lock.unlock()
//
//                    }
//                    //If the food has no unnasigned chefs, sends it back to dining
//                }
//            } catch(e:NullPointerException){ println("Null")}
//            //lock.unlock()
//        }
//    }
//Function to cook the food. Starts a proficiency thread and sleeps the time units needed
//    fun cook(f: Food, fId: Int, oId:String){
//    if (f.cookingApparatus == null){
//        activeTask.incrementAndGet()
//        cookSingle(f, fId, oId)
//
//        } else if (f.cookingApparatus == "oven"){
//            var flag = 0
//            for (o in 0 .. ovenList.size -1)
//                if(ovenList[o].isFree.get() == true) {
//                    apparatusMap[f.cookingApparatus]?.decrementAndGet()
//                    ovenList[o].isFree.set(false)
//                    orderList[oId]?.cooking_details?.get(fId)?.state?.set(2)
//                    val task = thread {
//                        ovenList[o].cook(f, fId, oId)
//                    }
//                    flag = 1
//                    break
////                    nr = o
//                }
//        if (flag == 0) orderList[oId]?.cooking_details?.get(fId)?.state?.set(0)
////            ovenList[nr].apOrders.put(oId+fId, ApplianceOrder(f, fId, oId))
//    } else if (f.cookingApparatus == "stove"){
//        availableFoods.decrementAndGet()
//        var flag = 0
//        for (s in 0..stoveList.size-1)
//            if(stoveList[s].isFree.get() == true) {
//                apparatusMap[f.cookingApparatus]?.decrementAndGet()
//                stoveList[s].isFree.set(false)
//                orderList[oId]?.cooking_details?.get(fId)?.state?.set(2)
//                val task = thread{
//                    stoveList[s].cook(f, fId, oId)
//                }
//                flag = 1
//                break
//
//                //                nr = s
//            }
//        if (flag == 0) orderList[oId]?.cooking_details?.get(fId)?.state?.set(0)
//
////        stoveList[nr].apOrders.put(oId+fId, ApplianceOrder(f, fId, oId))
//    }
//
//    }
//    fun cookSingle(f: Food, fId: Int, oId:String){
//        val task = thread {
//                var t = 3
//                   // f.preparationTime / orderList[oId]?.priority!!
////                    if ((orderList[oId]?.priority!! * 200) < temp) orderList[oId]?.priority else ((f.preparationTime * Constants.TIME_UNIT) - orderList[oId]?.cooking_details?.get(
////                        fId
////                    )?.cooking_time?.get()!!)
//                (t)?.toLong()?.let { sleep(it *Constants.TIME_UNIT) }
//            activeTask.decrementAndGet()
//            //Set food as finished
//                if (t != null) {
//                    orderList[oId]?.cooking_details?.get(fId)?.advanceCooking(t.toLong() *Constants.TIME_UNIT)
//                    if (orderList[oId]?.cooking_details?.get(fId)?.isFinished() == false) {
//                        orderList[oId]?.cooking_details?.get(fId)?.state?.set(0)
//                        availableFoods.decrementAndGet()
//                    } else if(orderList[oId]?.checkIfReady()==true)sendFood(oId)
//                }
//        }
//    }

//Function to send the food back to the DinningHall

}