package github.ast.parser

import com.github.ast.parser.*
import com.github.ast.parser.frameworkconfigurations.ComponentBreakdownFunction
import com.github.ast.parser.frameworkconfigurations.DetectFrameworkComponents
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import kastree.ast.psi.Parser
import mu.KotlinLogging
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import javax.json.Json
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
        val method = parseAST(
                """
                    fun bar() : String {
                        // Print hello
                        println("Hello, World!")
                    }
                """.trimIndent()
        )
        val stmts = arrayListOf<String>()
        val body = method.getMethodBody()

        parser.breakdownBody(body, stmts)
        verify(parser).breakdownStmts(body.block().stmts(), stmts)
    }

    @Test
    fun `Method body is an expression`() {
        val method = parseAST(
                """
                    override fun onHeader(text:Text) = runLater {
                        textarea.appendText("\tpart of header. skip\n")
	                }
                """.trimIndent()
        )
        val body = method.getMethodBody()

        val result = parser.breakdownBody(body, arrayListOf())
        verify(parser).breakdownExpr(body.expr(), "")
        assertEquals(result.size, 1)
    }

    @Test
    fun `Method body is a binary operation`() {

    }

    /**
     * Method Statement Detection
     */

    @Test
    fun `Method statements are empty`() {
        val method = parseAST(
                """
                    fun emptyFunction() {
                        // why would this be a thing ever lol oh well
                    }
                """.trimIndent()
        )

        val body = method.getMethodBody().block().stmts()

        val result = parser.breakdownStmts(body, arrayListOf())
        assertEquals(result.size, 0)
    }

    @Test
    fun `Method statement is an expression`() {
        val method = parseAST(
                """
                    fun changeCatAvi(catSchedule: CatSchedule) {
                        view.avi.children.clear()
                        val catScheduleScope = catSchedule
                        catScheduleScope.model.item = catSchedule
                    }
                """.trimIndent()
        )

        val body = method.getMethodBody().block().stmts()

        val result = parser.breakdownStmts(body, arrayListOf())
        verify(parser).breakdownExpr(body[0].asJsonObject.expr(), "")
        verify(parser).breakdownExpr(body[2].asJsonObject.expr(), "")
        assertEquals(result.size, 3)
    }

    @Test
    fun `Method statement is a declaration`() {
        val method = parseAST(
                """
                    fun editCatSchedule(catSchedule: CatSchedule) {
                        val catScheduleScope = CatScheduleScope()
                        catScheduleScope.model.item = catSchedule
                    }
                """.trimIndent()
        )

        val body = method.getMethodBody().block().stmts()

        val result = parser.breakdownStmts(body, arrayListOf())
        verify(parser).breakdownDecl(body[0].asJsonObject.decl(), "")
        assertEquals(result.size, 2)
    }

    @Test
    fun `Method statement is an expression call`() {
        // TODO
    }

    /**
     * Breakdown Node Declaration
     */

    @Test
    fun `Declaration is an expression`() {
        val method = parseAST(
                """
                    fun createPerson() {
                        val p = Person()
                    }
                """.trimIndent()
        )

        val declaration = method.getMethodBody().block().stmts()[0].asJsonObject.decl()

        val result = parser.breakdownDecl(declaration, "")
        verify(parser).breakdownDeclProperty(declaration, "")
        assertEquals(result, "val p = Person()")
    }

    @Test
    fun `Declaration is an anonymous function`() {
        // TODO
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

    private fun JsonObject.getMethodBody(): JsonObject = this.decls().getObject(0).body()

    companion object {
        private val logger = KotlinLogging.logger{}
    }

}