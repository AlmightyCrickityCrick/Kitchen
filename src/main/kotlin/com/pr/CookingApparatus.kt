package com.pr

import Constants
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicBoolean

//data class ApplianceOrder(val f:Food, val fId: Int, val oId: String)

class CookingApparatus:Thread() {
    var isFree=AtomicBoolean(true)

//    override fun run() {
//        super.run()
//        while (true){
//            if(apOrders.size > 0)
//                for (ao in apOrders.values)
//                    if(orderList[ao.oId]?.cooking_details?.get(ao.fId)?.isFinished() == false)
//                        cook(ao.f, ao.fId, ao.oId)
//                    else{
//                        if(orderList[ao.oId]?.checkIfReady() == true) chefList[orderList[ao.oId]?.cooking_details?.get(ao.fId)?.cook_id!! - 1].sendFood(ao.oId)
//                        if(orderList[ao.oId]?.cooking_details?.get(ao.fId)?.isFinished() == true) apOrders.remove(ao.oId+ao.fId)
//                    }
//        }
//    }

    fun cook(f: IntermediateDetail){
        var t = 3
            //if((menu[f.food_id -1].preparationTime *Constants.TIME_UNIT) - f.cooking_time.get() >= (1 * Constants.TIME_UNIT) )1 else ((menu[f.food_id -1].preparationTime *Constants.TIME_UNIT) - f.cooking_time.get()) /Constants.TIME_UNIT
            //menu[f.food_id -1].preparationTime / f.priority
        log.info {  " ${menu[f.food_id - 1].cookingApparatus} is cooking ${f.food_in_order_id} from ${f.order_id} order for $t"}
        log.info { "${foodList[1]?.get("null")?.size}" }
        //if((orderList[oId]?.priority!!*200) < ((f.preparationTime * Constants.TIME_UNIT)- orderList[oId]?.cooking_details?.get(fId)?.cooking_time?.get()!!)) orderList[oId]?.priority else ((f.preparationTime *Constants.TIME_UNIT)- orderList[oId]?.cooking_details?.get(fId)?.cooking_time?.get()!!)
        (t)?.toLong()?.let { sleep(it *Constants.TIME_UNIT)}
        if (menu[f.food_id - 1].cookingApparatus == "oven") ovenSem.release() else stoveSem.release()
        isFree.set(true)
        if (t != null) {
            f.advanceCooking((t).toLong() *Constants.TIME_UNIT)
            orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.advanceCooking((t).toLong() *Constants.TIME_UNIT)
            if(f.isFinished()) {
                availableFoods.decrementAndGet()
                if(orderList[f.order_id]?.checkIfReady()==true) chefList[orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.cook_id!! - 1].sendFood(f.order_id)

            } else {
                orderList[f.order_id]?.cooking_details?.get(f.food_in_order_id)?.state?.set(0)
                f.state.set(0)
                foodList[menu[f.food_id - 1].complexity]?.get(menu[f.food_id - 1].cookingApparatus)?.put(f)

            }

        }
            //Set food as finished
    }

}