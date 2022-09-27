package com.pr

import Constants
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicBoolean

data class ApplianceOrder(val f:Food, val fId: Int, val oId: String)

class CookingApparatus:Thread() {
    var isFree=AtomicBoolean(true)
    var apOrders = ConcurrentSkipListMap<String, ApplianceOrder>()

    override fun run() {
        super.run()
        while (true){
            if(apOrders.size > 0)
                for (ao in apOrders.values)
                    if(orderList[ao.oId]?.cooking_details?.get(ao.fId)?.isFinished() == false)
                        cook(ao.f, ao.fId, ao.oId)
                    else{
                        if(orderList[ao.oId]?.checkIfReady() == true) chefList[orderList[ao.oId]?.cooking_details?.get(ao.fId)?.cook_id!! - 1].sendFood(ao.oId)
                        if(orderList[ao.oId]?.cooking_details?.get(ao.fId)?.isFinished() == true) apOrders.remove(ao.oId+ao.fId)
                    }
        }
    }

    fun cook(f: Food, fId: Int, oId:String){
        apparatusMap[f.cookingApparatus]?.decrementAndGet()
        isFree.set(false)
        var t = f.preparationTime / orderList[oId]?.priority!!
        log.info {  " ${f.cookingApparatus} is cooking $fId from ${orderList[oId]?.order_id} order for $t"}
        //if((orderList[oId]?.priority!!*200) < ((f.preparationTime * Constants.TIME_UNIT)- orderList[oId]?.cooking_details?.get(fId)?.cooking_time?.get()!!)) orderList[oId]?.priority else ((f.preparationTime *Constants.TIME_UNIT)- orderList[oId]?.cooking_details?.get(fId)?.cooking_time?.get()!!)
        (t)?.toLong()?.let { sleep(it * Constants.TIME_UNIT ) }
        if (t != null) {
            orderList[oId]?.cooking_details?.get(fId)?.advanceCooking((t).toLong() * Constants.TIME_UNIT)
            if(orderList[oId]?.checkIfReady() == true) chefList[orderList[oId]?.cooking_details?.get(fId)?.cook_id!! - 1].sendFood(oId)
        }
            //Set food as finished
        isFree.set(true)
        apparatusMap[f.cookingApparatus]?.incrementAndGet()

    }

}