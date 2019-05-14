package com.github.ast.parser

import com.github.ast.parser.frameworkconfigurations.ComponentBreakdownFunction
import com.github.ast.parser.frameworkconfigurations.DetectFrameworkComponents
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import kastree.ast.psi.Parser
import mu.KotlinLogging
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import kotlin.test.assertEquals

class KParserImplTest {
    @get: Rule
    var rule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var breakdownComponentFunction: ComponentBreakdownFunction

    @Mock
    private lateinit var functions: DetectFrameworkComponents

    private lateinit var parser: KParserImpl

    private val gson = Gson()

    @Before
    fun  setup() {
        val currentPath = "some.path.demo"

        parser = spy(KParserImpl(
                currentPath,
                breakdownComponentFunction,
                HashMap(),
                functions
        ))
    }

    private fun parseAST(kotlinFile: String): JsonObject {
        val file = Parser.parseFile(kotlinFile, true)
        return gson.toJsonTree(file).asJsonObject
    }

    /**
     * Method Body Detection
     */

    @Test
    fun `Method body is a block function`() {
        val function = parseAST(
                """
                    fun bar() : String {
                        // Print hello
                        println("Hello, World!")
                    }
                """.trimIndent()
        )
        val stmts = arrayListOf<String>()
        val body = function.decls().getObject(0).body()

        parser.breakdownBody(body, stmts)
        verify(parser).breakdownStmts(body.block().stmts(), stmts)
    }

    @Test
    fun `Method body is an expression`() {
        val function = parseAST(
                """
                    override fun onHeader(text:Text) = runLater {
                        textarea.appendText("\tpart of header. skip\n")
	                }
                """.trimIndent()
        )
        val stmts = arrayListOf<String>()
        val body = function.decls().getObject(0).body()

        parser.breakdownBody(body, stmts)
        assertEquals(stmts.size, 1)
        verify(parser).breakdownExpr(body.expr(), "")
    }

    @Test
    fun `Method body has a single assignment Statement`() {

    }

    /**
     * Method Statement Detection
     */

    @Test
    fun `Method statements are empty`() {

    }

    @Test
    fun `Method statement is expression`() {

    }

    @Test
    fun `Method statement is a declaration`() {

    }

    @Test
    fun `Method statement is neither an expression nor a declaration`() {
        logger.debug("")
    }

    /**
     * Breakdown Node Declaration
     */

    @Test
    fun `Declaration is empty`() {
        val node = parseAST("var p = Person()")

        println(node)
    }

    @Test
    fun `Declaration is a simple member property`() {
        val node = parseAST("var p = Person()")

        println(node)
    }

    @Test
    fun `Declaration is a complex member property`() {
        val node = parseAST("var p = Person()")

        println(node)
    }

    @Test
    fun `Declaration is an anonymous function`() {
        val node = parseAST("var p = Person()")

        println(node)
    }

    /**
     * Breakdown Node Expression
     */

    @Test
    fun `Expression is empty`() {

    }

    @Test
    fun `Expression is a binary operation`() {

    }

    @Test
    fun `Expression is a set of arguments`() {

    }

    @Test
    fun `Expression is a name`() {

    }

    @Test
    fun `Expression is another expression`() {

    }

    @Test
    fun `Expression is a set of elements`() {

    }

    @Test
    fun `Expression is a set of parameters`() {

    }

    @Test
    fun `Expression is a primitive value`() {

    }

    @Test
    fun `Expression is a block expression`() {

    }

    /**
     * Breakdown Parameters
     */
    @Test
    fun `Parameters are empty` () {

    }

    @Test
    fun `Parameters are not empty` () {

    }

    /**
     * Breakdown Elements
     */
    @Test
    fun `Elements are empty` () {

    }

    @Test
    fun `Element is a string` () {

    }

    @Test
    fun `Element is an expression` () {

    }

    @Test
    fun `Element is a primitive value` () {

    }

    @Test
    fun `Element is a binary operation` () {

    }

    @Test
    fun `Element is a name` () {

    }

    @Test
    fun `Element is a receiver` () {

    }

    @Test
    fun `Element is none of the above` () {
        logger.debug("")
    }

    /**
     * Breakdown Binary Operations
     */

    @Test
    fun `There is only one binary operation`() {

    }

    @Test
    fun `There are multiple binary operations`() {

    }

    @Test
    fun `Binary operation is an evaluation statement`() {

    }

    @Test
    fun `Binary operation is a range`() {

    }

    @Test
    fun `Binary operation is a cast`() {

    }

    @Test
    fun `Binary operation contains multiple tokens`() {

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

    private fun tfxFunction(): String {
        return """
            fun editCatSchedule(catSchedule: CatSchedule) {
                val catScheduleScope = CatScheduleScope()
                catScheduleScope.model.item = catSchedule
                find(Editor::class, scope = catScheduleScope).openModal()
            }
        """.trimIndent()
    }

    companion object {
        private val logger = KotlinLogging.logger{}
    }

}