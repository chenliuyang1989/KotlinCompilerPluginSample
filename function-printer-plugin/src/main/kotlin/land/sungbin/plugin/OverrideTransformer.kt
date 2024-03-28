package land.sungbin.plugin

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName


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

    /**
     * 声明一个函数引用
     */
    @OptIn(FirIncompatiblePluginAPI::class)
    var funPrintln = context.referenceFunctions(FqName("kotlin.io.println")).single {

        val parameters = it.owner.valueParameters
        parameters.size == 1 && parameters[0].type ==  context.irBuiltIns.anyNType
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitClassNew(declaration: IrClass): IrStatement {



          // override 方法时，需要先将原来的方法删除掉
           declaration.declarations.removeIf { it is IrFunction && it.name.asString() == "printlnxxxx" }

        // this way to override function can not work
        val oneOverrideFunction =   declaration.addFunction("printlnxxxx",context.irBuiltIns.unitType,Modality.OPEN).apply {
            overriddenSymbols += printlnxxxx
            origin = JvmLoweredDeclarationOrigin.DEFAULT_IMPLS
            body = DeclarationIrBuilder(context,symbol).irBlockBody {
                + irCall(funPrintln).apply {
                    putValueArgument(0,irString("1212211212"))
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