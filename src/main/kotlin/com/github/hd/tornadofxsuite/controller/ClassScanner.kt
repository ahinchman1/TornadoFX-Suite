package com.example.demo.controller

import com.github.hd.tornadofxsuite.model.BareBreakDown
import com.github.hd.tornadofxsuite.model.ClassProperties
import com.github.hd.tornadofxsuite.model.INPUTS
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kastree.ast.Node
import kastree.ast.psi.Parser
import tornadofx.*

/*
 * TODO - AST mappings, composition.
 */
class ClassScanner: Controller() {

    var bareClasses = ArrayList<BareBreakDown>()
    var independentFunctions = ArrayList<String>()
    var detectedInputs = ArrayList<String>()
    var detectedViewControls = HashMap<String, ArrayList<String>>()

    fun parseAST(textFile: String) {
        val file = Parser.parseFile(textFile)
        // store class names and their methods
        file.decls.forEach {
            when (it) {
                is Node.Decl.Structured -> breakDownClass(it, file)
                is Node.Decl.Func -> independentFunctions.add(it.name)
            }
        }
    }

    private fun breakDownClass(someClass: Node.Decl.Structured, file: Node.File) {
        val className = someClass.name
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
        bareClasses.add(BareBreakDown(className, classProperties, classMembers))
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
            val isolated = node
                    .get("vars")
                    .asJsonArray
                    .get(0).asJsonObject
            val isolatedName = isolated.get("name").asString

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
                val type = node.get("expr").asJsonObject
                        .get("expr").asJsonObject
                        .get("name").asString
                val isolatedType = when(type) {
                    "inject" -> isolated.get("type").asJsonObject
                            .get("ref").asJsonObject
                            .get("pieces").asJsonArray
                            .get(0).asJsonObject
                            .get("name").asString
                    // TODO - find a better way to pattern match collections
                    "listOf" -> {
                        val listOfMemberType = node.get("expr").asJsonObject
                                .get("typeArgs").asJsonArray
                        if (listOfMemberType.size() > 0) {
                            "$type( " + listOfMemberType
                                    .get(0).asJsonObject
                                    .get("ref").asJsonObject
                                    .get("pieces").asJsonArray
                                    .get(0).asJsonObject
                                    .get("name").asString + ")"
                        } else {
                            type
                        }
                    }
                    "HashMap" -> "$type<" + node.get("expr").asJsonObject
                            .get("typeArgs").asJsonArray
                            .get(0).asJsonObject
                            .get("ref").asJsonObject
                            .get("pieces").asJsonArray
                            .get(0).asJsonObject
                            .get("name").asString + "," + node.get("expr").asJsonObject
                            .get("typeArgs").asJsonArray
                            .get(1).asJsonObject
                            .get("ref").asJsonObject
                            .get("pieces").asJsonArray
                            .get(0).asJsonObject
                            .get("name").asString + ">"
                    "ArrayList" -> "$type<" + node.get("expr").asJsonObject
                            .get("typeArgs").asJsonArray
                            .get(0).asJsonObject
                            .get("ref").asJsonObject
                            .get("pieces").asJsonArray
                            .get(0).asJsonObject
                            .get("name").asString + ">"
                    "mutableListOf" -> {
                        val listOfMemberType = node.get("expr").asJsonObject
                                .get("typeArgs").asJsonArray
                        if (listOfMemberType.size() > 0) {
                            "$type( " + listOfMemberType
                                    .get(0).asJsonObject
                                    .get("ref").asJsonObject
                                    .get("pieces").asJsonArray
                                    .get(0).asJsonObject
                                    .get("name").asString + ")"
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

    // For Kotlin block
    // recursively loop in the order of lambda.func.block.stmts
    private fun detectLambdaControls(node: JsonObject, className: String) {
        val root = node.get("expr")

        if (root.asJsonObject.get("lambda") != null) {
            val rootName = root.asJsonObject
                    .get("expr").asJsonObject
                    .get("name").asString

            // TornadoFX specific
            addControls<INPUTS>(rootName, className)

            // get elements in lambda
            val lambda = root.asJsonObject.get("lambda").asJsonObject
            val elements: JsonArray = lambda.get("func").asJsonObject
                    .get("block").asJsonObject
                    .get("stmts") as JsonArray

            elements.forEach {
                detectLambdaControls(it.asJsonObject, className)
            }
        }
    }

    fun detectModels() {
        // TODO
    }

    fun detectEvents() {
        // TODO
    }

    // using the enum class to check for control values here
    private inline fun <reified T : Enum<T>> addControls(control: String, className: String) {
        enumValues<T>().forEach { it ->
            if (control.toLowerCase() == (it.name).toLowerCase()) {
                detectedInputs.add(control)

                if (detectedViewControls.containsKey(className)) {
                    detectedViewControls[className]?.add(control)
                } else {
                    val controlCollection = ArrayList<String>()
                    detectedViewControls[className] = controlCollection
                }
            }
        }
    }
}