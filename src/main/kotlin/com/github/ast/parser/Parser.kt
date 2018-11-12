package com.github.ast.parser

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*

class KParser : Controller() {

    var classes = ArrayList<ClassBreakDown>()
    var independentFunctions = ArrayList<String>()
    var detectedInputs = ArrayList<String>()
    var detectedUIControls = HashMap<String, ArrayList<String>>()

    fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile)

        file.decls.forEach {node ->
            when (node) {
                is Node.Decl.Structured -> breakDownClass(node.name, file)
                is Node.Decl.Func -> independentFunctions.add(node.name)
            }
        }
    }

    private fun breakDownClass(className: String, file: Node.File) {
        val classProperties = ArrayList<ClassProperties>()
        val classMembers = ArrayList<String>()

        // Save for all files
        (file.decls[0] as Node.Decl.Structured).members.forEach {
            when (it) {
                is Node.Decl.Structured -> println("this is probably a companion object")
                is Node.Decl.Property -> convertToMemberJsonProperty(it, classProperties, className)
                is Node.Decl.Func -> classMembers.add(it.name)
            }
        }
        classes.add(ClassBreakDown(className, classProperties, classMembers))
    }

    /***
     * Properties -
     *     Properties tend to have 2 types:
     *     1) kastree.ast.Node.Expr.Call -> is a member property of a certain class
     *     2) kastree.ast.Node.Expr.Name -> an independent member property
     */
    private fun convertToMemberJsonProperty(property: Node.Decl.Property, propList: ArrayList<ClassProperties>, className: String) {
        val secondBit = "expr=Call(expr=Name(name="
        val string = property.toString()

        val gson = Gson()
        val nodesElement: JsonElement = gson.toJsonTree(property)

        if (string.contains(secondBit)) {
            val node = nodesElement.asJsonObject
            val isolated = node.vars().getObject(0)
            val isolatedName = isolated.name()

            if (isolatedName == "root") {
                detectLambdaControls(nodesElement.asJsonObject, className)
            }


            // check if property type is observable
            /*if (node.get("expr")?.asJsonObject?.get("rhs")
                            ?.asJsonObject?.get("expr")?.asJsonObject?.get("name")?.asString == "observable") {
                val leftSide = node.get("expr").asJsonObject
                        .get("lhs").asJsonObject
                type = leftSide
                        .get("expr").asJsonObject
                        .get("name").asString
                /*leftSide.get("args").asJsonArray.forEach {
                    // TODO find some way to recurse down the observable list stuff
                }*/

            } else {*/

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
            // }
            propList.add(ClassProperties(isolatedName, isolatedType))
        }
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

    fun JsonObject.lambda(): JsonObject = this.get("lambda").asJsonObject

    fun JsonObject.expr(): JsonObject = this.get("expr").asJsonObject

    fun JsonObject.ref(): JsonObject = this.get("ref").asJsonObject

    fun JsonObject.type(): JsonObject = this.get("type").asJsonObject

    fun JsonObject.func(): JsonObject = this.get("func").asJsonObject

    fun JsonObject.block(): JsonObject = this.get("block").asJsonObject

    fun JsonObject.stmts(): JsonArray = this.get("stmts").asJsonArray

    fun JsonArray.getObject(num: Int) = this.get(num).asJsonObject

    fun JsonObject.vars(): JsonArray = this.get("vars").asJsonArray

    fun JsonObject.typeArgs(): JsonArray = this.get("typeArgs").asJsonArray

    fun JsonObject.pieces(): JsonArray = this.get("pieces").asJsonArray

    fun JsonObject.name(): String = this.get("name").asString

}
