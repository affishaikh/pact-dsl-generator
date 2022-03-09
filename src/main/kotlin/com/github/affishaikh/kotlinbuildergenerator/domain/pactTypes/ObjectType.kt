package com.github.affishaikh.kotlinbuildergenerator.domain.pactTypes

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants

data class ObjectType(val name: String? = null, val types: List<Type>) : Type {

    override fun dslString() = listOf(
        Constants.NEW_LINE,
        Constants.OBJECT_STARTING,
        Constants.OPENING_BRACKET,
        stringify(name),
        Constants.CLOSING_BRACKET,
        types.joinToString("") { it.dslString() },
        Constants.NEW_LINE,
        Constants.CLOSE_OBJECT
    ).joinToString("")
}

private fun stringify(name: String?) = if (name == "") "" else "\"${name}\""
