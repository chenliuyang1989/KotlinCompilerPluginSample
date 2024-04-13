package land.sungbin.plugin

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.fieldByName
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrBranchImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

/**
 * -Dorg.gradle.debug=true -Pkotlin.compiler.execution.strategy=in-process
 */
class OverrideTransformer(
    val context: IrPluginContext,
    private val logger: MessageCollector,

    ) : IrElementTransformerVoidWithContext() {


        // this is the super method need to override
    @OptIn(FirIncompatiblePluginAPI::class)
    private val printlnxxxx =
        context.referenceFunctions(FqName("land.sungbin.sample.BaseClass.printlnxxxx")).single()



    // this is the super method need to override
    @OptIn(FirIncompatiblePluginAPI::class)
    private val PropertyChangeCallbackClass =
        context.referenceClass(FqName("land.sungbin.sample.PropertyChangeCallback"))

    /**
     * 声明一个函数引用
     */
    @OptIn(FirIncompatiblePluginAPI::class)
    var funPrintln = context.referenceFunctions(FqName("kotlin.io.println")).single {

        val parameters = it.owner.valueParameters
        parameters.size == 1 && parameters[0].type ==  context.irBuiltIns.anyNType
    }

//    @OptIn(FirIncompatiblePluginAPI::class)
//    val getNameMethod = context.referenceFunctions(FqName("land.sungbin.sample.ParamObject.getName")).single()

    @OptIn(FirIncompatiblePluginAPI::class)
    val ParamObject = context.referenceClass(FqName("land.sungbin.sample.ParamObject"))


    @OptIn(FirIncompatiblePluginAPI::class)
    var method1 = context.referenceFunctions(FqName("land.sungbin.sample.BaseClassImpl.method1")).first()
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitClassNew(declaration: IrClass): IrStatement {

        if(!declaration.name.toString().equals("BaseClassImpl"))  return super.visitClassNew(declaration)

        logger.report(CompilerMessageSeverity.WARNING,"函数名字是：,参数是$")

      val changeCallbacks = declaration.functions.filter { it.hasAnnotation(PropertyChangeCallbackClass!!) }


        changeCallbacks.forEach {

            val name = it.name
            val valueParams = it.valueParameters
            valueParams.forEach {
                param-> logger.report(CompilerMessageSeverity.WARNING,"函数名字是：${name},参数是${param}")
            }
        }


      val printFunction = declaration.declarations.first { it is IrFunction && it.name.asString() == "printlnxxxx"}

        // override 方法时，需要先将原来的方法删除掉
        declaration.declarations.removeIf { it is IrFunction && it.name.asString() == "printlnxxxx" }


        // 同一个类中使用irGetField
        val nameP =  ParamObject!!.owner.properties.filter { it.name.toString() == "name" }.first().backingField as IrField

        ParamObject.owner.addFunction("getNamexx",context.irBuiltIns.stringType,Modality.OPEN).apply {
           body =  DeclarationIrBuilder(context,symbol).irBlockBody {

                + irCall(funPrintln).apply {
                    putValueArgument(0,  irGetField(null, nameP))
                }

                + irReturn(irString("121212"))

            }
        }

        // this way to override function can not work
        val oneOverrideFunction =   declaration.addFunction("printlnxxxx",context.irBuiltIns.unitType,Modality.OPEN).apply {
            overriddenSymbols += printlnxxxx

            // 原函数的参数搬过来
            valueParameters +=  ( printFunction as IrFunction).valueParameters

//            origin = JvmLoweredDeclarationOrigin.DEFAULT_IMPLS
            body = DeclarationIrBuilder(context,symbol).irBlockBody {

                    + irCall(funPrintln).apply {
                        putValueArgument(0,irString("1212211212"))
                    }

                    + irCall(method1).also {
                        it1->
                        it1.dispatchReceiver = dispatchReceiverParameter?.let { it2 -> irGet(it2) }
                        it1.putValueArgument(0,irString("1212"))
                    }


                val nameGetter = ParamObject!!.getPropertyGetter("name")

               // Assuming `irGet` is used to retrieve the value from `nameGetter` and `valueParameters[0]` is the receiver
                val nameValue = irCall(nameGetter!!).apply {
                    dispatchReceiver = irGet(valueParameters[0]!!)
                }


                val irBranchs = arrayListOf<IrBranchImpl>()
                irBranchs.add(IrBranchImpl(irEquals(nameValue, irString("mmm")), irCall(funPrintln).apply {
                    putValueArgument(0, irString("The name is mmm"))
                    }))
                irBranchs.add(IrBranchImpl(irEquals(nameValue, irString("mmm2")), irCall(funPrintln).apply {
                    putValueArgument(0, irString("The name is mmm"))
                   }))
                irBranchs.add(IrBranchImpl(irEquals(nameValue, irString("mmm3")), irCall(funPrintln).apply {
                    putValueArgument(0, irString("The name is mmm"))
                }))


               // Adding the when condition
                + irWhen(context.irBuiltIns.unitType,irBranchs)


               + irCall(funPrintln).apply {
                 putValueArgument(0,  irCall(nameGetter!!).apply {
                    dispatchReceiver = irGet(valueParameters[0]!!) })
               }



                changeCallbacks.forEach {

                    if(it.name.asString() == "method1"){
                        + irCall(it).also {it1->

                            it1.dispatchReceiver = dispatchReceiverParameter?.let { it2 -> irGet(it2) }

                            it1.putValueArgument(0,irString("1212"))
                        }
                    }

                }

            }
        }


        return super.visitClassNew(declaration)
    }

    private fun IrClass.addOverride(
        baseFqName: FqName,
        name: String,
        returnType: IrType,
        modality: Modality = Modality.FINAL
    ): IrSimpleFunction = addFunction(name, returnType, modality).apply {
        overriddenSymbols = superTypes.mapNotNull { superType ->
            superType.classOrNull?.owner?.takeIf { superClass -> superClass.isSubclassOfFqName(baseFqName.asString()) }
        }.flatMap { superClass ->
            superClass.functions.filter { function ->
                function.name.asString() == name && function.overridesFunctionIn(baseFqName)
            }.map { it.symbol }.toList()
        }
    }

    fun IrClass.isSubclassOfFqName(fqName: String): Boolean =
        fqNameWhenAvailable?.asString() == fqName || superTypes.any { it.erasedUpperBound.isSubclassOfFqName(fqName) }


    fun IrSimpleFunction.overridesFunctionIn(fqName: FqName): Boolean =
        parentClassOrNull?.fqNameWhenAvailable == fqName || allOverridden().any { it.parentClassOrNull?.fqNameWhenAvailable == fqName }

}