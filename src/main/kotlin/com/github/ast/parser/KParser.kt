package com.github.ast.parser

import com.google.gson.*
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*
import java.util.*
import kotlin.collections.HashMap

class KParser: Controller() {

    var classes = ArrayList<ClassBreakDown>()
    var independentFunctions = ArrayList<String>()
    var detectedUIControls = HashMap<String, ArrayList<UINode>>()
    var mapClassViewNodes = HashMap<String, Digraph>()
    var tfxViews = HashMap<String, TornadoFXView>()
    var viewImports = HashMap<String, String>()
    val gson = Gson()

    fun parseAST(textFile: String, path: String) {
        val file = Parser.parseFile(textFile, true)

        file.decls.forEach { node ->
            when (node) {
                is Node.Decl.Structured -> breakDownClass(node.name, file, path)
                is Node.Decl.Func -> node.name ?: independentFunctions.add(node.name.toString())
            }
        }
    }

    private fun breakDownClass(className: String, file: Node.File, path: String) {
        val classes = ArrayList<ClassBreakDown>()
        val classProperties = ArrayList<Property>()
        val classMethods = ArrayList<Method>()
        val classParents = ArrayList<String>()
        val currentTFXView = TornadoFXView()

        gson.toJsonTree(file).asJsonObject

        val clazz = file.decls[0] as Node.Decl.Structured

        clazz.parents.forEach {
            val parentClass = gson.toJsonTree(it).asJsonObject.type().getType()
            classParents.add(parentClass)
            if (parentClass == "Fragment" || parentClass == "View") {
                currentTFXView.view = className
                currentTFXView.type = parentClass
            }
        }

        // Save for all files
        clazz.members.forEach {
            when (it) {
                is Node.Decl.Structured -> println("this is probably a companion object")
                is Node.Decl.Property -> convertToClassProperty(it, classProperties, className, path)
                is Node.Decl.Func -> breakdownClassMethod(it, classMethods)
            }
        }

        if (!currentTFXView.type.isNullOrEmpty()) {
            tfxViews[className] = currentTFXView
        }
        classes.add(ClassBreakDown(className, classParents, classProperties, classMethods))
    }

    private fun breakdownClassMethod(method: Node.Decl.Func, classMethods: ArrayList<Method>) {
        val methodJson = gson.toJsonTree(method).asJsonObject
        val methodStatements = ArrayList<String>()
        val methodContent = methodJson.body()

        breakdownBody(methodJson.body(), methodStatements)

        val parameters = ArrayList<Property>()
        methodJson.params().forEach { parameter ->
            val param = parameter.asJsonObject
            val paramRef = param.type().ref()
            val paramType = when {
                paramRef.has("pieces") -> paramRef.getType()
                paramRef.has("type") -> paramRef.type().getType()
                else -> TODO()
            }
            parameters.add(Property("val", param.name(), paramType))
        }

        var returnType = "Unit"
        if (methodJson.has("type")) {
            returnType = methodJson.type().ref().getType()
        }

        // TODO write a mechanism to detect nodes per function after AST parse job is complete
        classMethods.add(
                Method(
                        name = methodJson.name(),
                        parameters = parameters,
                        returnType = returnType,
                        methodStatements = methodStatements,
                        viewNodesAffected = ArrayList()
                )
        )
    }


    private fun breakdownStmts(stmts: JsonArray, methodStatements: ArrayList<String>?) {
        stmts.forEach { statement ->
            val stmt = statement.asJsonObject
            when {
                stmt.has("expr") -> methodStatements?.add(breakdownExpr(stmt.expr(), ""))
                stmt.has("decl") -> methodStatements?.add(breakdownDecl(stmt.decl(), ""))
                else ->  println("stmt has$stmt")
            }
        }
    }

    private fun breakdownBody(body: JsonObject, methodStatements: ArrayList<String>) {
        when {
            // regular block function
            body.has("block") -> breakdownStmts(body.block().stmts(), methodStatements)
            // function with reflective method calls
            body.has("expr") -> methodStatements.add(breakdownExpr(body.expr(), ""))
            // function has a single assignment statement
            else -> methodStatements.add(breakdownBinaryOperation((body.expr()), ""))
        }
    }

    private fun breakdownDecl(decl: JsonObject, buildStmt: String): String {
        return when {
            decl.has("expr") -> breakdownDeclProperty(decl, buildStmt)
            decl.has("body") -> {
                breakdownBody(decl.body(), arrayListOf())
                "method body here: $decl"
            }
            else -> "MISSING DECL $decl"
        }
    }

