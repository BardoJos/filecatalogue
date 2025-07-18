/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("EnumEntryName")

package com.farrusco.projectclasses.widget.calculation

enum class NormalOperators(val sign: String, val precedence: Int) {
    PLUS("+", 2),
    MINUS("-", 2),
    MULTIPLY("*", 3),
    DIVISION("/", 4),
    POWER("^", 5),
    EXPONENTIAL("E", 5),
    UNARY("u", 6);
}

enum class FunctionalOperators(val func: String) {
    sin("sin("),
    cos("cos("),
    tan("tan("),
    asin("asin("),
    acos("acos("),
    atan("atan("),
    sinh("sinh("),
    cosh("cosh("),
    tanh("tanh("),
    log2("log2("),
    log10("log10("),
    ln("ln("),
    logx("log"),
    sqrt("sqrt("),
    exp("exp(")

}

infix fun <T> String.isIn(operators: Array<T>): Boolean {

    for (operator in operators) {
        if (operator is NormalOperators) {
            if (this == operator.sign) {
                return true
            }
        } else if (operator is FunctionalOperators) {
            if (this.contains(operator.func)) {
                return true
            } else if (this.contains(FunctionalOperators.logx.func)) {
                return true
            }
        }
    }
    return false
}

infix fun <T> String.notIn(operators: Array<T>): Boolean {
    return !(this isIn operators)
}



