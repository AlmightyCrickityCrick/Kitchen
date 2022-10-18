package com.pr

import Constants
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

data class IntermediateDetail(
    val order_id: String, val food_id:Int, val food_in_order_id: Int, val priority: Int,
                              var cook_id:Int?=null,
                              var cooking_time: AtomicLong,
                              var state:AtomicInteger //0 = not started, 1 = occupied by cook,  2 = ocuppied by apparatus, 3 = finished
                              ){
    fun isFinished():Boolean{
        return state.get() == 3
    }

    fun advanceCooking(time:Long){
        cooking_time.addAndGet(time)
        if (cooking_time.get() >= menu[food_id-1].preparationTime *rest.time_unit) state.set(3)
       // println("${cooking_time.get()} + ${this.state.get()}")
    }
}

class IntermediateOrder(
    val order_id: Int,
    var state: AtomicInteger, //0 for not ready, 1 for ready
    var items: ArrayList<Int>,
    var priority:Int,
    var max_wait: Int, var pick_up_time: Long,
    var cooking_time: Long, var table_id: Int?=null,
    var waiter_id: Int?=null, var cooking_details: ArrayList<IntermediateDetail>) {

    fun toFinished():FinishedOrder{
        var cd = ArrayList<CookingDetail>()
        for (c in cooking_details){
            var detail = CookingDetail(c.food_id, c.cook_id)
            cd.add(detail)
        }
        return FinishedOrder(order_id, items, priority, max_wait, pick_up_time, cooking_time-System.currentTimeMillis(), table_id, waiter_id, cd)
    }

    fun checkIfReady():Boolean{
        for (cd in cooking_details) {
            if (!cd.isFinished()) return false
        }
        this.state.set(1)
        return true
    }

}