    private fun breakdownDeclProperty(decl: JsonObject, buildStmt: String): String {
        val isolated = decl.vars().getObject(0)
        val isolatedName = isolated.name()
        val property = when {
            decl.expr().has("expr") -> getProperty(
                    decl,
                    isolated,
                    isolatedName)
            decl.expr().has("lhs") &&
                decl.expr().has("oper") &&
                decl.expr().has("rhs") -> Property(
                    valOrVar(decl),
                    isolatedName,
                    breakdownBinaryOperation(decl.expr(), "")
            )
            decl.expr().has("value") -> Property(
                    valOrVar(decl),
                    isolatedName,
                    getPrimitiveType(decl.expr()))
            else -> TODO()
        }
        val declaration = "$buildStmt${property.valOrVar} $isolatedName: ${property.propertyType}"
        return "$declaration = ${breakdownExpr(decl, buildStmt)}"
    }

    private fun breakdownExpr(expr: JsonObject,
                              buildStmt: String,
                              methodStatements: ArrayList<String>? = arrayListOf()): String {
        when {
            expr.has("lhs") &&
                    expr.has("oper") &&
                    expr.has("rhs")-> breakdownBinaryOperation(expr, buildStmt)
            expr.has("args") -> getArguments(expr.args(), buildStmt)
            expr.has("name") -> buildStmt + expr.name()
            expr.has("expr") -> breakdownExpr(expr.expr(), buildStmt)
            expr.has("elems") -> getElems(expr.elems(), buildStmt)
            expr.has("params") -> getParams(expr.params(), buildStmt)
            expr.has("value") -> buildStmt + getValue(expr)
            expr.has("block") -> breakdownStmts(expr.block().stmts(), methodStatements)
            expr.size() == 0 -> {}
            else -> println(expr)
        }
        return buildStmt
    }

    private fun getParams(params: JsonArray, buildStmt: String): String {
        var buildParams = buildStmt
        params.forEach{ parameter ->
            buildParams += parameter.asJsonObject.vars().getObject(0).name()
        }
        return buildParams
    }

    private fun getElems(elems: JsonArray, buildStmt: String): String {
        var buildElems = buildStmt
        if (elems.size() > 0) {
            elems.forEach {
                val elem = it.asJsonObject
                buildElems += when {
                    elem.has("str") -> elem.str()
                    elem.has("expr") -> breakdownExpr(elem, buildElems)
                    elem.has("value") -> getValue(elem)
                    elem.has("lhs") &&
                            elem.has("oper") &&
                            elem.has("rhs") -> breakdownBinaryOperation(elem, buildElems)
                    elem.has("name") -> elem.name()
                    elem.has("recv") -> elem.recv().type().getType()
                    else -> println("Looks like this element type is: $elem")
                }
            }
        }
        return buildElems
    }

    private fun getArguments(arguments: JsonArray, buildStmt: String): String {
        var buildArgs = "$buildStmt("
        if (arguments.size() > 0) {
            arguments.forEachIndexed { index, argument ->
                val argExpression = argument.asJsonObject.expr()
                if (argExpression.has("elems")) {
                    buildArgs += getElems(argExpression.elems(), buildArgs)
                }
                buildArgs += if (index < arguments.size()) ", " else ")"
            }
        } else buildArgs += ")"
        return buildArgs
    }

    private fun getValue(value: JsonObject): String {
        val gValue = value.get("value")
        return when (value.get("form").asJsonPrimitive.toString()) {
            "\"BOOLEAN\"" ->  gValue.asBoolean.toString()
            "\"BYTE\"" -> gValue.asByte.toString()
            "\"CHAR\"" -> gValue.asCharacter.toString()
            "\"DOUBLE\"" -> gValue.asDouble.toString()
            "\"FLOAT\"" -> gValue.asFloat.toString()
            "\"INT\"" -> gValue.asInt.toString()
            "\"NULL\"" -> "null"
            else -> "Unrecognized value type"
        }
    }

    // TODO rewrite to accept 2 types for primitive
    private fun getPrimitiveType(value: JsonObject): String {
        return when (value.get("form").asJsonPrimitive.toString()) {
            "\"BOOLEAN\"" -> "Boolean"
            "\"BYTE\"" -> "Byte"
            "\"CHAR\"" -> "Char"
            "\"DOUBLE\"" -> "Double"
            "\"FLOAT\"" -> "Float"
            "\"INT\"" -> "Int"
            "\"NULL\"" -> "null"
            else -> "Unrecognized value type" // object type probs
        }
    }

    private fun getPrimitiveType(form: String): String {
        return when (form) {
            "\"BOOLEAN\"" -> "Boolean"
            "\"BYTE\"" -> "Byte"
            "\"CHAR\"" -> "Char"
            "\"DOUBLE\"" -> "Double"
            "\"FLOAT\"" -> "Float"
            "\"INT\"" -> "Int"
            "\"NULL\"" -> "null"
            else -> "Unrecognized value type" // object type probs
        }
    }

