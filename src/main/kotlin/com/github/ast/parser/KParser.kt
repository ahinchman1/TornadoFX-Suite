package com.github.ast.parser

import com.google.gson.*
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*

class KParser : Controller() {

    var classes = ArrayList<ClassBreakDown>()
    private var independentFunctions = ArrayList<String>()
    var detectedInputs = ArrayList<String>()
    var detectedUIControls = HashMap<String, ArrayList<String>>()
    val gson = Gson()

    fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile, true)

        file.decls.forEach {node ->
            when (node) {
                is Node.Decl.Structured -> breakDownClass(node.name, file)
                is Node.Decl.Func -> node.name ?: independentFunctions.add(node.name.toString())
            }
        }
    }

    private fun breakDownClass(className: String, file: Node.File) {
        val classProperties = ArrayList<Property>()
        val classMethods = ArrayList<Method>()

        // Save for all files
        (file.decls[0] as Node.Decl.Structured).members.forEach {
            when (it) {
                is Node.Decl.Structured -> println("this is probably a companion object")
                is Node.Decl.Property -> convertToClassProperty(it, classProperties, className)
                is Node.Decl.Func -> breakdownClassMethod(it, classMethods)
            }
        }
        classes.add(ClassBreakDown(className, classProperties, classMethods))
    }

    private fun breakdownClassMethod(method: Node.Decl.Func, classMethods: ArrayList<Method>) {
        val methodJson = gson.toJsonTree(method).asJsonObject
        val methodStatements = ArrayList<String>()
        val methodContent = methodJson.body()

        when {
            // regular block function
            methodContent.has("block") -> {
                val methodStmts = methodJson.body().block().stmts()
                if (methodStmts.size() > 0) {
                    methodStmts.forEach { statement ->
                        val stmt = statement.asJsonObject
                        when {
                            stmt.has("expr") ->
                                methodStatements.add(breakdownExpr(stmt.expr(), ""))
                            stmt.has("decl") ->
                                methodStatements.add(breakdownDecl(stmt.decl(), ""))
                            else -> println("stmt has$stmt")
                        }
                    }
                }
            }
            // function with reflective method calls
            methodContent.has("expr") -> methodStatements.add(breakdownExpr(methodJson, ""))
            // function has a single assignment statement
            else -> methodStatements.add(breakdownBinaryOperation((methodContent.expr()), ""))
        }

        val parameters = ArrayList<Property>()
        methodJson.params().forEach { parameter ->
            val param = parameter.asJsonObject
            val paramType = param.type().ref().getType()
            parameters.add(Property("val", param.name(), paramType))
        }

        var returnType = "Unit"
        if (methodJson.has("type")) {
            returnType = methodJson.type().ref().getType()
        }

        // TODO write a mechanism to detect nodes per function after AST parse job is complete
        classMethods.add(Method(
                name = methodJson.name(),
                parameters = parameters,
                returnType = returnType,
                methodStatements = methodStatements,
                viewNodesAffected = ArrayList()))
    }

    private fun breakdownDecl(decl: JsonObject, buildStmt: String): String {
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
            else -> TODO()
        }
        val declaration = "$buildStmt${property.valOrVar} $isolatedName: $isolated ="
        return "$declaration = ${breakdownExpr(decl, buildStmt)}"
    }

    private fun breakdownExpr(expr: JsonObject, buildStmt: String): String {
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
            expr.size() == 0 -> buildStmt
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
            elems.forEach { it ->
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

    /**
     * Gets arguments in expressions
     */
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

    // TODO - look into other types of binary operations
    private fun breakdownBinaryOperation(expr: JsonObject, buildStmt: String): String {
        val oper = expr.oper()
        val operator = when {
            oper.has("str")  -> oper.str()
            oper.has("token") -> {
                when (oper.token()) {
                    "DOT" -> "."
                    "ASSN" -> " = "
                    "NEQ" -> " != "
                    "EQ" -> " == "
                    else -> oper.token()
                }

            }
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
                                       className: String) {
        val secondBit = "expr=Call(expr=Name(name="
        val string = property.toString()

        val nodesElement: JsonElement = gson.toJsonTree(property)

        if (string.contains(secondBit)) {
            val node = nodesElement.asJsonObject
            val isolated = node.vars().getObject(0)
            val isolatedName = isolated.name()

            if (isolatedName == "root") {
                detectLambdaControls(nodesElement.asJsonObject, className)
            }

            val classProperty = when {
                node.expr().has("lhs") -> getObservableProperty(node, isolatedName)
                else -> getProperty(node, isolated, isolatedName)
            }

            propList.add(classProperty)
        }
    }

    private fun getValue(value: JsonObject): String {
        val gValue = value.get("value")
        return when (value.get("form").asJsonPrimitive.toString()) {
            "\"BOOLEAN\"" ->  gValue.asBoolean.toString()
            "\"FLOAT\"" -> gValue.asFloat.toString()
            "\"INTEGER\"" -> gValue.asInt.toString()
            "\"CHARACTER\"" -> gValue.asCharacter.toString()
            "\"NULL\"" -> gValue.asJsonNull.toString()
            else -> "Unrecognized value type"
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

        val isolatedType = when(type) {
            "inject" -> isolated.type().ref().pieces().getObject(0).name()
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

        return Property(valOrVar(node), isolatedName, isolatedType)
    }

    private fun valOrVar(node: JsonObject): String = if (node.readOnly()) "val " else "var "

    // For TornadoFX DSLs
    private fun detectLambdaControls(node: JsonObject, className: String) {
        val root = node.expr()

        if (root.has("lambda")) {
            val rootName = root.asJsonObject.expr().name()

            // TornadoFX specific
            addControls<INPUTS>(rootName, className)

            // get elements in lambda
            val lambda = root.asJsonObject.lambda()
            val elements: JsonArray = lambda.func().block().stmts()

            elements.forEach {
                detectLambdaControls(it.asJsonObject, className)
            }
        }
    }

    // using the enum class to check for control values here
    private inline fun <reified T : Enum<T>> addControls(control: String, className: String) {
        enumValues<T>().forEach { it ->
            if (control.toLowerCase() == (it.name).toLowerCase()) {
                detectedInputs.add(control)

                if (detectedUIControls.containsKey(className)) {
                    detectedUIControls[className]?.add(control)
                } else {
                    val controlCollection = ArrayList<String>()
                    detectedUIControls[className] = controlCollection
                }
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
}
