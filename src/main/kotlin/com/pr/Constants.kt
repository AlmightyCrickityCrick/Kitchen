import com.pr.Chef
import com.pr.Food

object Constants{
    val NR_OF_COOKS = 4
    val NR_OF_OVEN = 2
    val NR_OF_STOVE = 1
    val TIME_UNIT = 1000
    val DINING_URL = "http://dining-container:8080"

    fun getMenu():ArrayList<Food>{
        var foods= ArrayList<Food>()
        foods.add(
            Food(1, "pizza", 20, 2, "oven" )
        )

        foods.add(
            Food(2, "salad", 10, 1, null )
        )
        foods.add(
            Food(3, "zeama", 7, 1, "stove" )
        )
        foods.add(
            Food(4, "Scallop Sashimi", 32, 3, null )
        )
        foods.add(
            Food(5, "Island Duck", 35, 3, "oven" )
        )
        foods.add(
            Food(6, "Waffles", 10, 1, "stove" )
        )
        foods.add(
            Food(7, "Aubergine", 20, 2, "oven" )
        )
        foods.add(
            Food(8, "Lasagna", 30, 2, "oven" )
        )
        foods.add(
            Food(9, "Burger", 15, 1, "stove" )
        )
        foods.add(
            Food(10, "Gyros", 15, 1, null )
        )
        foods.add(
            Food(11, "Kebab", 15, 1, null )
        )
        foods.add(
            Food(12, "Unagi Maki", 20, 2, null )
        )
        foods.add(
            Food(13, "Tabacco Chicken", 30, 2, "oven" )
        )

        return foods

    }

    fun getChefs():ArrayList<Chef>{
        var chefList = arrayListOf<Chef>()
        var c1 = Chef()
        c1.setCook(1, 3, 4, "Gordon Ramsey", "U idiot Sandwhich")
        chefList.add(c1)

        c1 = Chef()
        c1.setCook(2, 2, 3, "Julia Child", "Why are we here?")
        chefList.add(c1)

        c1 = Chef()
        c1.setCook(3, 2, 2, "Alton Brown", "Just to suffer?")
        chefList.add(c1)

        c1 = Chef()
        c1.setCook(4, 1, 2, "Jamie Oliver", ":(")
        chefList.add(c1)
        return chefList
    }
}