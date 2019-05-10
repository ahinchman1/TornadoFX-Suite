package com.github.ast.parser

import com.github.ast.parser.nodebreakdown.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kastree.ast.Node
import java.util.ArrayList

/**
 * Uses the Kastree PSI Parser to break down relevant Kotlin files in a project
 * into a hierarchy of nodes and reverse-parse recursively into the desired data
 * structures for testing.
 */
interface KParser {

    /**
     * @return breakdown of classes used for generative testing
     */
    var classes: ArrayList<ClassBreakDown>

    /**
     * @return breakdown of independent functions located in files. Not used yet.
     */
    var independentFunctions: ArrayList<String>

    /**
     * key: class name
     * value: all UI Nodes in a particular class
     *
     * @return all [UINode] detected per class
     */
    var detectedUIControls: MapKClassTo<ArrayList<UINode>>

    /**
     * key: class name
     * value: a representation of the view hierarchy via a [Digraph]
     *
     * @return all available view hierarchies represented by directed graphs found in view classes
     */
    var mapClassViewNodes: MapKClassTo<Digraph>

    /**
     * key: class name
     * value: saved package name needed for a particular dependency import in test generation
     *
     * @return all possible project-specific dependencies needed in generated test file
     */
    var viewImports: MapKClassTo<String>

    /**
     * Allows for intensive recursive parsing of Psi Node tree parsing.
     *
     * @return [Gson] object
     */
    val gson: Gson

    /**
     * Pass in Kotlin code in the form of a [String] and use the Kastree PSI Parser
     * to start breaking declarations of a file down into their respective classes
     * or independent functions.
     *
     * @param textFile: [String] -> string value of Kotlin code read from file
     */
    fun parseAST(textFile: String)

    /**
     * Begin breaking down AST parsing for a Kotlin class by:
     *  - super classes
     *  - class properties
     *  - class methods
     *  - structs: companion objects, etc TODO get to this eventually
     *
     * @param className: [String] -> Node.Decl.name of class
     * @param file: [Node.File] -> kastree.ast.Node.File
     */
    fun breakDownClass(className: String, file: Node.File)

    /**
     * Convert method with [Gson] and start breaking down class methods
     * to record into a [Method] object.
     *
     *  TODO Part 1 - Start collecting the content of methods from current AST breakdown.
     *  TODO Part 2 - Map nodes-to-function as method analysis detects node property changes.
     *
     * @param method: [Node.Decl.Func] -> kastree.ast.Node.Decl.Func
     * @param classMethods: [ArrayList] -> ArrayList of [Methods] passed down
     *                                     to preserve methods per class
     */
    fun breakdownClassMethod(method: Node.Decl.Func, classMethods: ArrayList<Method>)

    /**
     * Determine whether a method is a block:
     *
     *     fun blockMethod() { ... }
     *
     * is an expression for reflective calls:
     *
     *     ::reflectiveMethod    TODO double check this one
     *
     * or is a single assignment statement:
     *
     *     fun expressionMethod(): String = "Hello world!"
     *
     * @param body: [JsonObject] -> containing the contents of the method
     * @param methodStatements: [ArrayList] -> ArrayList of [String] passed down
     *                                     to preserve methods per class
     */
    fun breakdownBody(body: JsonObject, methodStatements: ArrayList<String>)

    /**
     * Breakdown the contents of a method located in a block, or { }.
     *
     * @param stmts: [JsonArray] -> an Array of method statements found in a block, or { },
     *                              associated with the method function
     * @param methodStatements: [ArrayList] -> ArrayList of [Method] passed down
     *                                     to preserve methods per class
     */
    fun breakdownStmts(stmts: JsonArray, methodStatements: ArrayList<String>? = ArrayList())

    /**
     * Determine whether a node declaration is an expression:
     *     TODO record example here
     * or is a body:
     *     TODO record example here
     *
     * @param decl: [JsonObject] -> node declaration object
     * @param buildStmt: [String] -> Buildup of statement string to record
     *                               for method contents
     *
     * @return [String], continued statement building for method content
     */
    fun breakdownDecl(decl: JsonObject, buildStmt: String): String

    /**
     * TODO does not account for all decl property types nor is it recorded correctly
     * Detects different kinds of property declarations in a method and their assignment value
     * Expressions:
     *     TODO record examples here
     * Binary Operations:
     *     catScheduleScope.model.item = catSchedule
     * Value
     *
     * @param decl: [JsonObject] -> node declaration object
     * @param buildStmt: [String] -> Buildup of statement string to record
     *                               for method contents
     *
     * @return [String], continued statement building for method content
     */
    fun breakdownDeclProperty(decl: JsonObject, buildStmt: String): String

    /**
     * Detects different and breaks down expressions, see implementation
     *
     * @param expr: [JsonObject] -> node expression object
     * @param buildStmt: [String] -> Buildup of statement string to record
     *                               for method contents
     * @param methodStatements: [ArrayList] -> simply passed down if it's
     *                               coming from the method breakdown
     *
     * @return [String], continued statement building for method content
     */
    fun breakdownExpr(
            expr: JsonObject,
            buildStmt: String,
            methodStatements: ArrayList<String>? = arrayListOf()
    ): String

