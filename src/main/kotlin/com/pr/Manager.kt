package com.pr

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

val client = HttpClient()


class Manager {

    suspend fun checkOrders(){
        for(item in finishChannel){
            println("${item.order_id} checking for finishing")
            if (item!= null && orderList[item.order_id]?.checkIfReady() == true) sendFood(item.order_id)
        }
    }

    suspend fun sendFood(i:String) {
        var ord = orderList[i]?.toFinished()
        orderList.remove(i)
        if (ord != null) {
            val serilizedOrder = Json.encodeToString(FinishedOrder.serializer(), ord)
            println(serilizedOrder)
            log.info {  "${ord?.order_id} is done"}
                    val resp: HttpResponse = client.post(Constants.DINING_URL + "/distribution") {
                        setBody(serilizedOrder)
                    }
                }
            }


}