package com.github.hd.tornadofxsuite.concern

import com.github.hd.tornadofxsuite.model.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName

internal class ParserTest {

    @Test
    fun parse() {
        val ast = Parser().parse(sourceCode())
        val expectedClasses = listOf(
                Class(name = "Person",
                        properties = listOf(
                                Property(name = "firstName", readOnly = false),
                                Property(name = "lastName", readOnly = false)),
                        methods = listOf(
                                Function(name = "fullName", returnType = "String", parameters = emptyList()),
                                Function(name = "sampleFunc", returnType = "Int", parameters = listOf(
                                        Parameter(name = "sampleArg", dataType = "String"),
                                        Parameter(name = "sampleArg2", dataType = "Int"))))))
        val expectedFunctions = listOf(
                Function(name = "bar", parameters = emptyList(), returnType = "String"),
                Function(name = "baz", parameters = emptyList(), returnType = "String"))
        val expectedProperties = listOf(
                Property(name = "Person")
        )

        assert(ast.classes() == expectedClasses)
        assert(ast.functions() == expectedFunctions)
//        assert(ast.properties() == expectedProperties)
    }

    private fun sourceCode() : String {
        return """
            package foo

            fun bar() : String {
                // Print hello
                println("Hello, World!")
            }

            fun baz() : String = println("Hello, again!")

            class Person(firstName: String, lastName: String) {
                var firstName : String = firstName
                var lastName : String = lastName

                fun fullName() : String {
                    return ""
                }

                fun sampleFunc(sampleArg: String, sampleArg2: Int) : Int {
                    return sampleArgs2
                }
            }

            var p = Person()
        """.trimIndent()
    }
}