package com.github.affishaikh.kotlinbuildergenerator.domain.pactTypes

import com.github.affishaikh.kotlinbuildergenerator.constants.Constants
import java.util.Objects.isNull

data class StringType(val name: String? = null) : Type {

    override fun dslString() = listOf(
        Constants.NEW_LINE,
        Constants.STRING_TYPE,
        Constants.OPENING_BRACKET,
        if (isNull(name)) "" else stringify(name),
        Constants.CLOSING_BRACKET
    ).joinToString("")
}

private fun stringify(name: String?) = if (name == "") "" else "\"${name}\""
