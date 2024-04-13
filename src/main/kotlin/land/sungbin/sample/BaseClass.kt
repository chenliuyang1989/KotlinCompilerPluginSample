package land.sungbin.sample


/*
 * Author: Eric.chen$
 * Created: 2024/3/13$
 * Last modified: 2024/3/13$
 * Description: 
 */
open class BaseClass {



    @PropertyChangeCallback(name="methodFromBaseClass")
    fun methodFromBaseClass( p1:String):String{
        return "";
    }


    open fun printlnxxxx(p:ParamObject?,context: Context?){

        println("this is base println from Base Class")
    }


}