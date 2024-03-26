package land.sungbin.plugin

/**
 *
 *
 *@author eric
 *2023/11/18
 **/
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind.CLASS
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

class FullDiExtension(
    private val messageCollector: MessageCollector
) : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val classes = mutableListOf<IrClass>()

        moduleFragment.acceptChildren(object : IrElementVisitor<Unit, MutableList<IrClass>> {
            override fun visitElement(
                element: IrElement,
                data: MutableList<IrClass>
            ) {
                element.acceptChildren(this, data)
            }

            override fun visitClass(
                declaration: IrClass,
                data: MutableList<IrClass>
            ) {
                if (declaration.name.asString() == "AppComponentImpl") {
                    data += declaration
                } else {
                    super.visitClass(declaration, data)
                }
            }
        }, classes)

        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoidWithContext() {
            override fun visitPackageFragment(declaration: IrPackageFragment): IrPackageFragment {
                classes
                    .filter { it.getPackageFragment() == declaration }
                    .forEach {
                        messageCollector.report(WARNING, "Hello ${it.name}")

                        val irClass = pluginContext.irFactory
                            .buildClass { name = Name.identifier("AppComponentImplAbc")
                                kind = CLASS }
                            .apply {
                                this.parent = declaration
                                createImplicitParameterDeclarationWithWrappedDescriptor()

                                val constructorOfAny = pluginContext.irBuiltIns.anyClass.owner.constructors.first()
                                addSimpleDelegatingConstructor(constructorOfAny, pluginContext.irBuiltIns, isPrimary = true)
                            }

                        val irClass2 = pluginContext.irFactory
                            .buildClass { name = Name.identifier("AppComponentImplAbc22")
                                kind = CLASS }
                            .apply {
                                this.parent = declaration
                                createImplicitParameterDeclarationWithWrappedDescriptor()

                                val constructorOfAny = pluginContext.irBuiltIns.anyClass.owner.constructors.first()
                                addSimpleDelegatingConstructor(constructorOfAny, pluginContext.irBuiltIns, isPrimary = true)
                            }

                        declaration.addChild(irClass)
                        declaration.addChild(irClass2)

                    }

                return declaration
            }
        })
    }
}