    private fun getToken(token: String): String {
        return when (token) {
            "DOT" -> "."
            "ASSN" -> " = "
            "NEQ" -> " != "
            "NEG" -> "-"
            "EQ" -> " == "
            "RANGE" -> " .. "
            "AS" -> " as "
            else -> token
        }
    }

    private fun breakdownBinaryOperation(expr: JsonObject, buildStmt: String): String {
        val oper = expr.oper()
        val operator = when {
            oper.has("str")  -> oper.str()
            oper.has("token") -> getToken(oper.token())
            else -> "{$oper}"
        }

        return "$buildStmt${breakdownExpr(expr.lhs(), buildStmt)}$operator${breakdownExpr(expr.rhs(), buildStmt)}"
    }

    /***
     * Properties -
     *     Properties tend to have 2 types I care about:
     *     1) kastree.ast.Node.Expr.Call -> is a member property of a certain class
     *     2) kastree.ast.Node.Expr.Name -> an independent member property
     *
     * Note: This is the older version of getting properties. Down the road this ought to
     *       be refactored to use the above recursive functions
     */
    private fun convertToClassProperty(property: Node.Decl.Property,
                                       propList: ArrayList<Property>,
                                       className: String,
                                       path: String) {
        val secondBit = "expr=Call(expr=Name(name="
        val string = property.toString()

        val node = gson.toJsonTree(property).asJsonObject

        if (string.contains(secondBit)) {
            val isolated = node.vars().getObject(0)
            val isolatedName = isolated.name()

            if (isolatedName == "scope") {
                tfxViews[className]?.scope = node.expr().rhs().ref().getType()
            }

            if (isolatedName == "root") {
                viewImports[className] = saveViewImport(path)
                println("DETECTION ORDER")
                detectLambdaControls(node, className, LinkedList())
                println("END OF DETECTION ORDER")
            }

            val classProperty = when {
                node.expr().has("lhs") -> getObservableProperty(node, isolatedName)
                else -> getProperty(node, isolated, isolatedName)
            }

            propList.add(classProperty)
        }
    }

    private fun saveViewImport(path: String): String {
        return if (path.contains("kotlin/")) {
            path.split("kotlin")[1].replace("/", ".").substring(1)
        } else {
            path.split("java")[1].replace("/", ".").substring(1)
        }
    }


    /**
     * Observable class properties ought to be refactored to use the recursive breakdown
     * above for Binary Operations
     */
    private fun getObservableProperty(node: JsonObject, isolatedName: String): Property {
        val type = node.expr().lhs().expr().name()

        // build objects for primitive lists
        val isolatedType = when (type) {
            "listOf" -> {
                // get list
                val elements = node.expr().lhs().args()
                var list = ""

                elements.forEach { element ->
                    val elemNodeExpr = element.asJsonObject.expr()
                    val elemType = elemNodeExpr.expr().name()
                    list += "$elemType("
                    val objectItems = elemNodeExpr.args()
                    objectItems.forEachIndexed { index, property ->
                        val elem = property.asJsonObject.expr().elems().getObject(0)
                        when {
                            elem.has("str") -> list += "${elem.get("str")}"
                            elem.has("expr") -> list += breakdownExpr(elem, list)
                            else -> println("Looks like this element type is: $elem")
                        }
                        list += if (index != objectItems.size() - 1) ", " else ").observable"
                    }
                }
                list
            }
            else -> {
                println("OBSERVABLE TYPE $type")
                type
            }
        }
        val valOrVar = if (node.readOnly()) "val " else "var "

        return Property(valOrVar, isolatedName, isolatedType)
    }

    /**
     * This is probably fine for now
     */
    private fun getProperty(node: JsonObject, isolated: JsonObject, isolatedName: String): Property {

        val type = node.expr().expr().name()
        var isolatedType = ""
        when {
            // primitive property
            node.expr().expr().has("form") -> {
                isolatedType = getPrimitiveType(node.expr().expr().form())
            }
            // collection property
            node.expr().expr().has("name") -> {
                isolatedType = when(type) {
                    "inject" -> isolated.type().ref().getType()
                    "listOf" -> {
                        val listOfMemberType = node.expr().typeArgs()

                        if (listOfMemberType.size() > 0) {
                            "$type( " + listOfMemberType.getObject(0).ref().getType() + ")"
                        } else {
                            type
                        }
                    }
                    "HashMap" -> "$type<" +
                            node.expr().typeArgs().getObject(0).ref().getType() + ","+
                            node.expr().typeArgs().getObject(1).ref().getType() + ">"
                    "ArrayList" -> "$type<" +
                            node.expr().typeArgs().getObject(0).ref().getType() + ">"
                    "mutableListOf" -> {
                        val listOfMemberType = node.expr().typeArgs()

                        if (listOfMemberType.size() > 0) {
                            "$type( " + listOfMemberType.getObject(0).ref().getType() + ")"
                        } else {
                            type
                        }
                    }
                    else -> type
                }
            }
        }

        return Property(valOrVar(node), isolatedName, isolatedType)
    }

