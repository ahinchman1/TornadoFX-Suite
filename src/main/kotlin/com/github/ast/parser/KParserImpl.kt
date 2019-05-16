package com.github.ast.parser

import com.github.ast.parser.frameworkconfigurations.ComponentBreakdownFunction
import com.github.ast.parser.frameworkconfigurations.DetectFrameworkComponents
import com.github.ast.parser.frameworkconfigurations.TornadoFXView
import com.github.ast.parser.nodebreakdown.*
import com.google.gson.*
import kastree.ast.Node
import kastree.ast.psi.Parser
import java.util.*
import kotlin.collections.HashMap

open class KParserImpl(
        var currentPath: String,
        val componentBreakdownFunction: ComponentBreakdownFunction,
        // TODO used sealed classes to make view types here interchangeable
        var views: MapKClassTo<TornadoFXView> = HashMap(),
        private vararg val functions: DetectFrameworkComponents
): KParser {

    override var classes = ArrayList<ClassBreakDown>()

    override var independentFunctions = ArrayList<String>()

    override var detectedUIControls = MapKClassTo<ArrayList<UINode>>()

    override var mapClassViewNodes = MapKClassTo<Digraph>()

    override var viewImports = MapKClassTo<String>()

    override val gson = Gson()

    var utils = KParserUtils()

    override fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile, true)

        file.decls.forEach { node ->
            when (node) {
                is Node.Decl.Structured -> breakDownClass(node.name, file)
                is Node.Decl.Func -> node.name ?: independentFunctions.add(node.name.toString())
            }
        }
    }

    override fun breakDownClass(className: String, file: Node.File) {
        val classParents = ArrayList<String>()
        val classProperties = ArrayList<Property>()
        val classMethods = ArrayList<Method>()

        val structuredNode = file.decls[0] as Node.Decl.Structured

        // collecting this info for test information
        structuredNode.parents.forEach {
            val superClass = gson.toJsonTree(it).asJsonObject.type().getType()
            classParents.add(superClass)
            componentBreakdownFunction(superClass, className)
        }

        // Save for all files
        structuredNode.members.forEach {
            when (it) {
                is Node.Decl.Structured -> println("this is probably a companion object")
                is Node.Decl.Property -> convertToClassProperty(it, classProperties, className)
                is Node.Decl.Func -> breakdownClassMethod(it, classMethods)
            }
        }

        classes.add(ClassBreakDown(className, classParents, classProperties, classMethods))
    }

    override fun breakdownClassMethod(method: Node.Decl.Func, classMethods: ArrayList<Method>) {
        val methodJson = gson.toJsonTree(method).asJsonObject
        val methodContent = ArrayList<String>()

        breakdownBody(methodJson.body(), methodContent)

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
                        methodStatements = methodContent,
                        viewNodesAffected = ArrayList()
                )
        )
    }

    override fun breakdownBody(body: JsonObject, methodStatements: ArrayList<String>) {
        when {
            body.has("block") -> breakdownStmts(body.block().stmts(), methodStatements)
            body.has("expr") -> methodStatements.add(breakdownExpr(body.expr(), ""))
            else -> methodStatements.add(breakdownBinaryOperation((body.expr()), ""))
        }
    }

    override fun breakdownStmts(stmts: JsonArray, methodStatements: ArrayList<String>?) {
        stmts.forEach { statement ->
            val stmt = statement.asJsonObject
            when {
                stmt.has("expr") -> methodStatements?.add(breakdownExpr(stmt.expr(), ""))
                stmt.has("decl") -> methodStatements?.add(breakdownDecl(stmt.decl(), ""))
                else ->  println("stmt has$stmt")
            }
        }
    }

    override fun breakdownDecl(decl: JsonObject, buildStmt: String): String {
        return when {
            decl.has("expr") -> breakdownDeclProperty(decl, buildStmt)
            decl.has("body") -> {
                breakdownBody(decl.body(), arrayListOf())
                "method body here: $decl"
            }
            else -> "MISSING DECL $decl"
        }
    }

    /**
     * Not fucked up but does not account for all decl property types nor does it format properly
     */
    override fun breakdownDeclProperty(decl: JsonObject, buildStmt: String): String {
        val isolated = decl.vars().getObject(0)
        val isolatedName = isolated.name()
        var expression = breakdownExpr(decl.expr(), "")
        val property = when {
            decl.expr().has("expr") -> {
                if (decl.expr().has("args")) {
                    expression = getArguments(decl.expr().args(), expression)
                }

                if (decl.expr().has("expr") && decl.expr().has("oper")) {
                    Property(valOrVar(decl), isolatedName, decl.vars().getObject(0).type().ref().pieces().getObject(0).name())
                } else {
                    getProperty(
                            decl,
                            isolated,
                            isolatedName)
                }
            }
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
                    utils.getPrimitiveType(decl.expr()))
            else -> TODO()
        }
        val declaration = "$buildStmt${property.valOrVar} $isolatedName: ${property.propertyType}"

        return "$declaration = $expression"
    }

    override fun breakdownExpr(
            expr: JsonObject,
            buildStmt: String,
            methodStatements: ArrayList<String>?
    ): String {
        var exprStmt = buildStmt
        exprStmt += when {
            expr.has("lhs") &&
                    expr.has("oper") &&
                    expr.has("rhs")-> breakdownBinaryOperation(expr, exprStmt)
            expr.has("args") -> getArguments(expr.args(), exprStmt)
            expr.has("name") -> exprStmt + expr.name()
            expr.has("expr") -> breakdownExpr(expr.expr(), exprStmt)
            expr.has("elems") -> getElems(expr.elems(), exprStmt)
            expr.has("params") -> getParams(expr.params(), exprStmt)
            expr.has("value") -> exprStmt + utils.getPrimitiveValue(expr)
            expr.has("block") -> breakdownStmts(expr.block().stmts(), methodStatements)
            expr.size() == 0 -> {}
            else -> println(expr)
        }
        return exprStmt
    }

    override fun getParams(params: JsonArray, buildStmt: String): String {
        var buildParams = buildStmt
        params.forEach { parameter ->
            buildParams += parameter.asJsonObject.vars().getObject(0).name()
        }
        return buildParams
    }

    override fun getElems(elems: JsonArray, buildStmt: String): String {
        var buildElems = buildStmt
        if (elems.size() > 0) {
            elems.forEach {
                val elem = it.asJsonObject
                buildElems += when {
                    elem.has("str") -> elem.str()
                    elem.has("expr") -> breakdownExpr(elem, buildElems)
                    elem.has("value") -> utils.getPrimitiveValue(elem)
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

    override fun getArguments(arguments: JsonArray, buildStmt: String): String {
        var buildArgs = "$buildStmt("
        if (arguments.size() > 0) {
            arguments.forEachIndexed { index, argument ->
                val arg = argument.asJsonObject
                val argExpression = argument.asJsonObject.expr()

                buildArgs += when {
                    argExpression.has("name") -> argExpression.name()
                    argExpression.has("recv") -> argExpression.recv().type().getType() + "::class"
                    argExpression.has("elems") -> getElems(argExpression.elems(), buildArgs)
                    argExpression.has("expr") -> breakdownExpr(argExpression.expr(), "")
                    else -> "" // TODO
                }
                if (arg.has("name")) {
                    buildArgs = arg.name() + " = " + buildArgs
                }
                buildArgs += if (index < arguments.size() - 1) ", " else ")"
            }
        } else buildArgs += ")"
        return buildArgs
    }

    override fun breakdownBinaryOperation(expr: JsonObject, buildStmt: String): String {
        var buildBinary = buildStmt

        val lhs = expr.lhs()
        val leftHandSide = when {
            lhs.has("value") -> utils.getPrimitiveValue(lhs)
            lhs.has("name") -> lhs.name()
            lhs.has("expr") -> {
                var expression = breakdownExpr(lhs.expr(), "")
                if (lhs.expr().has("args")) {
                    expression = getArguments(lhs.args(), expression)
                }
                expression
            }
            else -> breakdownBinaryOperation(expr.lhs(), buildBinary)
        }

        val oper = expr.oper()
        val operator = when {
            oper.has("str")  -> oper.str()
            oper.has("token") -> utils.getToken(oper.token())
            else -> "{$oper}"
        }

        val rhs = expr.rhs()
        val rightHandSide = when {
            rhs.has("value") -> utils.getPrimitiveValue(rhs)
            rhs.has("name") -> rhs.name()
            rhs.has("expr") -> {
                var expression = breakdownExpr(rhs.expr(), "")
                if (rhs.expr().has("args")) {
                    expression = getArguments(rhs.args(), expression)
                }
                expression
            }
            else -> breakdownBinaryOperation(expr.rhs(), buildBinary)
        }

        buildBinary += "$leftHandSide$operator$rightHandSide"
        return buildBinary
    }

    override fun convertToClassProperty(
            property: Node.Decl.Property,
            propList: ArrayList<Property>,
            className: String
    ) {
        val secondBit = "expr=Call(expr=Name(name="
        val string = property.toString()

        val node = gson.toJsonTree(property).asJsonObject

        if (string.contains(secondBit)) {
            val isolated = node.vars().getObject(0)
            val isolatedName = isolated.name()

            functions.forEach {
                it(isolatedName, className, node)
            }

            val classProperty = when {
                node.expr().has("lhs") -> getObservableProperty(node, isolatedName)
                else -> getProperty(node, isolated, isolatedName)
            }

            propList.add(classProperty)
        }
    }

    override fun saveViewImport(): String {
        return if (currentPath.contains("kotlin/")) {
            currentPath.split("kotlin")[1].replace("/", ".").substring(1)
        } else {
            currentPath.split("java")[1].replace("/", ".").substring(1)
        }
    }


    /**
     * Observable class properties ought to be refactored to use the recursive breakdown
     * above for Binary Operations
     */
    override fun getObservableProperty(node: JsonObject, isolatedName: String): Property {
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
     * Lol this is f**ked up
     */
    override fun getProperty(node: JsonObject, isolated: JsonObject, isolatedName: String): Property {

        val type = node.expr().expr().name()
        var isolatedType = ""
        when {
            // primitive property
            node.expr().expr().has("form") -> {
                isolatedType = utils.getPrimitiveType(node.expr().expr().form())
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

    override fun valOrVar(node: JsonObject): String = if (node.readOnly()) "val " else "var "

    /**
     * Using enum classes to check for control values here
     */
    inline fun <reified T : Enum<T>> addControls(control: UINode, className: String) {
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

}
