package com.github.affishaikh.kotlinbuildergenerator.domain.pactTypes

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants

data class ArrayOfClassType(val name: String? = null, val types: List<Type>) : CompositeType {

    override fun dslString() = listOf(
        Constants.NEW_LINE,
        Constants.ARRAY_OF_OBJECT_STARTING,
        Constants.OPENING_BRACKET,
        stringify(name),
        ", 1",
        Constants.CLOSING_BRACKET,
        types.joinToString("") { it.dslString() },
        Constants.NEW_LINE,
        Constants.CLOSE_ARRAY
    ).joinToString("")
}

private fun stringify(name: String?) = if (name == "") "" else "\"${name}\""
