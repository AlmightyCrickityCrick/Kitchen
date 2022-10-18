package com.pr

import kotlinx.serialization.Serializable

@Serializable
data class Self(var nr_of_tables: Int,
                var nr_of_waiters: Int,
                var max_foods: Int,
                var time_unit: Int,
                var kitchen_url: String,
                var dining_url : String,
                var dining_port: Int,
                var kitchen_port: Int,
                var kitchen_ordering: String,
                var restaurant_name: String,
                var restaurant_id : Int,
                var food_ordering_url: String,
                var cook_prof: Int,
                var cook_ap:Int)
@Serializable
data class Food(val id: Int, val name : String, val preparationTime : Int, val complexity: Int, val cookingApparatus: String? =null){}


@Serializable
data class Order (val order_id : Int, var items: ArrayList<Int>, var priority:Int, var max_wait : Int,
                  var pick_up_time : Long, var table_id: Int?=null, var waiter_id: Int?=null)

@Serializable
data class CookingDetail(val food_id:Int, var cook_id:Int?=null){
}

@Serializable
data class FinishedOrder(
    val order_id: Int, var items: ArrayList<Int>, var priority:Int, var max_wait: Int, var pick_up_time: Long,
    var cooking_time: Long = 0, var table_id: Int?=null, var waiter_id: Int?=null, var cooking_details: ArrayList<CookingDetail>?=null){
    fun hasUnpreparedFood():Boolean{
        if (cooking_details == null || cooking_details!!.size == 0) return true
        for (d in cooking_details!!){
            if (d.cook_id==null) return true
        }
        return false
    }
}