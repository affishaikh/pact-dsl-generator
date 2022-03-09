package com.github.affishaikh.kotlinbuildergenerator.domain.pactTypes

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants

data class NumberType(val name: String? = null) : Type {

    override fun dslString() = listOf(
        Constants.NEW_LINE,
        Constants.NUMBER_TYPE,
        Constants.OPENING_BRACKET,
        stringify(name),
        Constants.CLOSING_BRACKET
    ).joinToString("")
}

private fun stringify(name: String?) = if (name == "") "" else "\"${name}\""