    /**
     * Kastree readOnly values indicates whether a value is a 'val' or 'var'
     */
    private fun valOrVar(node: JsonObject): String = if (node.readOnly()) "val " else "var "

    /**
     * TornadoFX specific:
     *    Detects TornadoFX View component DSLs which builds a digraph representation
     */
    private fun detectLambdaControls(node: JsonObject,
                                     className: String,
                                     nodeHier: LinkedList<String>,
                                     nodeLevel: Int = 0) {
        val root = node.expr()

        if (root.has("lambda")) {
            val rootName = root.asJsonObject.expr().name()
            nodeHier.addLast(rootName)
            println("$nodeLevel - $rootName")

            /**
             * Create Digraph if the class is new, otherwise, add node to the existing digraph.
             */
            val graphNode = UINode(rootName, nodeLevel, root, ArrayList())
            if (mapClassViewNodes.contains(className)) {
                mapClassViewNodes[className]?.addNode(graphNode)
            } else {
                val digraph = Digraph()
                digraph.addNode(graphNode)
                mapClassViewNodes[className] = digraph
            }

            /**
             * Add an edge (a child node) to the parent node level if there is a parent
             */
            val parentLevel = nodeLevel - 1
            if (parentLevel >= 0) {
                // find the parent node by index
                mapClassViewNodes[className]?.findLastElementWithParentLevel(parentLevel)?.let {
                    mapClassViewNodes[className]?.addEdge(it, graphNode)
                }
            }

            // TornadoFX specific
            addControls<INPUTS>(graphNode, className)

            // get elements in lambda
            val lambda = root.asJsonObject.lambda()
            val elements: JsonArray = lambda.func().block().stmts()

            elements.forEach {
                detectLambdaControls(it.asJsonObject, className, nodeHier, nodeLevel + 1)
            }
        }
    }

    /**
     * TornadoFX specific:
     *    Using enum classes to check for control values here
     */
    private inline fun <reified T : Enum<T>> addControls(control: UINode,
                                                         className: String) {
        enumValues<T>().forEach {
            if (control.uiNode.toLowerCase() == (it.name).toLowerCase()) {
                if (!detectedUIControls.containsKey(className)) {
                    val controlCollection = ArrayList<UINode>()
                    detectedUIControls[className] = controlCollection
                }
                detectedUIControls[className]?.add(control)
            }
        }
    }

    private fun JsonObject.getType() = this.pieces().getObject(0).name()

    private fun JsonObject.getCollectionType(num: Int) = this.expr().typeArgs().getObject(num).ref().getType()

    private fun JsonObject.lambda(): JsonObject = this.get("lambda").asJsonObject

    private fun JsonObject.expr(): JsonObject = this.get("expr").asJsonObject

    private fun JsonObject.ref(): JsonObject = this.get("ref").asJsonObject

    private fun JsonObject.type(): JsonObject = this.get("type").asJsonObject

    private fun JsonObject.func(): JsonObject = this.get("func").asJsonObject

    private fun JsonObject.block(): JsonObject = this.get("block").asJsonObject

    private fun JsonObject.stmts(): JsonArray = this.get("stmts").asJsonArray

    private fun JsonArray.getObject(num: Int) = this.get(num).asJsonObject

    private fun JsonObject.vars(): JsonArray = this.get("vars").asJsonArray

    private fun JsonObject.args(): JsonArray = this.get("args").asJsonArray

    private fun JsonObject.typeArgs(): JsonArray = this.get("typeArgs").asJsonArray

    private fun JsonObject.pieces(): JsonArray = this.get("pieces").asJsonArray

    private fun JsonObject.lhs(): JsonObject = this.get("lhs").asJsonObject

    private fun JsonObject.rhs(): JsonObject = this.get("rhs").asJsonObject

    private fun JsonObject.elems(): JsonArray = this.get("elems").asJsonArray

    private fun JsonObject.body(): JsonObject = this.get("body").asJsonObject

    private fun JsonObject.decl(): JsonObject = this.get("decl").asJsonObject

    private fun JsonObject.oper(): JsonObject = this.get("oper").asJsonObject

    private fun JsonObject.params(): JsonArray = this.get("params").asJsonArray

    private fun JsonObject.recv(): JsonObject = this.get("recv").asJsonObject

    // primitives

    private fun JsonObject.token(): String = this.get("token").asString

    private fun JsonObject.readOnly(): Boolean = this.get("readOnly").asBoolean

    private fun JsonObject.delegated(): Boolean = this.get("delegated").asBoolean

    private fun JsonObject.str(): String = this.get("str").asString

    private fun JsonObject.name(): String = this.get("name").asString

    private fun JsonObject.form(): String = this.get("form").asString
}
