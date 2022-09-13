package com.pr

import kotlinx.serialization.Serializable

data class Food(val id: Int, val name : String, val preparationTime : Int, val complexity: Int, val cookingApparatus: String? =null){}


@Serializable
data class Order (val order_id : Int, var items: ArrayList<Int>, var priority:Int, var max_wait : Int,
                  var pick_up_time : Long?  = null, var table_id: Int? = null, var waiter_id: Int? = null)

@Serializable
data class CookingDetail(val food_id:Int, var cook_id:Int?=null){
}

@Serializable
data class FinishedOrder(val order_id : Int, var items: ArrayList<Int>, var priority:Int, var max_wait : Int, var pick_up_time : Long,
                         var cooking_time:Int? = 0, var table_id: Int, var waiter_id: Int, var cooking_details: ArrayList<CookingDetail>?=null){
    fun hasUnpreparedFood():Boolean{
        if (cooking_details == null || cooking_details!!.size == 0) return true
        for (d in cooking_details!!){
            if (d.cook_id==null) return true
        }
        return false
    }
}