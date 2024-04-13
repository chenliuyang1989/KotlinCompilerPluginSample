package land.sungbin.sample


/*
 * Author: Eric.chen$
 * Created: 2024/3/13$
 * Last modified: 2024/3/13$
 * Description: 
 */
class BaseClassImpl(var age:Int): BaseClass() {


    @PropertyChangeCallback(name="method1")
    fun method1( p1:String):String{
        return "";
    }


    @PropertyChangeCallback(name="method2")
    fun method2(p1:String):String{

        return "";
    }


    fun test(){


    }

    fun show(){

        println("this is show method")
        val p = ParamObject()
        println(p.name)
        printlnxxxx(ParamObject(),Context());

    }

}

fun main(){

    val basne = BaseClassImpl(1)
    basne.printlnxxxx(ParamObject(),Context())

}