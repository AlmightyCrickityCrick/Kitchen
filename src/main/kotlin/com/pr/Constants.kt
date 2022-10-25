import com.pr.Chef
import com.pr.Food
import com.pr.MenuResource
import kotlinx.serialization.json.Json
import java.io.File

object Constants{
    val NR_OF_COOKS = 4
    val NR_OF_OVEN = 2
    val NR_OF_STOVE = 1
    val TIME_UNIT = 1000
    //val DINING_URL = "http://dining-container:8080"
    val DINING_URL = "http://dining-container1:8082"
    //val DINING_URL = "http://localhost:8080"

    fun getMenu():ArrayList<Food>{
        var conf = File("config/menu.json").inputStream().readBytes().toString(Charsets.UTF_8)
        var foods= Json{coerceInputValues = true}.decodeFromString(MenuResource.serializer(), conf).foods

        print(foods)
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