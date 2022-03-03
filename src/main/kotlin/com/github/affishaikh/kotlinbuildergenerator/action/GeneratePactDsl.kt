package com.github.affishaikh.kotlinbuildergenerator.action

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.AS_BODY
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.CLOSE_OBJECT
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.NEW_LINE
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.OBJECT_STARTING
import com.github.affishaikh.kotlinbuildergenerator.domain.ClassInfo
import com.github.affishaikh.kotlinbuildergenerator.domain.Parameter
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
        val pactDsl = listOf(ClassInfo(element.name!!, null, classProperties))
            .joinToString("\n") {
                createDslFromParams(it.parameters)
            }
        return pactDsl
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

    private fun createDslFromParams(parameters: List<Parameter>, staring: String = Constants.BODY_STARTING): String {

        return parameters.fold(staring) { result, parameter ->
            if (typeChecker.doesNeedABuilder(parameter.type)) {
                createDslFromParams(
                    parameter.type.properties(),
                    "$result${getDslPartForSingleValueTypes(OBJECT_STARTING, parameter.name)}"
                ).let {
                    listOf(it, CLOSE_OBJECT, NEW_LINE, AS_BODY).joinToString("")
                }
            } else {
                val dslPart = defaultValuesFactory.defaultValueForPactDsl(parameter.type)
                "$result${getDslPartForSingleValueTypes(dslPart, parameter.name)}"
            }
        }.let {
            "$it$NEW_LINE"
        }
    }

    private fun getDslPartForSingleValueTypes(pactDslType: String, name: String) =
        listOf(
            NEW_LINE,
            pactDslType,
            Constants.OPENING_BRACKET,
            stringify(name),
            Constants.CLOSING_BRACKET
        ).joinToString("")

    private fun stringify(name: String) = "\"${name}\""

    private fun KotlinType.properties(): List<Parameter> {
        return getConstructorParameters(this).map { valueParam ->
            Parameter(valueParam.name.identifier, valueParam.type)
        }
    }

    private fun getConstructorParameters(parameterType: KotlinType): MutableList<ValueParameterDescriptor> =
        parameterType.toClassDescriptor?.unsubstitutedPrimaryConstructor?.valueParameters!!
}
