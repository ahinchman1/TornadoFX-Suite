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

        val parameters = ArrayList<Property>()
        methodJson.params().forEach { parameter ->
            val param = parameter.asJsonObject
            val paramType = param.type().ref().pieces().getObject(0).name()
            parameters.add(Property("val", param.name(), paramType))
        }

        val returnType = methodJson.type().ref().pieces().getObject(0).name()

        // TODO write a mechanism to detect nodes per function after AST parse job is complete
        classMethods.add(Method(
                name = methodJson.name(),
                parameters = parameters,
                returnType = returnType,
                methodStatements = methodStatements,
                viewNodesAffected = ArrayList<String>()))
    }

    private fun breakdownDecl(decl: JsonObject, buildStmt: String): String {
        val isolated = decl.vars().getObject(0)
        val isolatedName = isolated.name()
        val property = getProperty(decl, isolated, isolatedName)
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
        }
        return buildStmt
    }

    /**
     * Gets arguments in expressions
     */
    private fun getArguments(arguments: JsonArray, buildStmt: String): String {
        var buildArgs = "$buildStmt("
        arguments.forEachIndexed { index, argument ->
            val elem = argument.asJsonObject.expr().elems().getObject(0)
            when {
                elem.has("str") -> buildArgs += "${elem.get("str")}"
                elem.has("expr") -> buildArgs += breakdownExpr(elem, buildArgs)
                else -> println("Looks like this element type is: $elem")
            }
            buildArgs += if (index < arguments.size()) ", " else ")"
        }
        return buildArgs
    }

    // TODO - look into other types of binary operations
    private fun breakdownBinaryOperation(expr: JsonObject, buildStmt: String): String {
        return if (expr.oper().token() == "DOT") {
            "$buildStmt${breakdownExpr(expr.lhs(), buildStmt)}.${breakdownExpr(expr.rhs(), buildStmt)}"
        } else {
            "Binary Operation has: ${expr.oper().token()}"
        }
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
                    val elemType = element.asJsonObject.expr().expr().name()
                    list += "$elemType("
                    val objectItems = element.asJsonObject.expr().args()
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
            "inject" -> isolated.type().ref()
                    .pieces().getObject(0).name()
            "listOf" -> {
                val listOfMemberType = node.expr().typeArgs()

                if (listOfMemberType.size() > 0) {
                    "$type( " + listOfMemberType.getObject(0).ref().pieces().getObject(0).name() + ")"
                } else {
                    type
                }
            }
            "HashMap" -> "$type<" +
                    node.expr().typeArgs().getObject(0).ref()
                            .pieces().getObject(0).name() + ","+
                    node.expr().typeArgs().getObject(1).ref()
                            .pieces().getObject(0).name() + ">"
            "ArrayList" -> "$type<" +
                    node.expr().typeArgs().getObject(0).ref()
                            .pieces().getObject(0).name() + ">"
            "mutableListOf" -> {
                val listOfMemberType = node.expr().typeArgs()

                if (listOfMemberType.size() > 0) {
                    "$type( " + listOfMemberType.getObject(0).ref().pieces().getObject(0).name() + ")"
                } else {
                    type
                }
            }
            else -> type
        }
        val valOrVar = if (node.readOnly()) "val " else "var "

        return Property(valOrVar, isolatedName, isolatedType)
    }

    // For TornadoFX DSLs
    private fun detectLambdaControls(node: JsonObject, className: String) {
        val root = node.expr()

        if (root.get("lambda") != null) {
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

    private fun JsonObject.name(): String = this.get("name").asString

    private fun JsonObject.lhs(): JsonObject = this.get("lhs").asJsonObject

    private fun JsonObject.rhs(): JsonObject = this.get("rhs").asJsonObject

    private fun JsonObject.elems(): JsonArray = this.get("elems").asJsonArray

    private fun JsonObject.body(): JsonObject = this.get("body").asJsonObject

    private fun JsonObject.decl(): JsonObject = this.get("decl").asJsonObject

    private fun JsonObject.readOnly(): Boolean = this.get("readOnly").asBoolean

    private fun JsonObject.delegated(): Boolean = this.get("delegated").asBoolean

    private fun JsonObject.oper(): JsonObject = this.get("oper").asJsonObject

    private fun JsonObject.token(): String = this.get("token").asString

    private fun JsonObject.params(): JsonArray = this.get("params").asJsonArray
}
