package com.pr

import java.util.concurrent.atomic.AtomicInteger

data class IntermediateDetail(val food_id:Int,
                              var cook_id:Int?=null,
                              var state:AtomicInteger //0 = not started, 1 = started 2 = ready
                              )

class IntermediateOrder(
    val order_id: Int,
    var state: AtomicInteger, //0 for not ready, 1 for ready
    var items: ArrayList<Int>,
    var priority:Int,
    var max_wait: Int, var pick_up_time: Long,
    var cooking_time: Long, var table_id: Int,
    var waiter_id: Int, var cooking_details: ArrayList<IntermediateDetail>) {

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
            if (cd.state.get() == 0 || cd.state.get() == 1) return false
        }
        this.state.set(1)
        return true
    }

}