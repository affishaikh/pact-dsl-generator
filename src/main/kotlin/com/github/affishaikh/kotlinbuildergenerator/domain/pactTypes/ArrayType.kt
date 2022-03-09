package com.github.affishaikh.kotlinbuildergenerator.domain.pactTypes

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants

data class ArrayType(val name: String? = null, val type: Type) : Type {

    override fun dslString() = listOf(
        Constants.NEW_LINE,
        Constants.ARRAY_STARTING,
        Constants.OPENING_BRACKET,
        stringify(name),
        Constants.CLOSING_BRACKET,
        type.dslString(),
        Constants.NEW_LINE,
        Constants.CLOSE_ARRAY
    ).joinToString("")
}

private fun stringify(name: String?) = if (name == "") "" else "\"${name}\""
