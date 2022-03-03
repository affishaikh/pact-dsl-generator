package com.github.affishaikh.kotlinbuildergenerator.services

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.BOOLEAN_TYPE
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.NUMBER_TYPE
import com.github.affishaikh.kotlinbuildergenerator.constants.Constants.STRING_TYPE
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlinx.serialization.compiler.resolve.toClassDescriptor

class DefaultValuesFactory {

    private val typeChecker = TypeChecker()

    fun defaultValue(parameterType: KotlinType): String {
        return when {
            KotlinBuiltIns.isBooleanOrNullableBoolean(parameterType) -> "false"
            KotlinBuiltIns.isCharOrNullableChar(parameterType) -> "''"
            KotlinBuiltIns.isDoubleOrNullableDouble(parameterType) -> "0.0"
            KotlinBuiltIns.isFloatOrNullableFloat(parameterType) -> "0.0f"
            typeChecker.isNullableInt(parameterType) -> "0"
            KotlinBuiltIns.isLongOrNullableLong(parameterType) -> "0"
            KotlinBuiltIns.isShort(parameterType) -> "0"
            KotlinBuiltIns.isCollectionOrNullableCollection(parameterType) -> "arrayOf()"
            KotlinBuiltIns.isStringOrNullableString(parameterType) -> "\"\""
            KotlinBuiltIns.isListOrNullableList(parameterType) -> "listOf()"
            KotlinBuiltIns.isSetOrNullableSet(parameterType) -> "setOf()"
            KotlinBuiltIns.isMapOrNullableMap(parameterType) -> "mapOf()"
            typeChecker.isBigDecimal(parameterType) -> "BigDecimal.ZERO"
            typeChecker.isEnumClass(parameterType) -> valueForEnum(parameterType)
            typeChecker.isDateTimeType(parameterType) -> initiateWithNow(parameterType)
            KotlinBuiltIns.isNullableAny(parameterType) -> "null"
            typeChecker.doesNeedABuilder(parameterType) -> "${parameterType.toString().replace("?", "")}Builder().build()"
            parameterType.isMarkedNullable -> "null"
            else -> ""
        }
    }

    fun defaultValueForPactDsl(parameterType: KotlinType): String {
        return when {
            KotlinBuiltIns.isBooleanOrNullableBoolean(parameterType) -> BOOLEAN_TYPE
            KotlinBuiltIns.isCharOrNullableChar(parameterType) -> STRING_TYPE
            KotlinBuiltIns.isDoubleOrNullableDouble(parameterType) -> NUMBER_TYPE
            KotlinBuiltIns.isFloatOrNullableFloat(parameterType) -> NUMBER_TYPE
            typeChecker.isNullableInt(parameterType) -> NUMBER_TYPE
            KotlinBuiltIns.isLongOrNullableLong(parameterType) -> NUMBER_TYPE
            KotlinBuiltIns.isStringOrNullableString(parameterType) -> STRING_TYPE
            typeChecker.isBigDecimal(parameterType) -> NUMBER_TYPE
            typeChecker.isEnumClass(parameterType) -> STRING_TYPE
            else -> ""
        }
    }

    private fun valueForEnum(parameterType: KotlinType): String {
        val enumConstructorParams = getConstructorParameters(parameterType).map { valueParam ->
            valueParam.name.identifier
        } + listOf("name", "ordinal")

        val enumVariableNames = parameterType.memberScope.getVariableNames().map { it.toString() }
        return "${parameterType.toString().replace("?", "")}.${(enumVariableNames - enumConstructorParams).first()}"
    }

    private fun getConstructorParameters(parameterType: KotlinType): MutableList<ValueParameterDescriptor> =
        parameterType.toClassDescriptor?.unsubstitutedPrimaryConstructor?.valueParameters!!

    private fun initiateWithNow(parameterType: KotlinType): String {
        val temporal = parameterType.constructor.declarationDescriptor?.name?.identifier
        return """$temporal.now()"""
    }
}