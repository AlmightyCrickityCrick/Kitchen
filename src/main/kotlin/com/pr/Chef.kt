package com.pr

import Constants
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

var lock = ReentrantLock()
class Chef :Thread(){
    var cookId = 0
    var rank = 0
    var proficiency = 0
    var cookName = ""
    var catchPhrase = ""
    var activeTask = AtomicInteger()
    lateinit var task : ThreadPoolExecutor

    override fun run() {
        var rank = this.rank
        while(true)
            //if (this.activeTask.get() < this.proficiency && orderList.size>0) {
                //while(availableFoods.get() == 0) onSpinWait()
                //yield()
                //searchTask()
               for (i in 0 until proficiency) thread { searchFood(this.rank) }

        // if(i==false) cookSurplus()

                //if(!i) rank = if (rank>1) rank - 1 else this.rank
            //}
    }

//    fun cookSurplus(i:Int){
//
//    }

     fun searchFood(rank:Int):Boolean{
        if ((foodList[rank]?.get("oven")?.isEmpty()) == false && (ovenSem.availablePermits() > 0)) {
                var f = foodList[rank]?.get("oven")?.take()
                if (f != null) {
                    orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.cook_id = this.cookId
                    COOK(f, "oven")
                    return true
                }

        }else if ((foodList[rank]?.get("stove")?.isEmpty()) == false && stoveSem.availablePermits()> 0) {
                var f = foodList[rank]?.get("stove")?.take()
                if (f != null) {
                    orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.cook_id = this.cookId
                    COOK(f, "stove")
                    return true
                }
        } else if ((foodList[rank]?.get("null")?.isEmpty()) == false){
                var f = foodList[rank]?.get("null")?.take()
                if (f != null) {
                    orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.cook_id = this.cookId
                    COOKSingle(f)
                    return true
                }
            }
        return false
    }

     fun COOK(f:IntermediateDetail, app:String){
        if( app== "oven"){
            var flag = 0
            for (o in 0 .. ovenList.size -1)
                if(ovenList[o].isFree.get() == true) {
                    ovenSem.acquire()
                    ovenList[o].isFree.set(false)
                    orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.state?.set(2)
                    val task = thread {
                        ovenList[o].cook(f)
                    }
                    flag = 1
                    break
//                    nr = o
                }
            if (flag == 0) {
                orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.state?.set(0)
                foodList[menu[f.food_id - 1].complexity]?.get(app)?.put(f)
            }
        } else if (app == "stove"){
            var flag = 0
            for (s in 0..stoveList.size-1)
                if(stoveList[s].isFree.get() == true) {
                    stoveSem.acquire()
                    stoveList[s].isFree.set(false)
                    orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.state?.set(2)
                    val task = thread{
                        stoveList[s].cook(f)
                    }
                    flag = 1
                    break

                    //                nr = s
                }
            if (flag == 0) {
                orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.state?.set(0)
                foodList[menu[f.food_id -1].complexity]?.get(app)?.put(f)
            }
        } else{
            COOKSingle(f)
        }
    }

     fun COOKSingle(f: IntermediateDetail){
       // val task = thread {
            var t = 3
            // f.preparationTime / orderList[oId]?.priority!!
//                    if ((orderList[oId]?.priority!! * 200) < temp) orderList[oId]?.priority else ((f.preparationTime * Constants.TIME_UNIT) - orderList[oId]?.cooking_details?.get(
//                        fId
//                    )?.cooking_time?.get()!!)
            log.info { "Chef ${this.cookId} is cooking ${f.food_in_order_id} from ${f.order_id} (complexity ${menu[f.food_id -1].complexity})" }
            (t)?.toLong()?.let { sleep(it *Constants.TIME_UNIT) }
            activeTask.decrementAndGet()
            //Set food as finished
            if (t != null) {
                orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.advanceCooking(t.toLong() *Constants.TIME_UNIT)
                f.advanceCooking((t).toLong() *Constants.TIME_UNIT)
                if (orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.isFinished() == false) {
                    orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.state?.set(0)
                    f.state.set(0)
                    foodList[menu[f.food_id -1].complexity]?.get(menu[f.food_id -1].cookingApparatus.toString())?.put(f)
                    availableFoods.decrementAndGet()
                } else {
                    if(orderList[f.order_id]?.checkIfReady()==true)sendFood(f.order_id)
                }
            }
       // }
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