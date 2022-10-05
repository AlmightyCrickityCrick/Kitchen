package com.pr

import Constants
import foodList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicBoolean

//data class ApplianceOrder(val f:Food, val fId: Int, val oId: String)

class CookingApparatus(    var isFree:AtomicBoolean, var channel : Channel<IntermediateDetail>) {
    suspend fun run(){
        for (f in channel){
            cook(f)
        }
    }

    suspend fun cook(f: IntermediateDetail){
        var t = 3
            //if((menu[f.food_id -1].preparationTime *Constants.TIME_UNIT) - f.cooking_time.get() >= (1 * Constants.TIME_UNIT) )1 else ((menu[f.food_id -1].preparationTime *Constants.TIME_UNIT) - f.cooking_time.get()) /Constants.TIME_UNIT
            //menu[f.food_id -1].preparationTime / f.priority
        log.info {  " ${menu[f.food_id - 1].cookingApparatus} is cooking ${f.food_in_order_id} from ${f.order_id} order for $t"}
        //if((orderList[oId]?.priority!!*200) < ((f.preparationTime * Constants.TIME_UNIT)- orderList[oId]?.cooking_details?.get(fId)?.cooking_time?.get()!!)) orderList[oId]?.priority else ((f.preparationTime *Constants.TIME_UNIT)- orderList[oId]?.cooking_details?.get(fId)?.cooking_time?.get()!!)
        (t)?.toLong()?.let { delay(it *Constants.TIME_UNIT)}
        isFree.set(true)
            f.advanceCooking((t).toLong() *Constants.TIME_UNIT)
            orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.advanceCooking((t).toLong() *Constants.TIME_UNIT)
            if(f.isFinished()) {
                availableFoods.decrementAndGet()
                finishChannel.send(f)
            } else {
                orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.state?.set(0)
                f.state.set(0)
                foodList[menu[f.food_id - 1].complexity]?.send(f)

            }

            //Set food as finished
    }

}