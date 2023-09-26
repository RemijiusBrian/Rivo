package dev.ridill.rivo.core.domain.service

import com.notkamui.keval.Keval
import dev.ridill.rivo.core.domain.util.tryOrNull

class ExpEvalService {
    fun isExpression(value: String): Boolean = value.any { char ->
        char == '+'
                || char == '-'
                || char == '*'
                || char == '/'
                || char == '%'
                || char == '^'
    }

    fun evalOrNull(expression: String): Double? = tryOrNull {
        Keval.eval(expression)
    }
}