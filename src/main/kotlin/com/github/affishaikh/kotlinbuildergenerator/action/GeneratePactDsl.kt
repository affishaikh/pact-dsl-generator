package com.github.affishaikh.kotlinbuildergenerator.action

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.AS_BODY
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.BODY_STARTING
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.NEW_LINE
import com.github.affishaikh.kotlinbuildergenerator.domain.Parameter
import com.github.affishaikh.kotlinbuildergenerator.domain.pactTypes.*
import com.github.affishaikh.kotlinbuildergenerator.services.DefaultValuesFactory
import com.github.affishaikh.kotlinbuildergenerator.services.FileService
import com.github.affishaikh.kotlinbuildergenerator.services.TypeChecker
import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.idea.intentions.SelfTargetingIntention
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlinx.serialization.compiler.resolve.toClassDescriptor

class GeneratePactDsl : SelfTargetingIntention<KtClass>(
    KtClass::class.java,
    "Generate Pact DSL"
) {
    private val defaultValuesFactory = DefaultValuesFactory()
    private val typeChecker = TypeChecker()

    override fun applyTo(element: KtClass, editor: Editor?) {
        val code = generatePactDsl(element)
        createFile(element, code)
    }

    override fun isApplicableTo(element: KtClass, caretOffset: Int): Boolean {
        val numberOfProperties = element.primaryConstructor?.valueParameters?.size ?: return false
        return numberOfProperties > 0
    }

    private fun generatePactDsl(element: KtClass): String {
        val classProperties = element.properties()
        val tokenizedProperties = tokenize(classProperties)
        return "$BODY_STARTING${createDslFromParams(tokenizedProperties)}"
    }

    private fun KtClass.properties(): List<Parameter> {
        return this.primaryConstructor?.valueParameters?.map {
            Parameter(it.name!!, it.type()!!)
        } ?: emptyList()
    }

    private fun createFile(element: KtClass, classCode: String) {
        val fileService = FileService()
        fileService.createFile(element, classCode)
    }

    private fun createDslFromParams(parameters: List<Type>): String {

        return parameters.mapIndexed { i, it ->
            it.dslString().let {
                when {
                    (parameters[i] is ObjectType && i < (parameters.size - 1) && parameters[i + 1] !is CompositeType) -> listOf(
                        it,
                        NEW_LINE,
                        AS_BODY
                    ).joinToString("")
                    (parameters[i] is ArrayType && i < (parameters.size - 1) && parameters[i + 1] !is CompositeType) -> listOf(
                        it,
                        NEW_LINE,
                        AS_BODY
                    ).joinToString("")
                    (parameters[i] is ArrayOfClassType && i < (parameters.size - 1) && parameters[i + 1] !is CompositeType) -> listOf(
                        it,
                        NEW_LINE,
                        AS_BODY
                    ).joinToString("")
                    else -> it
                }
            }
        }.joinToString("")
    }

    private fun KotlinType.properties(): List<Parameter> {
        return getConstructorParameters(this).map { valueParam ->
            Parameter(valueParam.name.identifier, valueParam.type)
        }
    }

    private fun getConstructorParameters(parameterType: KotlinType): MutableList<ValueParameterDescriptor> =
        parameterType.toClassDescriptor?.unsubstitutedPrimaryConstructor?.valueParameters!!

    private fun tokenize(parameters: List<Parameter>): List<Type> {
        return parameters.fold(emptyList()) { acc, parameter ->
            when {
                typeChecker.isClassType(parameter.type) -> acc + ObjectType(
                    parameter.name,
                    tokenize(parameter.type.properties())
                )
                typeChecker.isArrayOfClassType(parameter.type) -> acc + ArrayOfClassType(
                    parameter.name,
                    tokenize(parameter.type.arguments.first().type.properties())
                )
                typeChecker.isArray(parameter.type) -> acc + ArrayType(
                    parameter.name,
                    defaultValuesFactory.getTokenFor(parameter.type.arguments.first().type)
                )
                else -> acc + defaultValuesFactory.getTokenFor(parameter.type, parameter.name)
            }
        }
    }
}
