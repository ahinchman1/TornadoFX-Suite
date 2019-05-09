package com.github.ast.parser

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.util.ArrayList

class KParserTest {
    @get: Rule
    var rule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var componentBreakDown: (String, String, KParser) -> Unit

    @Mock
    lateinit var functions: (String, String, String, JsonObject, KParser) -> Unit

    var views = HashMap<String, TornadoFXView>()

    /**
     * breakdown of classes/files that may have independent functions
     */
    var classes = ArrayList<ClassBreakDown>()
    var independentFunctions = ArrayList<String>()

    /**
     * detectedUIControls key: by the class name
     */
    var detectedUIControls = HashMap<String, ArrayList<UINode>>()

    /**
     * mapClassViewNodes key: by the class name
     */
    var mapClassViewNodes = HashMap<String, Digraph>()

    /**
     * viewImports are saved for the test generator
     */
    var viewImports = HashMap<String, String>()

    /**
     * For recursive parsing
     */
    val gson = Gson()

    @Before
    fun setup() {
        // setup for parseAST, breakdownClass =
        gson.toJsonTree(sourceCode()).asJsonObject
    }

    @Test
    fun `breakdown decl that has expr`() {

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

    private fun function() : String {
        return """
            package foo

            fun bar() : String {
                // Print hello
                println("Hello, World!")
            }
        """.trimIndent()
    }

    private fun variable() : String {
        return """
            package foo

            var p = Person()
        """.trimIndent()
    }

}