package com.github.ast.parser

import com.google.gson.Gson
import com.google.gson.JsonObject
import kastree.ast.psi.Parser
import mu.KotlinLogging
import org.jetbrains.kotlin.compilerRunner.KotlinLogger
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class KParserImplTest {
    @get: Rule
    var rule: MockitoRule = MockitoJUnit.rule()

    private val gson = Gson()

    private fun setup(kotlinFile: String): JsonObject {
        val file = Parser.parseFile(kotlinFile, true)
        return gson.toJsonTree(file).asJsonObject
    }

    /**
     * Method Body Detection
     */

    @Test
    fun `Method body is a block function`() {

    }

    @Test
    fun `Method body is an expression`() {

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
        val node = setup("var p = Person()")

        println(node)
    }

    @Test
    fun `Declaration is a simple member property`() {
        val node = setup("var p = Person()")

        println(node)
    }

    @Test
    fun `Declaration is a complex member property`() {
        val node = setup("var p = Person()")

        println(node)
    }

    @Test
    fun `Declaration is an anonymous function`() {
        val node = setup("var p = Person()")

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

    private fun function() : String {
        return """
            package foo

            fun bar() : String {
                // Print hello
                println("Hello, World!")
            }
        """.trimIndent()
    }

    companion object {
        private val logger = KotlinLogging.logger{}
    }

}