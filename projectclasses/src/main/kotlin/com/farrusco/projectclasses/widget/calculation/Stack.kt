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
package com.farrusco.projectclasses.widget.calculation

class Stack<T> {
    private val stack = arrayListOf<T>()
    private var top = -1
    fun push(item: T) {
        stack.add(item)
        top++
    }

    fun pop(): T = stack.removeAt(top--)

    fun peek(): T = stack[top]

    fun isEmpty() = stack.isEmpty()

    fun size() = top + 1

    fun display() = println(stack)

    fun clear(){
        stack.clear()
        top = -1
    }
}