    /**
     * TODO FIND THE DIFFERENCE BETWEEN PARAMS, ARGUMENTS, AND PARAMS
     * Breaks down parameter values passed within a method call
     *
     * @param params: [JsonArray] -> node parameters
     * @param buildStmt: [String] -> Buildup of statement string to record
     *                               for method contents
     *
     * @return [String], continued statement building for method content
     */
    fun getParams(params: JsonArray, buildStmt: String): String

    /**
     * TODO FIND THE DIFFERENCE BETWEEN PARAMS, ARGUMENTS, AND PARAMS
     * Breaks down elements within a collection
     *
     * @param elems: [JsonArray] -> node elements
     * @param buildStmt: [String] -> Buildup of statement string to record
     *                               for method contents
     *
     * @return [String], continued statement building for method content
     */
    fun getElems(elems: JsonArray, buildStmt: String): String

    /**
     * TODO FIND THE DIFFERENCE BETWEEN PARAMS, ARGUMENTS, AND PARAMS
     * Breaks down parameter values passed within a method call
     *
     * @param arguments: [JsonArray] -> node arguments
     * @param buildStmt: [String] -> Buildup of statement string to record
     *                               for method contents
     *
     * @return [String], continued statement building for method content
     */
    fun getArguments(arguments: JsonArray, buildStmt: String): String

    /**
     * @param value: [JsonObject] -> node arguments
     *
     * @return [String], continued statement building for method content
     */
    fun getPrimitiveValue(value: JsonObject): String

    /**
     * TODO consolidate both getPrimitiveTypes
     * @param form: [JsonObject] -> node form
     *
     * @return [String], continued statement building for method content
     */
    fun getPrimitiveType(form: JsonObject): String

    /**
     * TODO consolidate both getPrimitiveTypes
     * @param form: [String] -> node form value
     *
     * @return [String], continued statement building for method content
     */
    fun getPrimitiveType(form: String): String

    /**
     * Accounts for operations such as
     *      | . | = | != | - | == | .. | as |
     *
     * @param token: [String] -> node form value
     *
     * @return [String], continued statement building for method content
     */
    fun getToken(token: String): String

    /**
     * Breakdowns all ${left-hand}.${right-hand] operations as needed.
     *
     * @param expr: [String] -> used to grab lhs(), the operation, and rhs()
     * @param buildStmt: [String] -> Buildup of statement string to record
     *                               for method contents
     *
     * @return [String], continued statement building for method content
     */
    fun breakdownBinaryOperation(expr: JsonObject, buildStmt: String): String

    /***
     * Properties -
     *     Properties tend to have 2 types I care about:
     *     1) kastree.ast.Node.Expr.Call -> is a member property of a certain class
     *     2) kastree.ast.Node.Expr.Name -> an independent member property
     *
     * Note: This is the older version of getting properties. Down the road
     *       this ought to be refactored to use the above recursive functions.
     *
     * Note: Execute vararg functions here to locate views and other view-specific
     *      components per configurations.
     *
     * @param property: [Node.Decl.Property] - kastree.ast.Node.Decl.Property
     * @param propList: [ArrayList] - contains class member properties
     *                                to be saved to class info
     */
    fun convertToClassProperty(
            property: Node.Decl.Property,
            propList: ArrayList<Property>,
            className: String
    )

    /***
     * Depending on whether the currentPath resides in a java file or a kotlin file
     * within the source code, the currentPath is rendered into an address for
     * importing a particular class for test generation.
     *
     * @return [String] file dependency address for import
     */
    fun saveViewImport(): String

    /***
     * // TODO check if this is TornadoFX specific
     * Usually observables come from lists denoted as observable() as an RxJava.
     * operation
     *
     * @param node: [JsonObject] - property based on binary operation
     * @param isolatedName: [String] - name assigned to property
     *
     * @return [Property] observable
     */
    fun getObservableProperty(node: JsonObject, isolatedName: String): Property

    /***
     * // TODO lol this is f**ked up
     * Grabs the type or the collection type of a class member property.
     *
     * @param node: [JsonObject] - node property
     * @param isolated: [JsonObject] - for potential dependency injection
     * @param isolatedName: [String] - name assigned to property
     *
     * @return [Property] a class member property or class member collection
     */
    fun getProperty(node: JsonObject, isolated: JsonObject, isolatedName: String): Property

    /***
     * Kastree readOnly values indicates whether a value is mutable or immutable.
     *
     * @param node: [JsonObject] - node property
     * @param isolated: [JsonObject] - for potential dependency injection
     * @param isolatedName: [String] - name assigned to property
     *
     * @return [String] 'val' or 'var'
     */
    fun valOrVar(node: JsonObject): String

}
