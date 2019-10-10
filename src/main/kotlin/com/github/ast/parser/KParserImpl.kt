package com.github.ast.parser

import com.github.ast.parser.frameworkconfigurations.ComponentBreakdownFunction
import com.github.ast.parser.frameworkconfigurations.DetectFrameworkComponents
import com.github.ast.parser.frameworkconfigurations.TornadoFXView
import com.github.ast.parser.nodebreakdown.*
import com.github.ast.parser.nodebreakdown.digraph.Digraph
import com.github.ast.parser.nodebreakdown.digraph.UINodeDigraph
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

    override var mapClassViewNodes = MapKClassToDigraph<UINodeDigraph>()

    override var viewImports = MapKClassTo<String>()

    override val gson = Gson()

    override fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile, true)

        file.decls.forEach { node ->
            when (node) {
                is Node.Decl.Structured -> breakDownClass(node.name, node)
                is Node.Decl.Func -> node.name ?: independentFunctions.add(node.name.toString())
            }
        }
    }

    override fun breakDownClass(className: String, structuredNode: Node.Decl.Structured) {
        val classParents = ArrayList<String>()
        val classProperties = ArrayList<Property>()
        val classMethods = ArrayList<Method>()

        structuredNode.parents.forEach { parentClass ->
            val superClassNode = gson.toJsonTree(parentClass).asJsonObject
            var superClass = superClassNode.type().getType()

            // if parent class accepts generic parameters
            if (superClassNode.hasTypeArguments()) {
                superClass += getGenericTypeArgs(superClassNode)
            }

            classParents.add(superClass)
            componentBreakdownFunction(superClass, className)
        }

        // Save for all files
        structuredNode.members.forEach { member ->
            when (member) {
                is Node.Decl.Structured -> println("this is probably a companion object")
                is Node.Decl.Property -> convertToClassProperty(member, classProperties, className)
                is Node.Decl.Func -> breakdownClassMethod(member, classMethods)
            }
        }

        classes.add(ClassBreakDown(className, classParents, classProperties, classMethods))
    }

    override fun breakdownClassMethod(method: Node.Decl.Func, classMethods: ArrayList<Method>) {
        val methodJson = gson.toJsonTree(method).asJsonObject
        val methodContent = ArrayList<String>()

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

        methodContent.addAll(breakdownBody(methodJson.body(), methodContent))

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

    override fun breakdownBody(body: JsonObject, methodStatements: ArrayList<String>): ArrayList<String> {
        val stmtArray = arrayListOf<String>()
        stmtArray.addAll(methodStatements)

        when {
            body.hasBlock()              -> stmtArray.addAll(breakdownStmts(body.block().stmts(), methodStatements))
            body.hasDeclaration() ||
                    body.hasExpression() -> stmtArray.add(breakdownExpr(body.expr(), ""))
            else ->                         stmtArray.add(breakdownBinaryOperation((body.expr()), ""))
        }

        return stmtArray
    }

    override fun breakdownStmts(stmts: JsonArray, methodStatements: ArrayList<String>): ArrayList<String> {
        val stmtArray = arrayListOf<String>()
        stmtArray.addAll(methodStatements)

        stmts.forEach { statement ->
            val stmt = statement.asJsonObject
            when {
                stmt.hasExpressionCall() -> stmtArray.add(getExpressionCall(stmt.expr()))
                stmt.hasExpression()  -> stmtArray.add(breakdownExpr(stmt.expr(), ""))
                stmt.hasDeclaration() -> stmtArray.add(breakdownDecl(stmt.decl(), ""))
                else ->  println("stmt has$stmt")
            }
        }
        return stmtArray
    }

    override fun breakdownDecl(decl: JsonObject, buildStmt: String): String {
        return when {
            decl.hasExpression()  -> breakdownDeclProperty(decl, buildStmt)
            decl.hasBody() -> {
                breakdownBody(decl.body(), arrayListOf())
                "method body here: $decl"
            }
            else -> "MISSING DECL $decl"
        }
    }

    override fun breakdownDeclProperty(decl: JsonObject, buildStmt: String): String {
        val propertyName = decl.vars().getObject(0).name()
        val propertyValue = when {
            decl.expr().hasExpressionCall() -> getExpressionCall(decl.expr())
            decl.expr().has("expr") && decl.expr().has("oper") -> {
                var expression = breakdownExpr(decl.expr(), "")
                expression += decl.vars().getObject(0).type().ref().getType()
                expression
            }
            decl.expr().hasBinaryOperation() -> breakdownBinaryOperation(decl.expr(), "")
            decl.expr().hasValue() -> {
                var expression = breakdownBinaryOperation(decl.expr(), "")
                expression
            }
            decl.expr().hasName() -> decl.expr().name()
            else -> TODO()
        }

        val declaration = "${valOrVar(decl)} $propertyName"

        return "$declaration = $propertyValue"
    }

    override fun breakdownExpr(
            expr: JsonObject,
            buildStmt: String,
            methodStatements: ArrayList<String>
    ): String {
        var exprStmt = buildStmt
        exprStmt += when {
            expr.hasBinaryOperation()-> breakdownBinaryOperation(expr, exprStmt)
            expr.hasExpressionCall() -> getExpressionCall(expr)
            expr.hasArguments() -> getArguments(expr.args(), exprStmt)
            expr.hasName() -> expr.name()
            expr.hasExpression() -> breakdownExpr(expr.expr(), exprStmt)
            expr.hasElements() -> getElems(expr.elems())
            expr.hasParameters() -> getParams(expr.params(), exprStmt)
            expr.hasValue() -> exprStmt + getPrimitiveValue(expr)
            expr.hasBlock() -> breakdownStmts(expr.block().stmts(), methodStatements)
            expr.size() == 0 -> {}
            else -> println(expr)
        }
        return exprStmt
    }

    override fun getExpressionCall(expr: JsonObject): String {
        val anonymousFunction = if (expr.hasLambda()) breakdownLambda(expr.lambda()) else ""
        return getArguments(expr.args(), expr.expr().name()) + anonymousFunction
    }

    private fun breakdownLambda(func: JsonObject): String {
        val functionContent = ArrayList<String>()
        var internalFunction = "{"

        if (func.hasParameters()) {
            val params = getParams(func.params(), "")
            if (params.isNotEmpty()) {
                internalFunction += "$params->\n"
            }
        }

        when {
            func.hasBody() -> functionContent.addAll(breakdownBody(func.body(), functionContent))
            func.hasBlock() -> functionContent.addAll(breakdownStmts(func.block().stmts(), functionContent))
            else -> functionContent.add("")
        }

        functionContent.forEach { internalFunction += it }

        return "$internalFunction}"
    }

    override fun getParams(params: JsonArray, buildStmt: String): String {
        var buildParams = buildStmt
        params.forEach { parameter ->
            buildParams += parameter.asJsonObject.vars().getObject(0).name()
        }
        return buildParams
    }

    override fun getElems(elems: JsonArray): String {
        var buildElems = ""
         if (elems.size() > 0) {
             elems.forEach {
                val elem = it.asJsonObject
                buildElems += when {
                    elem.hasName() -> elem.name()
                    elem.hasString() -> "\"" + elem.str() + "\""
                    elem.hasExpression() -> "$/{" + breakdownExpr(elem, "")+ "}"
                    elem.hasPrimitiveValue() -> getPrimitiveValue(elem)
                    elem.hasBinaryOperation() -> "$/{" + breakdownBinaryOperation(elem, "") + "}"
                    elem.hasReceiver() -> elem.recv().type().getType()
                    else -> "Looks like this element type is: $elem"
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
                val argExpression = arg.expr()

                buildArgs += when {
                    argExpression.hasName() -> argExpression.name()
                    argExpression.hasReceiver() -> argExpression.recv().type().getType() + "::class"
                    argExpression.hasElements() -> getElems(argExpression.elems())
                    argExpression.hasExpression() -> breakdownExpr(argExpression.expr(), "")
                    argExpression.hasBinaryOperation() -> breakdownBinaryOperation(argExpression, "")
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
            lhs.hasPrimitiveValue() -> getPrimitiveValue(lhs)
            lhs.hasName()           -> lhs.name()
            lhs.hasExpressionCall() -> getExpressionCall(lhs)
            lhs.hasExpression()     -> breakdownExpr(lhs.expr(), "")
            else                    -> breakdownBinaryOperation(expr.lhs(), buildBinary)
        }

        val oper = expr.oper()
        val operator = when {
            oper.hasString()        -> oper.str()
            oper.hasToken()         -> getToken(oper.token())
            else                    -> "{$oper}"
        }

        val rhs = expr.rhs()
        val rightHandSide = when {
            rhs.hasPrimitiveValue() -> getPrimitiveValue(rhs)
            rhs.hasName()           -> rhs.name()
            rhs.hasExpressionCall() -> getExpressionCall(rhs)
            rhs.hasExpression()     -> breakdownExpr(rhs.expr(), "")
            else                    -> breakdownBinaryOperation(expr.rhs(), buildBinary)
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
            currentPath.split("kotlin")[1].replace("/", ".").substring(1).split(".kt")[0]
        } else {
            currentPath.split("java")[1].replace("/", ".").substring(1).split(".kt")[0]
        }
    }


    /**
     * Observable class properties ought to be refactored to use the recursive breakdown
     * above for Binary Operations
     */
    override fun getObservableProperty(node: JsonObject, isolatedName: String): Property {

        // build objects for primitive lists
        val isolatedType = when (val type = node.expr().lhs().expr().name()) {
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
                            elem.hasString() -> list += "${elem.get("str")}"
                            elem.hasExpression() -> list += breakdownExpr(elem, list)
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
                            node.getGenericTypeArgument(0) + "," +
                            node.getGenericTypeArgument(1) + ">"
                    "ArrayList" -> "$type<" + node.getGenericTypeArgument(0) + ">"
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
     * Using enum classes to check if lambda is a data control
     */
    inline fun <reified T : Enum<T>> isControl(control: String): Boolean {
        var result = false
        enumValues<T>().forEach {
            if (control.toLowerCase() == (it.name).toLowerCase()) {
                result = true
            }
        }
        return result
    }

    /**
     * Using enum classes to check for control values here
     */
    inline fun <reified T : Enum<T>> addControls(control: UINode, className: String) {
        enumValues<T>().forEach {
            if (control.nodeType.toLowerCase() == (it.name).toLowerCase()) {
                if (!detectedUIControls.containsKey(className)) {
                    val controlCollection = ArrayList<UINode>()
                    detectedUIControls[className] = controlCollection
                }
                detectedUIControls[className]?.add(control)
            }
        }
    }

}
