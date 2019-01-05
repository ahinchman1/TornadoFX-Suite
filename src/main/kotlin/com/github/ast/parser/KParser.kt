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
        val file = Parser.parseFile(textFile, true)

        file.decls.forEach {node ->
            when (node) {
                is Node.Decl.Structured -> breakDownClass(node.name, file)
                is Node.Decl.Func -> node.name ?: independentFunctions.add(node.name.toString())
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
                is Node.Decl.Func -> it.name ?: classMembers.add(it.name.toString())
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
    private fun convertToMemberJsonProperty(property: Node.Decl.Property,
                                            propList: ArrayList<ClassProperties>,
                                            className: String) {
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

            val classProperty = when {
                node.expr().has("lhs") -> getObservableProperty(node, isolatedName)
                else -> getClassProperty(node, isolated, isolatedName)
            }

            propList.add(classProperty)
        }
    }

    private fun getObservableProperty(node: JsonObject, isolatedName: String): ClassProperties {
        val type = node.expr().lhs().expr().name()

        // get list
        val elements = node.expr().lhs().args()
        var list = ""

        // build objects for primitive lists
        val isolatedType = when (type) {
            "listOf" -> {
                elements.forEach { element ->
                    val elemType = element.asJsonObject.expr().expr().name()
                    list += "$elemType("
                    val objectItems = element.asJsonObject.expr().args()
                    objectItems.forEachIndexed { index, property ->
                        val elem = property.asJsonObject.expr().elems().getObject(0)
                        when {
                            elem.has("str") -> list += "${elem.get("str")}"
                            else -> println("Looks like this element type is: $elem")
                        }
                        list += if (index != objectItems.size()) ", " else ")"
                    }
                }
                list
            }
            else -> {
                println("OBSERVABLE TYPE $type")
                type
            }
        }
        return ClassProperties(isolatedName, isolatedType)
    }

    private fun getClassProperty(node: JsonObject, isolated: JsonObject, isolatedName: String): ClassProperties {

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

        return ClassProperties(isolatedName, isolatedType)
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

}
